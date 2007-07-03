#!/bin/sh

# Modify the next line to change the JVM
JAVA_CMD=cacao

if [ "$MIDPATH_HOME" = "" ]; then
  MIDPATH_HOME=$(pwd)/..
fi

# Set the classpath
CP=$MIDPATH_HOME/dist/midpath.jar:$MIDPATH_HOME/resources-embedded:$MIDPATH_HOME/dist/sdljava-j2se.jar:$MIDPATH_HOME/dist/escher-x11-cldc.jar:$MIDPATH_HOME/dist/kxml2-2.3.0.jar

# Path of the native libraries
JLP=$MIDPATH_HOME/dist

# The MIDlet launcher for J2SE
CLASS=org.thenesis.midpath.main.J2SEMIDletLauncher

$JAVA_CMD -Djava.library.path=${JLP} -cp ${CP} ${CLASS}

