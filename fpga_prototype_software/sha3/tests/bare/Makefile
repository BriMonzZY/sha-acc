bmarkdir := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

TARGET := riscv64-unknown-elf
# CFLAGS := -mcmodel=medany -std=gnu99 -O2 -fno-common -fno-builtin-printf -Wall -fno-tree-loop-distribute-patterns
CFLAGS := -mcmodel=medany -std=gnu99 -O2 -fno-common -Wall
LDFLAGS := -static -nostdlib -nostartfiles -lgcc

PROGRAMS := sha3-rocc sha3-sw

objs := crt.o syscalls.o
ldscript := link.ld

include $(bmarkdir)/../common.mk
