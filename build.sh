
# Adjust these two variables to your environment
JAVAC_CMD=jikes
BOOTCLASSPATH=/usr/share/classpath/glibj.zip

DIST_HOME=`pwd`

# Build SDLJava
cd $DIST_HOME/external/sdljava/java
make jar JAVAC=$JAVAC_CMD JAR_FILE="sdljava-core.jar" JAVAC_FLAGS="-bootclasspath $BOOTCLASSPATH"
mkdir -p $DIST_HOME/dist
cp $DIST_HOME/external/sdljava/java/sdljava-core.jar $DIST_HOME/dist

# Build the SDLJava native part
cd $DIST_HOME/external/sdljava/native
make
cp *.so $DIST_HOME/dist

# Build MIDPath
cd $DIST_HOME/src
make jar JAVAC=$JAVAC_CMD JAR_FILE="midpath.jar" JAVAC_FLAGS="-bootclasspath $BOOTCLASSPATH -classpath $DIST_HOME/dist/sdljava-core.jar:$DIST_HOME/lib/kxml2-2.3.0.jar"
cp $DIST_HOME/src/midpath.jar $DIST_HOME/dist

# Add resources to the midpath.jar
jar uvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/resources-embedded/ .

cd $DIST_HOME/tests
make jar JAVAC=$JAVAC_CMD JAR_FILE="midpath-tests.jar" JAVAC_FLAGS="-bootclasspath $BOOTCLASSPATH -classpath $DIST_HOME/dist/midpath.jar"
cp $DIST_HOME/tests/midpath-tests.jar $DIST_HOME/dist

