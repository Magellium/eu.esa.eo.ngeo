# !/bin/bash

# if DM_WEBAPP_URL is set override the default web application URL from the built-in properties (defaults to empty)
DM_WEBAPP_URL=${DM_WEBAPP_URL:-}

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

export DM_HOME=$DIR

# if an argument is provided, assume that a file with comamnds to be executed is being provided 
if [ -z "$1" ]
  then
    export CMD_FILE=
  else
    export CMD_FILE="--cmdfile $1"
fi

$DM_HOME/jre/jre1.7.0_21/bin/java -DDM_HOME=$DM_HOME -DDM_WEBAPP_URL=$DM_WEBAPP_URL -jar "$DM_HOME/bin/download-manager-command-line.jar" $CMD_FILE
