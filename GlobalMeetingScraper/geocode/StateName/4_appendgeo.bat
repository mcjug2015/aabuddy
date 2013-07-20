@echo off
if NOT EXIST data\Addresses_output_*.txt (
  echo ---NO file Addresses_output_*.txt in directory data---
  exit /b
)

@echo on
for /F %%f in ('dir /b /on data\Meetings_??.psv') do (
  set FILENAME=%%f
  set STATENAME=%%f
)

set STATENAME=%STATENAME:~9,2%

copy data\%FILENAME% Meetings.psv

if EXIST Addresses_output.txt (
  del Addresses_output.txt
) 
for /F %%f in ('dir /b /on data\Addresses_output_*.txt') do (
  type data\%%f >> Addresses_output.txt
)

%JAVA_HOME%\bin\java -cp bin GeoMeetings

move GeoMeetings.psv geooutput\%STATENAME%_GeoMeetings.psv
move Summary.txt geooutput\%STATENAME%_Summary.txt
move GeoMeetingsFail.psv geooutput\%STATENAME%_GeoMeetingsFail.psv

del Meetings.psv
del Addresses_output.txt
