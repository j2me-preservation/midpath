
CFLAGS = -I/usr/include/classpath
LDFLAGS =  

SOURCES = org_thenesis_midpath_ui_backend_fb_FBCanvas.c

OBJECTS = $(patsubst %.c, %.o, $(SOURCES))

all: libmidpathfb.so

%.o : %.c
	$(CC) $(CFLAGS) -fPIC -g -c -Wall -o $(patsubst %.c, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o 


