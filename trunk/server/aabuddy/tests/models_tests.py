''' test package for models.py '''
import datetime
from django.test import TestCase
from aabuddy.models import Meeting, MeetingNotThere, NotThereView
from django.contrib.auth.models import User
from django.contrib.gis.geos.factory import fromstr

class TestMeeting(TestCase):
    ''' test class for meeting model '''
    fixtures = ['test_users.json', 'test_meetings.json']
    
    def tearDown(self):
        '''tear down tests'''

    def __get_test_meeting(self):
        meeting = Meeting()
        meeting.name = "a"
        meeting.description = "b"
        meeting.day_of_week = 3
        meeting.start_time = datetime.datetime.strptime('12:20:00', '%H:%M:%S')
        meeting.end_time = datetime.datetime.strptime('00:00:00', '%H:%M:%S')
        meeting.address = "c"
        meeting.geo_location = fromstr('POINT(%s %s)' % (2, 1), srid=4326)
        return meeting

    def test_meeting_get_psv_row(self):
        self.assertEqual(self.__get_test_meeting().get_psv_row(), "a|b|3|12:20:00|00:00:00|c|1.0|2.0")
        
    def test_meeting_save(self):
        meeting = self.__get_test_meeting()
        self.assertEqual(meeting.created_date, datetime.datetime(1982,12,22))
        meeting.creator = User.objects.get(username='test_user')
        meeting.save()
        meeting = Meeting.objects.get(name='a')
        created_date = meeting.created_date
        self.assertGreater(created_date, datetime.datetime(1982,12,22))
        meeting.save()
        self.assertEqual(created_date, meeting.created_date)
        
    def test_meeting_str(self):
        self.assertEqual(Meeting.objects.get(name='test_meeting1').__str__(), "Meeting id: 1, Creator: test_user")


class TestMeetingNotThere(TestCase):
    ''' test class for meetingnotthere model '''
    
    def test_str(self):
        mnt = MeetingNotThere()
        mnt.pk = 1
        mnt.request_host = 'mooo'
        self.assertEqual(mnt.__str__(), "Not There id: 1, host: mooo")


class TestNotThereView(TestCase):
    ''' test class for notthereview model '''
    
    def test_str(self):
        ntv = NotThereView()
        ntv.not_there_count = 5
        ntv.meeting_name = 'zzz'
        self.assertEqual(ntv.__str__(), "NTV row meeting_name: zzz, count: 5")