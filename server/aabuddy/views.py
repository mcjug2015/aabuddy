from django.contrib.gis.geos import *
from django.contrib.gis.measure import D
from aabuddy.models import Meeting, UserConfirmation, MeetingNotThere
import json
from django.http import HttpResponse
import logging
from django.views.decorators.csrf import csrf_exempt
import datetime
from django.contrib.auth.models import User
from django.core.mail import send_mail
import random
import string
from django import forms
from django.shortcuts import render_to_response
from django.core.context_processors import csrf
from django.core.exceptions import ValidationError

logger = logging.getLogger(__name__)


class ResetPasswordForm(forms.Form):
    username = forms.CharField(max_length=100, label='Username', required=True)
    new_password = forms.CharField(widget=forms.PasswordInput, max_length=100, label='New Password', required=True)
    confirm_password = forms.CharField(widget=forms.PasswordInput, max_length=100, label='Confirm Password')
    user_confirmation = forms.CharField(widget=forms.HiddenInput, required=True)
    
    def clean(self):
        '''perform custom validation'''
        cleaned_data = self.cleaned_data
        new_password = cleaned_data.get("new_password")
        confirm_password = cleaned_data.get("confirm_password")
        if new_password != confirm_password:
            raise ValidationError('The passwords you enterred do not match')
        return cleaned_data


class DayOfWeekGetParams():
    ''' god damn it, none of this ugliness would be here if tastypie 0.9.12 would come out already '''
    possible_vals = ['day_of_week__eq', 'day_of_week__gt', 'day_of_week__gte', 'day_of_week__lt', 'day_of_week__lte']
    
    def __init__(self, param_dict):
        self.vals = {}
        for p_val in self.possible_vals:
            actual = param_dict.get(p_val, None)
            if actual:
                self.vals[p_val] = actual
        logger.debug('DayOfWeekGetParams vals are: %s' % str(self.vals))
    
    def apply_filters(self, queryset):
        if 'day_of_week__eq' in self.vals:
            queryset = queryset.filter(day_of_week=self.vals['day_of_week__eq'])
        if 'day_of_week__gt' in self.vals:
            queryset = queryset.filter(day_of_week__gt=self.vals['day_of_week__gt'])
        if 'day_of_week__gte' in self.vals:
            queryset = queryset.filter(day_of_week__gte=self.vals['day_of_week__gte'])
        if 'day_of_week__lt' in self.vals:
            queryset = queryset.filter(day_of_week__lt=self.vals['day_of_week__lt'])
        if 'day_of_week__lte' in self.vals:
            queryset = queryset.filter(day_of_week__lte=self.vals['day_of_week__lte'])
        return queryset
    
class TimeParams():
    possible_vars = ['start_time', 'end_time']
    possible_appendixes = ['gte', 'lte']
    
    def __init__(self, param_dict):
        self.vals = {}
        for var in self.possible_vars:
            for appendix in self.possible_appendixes:
                var_name = '%s__%s' % (var, appendix)
                param_value = param_dict.get(var_name, None)
                if param_value:
                    self.vals[var_name] = datetime.datetime.strptime(param_value, '%H%M%S')
        logger.debug('TimeParams vals are: %s' % str(self.vals))
    
    def apply_filters(self, queryset):
        start_time = None
        end_time = None
        if 'start_time__gte' in self.vals:
            start_time = self.vals['start_time__gte']
            queryset = queryset.filter(start_time__gte=start_time)
        
        if 'end_time__lte' in self.vals:
            end_time = self.vals['end_time__lte']
            queryset = queryset.filter(end_time__lte=end_time)
        
        if start_time and end_time:
            if end_time > start_time:
                queryset = queryset.filter(start_time__lte=end_time, end_time__gte=start_time)
            
        return queryset


def temp_meeting_to_json_obj(meeting):
    ''' tastypie not gonna support geodjango fields till 0.9.1.2, gotta whip something up in the meantime '''
    json_obj = {}
    json_obj['day_of_week'] = meeting.day_of_week
    json_obj['start_time'] = str(meeting.start_time)
    json_obj['end_time'] = str(meeting.end_time)
    json_obj['name'] = meeting.name
    json_obj['description'] = meeting.description
    json_obj['address'] = meeting.address
    json_obj['internal_type'] = meeting.internal_type
    json_obj['lat'] = meeting.geo_location.y
    json_obj['long'] = meeting.geo_location.x
    try:
        json_obj['distance'] = meeting.distance.mi
    except:
        json_obj['distance'] = 0
    json_obj['id'] = meeting.pk
    json_obj['creator'] = meeting.creator.username
    json_obj['created_date'] = meeting.created_date.isoformat()
    return json_obj


def temp_json_obj_to_meeting(json_obj):
    meeting = Meeting()
    meeting.day_of_week = json_obj['day_of_week']
    if not (meeting.day_of_week >= 1 and meeting.day_of_week) <= 7:
        raise ValueError("Day of week must be an integer between 1 and 7 inclusive")
    meeting.start_time = datetime.datetime.strptime(json_obj['start_time'], '%H:%M:%S')
    meeting.end_time = datetime.datetime.strptime(json_obj['end_time'], '%H:%M:%S')
    meeting.name = json_obj['name']
    meeting.description = json_obj['description']
    meeting.address = json_obj['address']
    meeting.internal_type = Meeting.SUBMITTED
    meeting.geo_location = fromstr('POINT(%s %s)' % (json_obj['long'], json_obj['lat']), srid=4326)
    return meeting

def get_meetings_count_query_set(name, distance_miles, latitude, longitude,
                           day_of_week_params, day_of_week_in_params,
                           time_params, limit, offset, order_by_column):
    meetings = Meeting.objects.all()
    if name:
        meetings = meetings.filter(name__icontains=name)
    meetings = day_of_week_params.apply_filters(meetings)
    if day_of_week_in_params:
        meetings = meetings.filter(day_of_week__in=day_of_week_in_params)
    meetings = time_params.apply_filters(meetings)
    if distance_miles and latitude and longitude:
        pnt = fromstr('POINT(%s %s)' % (longitude, latitude), srid=4326)
        meetings = meetings.filter(geo_location__distance_lte=(pnt, D(mi=distance_miles)))
        meetings = meetings.distance(pnt).order_by('distance')

    if order_by_column:
        meetings = meetings.order_by(order_by_column)

    pre_offset_count = meetings.count()
    logger.debug('Preoffset count is: %s' % pre_offset_count)
    if offset is not None and limit is not None:
        meetings = meetings[offset:limit]
    elif offset is not None or limit is not None:
        raise ValueError("You must pass in both an offset and a limit, or neither of them.")
    
    return (pre_offset_count, meetings)


def get_meeting_by_id(request):
    if request.method == 'GET':
        meeting_id = request.GET.get('meeting_id', None)
        retval_obj = get_json_obj_for_meetings([Meeting.objects.get(pk = meeting_id)], 1, 1)
        return HttpResponse(content=json.dumps(retval_obj))
    else:
        return HttpResponse(content="You must use GET to retrieve meetings", status=400)


def get_meetings_within_distance(request):
    ''' get all meetings within distance miles from passed in lat/long '''
    if request.method == 'GET':
        logger.info("Got request with params: %s" % str(request.GET))
        name = request.GET.get('name', None)
        distance_miles = request.GET.get('distance_miles', 50)
        latitude = request.GET.get('lat', 39.0839)
        longitude = request.GET.get('long', -77.1531)
        day_of_week_params = DayOfWeekGetParams(request.GET)
        day_of_week_in_params = request.GET.getlist('day_of_week_in')
        time_params = TimeParams(request.GET)
        limit = request.GET.get("limit", 1000)
        offset = request.GET.get("offset", 0)
        order_by = request.GET.get("order_by", None)
        (count, meetings) = get_meetings_count_query_set(name, distance_miles, latitude, longitude,
                                          day_of_week_params, day_of_week_in_params,
                                          time_params, limit, offset, order_by)
        logger.debug("Number of meetings in qs is: %s" % len(meetings))
        retval_obj = get_json_obj_for_meetings(meetings, total_count=count)
        return HttpResponse(content=json.dumps(retval_obj))
    else:
        return HttpResponse(content="You must use GET to retrieve meetings", status=400)

@csrf_exempt
def send_reset_conf(request):
    if request.method == 'POST':
        username = request.POST.get('username', None)
        user = User.objects.get(username=username)
        user_confirmation = UserConfirmation(user=user)
        user_confirmation.expiration_date = datetime.datetime.now() + datetime.timedelta(days=3)
        user_confirmation.confirmation_key = ''.join([random.choice(string.digits + string.letters) for i in range(0, 63)])
        user_confirmation.save()
        link_address = '?confirmation=' + user_confirmation.confirmation_key
        link_address = request.build_absolute_uri('reset_password/') + link_address
        message_text = "Click the link below to reset your aabuddy password\n%s" % link_address
        send_email_to_user(user, "Your AA Buddy reset password confirmation", message_text)
        return HttpResponse(status=200)

@csrf_exempt
def create_user(request):
    ''' create a new user '''
    if request.method == 'POST':
        username = request.POST.get('username', None)
        password = request.POST.get('password', None)
        logger.debug("About to register %s" % (username))
        user = User(username=username, email=username, first_name='NOT_SET', last_name='NOT_SET', is_active=False,
                    is_superuser=False, is_staff=False)
        user.set_password(password)
        user.save()
        user_confirmation = UserConfirmation(user=user)
        user_confirmation.expiration_date = datetime.datetime.now() + datetime.timedelta(days=3)
        user_confirmation.confirmation_key = ''.join([random.choice(string.digits + string.letters) for i in range(0, 63)])
        user_confirmation.save()
        link_address = request.build_absolute_uri() + '/?confirmation=' + user_confirmation.confirmation_key
        message_text = "Click the link below to complete the registration perocess\n%s" % link_address
        send_email_to_user(user, "Thanks you for registering on AA Buddy", message_text)
        return HttpResponse(status=200)
    if request.method == 'GET':
        conf_key = request.GET.get('confirmation', None)
        if conf_key:
            user_confirmation = UserConfirmation.objects.get(confirmation_key=conf_key)
            if user_confirmation.expiration_date > datetime.datetime.now():
                user = user_confirmation.user
                user.is_active = True
                user.save()
                user_confirmation.expiration_date = datetime.datetime.now()
                user_confirmation.save()
                return HttpResponse(content="Successfully Activated %s's account, you can now submit meetings" % user.username, status=200)
            else:
                return HttpResponse(content="User Confirmation out of date!", status=400)
        else:
            return HttpResponse(content="No user confirmation specified!", status=400)


@csrf_exempt
def change_password(request):
    do_basic_auth(request)
    if request.method == 'POST' and request.user.is_authenticated() and request.user.is_active:
        new_password = request.POST.get("new_password")
        user = request.user
        user.set_password(new_password)
        user.save()
        return HttpResponse(status=200)
    elif request.method == 'POST':
        return HttpResponse(content="User not logged in or inactive", status=401)
    else:
        return HttpResponse(content="You must use POST to change password", status=400)
    
def reset_password(request):
    if request.method == 'POST':
        form = ResetPasswordForm(request.POST, request.FILES)
        if form.is_valid():
            logger.debug('User Confirmation is:' + form.cleaned_data['user_confirmation'])
            user_conf_key = form.cleaned_data['user_confirmation']
            user_confirmation = UserConfirmation.objects.get(confirmation_key=user_conf_key)
            if user_confirmation.expiration_date > datetime.datetime.now():
                user = user_confirmation.user
                user.set_password(form.cleaned_data['new_password'])
                user.save()
                user_confirmation.expiration_date = datetime.datetime.now()
                user_confirmation.save()
                return render_to_response('reset_password.html', {})
            else:
                return HttpResponse(content="User confirmation is invalid, expired or does not exist", status=401)
    else:
        user_conf_key = request.GET.get('confirmation', None)
        user_conf = UserConfirmation.objects.filter(confirmation_key=user_conf_key)
        if len(user_conf) == 1 and user_conf[0].expiration_date > datetime.datetime.now():
            form = ResetPasswordForm()
            form.fields['user_confirmation'].widget.attrs = form.fields['user_confirmation'].widget.build_attrs(extra_attrs={'value': user_conf[0].confirmation_key})
        else:
            return HttpResponse(content="User confirmation is invalid, expired or does not exist", status=401)
        
    context = {'form': form}
    context.update(csrf(request))
    return render_to_response('reset_password.html', context)


def send_email_to_user(user, subject_text, message_text):
    logger.debug("About to send conf email with message %s" % message_text)
    send_mail(subject=subject_text, 
              message=message_text, 
              from_email="aabuddy@noreply.com", recipient_list=[user.email], fail_silently=False)

def get_json_obj_for_meetings(meetings, total_count=None, current_count=None):
    if total_count is None:
        total_count = len(meetings)
    if current_count is None:
        current_count = len(meetings)
    retval_obj = {'meta': {'total_count': total_count, 'current_count': current_count}, 'objects': []}
    for meeting in meetings:
            retval_obj['objects'].append(temp_meeting_to_json_obj(meeting))
    return retval_obj


@csrf_exempt
def find_similar(request):
    if request.method == 'POST':
        json_obj = json.loads(request.raw_post_data)
        meeting = temp_json_obj_to_meeting(json_obj)
        similar_meetings = find_similar_to_meeting(meeting)
        retval_obj = get_json_obj_for_meetings(similar_meetings)
        return HttpResponse(content=json.dumps(retval_obj))


def find_similar_to_meeting(meeting):
    similar_meetings = Meeting.objects.filter(day_of_week=meeting.day_of_week, 
                                                  start_time__lte=meeting.start_time+datetime.timedelta(minutes=10),
                                                  start_time__gte=meeting.start_time-datetime.timedelta(minutes=10),
                                                  end_time__lte=meeting.end_time+datetime.timedelta(minutes=10),
                                                  end_time__gte=meeting.end_time-datetime.timedelta(minutes=10))
    similar_meetings = similar_meetings.filter(geo_location__distance_lte=(meeting.geo_location, D(mi=0.1)))
    similar_meetings = similar_meetings.distance(meeting.geo_location).order_by('distance')
    similar_meetings = similar_meetings[0:20]
    return similar_meetings


@csrf_exempt
def save_meeting(request):
    ''' save a meeting '''
    do_basic_auth(request)
    logger.debug("request user is: " + request.user.username)
    if request.method == 'POST' and request.user.is_authenticated() and request.user.is_active:
        logger.debug("posting meeting, username is %s; meeting raw data is %s" % (request.user.username, request.raw_post_data))
        json_obj = json.loads(request.raw_post_data)
        logger.debug("About to try and save json: %s" % str(json_obj))
        meeting = temp_json_obj_to_meeting(json_obj)
        meeting.creator = request.user
        meeting.save()
        logger.debug("meeting %s posted!" % meeting.name)
        retval_obj = get_json_obj_for_meetings([Meeting.objects.get(pk = meeting.pk)], 1, 1)
        return HttpResponse(content=json.dumps(retval_obj), status=200)
    elif request.method == 'POST':
        return HttpResponse(content="User not logged in or inactive", status=401)
    else:
        return HttpResponse(content="You must use POST to submit meetings", status=400)


@csrf_exempt
def post_meeting_not_there(request):
    ''' save a meeting not there object '''
    do_basic_auth(request)
    logger.debug("request user is: " + request.user.username)
    if request.method == 'POST':
        notThere = MeetingNotThere()
        if 'REMOTE_ADDR' in request.META:
            notThere.request_host = request.META['REMOTE_ADDR']
        if 'HTTP_USER_AGENT' in request.META:
            notThere.user_agent = request.META['HTTP_USER_AGENT']
        if request.user.is_authenticated() and request.user.is_active:
            notThere.user = request.user
        logger.debug("OOOOO %s" % str(request.POST.items))
        notThere.meeting = Meeting.objects.get(pk = request.POST.get('meeting_id', None))
        notThere.unique_phone_id = request.POST.get('unique_phone_id', None)
        note = request.POST.get('note', None)
        if note:
            notThere.note = note
        
        notThere.save()
        return HttpResponse(content=json.dumps({'not_there_id': notThere.pk}), status=200)
    else:
        return HttpResponse(content="You must use POST to submit not_theres", status=400)

@csrf_exempt
def validate_user_creds(request):
    do_basic_auth(request)
    logger.debug("username: %s; active: %s; is_authenticated: %s" % (request.user.username,
                                                                     request.user.is_active,
                                                                     request.user.is_authenticated()))
    if request.user.is_authenticated() and request.user.is_active:
        return HttpResponse(status=200)
    else:
        return HttpResponse(status=401)


@csrf_exempt
def delete_my_meeting(request):
    do_basic_auth(request)
    meeting_id = request.GET.get('meeting_id')
    logger.debug("gonna try and delete meeting %s for username %s" % (meeting_id, request.user.username))
    meeting = Meeting.objects.get(pk = meeting_id)
    if request.user.is_authenticated() and request.user.is_active and meeting.creator.username == request.user.username:
        meeting.delete()
        return HttpResponse(status=200)
    

def do_basic_auth(request, *args, **kwargs):
    from django.contrib.auth import authenticate, login
    if request.META.has_key('HTTP_AUTHORIZATION'):
        logger.debug("request header for auth present.")
        authmeth, auth = request.META['HTTP_AUTHORIZATION'].split(' ', 1)
        if authmeth.lower() == 'basic':
            logger.debug("authmeth is good")
            auth = auth.strip().decode('base64')
            username, password = auth.split(':', 1)
            logger.debug("about to try and authenticate " + username)
            user = authenticate(username=username, password=password)
            if user:
                logger.debug("user %s authenticated!" % username)
                login(request, user)