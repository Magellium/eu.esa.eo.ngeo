@ECHO OFF
TITLE Download Manager ${project.parent.version} console

REM if DM_PORT is set use it to define the port on which the DM's web server binds to (defaults to 8082)
if not [%1]==[] set DM_PORT=%1
if [%1]==[] set DM_PORT=8082

REM Note that %DM_HOME% will have a trailing '\'
SET DM_HOME=%~dp0

REM SET DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000

REM TODO: Locate the trust store in a more suitable folder
SET SYS_PROPS=-DDM_HOME="%DM_HOME%\" -Djava.net.preferIPv4Stack=true -Djavax.net.ssl.trustStore="%DM_HOME%conf\download-manager-truststore.jks" -Dlog4j.configuration="file:\%DM_HOME%conf\log4j.xml"

REM In the following command, we put a literal '\' after  _DDM_HOME="%DM_HOME%  because %DM_HOME% has a trailing slash and java.exe uses '\' as the character to escape double quote characters and backslashes.
set MYCOMMAND="%DM_HOME%jre\jre1.7.0_21\bin\java" %SYS_PROPS% %DEBUG_OPTIONS% -jar "%DM_HOME%bin\download-manager-webapp-jetty-console.war" --headless --port %DM_PORT% --contextPath /download-manager

echo Command=%MYCOMMAND%
echo

%MYCOMMAND%