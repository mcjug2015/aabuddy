from django.contrib.gis.geos import *
from django.contrib.gis.measure import D
from aabuddy.models import Meeting
import json
from django.http import HttpResponse
import logging
from django.views.decorators.csrf import csrf_exempt
import datetime
logger = logging.getLogger(__name__)


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
            queryset = queryset.filter(day_of_week__eq=self.vals['day_of_week__eq'])
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
    possible_appendixes = ['gt', 'gte', 'lt', 'lte']
    
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
        if 'start_time__gt' in self.vals:
            queryset = queryset.filter(start_time__gt=self.vals['start_time__gt'])
        if 'start_time__gte' in self.vals:
            queryset = queryset.filter(start_time__gte=self.vals['start_time__gte'])
        if 'start_time__lt' in self.vals:
            queryset = queryset.filter(start_time__lt=self.vals['start_time__lt'])
        if 'start_time__lte' in self.vals:
            queryset = queryset.filter(start_time__lte=self.vals['start_time__lte'])
        
        if 'end_time__gt' in self.vals:
            queryset = queryset.filter(end_time__gt=self.vals['end_time__gt'])
        if 'end_time__gte' in self.vals:
            queryset = queryset.filter(end_time__gte=self.vals['end_time__gte'])
        if 'end_time__lt' in self.vals:
            queryset = queryset.filter(end_time__lt=self.vals['end_time__lt'])
        if 'end_time__lte' in self.vals:
            queryset = queryset.filter(end_time__lte=self.vals['end_time__lte'])
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
    json_obj['distance'] = meeting.distance.mi
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
    meeting.geo_location = fromstr('POINT(%s %s)' % (json_obj['lat'], json_obj['long']), srid=4326)
    return meeting
    

def get_meetings_count_query_set(distance_miles, latitude, longitude,
                           day_of_week_params, day_of_week_in_params,
                           time_params, limit, offset, order_by_column):
    meetings = Meeting.objects.all()
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

    if offset and limit:
        meetings = meetings[offset:limit]
    elif offset or limit:
        raise ValueError("You must pass in both an offset and a limit, or neither of them.")
    
    return (pre_offset_count, meetings)


def get_meetings_within_distance(request):
    ''' get all meetings within distance miles from passed in lat/long '''
    if request.method == 'GET':
        logger.info("Got request with params: %s" % str(request.GET))
        distance_miles = request.GET.get('distance_miles', 50)
        latitude = request.GET.get('lat', 39.0839)
        longitude = request.GET.get('long', -77.1531)
        day_of_week_params = DayOfWeekGetParams(request.GET)
        day_of_week_in_params = request.GET.getlist('day_of_week_in')
        time_params = TimeParams(request.GET)
        limit = request.GET.get("limit", None)
        offset = request.GET.get("offset", None)
        order_by = request.GET.get("order_by", None)
        (count, meetings) = get_meetings_count_query_set(distance_miles, latitude, longitude,
                                          day_of_week_params, day_of_week_in_params,
                                          time_params, limit, offset, order_by)
        retval_obj = {'meta': {'total_count': count}, 'objects': []}
        for meeting in meetings:
            retval_obj['objects'].append(temp_meeting_to_json_obj(meeting))
        retval_obj['meta']['current_count'] = len(meetings)
        return HttpResponse(json.dumps(retval_obj))
    

@csrf_exempt
def save_meeting(request):
    ''' save a meeting '''
    if request.method == 'POST':
        json_obj = json.loads(request.raw_post_data)
        logger.debug("About to try and save json: %s" % str(json_obj))
        meeting = temp_json_obj_to_meeting(json_obj)
        meeting.save()
        return HttpResponse(200)
    