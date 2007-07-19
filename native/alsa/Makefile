
CFLAGS = `pkg-config --cflags alsa` -I/usr/include/classpath
LDFLAGS = `pkg-config --libs alsa` 

SOURCES = org_thenesis_midpath_sound_backend_alsa_AlsaSink.c

OBJECTS=$(patsubst %.c, %.o, $(SOURCES))

all: libmidpathalsa.so

%.o : %.c
	$(CC) $(CFLAGS) -fPIC -g -c -Wall -o $(patsubst %.c, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o

