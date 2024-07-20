// See LICENSE.Sifive for license details.
#ifndef _SDBOOT_KPRINTF_H
#define _SDBOOT_KPRINTF_H

#include <platform.h>
#include <stdint.h>

#define REG32(p, i)	((p)[(i) >> 2])

#ifndef UART_CTRL_ADDR
  #ifndef UART_NUM
    #define UART_NUM 0
  #endif

  #define _CONCAT3(A, B, C) A ## B ## C
  #define _UART_CTRL_ADDR(UART_NUM) _CONCAT3(UART, UART_NUM, _CTRL_ADDR)
  #define UART_CTRL_ADDR _UART_CTRL_ADDR(UART_NUM)
#endif
// static volatile uint32_t * const uart = (void *)(UART_CTRL_ADDR);
static volatile uint32_t * const uart = (void *)(0x64000000);

static inline void kputc(char c)
{
	volatile uint32_t *tx = &REG32(uart, UART_REG_TXFIFO);
// #ifdef __riscv_atomic
	int32_t r;
	do {
		__asm__ __volatile__ (
			"amoor.w %0, %2, %1\n"
			: "=r" (r), "+A" (*tx)
			: "r" (c));
	} while (r < 0);
// #else
	// while ((int32_t)(*tx) < 0);
	// *tx = c;
// #endif
}

// extern void kputs(const char *);
// extern void kprintf(const char *, ...);

// #ifdef DEBUG
// #define dprintf(s, ...)	kprintf((s), ##__VA_ARGS__)
// #define dputs(s)	kputs((s))
// #else
// #define dprintf(s, ...) do { } while (0)
// #define dputs(s)	do { } while (0)
// #endif



#include <stdarg.h>
#include <stdint.h>
#include <stdbool.h>

static inline void _kputs(const char *s)
{
	char c;
	for (; (c = *s) != '\0'; s++)
		kputc(c);
}

void kputs(const char *s)
{
	_kputs(s);
	kputc('\r');
	kputc('\n');
}

void kprintf(const char *fmt, ...)
{
	va_list vl;
	bool is_format, is_long, is_char;
	char c;

	va_start(vl, fmt);
	is_format = false;
	is_long = false;
	is_char = false;
	while ((c = *fmt++) != '\0') {
		if (is_format) {
			switch (c) {
			case 'l':
				is_long = true;
				continue;
			case 'h':
				is_char = true;
				continue;
			case 'x': {
				unsigned long n;
				long i;
				if (is_long) {
					n = va_arg(vl, unsigned long);
					i = (sizeof(unsigned long) << 3) - 4;
				} else {
					n = va_arg(vl, unsigned int);
					i = is_char ? 4 : (sizeof(unsigned int) << 3) - 4;
				}
				for (; i >= 0; i -= 4) {
					long d;
					d = (n >> i) & 0xF;
					kputc(d < 10 ? '0' + d : 'a' + d - 10);
				}
				break;
			}
			case 's':
				_kputs(va_arg(vl, const char *));
				break;
			case 'c':
				kputc(va_arg(vl, int));
				break;
			}
			is_format = false;
			is_long = false;
			is_char = false;
		} else if (c == '%') {
			is_format = true;
		} else {
			kputc(c);
		}
	}
	va_end(vl);
}




#endif /* _SDBOOT_KPRINTF_H */
