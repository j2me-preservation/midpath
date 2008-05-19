JNI_INCLUDE = -I/usr/include/classpath

ifdef HILDON
# Gtk in Hildon AF
CFLAGS = `pkg-config --cflags gtk+-2.0 libhildon` $(JNI_INCLUDE) $(CFLAGS)
LDFLAGS = `pkg-config --libs gtk+-2.0 libhildon` $(LDFLAGS)

SOURCES=org_thenesis_microbackend_ui_gtk_GTKBackend.c maemo/maemo_input.c

else

# Plain Gtk
CFLAGS = `pkg-config --cflags gtk+-2.0` $(JNI_INCLUDE) $(CFLAGS)
LDFLAGS = `pkg-config --libs gtk+-2.0` $(LDFLAGS)

SOURCES=org_thenesis_microbackend_ui_gtk_GTKBackend.c standard/standard_input.c

endif

OBJECTS=$(patsubst %.c, %.o, $(SOURCES))

all: libmicrobackendgtk.so

%.o : %.c
	$(CC) $(CFLAGS) -fPIC -g -c -Wall -o $(patsubst %.c, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o maemo/*.o
