@echo off
setlocal EnableDelayedExpansion

for /F %%f in ('dir /b /on data\Addresses_json_*.txt') do (

  set FILENAME=%%f
  set SEQ=%%f
  set SEQ=!SEQ:~15,2!

  copy data\!FILENAME! Addresses_json.txt

  %JAVA_HOME%\bin\java -cp bin JSONTextView

  move Addresses_json_text_view.txt data\Addresses_json_text_view_!SEQ!.txt
)
del Addresses_json.txt
echo format_json_array DONE

  