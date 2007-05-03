
CC = gcc 
CFLAGS = `pkg-config --cflags gtk+-2.0`
LDFLAGS = `pkg-config --libs gtk+-2.0`

gtkcanvas: org_thenesis_midpath_ui_backend_gtk_GTKCanvas.c
	$(CC) org_thenesis_midpath_ui_backend_gtk_GTKCanvas.c -o libgtkcanvas.so $(CFLAGS) $(LDFLAGS) -shared -Wl,-soname,libgtkbackend.so -I/usr/include/classpath -static -lc
	
