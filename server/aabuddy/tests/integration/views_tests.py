''' module for views integration tests '''
from django.test import TestCase
import aabuddy.views as views
from aabuddy.models import Meeting, UserConfirmation
import json
from mockito import unstub, when, any, mock, verify, contains
from django.contrib.auth.models import User
import datetime
import base64


class TestViews(TestCase):
    ''' tests for views methods '''
    fixtures = ['test_users.json', 'test_meetings.json']
    
    def tearDown(self):
        '''tear down tests'''
        unstub()
    
    def test_get_meeting_by_id_happy_path(self):
        resp = self.client.get('/aabuddy/get_meeting_by_id', {'meeting_id': 1})
        self.assertEqual(resp.status_code, 200)
        json_meeting_obj = json.loads(resp.content)
        self.assertEquals(json_meeting_obj['objects'][0]['name'], 'test_meeting1')
        
    def test_get_meeting_by_id_post(self):
        resp = self.client.post('/aabuddy/get_meeting_by_id')
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "You must use GET to retrieve meetings")
        
    def test_get_meeting_by_id_empty(self):
        resp = self.client.get('/aabuddy/get_meeting_by_id', {'meeting_id': 666})
        self.assertEqual(resp.status_code, 200)
        json_meeting_obj = json.loads(resp.content)
        self.assertEquals(len(json_meeting_obj.keys()), 0)
        
    def test_get_meetings_within_distance(self):
        resp = self.client.get('/aabuddy/get_meetings', {'order_by': 'name'})
        self.assertEqual(resp.status_code, 200)
        json_meeting_obj = json.loads(resp.content)
        self.assertEquals(json_meeting_obj['objects'][0]['name'], 'test_meeting1')
        
    def test_get_meetings_within_distance_post(self):
        resp = self.client.post('/aabuddy/get_meetings', {'order_by': 'name'})
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "You must use GET to retrieve meetings")
        
    def test_send_reset_conf(self):
        when(views).send_email_to_user(any(), any(), any()).thenReturn(None)
        resp = self.client.post('/aabuddy/send_reset_conf', {'username': 'test_user'})
        self.assertEqual(resp.status_code, 200)
        verify(views, times=1).send_email_to_user(any(), any(), any())
        
    def test_send_reset_conf_no_user(self):
        resp = self.client.post('/aabuddy/send_reset_conf', {'username': 'papa_nurgle'})
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "You must specify a valid username to send a password reset confirmation")
        
    def test_send_reset_conf_get(self):
        resp = self.client.get('/aabuddy/send_reset_conf', {'username': 'papa_nurgle'})
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "You must use POST to send a reset confimation.")
        
    def test_create_user_post(self):
        self.assertEqual(User.objects.filter(username='papa_nurgle').count(), 0)
        self.assertEqual(UserConfirmation.objects.filter(user__username='papa_nurgle').count(), 0)
        when(views).send_email_to_user(any(), any(), any()).thenReturn(None)
        resp = self.client.post('/aabuddy/create_user', {'username': 'papa_nurgle', 'password': 'I am a cow'})
        self.assertEqual(resp.status_code, 200)
        verify(views, times=1).send_email_to_user(any(), any(), any())
        self.assertEqual(User.objects.filter(username='papa_nurgle').count(), 1)
        self.assertEqual(UserConfirmation.objects.filter(user__username='papa_nurgle').count(), 1)
        
    def test_create_user_post_no_username(self):
        resp = self.client.post('/aabuddy/create_user', {})
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "You must pass in an untaken username and a non-empty password.")
    
    def test_create_user_post_taken_username(self):
        self.assertEqual(User.objects.filter(username='test_user').count(), 1)
        self.assertEqual(UserConfirmation.objects.filter(user__username='test_user').count(), 0)
        when(views).send_email_to_user(any(), any(), any()).thenReturn(None)
        resp = self.client.post('/aabuddy/create_user', {'username': 'test_user', 'password': 'moooo'})
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "You must pass in an untaken username and a non-empty password.")
        verify(views, times=0).send_email_to_user(any(), any(), any())
        self.assertEqual(User.objects.filter(username='test_user').count(), 1)
        self.assertEqual(UserConfirmation.objects.filter(user__username='test_user').count(), 0)
        
    def test_create_user_get(self):
        user = User.objects.get(username='test_user')
        user.is_active = False
        user.save()
        self.assertFalse(User.objects.get(username='test_user').is_active)
        uc = UserConfirmation(user=user,
                              expiration_date=datetime.datetime.now() + datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.get('/aabuddy/create_user', {'confirmation': 'mooo123'})
        self.assertEqual(resp.status_code, 200)
        self.assertTrue(User.objects.get(username='test_user').is_active)
    
    def test_create_user_get_no_conf(self):
        resp = self.client.get('/aabuddy/create_user')
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "User confirmation unspecified or invalid!")
        
    def test_create_user_get_invalid_conf(self):
        resp = self.client.get('/aabuddy/create_user')
        self.assertEqual(resp.status_code, 400, {'confirmation': 'mooo123'})
        self.assertEqual(resp.content, "User confirmation unspecified or invalid!")
        
    def test_create_user_get_expired_conf(self):
        uc = UserConfirmation(user=User.objects.get(username='test_user'),
                              expiration_date=datetime.datetime.now() - datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.get('/aabuddy/create_user', {'confirmation': 'mooo123'})
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "User Confirmation out of date!")
        
    def test_change_password(self):
        credentials = base64.b64encode('test_user:oooAAA')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 401)
        
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/change_password', {'new_password': 'oooAAA'})
        self.assertEqual(resp.status_code, 200)
        
        credentials = base64.b64encode('test_user:oooAAA')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 200)
        
    def test_change_password_blank(self):
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/change_password')
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "You must specify a non-empty new password.")
        
    def test_change_password_invalid_auth(self):
        credentials = base64.b64encode('test_user:INVALID_PASSWORD')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/change_password')
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "User not logged in or inactive")
        
    def test_change_password_inactive_user(self):
        user = User.objects.get(username='test_user')
        user.is_active = False
        user.save()
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/change_password')
        self.assertEqual(resp.status_code, 401)
        self.assertEqual(resp.content, "User not logged in or inactive")

    def test_change_password_get(self):
        resp = self.client.get('/aabuddy/change_password')
        self.assertEqual(resp.status_code, 400)
        self.assertEqual(resp.content, "You must use POST to change password")


class TestResetPassword(TestCase):
    ''' tests for the reset password form '''
    fixtures = ['test_users.json']
    
    def test_reset_password_form_password_mismatch(self):
        resp = self.client.post("/aabuddy/reset_password/", {"new_password": "aaaa",
                                                             "confirm_password": "bbb",
                                                             "user_confirmation": "123"})
        self.assertEqual(resp.status_code, 200)
        self.assertIn("The passwords you enterred do not match", resp.context['form'].errors['__all__'])
    
    def test_reset_password_form_valid(self):
        ''' when the form is valid, but user conf is not in the db we get an explosion. '''
        resp = self.client.post("/aabuddy/reset_password/", {"new_password": "aaaa",
                                                             "confirm_password": "aaaa",
                                                             "user_confirmation": "123"})
        self.assertEqual(resp.status_code, 401)
        self.assertIn("User confirmation is invalid, expired or does not exist", resp.content)
        
    def test_reset_password_happy_path(self):
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 200)
        uc = UserConfirmation(user=User.objects.get(username='test_user'),
                              expiration_date=datetime.datetime.now() + datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.post("/aabuddy/reset_password/", {"new_password": "aaaa",
                                                             "confirm_password": "aaaa",
                                                             "user_confirmation": "mooo123"})
        self.assertEqual(resp.status_code, 200)
        credentials = base64.b64encode('test_user:aaaa')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 200)

    def test_expired_conf(self):
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 200)
        uc = UserConfirmation(user=User.objects.get(username='test_user'),
                              expiration_date=datetime.datetime.now() - datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.post("/aabuddy/reset_password/", {"new_password": "aaaa",
                                                             "confirm_password": "aaaa",
                                                             "user_confirmation": "mooo123"})
        self.assertEqual(resp.status_code, 401)
        self.assertIn("User confirmation is expired.", resp.content)
        credentials = base64.b64encode('test_user:1chpok1')
        self.client.defaults['HTTP_AUTHORIZATION'] = 'Basic ' + credentials
        resp = self.client.post('/aabuddy/validate_user_creds')
        self.assertEqual(resp.status_code, 200)
        
    def test_get_happy_path(self):
        uc = UserConfirmation(user=User.objects.get(username='test_user'),
                              expiration_date=datetime.datetime.now() + datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.get("/aabuddy/reset_password/", {"confirmation": "mooo123"})
        self.assertEqual(resp.status_code, 200)
        self.assertEqual(resp.context['form'].fields['user_confirmation'].widget.attrs['value'], "mooo123")
        
    def test_get_no_user_conf(self):
        resp = self.client.get("/aabuddy/reset_password/")
        self.assertEqual(resp.status_code, 401)
        self.assertIn("User confirmation is invalid, expired or does not exist", resp.content)
        
    def test_get_expired_user_conf(self):
        uc = UserConfirmation(user=User.objects.get(username='test_user'),
                              expiration_date=datetime.datetime.now() - datetime.timedelta(days=3),
                              confirmation_key='mooo123')
        uc.save()
        resp = self.client.get("/aabuddy/reset_password/", {"confirmation": "mooo123"})
        self.assertEqual(resp.status_code, 401)
        self.assertIn("User confirmation is invalid, expired or does not exist", resp.content)

class TestGetMeetingsMethod(TestCase):
    ''' class to test the get_meetings_count_query_set method '''
    fixtures = ['test_users.json', 'test_lots_of_meetings.json']
    
    def test_all_empty_none(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertGreater(count, 0)
        self.assertEqual(count, Meeting.objects.all().count())
        
    def test_name(self):
        (count, meetings) = views.get_meetings_count_query_set('test_meeting1', None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
        (count, meetings) = views.get_meetings_count_query_set('eting1', None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
    def test_day_of_week_eq(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({'day_of_week__eq': 1}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].day_of_week, 1)
        
    def test_day_of_week_gt(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({'day_of_week__gt': 2}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].day_of_week, 3)
    
    def test_day_of_week_gte(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({'day_of_week__gte': 2}), None,
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 2)
        self.assertGreaterEqual(meetings[0].day_of_week, 2)
        self.assertGreaterEqual(meetings[1].day_of_week, 2)
        
    def test_day_of_week_in(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), [1, 2],
                                          views.TimeParams({}), None, None, None)
        self.assertEqual(count, 2)
        self.assertIn(meetings[0].day_of_week, [1, 2])
        self.assertIn(meetings[1].day_of_week, [1, 2])
        
    def test_time_gte(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({'start_time__gte': '000001'}), None, None, None)
        self.assertEqual(count, 2)
        
    def test_time_lte(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({'end_time__lte': '010000'}), None, None, None)
        self.assertEqual(count, 2)
    
    def test_time_gte_lte(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({'start_time__gte': '233000', 'end_time__lte': '003000'}),
                                          None, None, None)
        self.assertEqual(count, 1)
        self.assertEqual(meetings[0].name, 'test_meeting2')
        
    def test_distance(self):
        (count, meetings) = views.get_meetings_count_query_set(None, 10, 39.4142, -77.4108,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}),
                                          None, None, None)
        self.assertGreater(count, 0)
        self.assertEqual(count, 2)
        
    def test_order_by_name(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}),
                                          None, None, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        self.assertEqual(meetings[1].name, 'test_meeting2')
        self.assertEqual(meetings[2].name, 'test_meeting3')
        
    def test_order_by_start_time(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}),
                                          None, None, 'start_time')
        self.assertEqual(count, 3)
        self.assertEqual(meetings[0].name, 'test_meeting3')
        self.assertEqual(meetings[1].name, 'test_meeting1')
        self.assertEqual(meetings[2].name, 'test_meeting2')
        
    def test_limit_offset(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}),
                                          1, 0, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(len(meetings), 1)
        self.assertEqual(meetings[0].name, 'test_meeting1')
        
    def test_limit_offset_by_one(self):
        (count, meetings) = views.get_meetings_count_query_set(None, None, None, None,
                                          views.DayOfWeekGetParams({}), None,
                                          views.TimeParams({}),
                                          2, 1, 'name')
        self.assertEqual(count, 3)
        self.assertEqual(len(meetings), 1)
        self.assertEqual(meetings[0].name, 'test_meeting2')
        
    def test_limit_offset_exception(self):
        self.assertRaisesMessage(ValueError, 
                                 "You must pass in both an offset and a limit, or neither of them.", 
                                 views.get_meetings_count_query_set, 
                                 None, None, None, None, views.DayOfWeekGetParams({}), 
                                 None, views.TimeParams({}), 2, None, 'name')