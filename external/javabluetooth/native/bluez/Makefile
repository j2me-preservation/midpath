
CC = gcc 
CFLAGS = -I/usr/include/classpath
LDFLAGS = -lbluetooth

SOURCES=org_javabluetooth_stack_hci_BlueZSocket.c

OBJECTS=$(patsubst %.c, %.o, $(SOURCES))

all: libjavabluez.so

%.o : %.c
	$(CC) $(CFLAGS) -fPIC -g -c -Wall -o $(patsubst %.c, %.o, $<) $<

%.so: $(OBJECTS)
	$(CC) $(LDFLAGS) -shared -Wl,-soname,$@ -lc -o $@ $^

clean:
	rm -f *.so *.o
	
