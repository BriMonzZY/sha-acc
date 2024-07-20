basedir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
srcdir := $(basedir)/src
klibdir := $(basedir)/klib

PROGRAMS ?= sha3-sw sha3-rocc

CC := $(TARGET)-gcc
OBJDUMP := $(TARGET)-objdump
OBJCOPY := $(TARGET)-objcopy

CFLAGS += -I $(srcdir) -I $(klibdir)

hdrs := $(wildcard *.h) $(wildcard $(srcdir)/*.h) $(wildcard $(klibdir)/*.h)
objs ?=
ldscript ?=

%.o: $(srcdir)/%.c $(hdrs)
	$(CC) $(CFLAGS) -o $@ -c $<
%.o: %.c $(hdrs)
	$(CC) $(CFLAGS) -o $@ -c $<
%.o: %.S $(hdrs)
	$(CC) $(CFLAGS) -D__ASSEMBLY__=1 -o $@ -c $<

%.riscv: %.o $(objs) $(ldscript)
	$(CC) $(LDFLAGS) $(if $(ldscript),-T $(ldscript)) -o $@ $< $(objs)

%.dump: %.riscv
	$(OBJDUMP) -d $< > $@

%.bin: %.riscv
	$(OBJCOPY) -O binary $< $@

.DEFAULT: elf

.PHONY: elf dumps
elf: $(addsuffix .riscv,$(PROGRAMS))
dumps: $(addsuffix .dump,$(PROGRAMS))
bin: $(addsuffix .bin,$(PROGRAMS))

.PHONY: clean
clean:
	rm -f -- *.riscv *.o *.dump

.SUFFIXES:
.SUFFIXES: .o .c .S
