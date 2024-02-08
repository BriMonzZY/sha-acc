CC := gcc
CFLAGS := -Wall -Wextra -Wpedantic -O2 -g
LDFLAGS :=
LDLIBS :=

.SUFFIXES: .c .o
.c.o:
	@mkdir -p build
	$(CC) -c $(CFLAGS) -o build/$@ $<

all: sha3sum

sha3sum: sha3.o sha3sum.o
	$(CC) $(LDFLAGS) -o build/$@ sha3sum.c sha3.c $(LDLIBS)

clean:
	cd build && rm -rf *
