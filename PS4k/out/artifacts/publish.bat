del "%CD%\webdata\PS4k.jar"
copy "%CD%\PS4k_jar\PS4k.jar" "%CD%\webdata\PS4k.jar"
rmdir /s /q "%CD%\webdata\lib" 
xcopy /i "%CD%\PS4k_jar\lib" "%CD%\webdata\lib"
java -classpath "%CD%\webdata\getdown-1.2.jar" com.threerings.getdown.tools.Digester "%CD%\webdata\
"C:\Users\VM Win 7\Dropbox\Tools\WinSCP\WinSCP.exe" /console /script=upload-script.txt