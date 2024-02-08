CC := gcc
CFLAGS := -Wall -Wextra -Wpedantic -O2 -g
LDFLAGS :=
LDLIBS :=

.SUFFIXES: .c .o
.c.o:
	@mkdir -p build
	$(CC) -c $(CFLAGS) -o build/$@ $<

all: shasum

shasum: sha2.o shasum.o
	$(CC) $(LDFLAGS) -o build/$@ shasum.c sha2.c $(LDLIBS)

clean:
	cd build && rm -rf *
