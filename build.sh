#!/bin/sh
# Usage: type ./build.sh --help

# Adjust these two variables to your environment
JAVAC_CMD=ecj
GNU_CLASSPATH_PATH=/usr/share/classpath/glibj.zip

#==========================================
# You should not change anything below
#==========================================

while getopts "haesgq-:" option ; do
	if [ "$option" = "-" ] ; then
		case $OPTARG in
			help ) option=h ;;
			alsa ) option=a ;;
			esd ) option=e ;;
			gtk ) option=g ;;
			qt3 ) option=q ;;
			qt4 ) option=t ;;
			sdl ) option=s ;;
			fb ) option=f ;;
			* ) echo "Option $OPTARG unknown" ;;
		esac
	fi
	case $option in
		h ) echo "Usage : $(basename $0) [options...] [target]"
		    echo " Options :"
		    echo "  -h	--help : Show this help"
		    echo "  -a	--alsa : Compile ALSA native code"
		    echo "  -e	--esd  : Compile ESD native code"
		    echo "  -g	--gtk  : Compile GTK native code"
		    echo "  -q	--qt3  : Compile Qt3 native code"
		    echo "  -t	--qt4  : Compile Qt4 native code"
		    echo "  -s	--sdl  : Compile SDL native code"
		    echo "  -f	--fb   : Compile Linux framebuffer native code"
		    echo " Targets :"
		    echo "  generic (default)"
		    echo "  maemo (compile libmidpathgtk with hildon libraries)"
		    exit 0
		    ;;
		a ) ALSA_ENABLED=yes
			echo "ALSA enabled" ;;
		e ) ESD_ENABLED=yes
			echo "ESD enabled" ;;
		g ) GTK_ENABLED=yes
			echo "GTK enabled" ;;
		q ) QT3_ENABLED=yes
			echo "QT3 enabled" ;;
		t ) QT4_ENABLED=yes
			echo "QT4 enabled" ;;
		s ) SDL_ENABLED=yes
			echo "SDL enabled" ;;
		f ) FB_ENABLED=yes
			echo "FB enabled" ;;
		? ) echo "Unknown" ;;
	esac
done

shift $((OPTIND - 1))
while [ $# -ne 0 ] ; do
	if [ "$1" = "maemo" ]
	then
		echo "Target: MAEMO"
		MAEMO_ENABLED=yes
	fi
	shift
done


DIST_HOME=`pwd`
mkdir -p $DIST_HOME/dist

# Build CLDC1.1
# Build base classes
cd $DIST_HOME/external/cldc1.1/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1" || exit 1
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes || exit 1
# Build CLDC extra classes for MIDP2
cd $DIST_HOME/src/cldc-glue
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -sourcepath $DIST_HOME/src/cldc-glue -source 1.3 -target 1.1"
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes
# Make a jar
jar cvf  $DIST_HOME/dist/cldc1.1.jar -C $DIST_HOME/external/cldc1.1/classes .

CLDC_PATH=$DIST_HOME/dist/cldc1.1.jar

# Build SDLJava for CLDC
cd $DIST_HOME/external/sdljava-cldc/java
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -sourcepath $DIST_HOME/external/sdljava-cldc/java -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="sdljava-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/sdljava-cldc/java/sdljava-cldc.jar $DIST_HOME/dist

# Build Escher X11 library
cd $DIST_HOME/external/escher-cldc/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -sourcepath $DIST_HOME/external/escher-cldc/core -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="escher-x11-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/escher-cldc/core/escher-x11-cldc.jar $DIST_HOME/dist

# Build MP3 library
cd $DIST_HOME/external/jlayerme-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -sourcepath $DIST_HOME/external/jlayerme-cldc/src -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="jlayerme-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/jlayerme-cldc/src/jlayerme-cldc.jar $DIST_HOME/dist

# Build OGG library
cd $DIST_HOME/external/jorbis-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -sourcepath $DIST_HOME/external/jorbis-cldc/src -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="jorbis-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/jorbis-cldc/src/jorbis-cldc.jar $DIST_HOME/dist

# Build Bluetooth library
cd $DIST_HOME/external/avetanabt-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -sourcepath $DIST_HOME/external/avetanabt-cldc/src -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="avetanabt-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/avetanabt-cldc/src/avetanabt-cldc.jar $DIST_HOME/dist

# Build MicroBackend library
cd $DIST_HOME/src/microbackend
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/lib/swt.jar -sourcepath $DIST_HOME/src/microbackend -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/lib/swt.jar -source 1.3 -target 1.1" JAR_FILE="microbackend.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/src/microbackend/microbackend.jar $DIST_HOME/dist

# Build MIDPath
cd $DIST_HOME/src/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/microbackend.jar:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/avetanabt-cldc.jar:$DIST_HOME/lib/kxml2-2.3.0.jar -sourcepath $DIST_HOME/src/core -source 1.3 -target 1.1" || exit 1
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$GNU_CLASSPATH_PATH:$DIST_HOME/dist/microbackend.jar:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/avetanabt-cldc.jar:$DIST_HOME/lib/kxml2-2.3.0.jar -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/src/core/classes || exit 1
jar cvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/src/core/classes .

# Include com.sun.cldchi.jvm.JVM class (J2SE glue) in jars which could be used in a J2SE environment  
mkdir -p $DIST_HOME/src/j2se-glue/classes
cd $DIST_HOME/src/j2se-glue
ecj -bootclasspath $GNU_CLASSPATH_PATH -source 1.3 -target 1.1 -d $DIST_HOME/src/j2se-glue/classes com/sun/cldchi/jvm/JVM.java
jar uvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/src/j2se-glue/classes .
jar uvf $DIST_HOME/dist/microbackend.jar -C $DIST_HOME/src/j2se-glue/classes .

# Add resources to the midpath.jar
#(cd $DIST_HOME/resources-embedded && find | grep -v "/.svn") > resources.list
#jar uvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/resources-embedded/ @resources.list

cd $DIST_HOME/tests
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -sourcepath $DIST_HOME/tests -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="midpath-tests.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/tests/midpath-tests.jar $DIST_HOME/dist

# Add other required libraries to the dist directory
cp $DIST_HOME/lib/kxml2-2.3.0.jar $DIST_HOME/dist

# Build native code

if [ "$GTK_ENABLED" = "yes" ]; then
if [ "$MAEMO_ENABLED" = "yes" ]; then
	cd $DIST_HOME/native/microbackend/gtk
	make -f Makefile.maemo || exit 1
	cp *.so $DIST_HOME/dist
else
	cd $DIST_HOME/native//microbackend/gtk
	make -f Makefile || exit 1
	cp *.so $DIST_HOME/dist
fi
fi

if [ "$ALSA_ENABLED" = "yes" ]; then
# Build the ALSA native part
cd $DIST_HOME/native/alsa
make || exit 1
cp *.so $DIST_HOME/dist
fi

if [ "$ESD_ENABLED" = "yes" ]; then
# Build the ESounD native part
cd $DIST_HOME/native/esd
make || exit 1
cp *.so $DIST_HOME/dist
fi

if [ "$QT3_ENABLED" = "yes" ]; then
# Build the Qt native part
cd $DIST_HOME/native/microbackend/qt
make || exit 1
cp *.so $DIST_HOME/dist
fi

if [ "$QT4_ENABLED" = "yes" ]; then
# Build the Qt native part
cd $DIST_HOME/native/microbackend/qt
make QT4_BACKEND=yes || exit 1
cp *.so $DIST_HOME/dist
fi

if [ "$SDL_ENABLED" = "yes" ]; then
# Build the SDLJava native part
cd $DIST_HOME/external/sdljava-cldc/native
make || exit 1
cp *.so $DIST_HOME/dist
fi

if [ "$FB_ENABLED" = "yes" ]; then
# Build the Linux framebuffer native part
cd $DIST_HOME/native/microbackend/fb
make || exit 1
cp *.so $DIST_HOME/dist
fi
