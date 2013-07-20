Prepare:
- install the chrome browser
- make sure the environment variable of JAVA_HOME being set to a JDK directory
- copy and paste the directory "StateName" (under GlobalMeetingScraper\geocode)
- make sure to delete directory ".svn" if the directory is included
- replace "StateName" by an abbreviated state name
- copy Meetings_*.psv from SVN (in GlobalMeetingScraper\outputdata) to subdirectory "data"
  the file name MUST be Meetings_*.psv

IMPORTANT: Configure POWER settings on your laptop to prevent Windows from the hibernation mode



Step 1: Compile java files
- work on the directory with a state name. That is the directory consisting of those batch files.
- run 1_compile.bat
- check five new class files are created in directory "bin"


Step 2: Create JSON files for geocoding using json_geo.html
-run 2_split.bat
-check new Addresses_json_nn.txt in directory "data"

Optional (to view JSON arrary in text format)
-run optional_after_2_formatjsonarrary.bat to create Addresses_json_text_view_*.txt
-another option: use JSON online validator to view JSON array, e.g., http://jsonlint.com/

NOTE: I use bin\json-simple-1.1.1.jar to create JSON array.
The library will convert non-displayable charachers to a format, e.g., \t200.


Step 3: Geocode from JSON files
-close the Chrome browser if it is opened
-run 3_geocode.bat
-check Addresses_output_*.txt, Addresses_fail_*.txt, Addresses_debug_*.txt
 in directory "data"
 Addresses_output_*.txt - use them to populate lat and lon to Meetings_*.psv
 Addresses_fail_*.txt - those addresses whose lat and lon are not found
   NOTE: we need to run geocode.bat again after manually correcting addresses
 Addresses_debug_*.txt - for adjusting performance and debuging

NOTE 1: How to solve issues using the batch file, geocode.bat, when bad data happen 
-rename directory "data" to another name, e.g., "data_kept"
-create directory "data"
-copy those Addresses_json_nn.txt which were not geocoded to directory "data"
-run geocode.bat
-copy Addresses_output_*.txt, Addresses_fail_*.txt, Addresses_debug_*.txt from "data" to "data_kept"

My Case for NOTE 1:
-I noticed that json_geo.html is stopped when Addresses_json_06.txt was processing from the DOS window
-I found "Progress: Process 47 of 275" in the Chrome browser
-I looked up Addresses_json_text_view_06.txt and found the following line
   500 N. \"F\" Street, Fort Smith, AL 72901
 The Address in Meetings_AL.psv is the following
   500 N. "F" Street, Fort Smith, AL 72901
 In a JSON array, " (comman) is converted to \" (back slash and comma)
 But it caused stopping when running json_geo.html
-I changed the above address to the address below in Meetings_AL.psv and Addresses_json_06.txt
   500 N. F Street, Fort Smith, AL 72901
-rename directory "data" to "data_kept"
-create directory "data"
-copy Addresses_json_06.txt to Addresses_json_09.txt from directory "data_kept" to "data"
-run geocode.bat
-copy Addresses_output_*.txt, Addresses_fail_*.txt, Addresses_debug_*.txt from "data" to "data_kept"

NOTE 2: How to run json_geo.html manually
-Create one JSON array file in any option and place the file on the home directory.
 The file name MUST be named Addresses_json.txt
 1. copy from Addresses_json_nn.txt and rename it.
 2. copy from Addresses_json_text_view_nn.txt and rename it.
 3. manually edit a JSON arrary file and validate on a JSON online validator
 4. manually create a JSON array file from Addresses_fail_nn.txt
 5. understand split.bat and use it 
-create a Chrome shortcut on desktop
-right-click the icon for the Chrome shortcut > Properties
-add " --allow-file-access-from-files" at the end of chrome.exe
 It will look like
 C:\Users\username\AppData\Local\Google\Chrome\Application\chrome.exe --allow-file-access-from-files
-click the Chrome shortcut on the Desktop (Do not launch Chrome in another way)
-drag and drop the JSON file to the Chrome browser
-look up output file Addresses_output.txt in directory %USERPROFILE%\Downloads


Step 4: Populate from Addresses_output_nn.txt to Meetings_*.psv
-run 4_appendgeo.bat
-check SS_GeoMeetings.psv, SS_GeoMeetingsFail.psv, and SS_Summary.txt in directory "geooutput".
