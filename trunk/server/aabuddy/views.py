from django.contrib.gis.geos import *
from django.contrib.gis.measure import D
from aabuddy.models import Meeting
import json
from django.http import HttpResponse
import logging
from django.views.decorators.csrf import csrf_exempt
import datetime
logger = logging.getLogger(__name__)


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
    json_obj['lat'] = meeting.geo_location.x
    json_obj['long'] = meeting.geo_location.y
    return json_obj


def temp_json_obj_to_meeting(json_obj):
    meeting = Meeting()
    meeting.day_of_week = json_obj['day_of_week']
    logger.debug("MOOOOOO " + json_obj['start_time'] + " OOO " + json_obj['end_time'])
    meeting.start_time = datetime.datetime.strptime(json_obj['start_time'], '%H:%M:%S')
    meeting.end_time = datetime.datetime.strptime(json_obj['end_time'], '%H:%M:%S')
    meeting.name = json_obj['name']
    meeting.description = json_obj['description']
    meeting.address = json_obj['address']
    meeting.internal_type = json_obj['internal_type']
    meeting.geo_location = fromstr('POINT(%s %s)' % (json_obj['lat'], json_obj['long']), srid=4326)
    return meeting
    

def get_meetings_within_distance(request):
    ''' get all meetings within distance miles from passed in lat/long '''
    if request.method == 'GET':
        distance_miles = request.GET.get('distance_miles', 500)
        latitude = request.GET.get('lat', -77.1531)
        longitude = request.GET.get('long', 39.0839)
        pnt = fromstr('POINT(%s %s)' % (latitude, longitude), srid=4326)
        meetings = Meeting.objects.filter(geo_location__distance_lte=(pnt, D(mi=distance_miles)))
        retval_obj = []
        for meeting in meetings:
            retval_obj.append(temp_meeting_to_json_obj(meeting))
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
    