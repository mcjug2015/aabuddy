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
curl -v -H "Content-Type: application/json" -X POST -d '{"address": "sfdfdsfsdfsd", "day_of_week": "Monday", "description": "fdsfdsdfdsf", "end_time": "14:33:59", "internal_type": "Submitted", "latitude": 12321.1, "longitude": 2131.3000000000002, "name": "sdgsdfsd", "start_time": "11:30:00"}' http://localhost:8000/aabuddy/api/v1/meeting/

** grab meetings:
http://localhost:8888/aabuddy/api/v1/meeting/?format=json

VM u/p:
root/reverse

** CentOS commands:
list users:
cat /etc/passwd | cut -d ":" -f1
