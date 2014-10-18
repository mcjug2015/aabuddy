$> sudo sh setuptools-0.6c11-py2.6.egg
$> sudo easy_install pip
$> pip install virtualenv
$> virtualenv --no-site-packages local-python
$> source local-python/bin/activate
$> pip install Fabric==1.4.3
** OR
$> sudo env ARCHFLAGS="-arch i386 -arch x86_64"
$> pip install Fabric==1.4.3

** local(mac)
$> fab dev_setup


** remote - virtbox
$> fab localvm setup

$> fab localvm deploy_workingenv


** make sure that a username and password are valid and the user is active(allowed to submit meetings)
curl -v --user USERNAME:PASSWORD https://localhost:8888/meetingfinder/validate_user_creds
curl -v --user USERNAME:PASSWORD https://mcasg.org/meetingfinder/validate_user_creds --insecure

** delete a meeting created by a user you authenticate
curl -v --user USERNAME:PASSWORD https://localhost:8888/meetingfinder/delete_my_meeting?meeting_id=1 --insecure

** test creating an inactive user with curl
curl -v -H "Content-Type: application/json" -X POST -d "username=victor.semenov@gmail.com&password=testpassword1" https://localhost:8888/meetingfinder/create_user



** change user password, you must pass in valid existing username/password and use a POST. After that you can
** use validate_user_creds to verify that the change went through.
curl -v --user victor.semenov@gmail.com:testpassword1 -H "Content-Type: application/json" -X POST -d "new_password=mooo" https://localhost:8888/meetingfinder/change_password

** test requesting a password reset for email for a user with curl
curl -v -H "Content-Type: application/json" -X POST -d "username=victor.semenov@gmail.com" https://localhost:8888/meetingfinder/send_reset_conf


** test uploading a meeting with curl:
day_of_week goes from 1 to 7
1 = Monday
2 = Tuesday
...
6 = Saturday
7 = Sunday

internal_type valid values can be found in aabuddy/models.py/Meeting/INTERNAL_TYPE_CHOICES. They are case insensitive
when submitted to the url below, so Submitted and submitted are both valid. If success and status 200, the returned number is the meeting id.
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "sfdfdsfsdfsd", "day_of_week": 1, "description": "fdsfdsdfdsf", "end_time": "17:33:59", "lat": -77.4108, "long": 39.4142, "name": "Frederick meeting", "start_time": "16:30:00"}' --user USERNAME:PASSWORD https://108.179.217.242/meetingfinder/save_meeting
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "1850 Rockville pike, Rockville, MD, 20852", "day_of_week": 3, "description": "Awesome meeting", "end_time": "18:45:00", "lat": -77.121946, "long": 39.059950, "name": "Rockville meeting", "start_time": "17:45:00"}' --user victor.semenov@gmail.com:testpassword1 https://mcasg.org/meetingfinder/save_meeting

For querying the day_of_week can be passed in with the following modifiers:
__eq - day of week equals to
__gt - greater than
__gte - greater than or equal to
__lt - lest than
__lte - less then or equal to

start_time and end_time can be queried just like day_of_week, but do not support the eq operator

** grab meetings:
-- all:
http://localhost:8888/meetingfinder/get_meetings/
-- all that contain the string mooo in their name(case insensitive):
http://localhost:8888/meetingfinder/get_meetings/?name=mooo
-- within 50 miles of specified location
http://localhost:8888/meetingfinder/get_meetings/?lat=39.0839&long=-77.1531&distance_miles=50
-- within 50 miles of specified location between monday and wednesday inclusive:
http://localhost:8888/meetingfinder/get_meetings/?lat=39.0839&long=-77.1531&distance_miles=50&day_of_week__gte=1&day_of_week__lte=3
-- within 50 miles of specified location between monday and wednesday inclusive, starting at or after 4:30pm and ending before 6:30pm:
http://localhost:8888/meetingfinder/get_meetings/?lat=39.0839&long=-77.1531&distance_miles=50&day_of_week__gte=1&day_of_week__lte=3&start_time__gte=163000&end_time__lte=183000
-- within 50 miles of specified location on either monday or saturday
http://localhost:8888/meetingfinder/get_meetings/?lat=39.0839&long=-77.1531&distance_miles=50&day_of_week_in=1&day_of_week_in=6
-- the 0th to the 2nd meeting of all meetings, sorted by description; total returned - 3 meetings, notice the n+1 passed to the limit.
http://localhost:8888/meetingfinder/get_meetings/?offset=0&limit=3&order_by=description
-- the 3rd meeting out of the list of all meetings sorted by start_time
http://localhost:8888/meetingfinder/get_meetings/?offset=2&limit=3&order_by=start_time
-- get meetings that have either type with id = 1 or with id = 2 
http://localhost/meetingfinder/get_meetings/?lat=39.1207&long=-84.4516&type_ids=1&type_ids=2


The returned json object looks like this:
{
    "meta": {"total_count": 3,
             "current_count": 1}, 
    "objects": [{"internal_type": "submitted", "description": "Awesome meeting", "start_time": "07:45:00", "long": 39.05995, "day_of_week": 7, "end_time": "08:45:00", "address": "1850 Rockville pike, Rockville, MD, 20852", "lat": -77.121946, "name": "Rockville meeting"}]
}
total_count is the number of meetings you would have gotten back if didn't pass in a limit and an offset
current_count is the number of meetings currently in the "objects" list(with limit and offset applied if you passed them in) 

Find meeting by db id:
http://localhost:8888/meetingfinder/get_meeting_by_id?meeting_id=1
returns same json as above with a single meeting in the "objects"


Find meetings similar to the one you submit via post
Rules - no authentication needed.
Post the full meeting object.
Currently similarity is determined via day_of_week, start_time and end_time +/- 10 minutes, and distance within 0.1 miles
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "1850 Rockville pike, Rockville, MD, 20852", "day_of_week": 3, "description": "Awesome meeting", "end_time": "18:45:00", "lat": -77.121946, "long": 39.059950, "name": "Rockville meeting", "start_time": "17:45:00"}' http://mcasg.org/meetingfinder/find_similar


Post a meeting_not_there object:
curl -v -X POST -d 'meeting_id=1&unique_phone_id=TROLLOLOLOLOLO&note=meeting_sucks' --user admin:1chpok1 http://localhost:8888/meetingfinder/post_meeting_not_there
also works without a user and password
curl -v -X POST -d 'meeting_id=1&unique_phone_id=TROLLOLOLOLOLO' http://localhost:8888/meetingfinder/post_meeting_not_there
** NEW **
You can optionally add notes to meeting_not_there:
curl -v -X POST -d 'meeting_id=1&unique_phone_id=TROLLOLOLOLOLO&note=NO_MEETING_WAS_HELD' http://localhost:8888/meetingfinder/post_meeting_not_there

*** Meeting types ***
Associate types with a meeting using their respective ids:
curl -H "Content-Type: application/json" -X POST -d '{"meeting_id": 197, "type_ids": [2, 3]}' 'http://127.0.0.1/meetingfinder/save_types_for_meeting/'
Note - Any types previously associated with the meeting will be purged before new types are associated



VM u/p:
root/reverse

Django admin u/p:
admin/1chpok1

Virtualbox Log location - /var/www/aabuddy/logs/aabuddy.log


** CentOS commands:
list users:
cat /etc/passwd | cut -d ":" -f1
