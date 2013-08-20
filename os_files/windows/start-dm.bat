@ECHO OFF
TITLE Download Manager 0.4.1 console

REM Note that %DM_HOME% will have a trailing '\'
SET DM_HOME=%~dp0

REM SET DEBUG_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000

REM TODO: Locate the trust store in a more suitable folder
SET SYS_PROPS=-DDM_HOME="%DM_HOME%\" -Djava.net.preferIPv4Stack=true -Djavax.net.ssl.trustStore="%DM_HOME%conf\dmtu-truststore.jks"

REM In the following command, we put a literal '\' after  _DDM_HOME="%DM_HOME%  because %DM_HOME% has a trailing slash and java.exe uses '\' as the character to escape double quote characters and backslashes.
set MYCOMMAND="%DM_HOME%jre\jre1.7.0_21\bin\java" %SYS_PROPS% %DEBUG_OPTIONS% -jar "%DM_HOME%bin\download-manager-webapp-jetty-console.war" --headless --port 8082 --contextPath /download-manager

echo Command=%MYCOMMAND%
echo

%MYCOMMAND%