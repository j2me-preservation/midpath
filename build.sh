
# Adjust these two variables to your environment
JAVAC_CMD=ecj
GNU_CLASSPATH_PATH=/usr/share/classpath/glibj.zip

#==========================================
# You should not change anything below
#==========================================

DIST_HOME=`pwd`
mkdir -p $DIST_HOME/dist

## Build CLDC1.1 ##
# Patch cldc1.1 base classes
cd $DIST_HOME/external/cldc1.1
cp -rf cacao-cldc-patch/ResourceInputStream.java src/com/sun/cldc/io/
# Build cldc1.1 base classes
cd $DIST_HOME/external/cldc1.1/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1"
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes
# Build CLDC extra classes for MIDP2
cd $DIST_HOME/src/cldc-glue
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -source 1.3 -target 1.1"
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes
# Make a jar 
cd $DIST_HOME/external/cldc1.1/classes
jar cvf cldc1.1.jar *
mv cldc1.1.jar $DIST_HOME/dist

CLDC_PATH=$DIST_HOME/dist/cldc1.1.jar

# Build SDLJava for CLDC
cd $DIST_HOME/external/sdljava-cldc
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH -source 1.3 -target 1.1" JAR_FILE="sdljava-cldc.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/sdljava-cldc/sdljava-cldc.jar $DIST_HOME/dist
  
# Build SDLJava for J2SE
cd $DIST_HOME/external/sdljava/java
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $GNU_CLASSPATH_PATH"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $GNU_CLASSPATH_PATH" JAR_FILE="sdljava-j2se.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/sdljava/java/sdljava-j2se.jar $DIST_HOME/dist

# Build Escher X11 library
cd $DIST_HOME/external/escher-cldc/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="escher-x11-cldc.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/escher-cldc/core/escher-x11-cldc.jar $DIST_HOME/dist

# Build MP3 library
cd $DIST_HOME/external/jlayerme-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="jlayerme-cldc.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/jlayerme-cldc/src/jlayerme-cldc.jar $DIST_HOME/dist

# Build OGG library
cd $DIST_HOME/external/jorbis-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="jorbis-cldc.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/jorbis-cldc/src/jorbis-cldc.jar $DIST_HOME/dist

# Build Bluetooth library
cd $DIST_HOME/external/javabluetooth/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $GNU_CLASSPATH_PATH:$CLDC_PATH:$DIST_HOME/lib/RXTXcomm.jar -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $GNU_CLASSPATH_PATH:$CLDC_PATH:$DIST_HOME/lib/RXTXcomm.jar -source 1.3 -target 1.1" JAR_FILE="jsr82-bluetooth.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/external/javabluetooth/src/jsr82-bluetooth.jar $DIST_HOME/dist

# Build the SDLJava native part
#cd $DIST_HOME/external/sdljava/native
#make
#cp *.so $DIST_HOME/dist

# Build MIDPath
cd $DIST_HOME/src/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/jsr82-bluetooth.jar:$DIST_HOME/lib/kxml2-2.3.0.jar:$DIST_HOME/lib/swt.jar -source 1.3 -target 1.1"
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/jsr82-bluetooth.jar:$DIST_HOME/lib/kxml2-2.3.0.jar:$DIST_HOME/lib/swt.jar -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/src/core/classes
# Compile JVM.java separately as it can't be compiled against cldc.jar
ecj -bootclasspath $GNU_CLASSPATH_PATH -source 1.3 -target 1.1 -d $DIST_HOME/src/core/classes com/sun/cldchi/jvm/JVM.java
cd $DIST_HOME/src/core/classes
jar cvf $DIST_HOME/dist/midpath.jar *
#cp $DIST_HOME/src/core/midpath.jar $DIST_HOME/dist

# Add resources to the midpath.jar
#jar uvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/resources-embedded/ .

cd $DIST_HOME/tests
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -source 1.3 -target 1.1"
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="midpath-tests.jar" JAR_FLAGS="cvf"
cp $DIST_HOME/tests/midpath-tests.jar $DIST_HOME/dist

# Add other required libraries to the dist directory
cp $DIST_HOME/lib/kxml2-2.3.0.jar $DIST_HOME/dist
cp $DIST_HOME/lib/RXTXcomm.jar $DIST_HOME/dist

