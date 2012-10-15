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

** test uploading a meeting with curl:
day_of_week goes from 1 to 7
1 = Monday
2 = Tuesday
...
6 = Saturday
7 = Sunday

internal_type valid values can be found in aabuddy/models.py/Meeting/INTERNAL_TYPE_CHOICES. They are case insensitive
when submitted to the url below, so Submitted and submitted are both valid.
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "sfdfdsfsdfsd", "day_of_week": 1, "description": "fdsfdsdfdsf", "end_time": "17:33:59", "internal_type": "Submitted", "lat": -77.4108, "long": 39.4142, "name": "Frederick meeting", "start_time": "16:30:00"}' http://localhost:8888/aabuddy/save_meeting
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "1850 Rockville pike, Rockville, MD, 20852", "day_of_week": 7, "description": "Awesome meeting", "end_time": "6:45:00", "internal_type": "Submitted", "lat": -77.121946, "long": 39.059950, "name": "Rockville meeting", "start_time": "7:45:00"}' http://localhost:8888/aabuddy/save_meeting


For querying the day_of_week can be passed in with the following modifiers:
__eq - day of week equals to
__gt - greater than
__gte - greater than or equal to
__lt - lest than
__lte - less then or equal to

start_time and end_time can be queried just like day_of_week, but do not support the eq operator

** grab meetings:
-- all:
http://localhost:8888/aabuddy/get_meetings/
-- within 50 miles of specified location
http://localhost:8888/aabuddy/get_meetings/?lat=-77.1531&long=39.0839&distance_miles=50
-- within 50 miles of specified location between monday and wednesday inclusive:
http://localhost:8888/aabuddy/get_meetings/?lat=-77.1531&long=39.0839&distance_miles=50&day_of_week__gte=1&day_of_week__lte=3
-- within 50 miles of specified location between monday and wednesday inclusive, starting at or after 4:30pm and ending before 6:30pm:
http://localhost:8888/aabuddy/get_meetings/?lat=-77.1531&long=39.0839&distance_miles=50&day_of_week__gte=1&day_of_week__lte=3&start_time__gte=163000&end_time__lte=183000


VM u/p:
root/reverse

Django admin u/p:
admin/1chpok1

Virtualbox Log location - /var/www/aabuddy/logs/aabuddy.log


** CentOS commands:
list users:
cat /etc/passwd | cut -d ":" -f1
