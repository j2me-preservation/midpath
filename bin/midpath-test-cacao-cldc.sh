#!/bin/sh

# Adapt the next line to your environment
JAVA_CMD=cacao

if [ "$MIDPATH_HOME" = "" ]; then
  MIDPATH_HOME=$(pwd)/..
fi

# Set the classpath
BCP=$MIDPATH_HOME/dist/cldc1.1.jar:$MIDPATH_HOME/dist/midpath.jar:$MIDPATH_HOME/resources-embedded:$MIDPATH_HOME/dist/sdljava-cldc.jar:$MIDPATH_HOME/dist/escher-x11-cldc.jar:$MIDPATH_HOME/dist/kxml2-2.3.0.jar
# Add the  MIDlet jar to the classpath  (must be loaded by the main 
# classloader yet)
BCP=$BCP:$MIDPATH_HOME/dist/midpath-tests.jar

# Path of the native libraries
JLP=$MIDPATH_HOME/dist
export LD_LIBRARY_PATH=$JLP 

CLASS=org.thenesis.midpath.main.StandardMIDletLauncher
# The classname of the MIDlet (we have to set it manually yet)
MIDLET=org.thenesis.midpath.test.HelloWorldMidlet
#MIDLET=org.thenesis.midpath.test.TextFieldTest

$JAVA_CMD -Xbootclasspath:${BCP} ${CLASS} ${MIDLET}

