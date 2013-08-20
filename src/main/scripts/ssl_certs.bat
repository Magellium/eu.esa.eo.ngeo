@echo off

if [%1]==[] goto usage
if [%2]==[] goto usage

echo You may need to run this as Administrator
echo Run this batch file only from the directory that contains it. (This is because relative paths are used.)

set OPENSSL_BIN_DIR=D:\Programs\OpenSSL-Win64\bin
set OPENSSL_EXE=%OPENSSL_BIN_DIR%\openssl.exe
set OPENSSL_CONF=%OPENSSL_BIN_DIR%\openssl.cfg

set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_21"
set JAVA_BIN=%JAVA_HOME%\bin
set KEYTOOL_EXE=%JAVA_BIN%\keytool.exe

set APP_NAME=DMTU
set KEYSTORE_FILENAME_P12=%APP_NAME%.p12
set KEYSTORE_FILENAME_JKS=%APP_NAME%.jks
set KEYSTORE_PASSWORD=whatever

set IDP_CERTIFICATE_FILEPATH=%1
set SP_CERTIFICATE_FILEPATH=%2

echo ---------------------------------------------------------------------------------------------------------------------------------------------------
echo Generate the client truststore file
echo Note: These commands were probably inspired by http://stackoverflow.com/questions/1666052/java-https-client-certificate-authentication
echo ---------------------------------------------------------------------------------------------------------------------------------------------------

set TEMP_DIR=..\..\..\target\temp
IF NOT exist %TEMP_DIR% (
	mkdir %TEMP_DIR%
) ELSE (
	del /Q %TEMP_DIR%\*.*
)

set CLIENT_TRUSTSTORE_FILE_PATHNAME=..\..\..\target\temp\dmtu-truststore.jks
set MERGED=merged_client-truststore.jks

REM The StackOverflow article used "keytool -genkey" which, in Java 6, was replaced by Sun/Oracle with "keytool -genkeypair".
REM @echo ON
%KEYTOOL_EXE% -genkeypair -dname "cn=CLIENT" -alias truststorekey -keyalg RSA -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -keypass whatever -storepass %KEYSTORE_PASSWORD% || goto :error
REM @echo OFF

echo You are about to be prompted for the keystore password twice; type '%KEYSTORE_PASSWORD%' each time.
REM The following keytool command rejects non-X509 certificates
REM @echo ON
%KEYTOOL_EXE% -import -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -file %IDP_CERTIFICATE_FILEPATH% -alias IDP  || goto :error
%KEYTOOL_EXE% -import -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -file %SP_CERTIFICATE_FILEPATH%  -alias SP || goto :error
REM @echo OFF

echo ---------------------------------------------------------------------------------------------------------------------------------------------------
echo Now merge our b2bserver-specific trusted certificates with the standard trusted certificates of the out-of-the-box JRE
echo ---------------------------------------------------------------------------------------------------------------------------------------------------
@echo ON
REM copy %JAVA_HOME%\jre\lib\security\cacerts %MERGED% || goto :error
@echo OFF

echo You are about to be prompted for the destination and source password; type 'changeit' and '%KEYSTORE_PASSWORD%' respectively.
@echo ON
REM %KEYTOOL_EXE% -importkeystore -srckeystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -destkeystore %MERGED% || goto :error
@echo OFF

echo Please now point the DMTU webapp to the client truststore (%CLIENT_TRUSTSTORE_FILE_PATHNAME%).

exit /B

:error
echo Failed with error code %errorlevel%.
exit /B %errorlevel%

:usage
echo Usage: %0 ^<IDP_FILEPATH^> ^<SP_FILEPATH^>
exit /B 1
