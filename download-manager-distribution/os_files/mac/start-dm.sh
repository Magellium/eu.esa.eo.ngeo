# !/bin/bash

# if DM_PORT is set use it to define the port on which the DM's web server binds to (defaults to 8082)
DM_PORT=${DM_PORT:-8082}

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

export DM_HOME=$DIR

SYS_PROPS="-DDM_HOME=$DM_HOME -Djava.net.preferIPv4Stack=true -Djavax.net.ssl.trustStore=$DM_HOME/conf/download-manager-truststore.jks -Dlog4j.configuration=file:$DM_HOME/conf/log4j.xml"

$DM_HOME/jre/jre1.7.0_21/bin/java $SYS_PROPS -jar "$DM_HOME/bin/download-manager-webapp.war" --headless --port $DM_PORT --contextPath /download-manager