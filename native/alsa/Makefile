
CC = gcc 
CFLAGS = `pkg-config --cflags alsa`
LDFLAGS = `pkg-config --libs alsa`

alsasink: org_thenesis_midpath_sound_backend_alsa_AlsaSink.c
	$(CC) org_thenesis_midpath_sound_backend_alsa_AlsaSink.c -o libalsasink.so $(CFLAGS) $(LDFLAGS) -shared -Wl,-soname,libalsasink.so -I/usr/include/classpath -static -lc
	
