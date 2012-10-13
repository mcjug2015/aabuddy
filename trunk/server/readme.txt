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
day_of_week goes from 0 to 6
1 = Monday
2 = Tuesday
...
6 = Saturday
7 = Sunday

internal_type valid values can be found in aabuddy/models.py/Meeting/INTERNAL_TYPE_CHOICES. They are case insensitive
when submitted to the url below, so Submitted and submitted are both valid.
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "sfdfdsfsdfsd", "day_of_week": 1, "description": "fdsfdsdfdsf", "end_time": "17:33:59", "internal_type": "Submitted", "lat": -77.4108, "long": 39.4142, "name": "Frederick meeting", "start_time": "16:30:00"}' http://localhost:8888/aabuddy/save_meeting


** grab meetings:
http://localhost:8888/aabuddy/get_meetings/
http://localhost:8888/aabuddy/get_meetings/?lat=-77.1531&long=39.0839&distance_miles=50

VM u/p:
root/reverse

** CentOS commands:
list users:
cat /etc/passwd | cut -d ":" -f1
