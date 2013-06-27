REM You may need to run this as Administrator
REM Run this batch file only from the directory that contains it. (This is because relative paths are used.)

@echo ON

set OPENSSL_BIN_DIR=D:\Programs\OpenSSL-Win64\bin
set OPENSSL_EXE=%OPENSSL_BIN_DIR%\openssl.exe
set OPENSSL_CONF=%OPENSSL_BIN_DIR%\openssl.cfg

set JAVA_HOME=D:\Programs\Java\jdk1.7.0_03
set JAVA_BIN=%JAVA_HOME%\bin
set KEYTOOL_EXE=%JAVA_BIN%\keytool.exe

set APP_NAME=DMTU
set KEYSTORE_FILENAME_P12=%APP_NAME%.p12
set KEYSTORE_FILENAME_JKS=%APP_NAME%.jks
set KEYSTORE_PASSWORD=whatever
set GMV_TEST_IDP_CERTIFICATE_FILEPATH=..\resources\certs\umsso.pem


REM NB: The following is probably partly mis-named; although Ian obtained it using Firefox, it's surely a reference to the *SP's* certificate. 
set GMV_TEST_IDP_CERTIFICATE_FILEPATH_FROM_BROWSER=..\resources\certs\webs.novalocal.crt


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

set CLIENT_TRUSTSTORE_FILE_PATHNAME=..\..\..\target\temp\client-truststore.jks
set MERGED=merged_client-truststore.jks

REM The StackOverflow article used "keytool -genkey" which, in Java 6, was replaced by Sun/Oracle with "keytool -genkeypair".
REM @echo ON
%KEYTOOL_EXE% -genkeypair -dname "cn=CLIENT" -alias truststorekey -keyalg RSA -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -keypass whatever -storepass %KEYSTORE_PASSWORD% || goto :error
REM @echo OFF

echo You are about to be prompted for the keystore password twice; type '%KEYSTORE_PASSWORD%' each time.
REM The following keytool command rejects non-X509 certificates
REM @echo ON
%KEYTOOL_EXE% -import -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -file %GMV_TEST_IDP_CERTIFICATE_FILEPATH%               -alias IDP  || goto :error
%KEYTOOL_EXE% -import -keystore %CLIENT_TRUSTSTORE_FILE_PATHNAME% -file %GMV_TEST_IDP_CERTIFICATE_FILEPATH_FROM_BROWSER%  -alias IDP2 || goto :error
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
