@echo off
setlocal EnableDelayedExpansion

for /F %%f in ('dir /b /on data\Addresses_json_??.txt') do (

  if EXIST "%USERPROFILE%\Downloads\Addresses_output*.txt" (
    del "%USERPROFILE%\Downloads\Addresses_output*.txt"
  )

rem if Chrome is allowed to download multiple file in a page
rem    without manully clicking the Allow button
rem it is better to download three files.
rem A workaround right now is to combine three files to one file
rem then class GeocodeResult will split the file to three files.
rem  if EXIST "%USERPROFILE%\Downloads\Addresses_fail*.txt" (
rem    del "%USERPROFILE%\Downloads\Addresses_fail*.txt"
rem  )
rem  if EXIST "%USERPROFILE%\Downloads\Addresses_debug*.txt" (
rem    del "%USERPROFILE%\Downloads\Addresses_debug*.txt"
rem  )

  set FILENAME=%%f
  set SEQ=%%f
  set SEQ=!SEQ:~15,2!

  rem file Addresses_json.txt is the input of json_geo.html
  rem so copy Addresses_json_nn.txt to Addresses_json.txt
  rem file Addresses_output.txt is the output of json_geo.html
  rem however, the output file is located in %USERPROFILE%\Downloads
  copy data\!FILENAME! Addresses_json.txt
  start chrome.exe --allow-file-access-from-files "%CD%\json_geo.html"
  
  call :wait_until_geocoding_complete

  rem file Addresses_output.txt is the input of class GeocodeResult
  rem class GeocodeResult must has output file Addresses_out.txt
  rem probably not have output Addresses_fail.txt or Addresses_debug.txt
  if EXIST Addresses_output.txt (
    del Addresses_output.txt
  )
  if EXIST Addresses_out.txt (
    del Addresses_out.txt
  )
  if EXIST Addresses_fail.txt (
    del Addresses_fail.txt
  )
  if EXIST Addresses_debug.txt (
    del Addresses_debug.txt
  )
  move "%USERPROFILE%\Downloads\Addresses_output.txt" Addresses_output.txt
  %JAVA_HOME%\bin\java -cp bin GeocodeResult

  move Addresses_out.txt data\Addresses_output_!SEQ!.txt
  if EXIST Addresses_fail.txt (
    move Addresses_fail.txt data\Addresses_fail_!SEQ!.txt
  )
  if EXIST Addresses_debug.txt (
    move Addresses_debug.txt data\Addresses_debug_!SEQ!.txt
  )

echo GEOCODING !FILENAME! complete
)
del Addresses_json.txt
del Addresses_output.txt
echo DONE
exit /b



:wait_until_geocoding_complete
  :loop
  if NOT EXIST "%USERPROFILE%\Downloads\Addresses_output.txt" (
    ping 1.1.1.1 -n 1 -w 5000 > nul
    goto loop
  )
goto :eof

