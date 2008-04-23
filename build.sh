#!/bin/sh
# Usage: type ./build.sh --help

# Adjust these two variables to your environment
JAVAC_CMD=javac
JAVA_SE_LIBRARY_PATH=/usr/share/classpath/glibj.zip

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


#---------------------
# Build CLDC1.1
#---------------------

# Build base classes
cd $DIST_HOME/external/cldc1.1/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1" || exit 1
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath . -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes || exit 1
# Build CLDC extra classes for MIDP2
cd $DIST_HOME/components/cldc-glue
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -sourcepath $DIST_HOME/components/cldc-glue -source 1.3 -target 1.1"
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/external/cldc1.1/classes -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/external/cldc1.1/classes
# Make a jar
jar cvf  $DIST_HOME/dist/cldc1.1.jar -C $DIST_HOME/external/cldc1.1/classes .

CLDC_PATH=$DIST_HOME/dist/cldc1.1.jar

#--------------------------
# Build external libraries
#--------------------------

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

# Build jGL library
cd $DIST_HOME/external/jgl-cldc/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="jgl-cldc.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/external/jgl-cldc/src/jgl-cldc.jar $DIST_HOME/dist

# Build MicroBackend library
cd $DIST_HOME/components/microbackend
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/lib/swt.jar -sourcepath $DIST_HOME/components/microbackend -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/escher-x11-cldc.jar:$DIST_HOME/lib/swt.jar -source 1.3 -target 1.1" JAR_FILE="microbackend.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/microbackend/microbackend.jar $DIST_HOME/dist

#--------------------
# Build MIDPath core
#--------------------

# Build core
cd $DIST_HOME/components/core/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/microbackend.jar:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/avetanabt-cldc.jar:$DIST_HOME/lib/kxml2-2.3.0.jar -sourcepath $DIST_HOME/components/core/src -source 1.3 -target 1.1" || exit 1
make install JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/microbackend.jar:$DIST_HOME/dist/sdljava-cldc.jar:$DIST_HOME/dist/jlayerme-cldc.jar:$DIST_HOME/dist/jorbis-cldc.jar:$DIST_HOME/dist/avetanabt-cldc.jar:$DIST_HOME/lib/kxml2-2.3.0.jar -source 1.3 -target 1.1" CLASS_DIR=$DIST_HOME/components/core/src/classes || exit 1
jar cvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/components/core/src/classes .

# Add resources to the midpath.jar
(cd $DIST_HOME/components/core/resources && find -type f | grep -v "/.svn") > $DIST_HOME/components/core/resources.list
cd $DIST_HOME/components/core/resources
jar uvf $DIST_HOME/dist/midpath.jar @$DIST_HOME/components/core/resources.list

# Include com.sun.cldchi.jvm.JVM class (J2SE glue) in jars which could be used in a J2SE environment  
mkdir -p $DIST_HOME/components/j2se-glue/classes
cd $DIST_HOME/components/j2se-glue
ecj -bootclasspath $JAVA_SE_LIBRARY_PATH -source 1.3 -target 1.1 -d $DIST_HOME/components/j2se-glue/classes com/sun/cldchi/jvm/JVM.java
jar uvf $DIST_HOME/dist/midpath.jar -C $DIST_HOME/components/j2se-glue/classes .
jar uvf $DIST_HOME/dist/microbackend.jar -C $DIST_HOME/components/j2se-glue/classes .

cd $DIST_HOME/tests
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -sourcepath $DIST_HOME/tests -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $DIST_HOME/dist/midpath.jar:$CLDC_PATH -source 1.3 -target 1.1" JAR_FILE="midpath-tests.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/tests/midpath-tests.jar $DIST_HOME/dist

# Add other required libraries to the dist directory
cp $DIST_HOME/lib/kxml2-2.3.0.jar $DIST_HOME/dist

MIDPATH_CORE_PATH=$DIST_HOME/dist/midpath.jar

#---------------------
# Build optional JSRs
#---------------------

# Build Location API (JSR179)
cd $DIST_HOME/components/jsr179-location/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/lib/kxml2-2.3.0.jar:$DIST_HOME/dist/avetanabt-cldc.jar -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/lib/kxml2-2.3.0.jar:$DIST_HOME/dist/avetanabt-cldc.jar -source 1.3 -target 1.1" JAR_FILE="jsr179-location.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr179-location/core/jsr179-location.jar $DIST_HOME/dist
# Add resources to jsr179-location.jar
(cd $DIST_HOME/components/jsr179-location/resources && find -type f | grep -v "/.svn") > $DIST_HOME/components/jsr179-location/resources.list
cd $DIST_HOME/components/jsr179-location/resources
jar uvf $DIST_HOME/dist/jsr179-location.jar @$DIST_HOME/components/jsr179-location/resources.list

# Build Wireless Messaging API (JSR205)
cd $DIST_HOME/components/jsr205-messaging/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH -source 1.3 -target 1.1" JAR_FILE="jsr205-messaging.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr205-messaging/core/jsr205-messaging.jar $DIST_HOME/dist

# Build M2G/SVG (JSR226) core
cd $DIST_HOME/components/jsr226-svg/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$JAVA_SE_LIBRARY_PATH -source 1.3 -target 1.1" JAR_FILE="jsr226-svg-core.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr226-svg/core/jsr226-svg-core.jar $DIST_HOME/dist
# Add resources to jsr226-svg-core.jar
(cd $DIST_HOME/components/jsr226-svg/resources && find -type f | grep -v "/.svn") > $DIST_HOME/components/jsr226-svg/resources.list
cd $DIST_HOME/components/jsr226-svg/resources
jar uvf $DIST_HOME/dist/jsr226-svg-core.jar @$DIST_HOME/components/jsr226-svg/resources.list

# Build M2G/SVG (JSR226) MIDP2 implementation
cd $DIST_HOME/components/jsr226-svg/midp2
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/dist/jsr226-svg-core.jar -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/dist/jsr226-svg-core.jar -source 1.3 -target 1.1" JAR_FILE="jsr226-svg-midp2.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr226-svg/midp2/jsr226-svg-midp2.jar $DIST_HOME/dist

# Build M2G/SVG (JSR226) AWT implementation
#cd $DIST_HOME/components/jsr226-svg/awt
#make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/jsr226-svg-core.jar -source 1.3 -target 1.1" || exit 1
#make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/jsr226-svg-core.jar -source 1.3 -target 1.1" JAR_FILE="jsr226-svg-awt.jar" JAR_FLAGS="cvf" || exit 1
#cp $DIST_HOME/components/jsr226-svg/awt/jsr226-svg-awt.jar $DIST_HOME/dist

# Build OpenGL ES (JSR239) core
cd $DIST_HOME/components/jsr239-opengl/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JAVA_SE_LIBRARY_PATH -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JAVA_SE_LIBRARY_PATH -source 1.3 -target 1.1" JAR_FILE="jsr239-opengles-core.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr239-opengl/core/jsr239-opengles-core.jar $DIST_HOME/dist

# Build OpenGL ES (JSR239) pure Java implementation based on jGL
cd $DIST_HOME/components/jsr239-opengl/implementations/jgl
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar:$DIST_HOME/dist/jgl-cldc.jar -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JAVA_SE_LIBRARY_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar:$DIST_HOME/dist/jgl-cldc.jar -source 1.3 -target 1.1" JAR_FILE="jsr239-opengles-jgl.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr239-opengl/implementations/jgl/jsr239-opengles-jgl.jar $DIST_HOME/dist

# Build OpenGL ES (JSR239) NIO classes (only used with CLDC JVMs)
cd $DIST_HOME/components/jsr239-opengl/nio-cldc
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar -source 1.3 -target 1.1" JAR_FILE="jsr239-nio.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr239-opengl/nio-cldc/jsr239-nio.jar $DIST_HOME/dist

# Build M3G (JSR184)
cd $DIST_HOME/components/jsr184-m3g/core
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar:$DIST_HOME/dist/jsr239-nio.jar -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$DIST_HOME/dist/jsr239-opengles-core.jar:$DIST_HOME/dist/jsr239-nio.jar -source 1.3 -target 1.1" JAR_FILE="jsr184-m3g.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/components/jsr184-m3g/core/jsr184-m3g.jar $DIST_HOME/dist

#-------------------
# Build demos
#-------------------

JSR_PATH=$DIST_HOME/dist/jsr179-location.jar:$DIST_HOME/dist/jsr205-messaging.jar:$DIST_HOME/dist/jsr226-svg-core.jar:$DIST_HOME/dist/jsr239-opengles-core.jar:$DIST_HOME/dist/jsr239-nio.jar:$DIST_HOME/dist/jsr184-m3g.jar

cd $DIST_HOME/demos/src
make JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JSR_PATH -source 1.3 -target 1.1" || exit 1
make jar JAVAC=$JAVAC_CMD JAVAC_FLAGS="-bootclasspath $CLDC_PATH:$MIDPATH_CORE_PATH:$JSR_PATH -source 1.3 -target 1.1" JAR_FILE="midpath-demos.jar" JAR_FLAGS="cvf" || exit 1
cp $DIST_HOME/demos/src/midpath-demos.jar $DIST_HOME/dist
# Add resources to midpath-demos.jar
(cd $DIST_HOME/demos/resources && find -type f | grep -v "/.svn") > $DIST_HOME/demos/resources.list
cd $DIST_HOME/demos/resources
jar uvf $DIST_HOME/dist/midpath-demos.jar @$DIST_HOME/demos/resources.list

#------------------- 
# Build native code
#-------------------

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
