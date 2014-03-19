@ECHO OFF
REM Note that %DM_HOME% will have a trailing '\'
SET DM_HOME=%~dp0

REM SET DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000

REM if DM_WEBAPP_URL is set override the default web application URL from the built-in properties (defaults to empty)
if not [%1]==[] set DM_WEBAPP_URL=%1
if [%1]==[] set DM_WEBAPP_URL=

REM if an argument is provided, assume that a file with commands to be executed is being provided 
if not [%2]==[] set CMD_FILE=--cmdfile %2
if [%2]==[] set CMD_FILE=

REM In the following command, we put a literal '\' after  _DDM_HOME="%DM_HOME%  because %DM_HOME% has a trailing slash and java.exe uses '\' as the character to escape double quote characters and backslashes.
"%DM_HOME%jre\jre1.7.0_21\bin\java" -DDM_HOME="%DM_HOME%\" -DDM_WEBAPP_URL="%DM_WEBAPP_URL%" %DEBUG_OPTIONS% -jar "%DM_HOME%bin\download-manager-command-line.jar" %CMD_FILE%