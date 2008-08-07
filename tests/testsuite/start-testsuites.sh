#!/bin/sh

TESTSUITE_ROOT=$(pwd)
JAVA_CMD=cacao
CLDC_JAR_PATH=${TESTSUITE_ROOT}/dist/cldc1.1.jar

# You should not change anything below
CLDC_TESTSUITE_JAR_PATH=${TESTSUITE_ROOT}/dist/testsuite-cldc.jar
JNI_TESTSUITE_JAR_PATH=${TESTSUITE_ROOT}/dist/testsuite-jni.jar
export LD_LIBRARY_PATH=${TESTSUITE_ROOT}/dist

echo "Starting CLDC test suite..."
${JAVA_CMD} -Xbootclasspath/c:$CLDC_JAR_PATH:$CLDC_TESTSUITE_JAR_PATH org.thenesis.midpath.test.suite.cldc.CLDCTestSuite || exit 1
# Uncomment the next line to test against a Java SE VM
#${JAVA_CMD} -cp $CLDC_JAR_PATH:$CLDC_TESTSUITE_JAR_PATH org.thenesis.midpath.test.suite.cldc.CLDCTestSuite || exit 1

echo "Starting JNI test suite...."
${JAVA_CMD} -Xbootclasspath/c:$CLDC_JAR_PATH:$CLDC_TESTSUITE_JAR_PATH:$JNI_TESTSUITE_JAR_PATH org.thenesis.midpath.test.suite.jni.JNITestSuite

