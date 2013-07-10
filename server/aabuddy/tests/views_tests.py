''' Test package for views.py '''
from django.test import TestCase
from aabuddy.views import temp_meeting_to_json_obj, temp_json_obj_to_meeting,\
    DayOfWeekGetParams, TimeParams, get_meetings_count_query_set
from aabuddy.models import Meeting
import datetime
from django.contrib.auth.models import User
from django.contrib.gis.geos.factory import fromstr
import json
from mockito import unstub, when, any, mock, verify, contains
from django.http import QueryDict
import re

TEST_MEETING_JSON = '''
        {"day_of_week": 1, 
        "start_time": "14:22:59",
        "end_time": "15:54:18",
        "name": "test_name",
        "description": "test_description",
        "address": "test_address",
        "long": -77.4108,
        "lat": 39.4142}
'''

class TestViews(TestCase):
    ''' Tests for views.py '''
    fixtures = ['test_users.json', 'test_meetings.json']
    
    def tearDown(self):
        '''tear down tests'''
        unstub()
        
    def __get_test_meeting(self):
        ''' Returns a test meeting '''
        creator = User(username='Mooo')
        location = fromstr('POINT(-95.362293 29.756539)', srid=4326)
        return Meeting(name='Mooo', day_of_week=1, start_time=datetime.datetime.now(),
                          end_time=datetime.datetime.now(), description='test', address='Test',
                          creator=creator, created_date=datetime.datetime.now(),
                          geo_location=location)
    
    def test_temp_meeting_to_json_obj(self):
        ''' test the temp_meeting_to_json_obj without a distance or an id '''
        meeting = self.__get_test_meeting()
        retval = temp_meeting_to_json_obj(meeting)
        self.assertEqual(retval['name'], meeting.name)
        self.assertEqual(retval['distance'], 0)
        self.assertEqual(retval['id'], None)
        
    def test_temp_meeting_to_json_obj_with_distance(self):
        ''' test the temp_meeting_to_json_obj with a distance '''
        meeting = self.__get_test_meeting()
        meeting.distance = mock()
        meeting.distance.mi = 3.9
        retval = temp_meeting_to_json_obj(meeting)
        self.assertEqual(retval['distance'], 3.9)
    
    def test_temp_meeting_to_json_obj_with_pk(self):
        ''' test the temp_meeting_to_json_obj with an id '''
        meeting = self.__get_test_meeting()
        meeting.pk = 30001233
        retval = temp_meeting_to_json_obj(meeting)
        self.assertEqual(retval['id'], 30001233)
    
    def test_temp_json_obj_to_meeting(self):
        ''' test temp_json_obj_to_meeting '''
        json_obj = json.loads(TEST_MEETING_JSON)
        meeting = temp_json_obj_to_meeting(json_obj)
        self.assertEqual(meeting.name, 'test_name')
        self.assertEqual(meeting.geo_location.x, -77.4108)
        self.assertEqual(meeting.geo_location.y, 39.4142)
        self.assertEqual(meeting.start_time.hour, 14)
        self.assertEqual(meeting.start_time.minute, 22)
        self.assertEqual(meeting.start_time.second, 59)
        
    def test_temp_json_obj_to_meeting_bad_day_of_week(self):
        ''' test temp_json_obj_to_meeting with a bad day_of_week '''
        json_obj = json.loads(TEST_MEETING_JSON)
        json_obj["day_of_week"] = 73
        self.assertRaisesMessage(ValueError, "Day of week must be an integer between 1 and 7 inclusive",
                                 temp_json_obj_to_meeting, json_obj)
        
    def test_get_meetings_count_query_set(self):
        ''' test get_meetings_count_query_set '''
        name = 'test'
        distance_miles = 50
        latitude = 39.0839
        longitude = -77.1531
        day_of_week_params = DayOfWeekGetParams(QueryDict(''))
        day_of_week_in_params = []
        time_params = TimeParams(QueryDict(''))
        limit = 1000
        offset = 0
        order_by = None
        (count, meetings) = get_meetings_count_query_set(name, distance_miles, latitude, longitude,
                                          day_of_week_params, day_of_week_in_params,
                                          time_params, limit, offset, order_by)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].pk, 1)
    
    def test_get_meetings_count_query_set_misses_day_of_week(self):
        ''' test get_meetings_count_query_set when meeting is missed due to day of week '''
        name = 'test'
        distance_miles = 50
        latitude = 39.0839
        longitude = -77.1531
        day_of_week_qd = QueryDict('').copy()
        day_of_week_qd.update({"day_of_week__eq": 2})
        day_of_week_params = DayOfWeekGetParams(day_of_week_qd)
        day_of_week_in_params = []
        time_params = TimeParams(QueryDict(''))
        limit = 1000
        offset = 0
        order_by = None
        (count, meetings) = get_meetings_count_query_set(name, distance_miles, latitude, longitude,
                                          day_of_week_params, day_of_week_in_params,
                                          time_params, limit, offset, order_by)
        self.assertEqual(count, 0)
        self.assertEqual(len(meetings), 0)


class TestDayOfWeekGetParams(TestCase):
    ''' Test class for DayOfWeekGetParams '''
    
    def test_init(self):
        dp = DayOfWeekGetParams({"mooo": "aaa", "day_of_week__gte": None, 'day_of_week__lt': "123aaa"})
        self.assertEqual(dp.vals, {'day_of_week__lt': "123aaa"})
        
    def test_apply_filters_all(self):
        dp = DayOfWeekGetParams({'day_of_week__eq': 1, 
                                 'day_of_week__gt': 2, 
                                 'day_of_week__gte': 3, 
                                 'day_of_week__lt': 4, 
                                 'day_of_week__lte': 5})
        retval = dp.apply_filters(Meeting.objects.all())
        self.assertIsNotNone(retval)
        query_str = str(retval.query)
        self.assertIsNotNone(re.match(".*WHERE.*day_of_week.*\=.*1", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*day_of_week.*\>.*2", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*day_of_week.*\>\=.*3", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*day_of_week.*\<.*4", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*day_of_week.*\<\=.*5", query_str))


class TestTimeParams(TestCase):
    ''' Test class for TimeParams '''
    
    def test_init(self):
        tp = TimeParams({"moooo": "oooo", 'start_time__gte': '', 'end_time__lte': '134455'})
        self.assertEqual(tp.vals.keys(), ['end_time__lte'])
        self.assertEqual(tp.vals['end_time__lte'], datetime.datetime.strptime('134455', '%H%M%S'))
        
    def test_apply_filters_endtime_bigger(self):
        tp = TimeParams({"moooo": "oooo", 'start_time__gte': '124050', 'end_time__lte': '134455'})
        retval = tp.apply_filters(Meeting.objects.all())
        query_str = str(retval.query)
        self.assertIsNotNone(re.match(".*WHERE.*start_time.*\>\=.*12:40:50", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*end_time.*\<\=.*13:44:55", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*start_time.*\<\=.*13:44:55", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*end_time.*\>\=.*12:40:50", query_str))

    def test_apply_filters_endtime_smaller(self):
        tp = TimeParams({"moooo": "oooo", 'start_time__gte': '234050', 'end_time__lte': '004455'})
        retval = tp.apply_filters(Meeting.objects.all())
        query_str = str(retval.query)
        self.assertIsNotNone(re.match(".*WHERE.*start_time.*\>\=.*23:40:50", query_str))
        self.assertIsNotNone(re.match(".*WHERE.*end_time.*\<\=.*00:44:55", query_str))
        self.assertIsNone(re.match(".*WHERE.*start_time.*\<\=.*13:44:55", query_str))
        self.assertIsNone(re.match(".*WHERE.*end_time.*\>\=.*12:40:50", query_str))