
ifdef QT4_BACKEND
CFLAGS =  -I/usr/include/qt4 -I/usr/include/qt4/QtGui -I/usr/include/classpath
LDFLAGS =  -lQtGui
DFLAGS = -DQT4_BACKEND
else
CFLAGS = `pkg-config --cflags qt-mt` -I/usr/include/classpath
LDFLAGS = `pkg-config --libs qt-mt`
endif

SOURCES = org_thenesis_midpath_ui_backend_qt_QTCanvas.cpp

OBJECTS = $(patsubst %.cpp, %.o, $(SOURCES))

all: libmidpathqt.so

%.o : %.cpp
	$(CC) $(CFLAGS) $(DFLAGS) -fPIC -g -c -Wall -o $(patsubst %.cpp, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o 


