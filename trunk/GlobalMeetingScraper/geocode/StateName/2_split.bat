@echo off
if NOT EXIST data\Meetings_*.psv (
  echo ---NO Meetings file in directory data---
  exit /b
)

@echo on
copy data\Meetings_*.psv Meetings.txt
%JAVA_HOME%\bin\java -cp bin MeetingAddress

call sort MeetingAddresses.txt /o SortedAddresses.txt

%JAVA_HOME%\bin\java -cp bin;bin\json-simple-1.1.1.jar  DistinctAddress
move MeetingAddresses.txt data\MeetingAddresses.txt
copy SortedAddresses.txt data\SortedAddresses.txt

del Meetings.txt SortedAddresses.txt
move Addresses_json_*.txt data\