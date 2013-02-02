''' Test package for views.py '''
import sys
from django.test import TestCase

class TestViews(TestCase):
    
    def test_temp_meeting_to_json_obj(self):
        print sys.path
        from aabuddy.views import temp_meeting_to_json_obj
        from aabuddy.models import Meeting
        import datetime
        from django.contrib.auth.models import User
        from django.contrib.gis.geos.factory import fromstr
        creator = User(username='Mooo')
        location = fromstr('POINT(-95.362293 29.756539)', srid=4326)
        meeting = Meeting(name='Mooo', day_of_week=1, start_time=datetime.datetime.now(),
                          end_time=datetime.datetime.now(), description='test', address='Test',
                          creator=creator, created_date=datetime.datetime.now(),
                          geo_location=location)
        retval = temp_meeting_to_json_obj(meeting)
        self.assertEqual(retval['name'], meeting.name)