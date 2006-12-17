#!/bin/sh

# Modify the next line to change the JVM
JAVA_CMD=cacao

if [ "$MIDPATH_HOME" = "" ]; then
  MIDPATH_HOME=$(pwd)/..
fi

# Set the classpath
CP=$MIDPATH_HOME/dist/midpath.jar:$MIDPATH_HOME/dist/sdljava-core.jar:$MIDPATH_HOME/lib/kxml2-2.3.0.jar
# Add the  MIDlet jar to the classpath  (must be loaded by the main 
# classloader yet)
CP=$CP:$MIDPATH_HOME/dist/midpath-tests.jar

# Path of the SDLJava native libraries
JLP=$MIDPATH_HOME/dist

CLASS=org.thenesis.midpath.MIDletLauncher
# The classname of the MIDlet (we have to set it manually yet)
MIDLET=org.thenesis.midpath.test.HelloWorldMidlet

$JAVA_CMD -Djava.library.path=${JLP} -cp ${CP} ${CLASS} ${MIDLET}

