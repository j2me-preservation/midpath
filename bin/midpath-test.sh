#!/bin/sh

# Modify the next line to change the JVM
JAVA_CMD=cacao

if [ "$MIDPATH_HOME" = "" ]; then
  MIDPATH_HOME=$(pwd)/..
fi

# Set the classpath
CP=$MIDPATH_HOME/dist/midpath.jar:$MIDPATH_HOME/resources-embedded:$MIDPATH_HOME/dist/sdljava-j2se.jar:$MIDPATH_HOME/dist/escher-x11-cldc.jar:$MIDPATH_HOME/dist/kxml2-2.3.0.jar
# Add the  MIDlet jar to the classpath  (must be loaded by the main classloader)
CP=$CP:$MIDPATH_HOME/dist/midpath-tests.jar

# Path of the native libraries
JLP=$MIDPATH_HOME/dist

CLASS=org.thenesis.midpath.main.StandardMIDletLauncher
# The classname of the MIDlet
MIDLET=org.thenesis.midpath.test.HelloWorldMidlet
#MIDLET=org.thenesis.midpath.test.TextFieldTest

$JAVA_CMD -Djava.library.path=${JLP} -cp ${CP} ${CLASS} ${MIDLET}

