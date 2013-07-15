# !/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

export DM_HOME=$DIR

# FIXME: MacOS JRE has "Contents\Home" as parents of the JRE bin directory; that will break the following line:
$DM_HOME/jre/jre1.7.0_21/bin/java -DDM_HOME=$DM_HOME -jar "$DM_HOME/bin/download-manager-command-line.jar"
