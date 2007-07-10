
CFLAGS = `pkg-config --cflags qt-mt` -I/usr/include/classpath
LDFLAGS = `pkg-config --libs qt-mt` 

SOURCES = org_thenesis_midpath_ui_backend_qt_QTCanvas.cpp

OBJECTS = $(patsubst %.cpp, %.o, $(SOURCES))

all: libmidpathqt.so

%.o : %.cpp
	$(CC) $(CFLAGS) -fPIC -g -c -Wall -o $(patsubst %.cpp, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o 


