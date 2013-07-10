''' module for views integration tests '''
from django.test import TestCase
from aabuddy.views import get_meetings_count_query_set, DayOfWeekGetParams,\
    TimeParams
from aabuddy.models import Meeting


class TestResetPassword(TestCase):
    ''' tests for the reset password form '''
    
    def test_reset_password_form_password_mismatch(self):
        resp = self.client.post("/aabuddy/reset_password/", {"username": "moooo",
                                                             "new_password": "aaaa",
                                                             "confirm_password": "bbb",
                                                             "user_confirmation": "123"})
        self.assertEqual(resp.status_code, 200)
        self.assertIn("The passwords you enterred do not match", resp.context['form'].errors['__all__'])
    
    def test_reset_password_form_valid(self):
        ''' when the form is valid, but user conf is not in the db we get an explosion. '''
        resp = self.client.post("/aabuddy/reset_password/", {"username": "moooo",
                                                             "new_password": "aaaa",
                                                             "confirm_password": "aaaa",
                                                             "user_confirmation": "123"})
        self.assertEqual(resp.status_code, 401)
        self.assertIn("User confirmation is invalid, expired or does not exist", resp.content)


class TestGetMeetingsMethod(TestCase):
    ''' class to test the get_meetings_count_query_set method '''
    fixtures = ['test_users.json', 'test_lots_of_meetings.json']
    
    def test_all_empty_none(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}), None, None, None)
        self.assertGreater(count, 0)
        self.assertEqual(count, Meeting.objects.all().count())
        
    def test_name(self):
        (count, meetings) = get_meetings_count_query_set('test_meeting1', None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
        (count, meetings) = get_meetings_count_query_set('eting1', None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
    def test_day_of_week_eq(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({'day_of_week__eq': 1}), None,
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].day_of_week, 1)
        
    def test_day_of_week_gt(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({'day_of_week__gt': 2}), None,
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].day_of_week, 3)
    
    def test_day_of_week_gte(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({'day_of_week__gte': 2}), None,
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 2)
        self.assertGreaterEqual(meetings[0].day_of_week, 2)
        self.assertGreaterEqual(meetings[1].day_of_week, 2)
        
    def test_day_of_week_in(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), [1, 2],
                                          TimeParams({}), None, None, None)
        self.assertEqual(count, 2)
        self.assertIn(meetings[0].day_of_week, [1, 2])
        self.assertIn(meetings[1].day_of_week, [1, 2])
        
    def test_time_gte(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({'start_time__gte': '000001'}), None, None, None)
        self.assertEqual(count, 2)
        
    def test_time_lte(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({'end_time__lte': '010000'}), None, None, None)
        self.assertEqual(count, 2)
    
    def test_time_gte_lte(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({'start_time__gte': '233000', 'end_time__lte': '003000'}),
                                          None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting2')
        
    def test_distance(self):
        (count, meetings) = get_meetings_count_query_set(None, 10, 39.4142, -77.4108,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}),
                                          None, None, None)
        self.assertGreater(count, 0)
        self.assertEqual(count, 2)
        
    def test_order_by_name(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}),
                                          None, None, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        self.assertEqual(meetings[1].name, 'test_meeting2')
        self.assertEqual(meetings[2].name, 'test_meeting3')
        
    def test_order_by_start_time(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}),
                                          None, None, 'start_time')
        self.assertEqual(count, 3)
        self.assertEqual(meetings[0].name, 'test_meeting3')
        self.assertEqual(meetings[1].name, 'test_meeting1')
        self.assertEqual(meetings[2].name, 'test_meeting2')
        
    def test_limit_offset(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}),
                                          1, 0, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(len(meetings), 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
    def test_limit_offset_by_one(self):
        (count, meetings) = get_meetings_count_query_set(None, None, None, None,
                                          DayOfWeekGetParams({}), None,
                                          TimeParams({}),
                                          2, 1, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(len(meetings), 1)
        self.assertEqual(meetings[0].name, 'test_meeting2')
        
    def test_limit_offset_exception(self):
        self.assertRaisesMessage(ValueError, 
                                 "You must pass in both an offset and a limit, or neither of them.", 
                                 get_meetings_count_query_set, 
                                 None, None, None, None, DayOfWeekGetParams({}), 
                                 None, TimeParams({}), 2, None, 'name')