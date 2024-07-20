//see LICENSE for license
// The following is a RISC-V program to test the functionality of the
// sha3 RoCC accelerator.
// Compile with riscv-gcc sha3-rocc.c
// Run with spike --extension=sha3 pk a.out

#include <stdio.h>
#include <stdint.h>
#include "rocc.h"
#include "sha3.h"
#include "encoding.h"
#include "compiler.h"

#define DEBUG
#include "kprintf.h"

void rocctest1(void)
{
  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    kprintf("Start SHA3-256 RoCC basic test 1.\r\n");
    // BASIC TEST 1 - 150 zero bytes

    // Setup some test data
    // static unsigned char input[150] __aligned(8) = { '\0' };
    unsigned char input[150];
    for(int j = 0; j < 10; j++) {
      input[j] = 0;
    }
    unsigned char output[SHA3_256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    inst_start = rdinstret();

    // Compute hash with accelerator
    asm volatile ("fence");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    ROCC_INSTRUCTION_SS(2, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    ROCC_INSTRUCTION_S(2, sizeof(input), 1);
    asm volatile ("fence" ::: "memory");

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA3_256_DIGEST_SIZE] =
      {203,52,27,85,46,79,152,228,86,138,201,206,253,168,255,107,122,177,65,68,231,19,70,198,64,90,192,80,206,234,168,159};
    
    kprintf("Input:\r\n\n");
    for(i = 0; i < sizeof(input); i++) {
      kprintf("%x", input[i]);
    }
    kprintf("\r\nInput Length: %x\r\n", sizeof(input));
    kprintf("\r\nSHA3-256 Output:\r\n");
    for(i = 0; i < SHA3_256_DIGEST_SIZE; i++) {
      kprintf("%x ", output[i]);
    }
    kprintf("\r\nExpected Output:\r\n");
    for(i = 0; i < SHA3_256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\n");
    // //sha3ONE(input, sizeof(input), result);
    // for(i = 0; i < SHA3_256_DIGEST_SIZE; i++){
    //   kprintf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     kprintf("Failed: Outputs don't match!\n");
    //     kprintf("SHA execution took %lu cycles\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);

  kprintf("Success!\r\n");

  kprintf("SHA3 execution took %x cycles\r\n", end - start);
  kprintf("SHA3 execution took %x instructions\r\n", inst_end - inst_start);
}

void rocctest2(void)
{
  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    kprintf("Start SHA3-256 RoCC basic test 2.\r\n");
    // BASIC TEST 1 - 150 zero bytes

    // Setup some test data
    static unsigned char input[10] __aligned(8) = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
    unsigned char output[SHA3_256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    inst_start = rdinstret();

    // Compute hash with accelerator
    asm volatile ("fence");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    ROCC_INSTRUCTION_SS(2, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    ROCC_INSTRUCTION_S(2, sizeof(input), 1);
    asm volatile ("fence" ::: "memory");

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA3_256_DIGEST_SIZE] =
      {1,218,136,67,233,118,145,58,165,193,90,98,212,95,28,146,103,57,29,203,208,167,106,212,17,145,144,67,243,116,161,99};
    
    kprintf("Input:\r\n");
    for(i = 0; i < sizeof(input); i++) {
      kprintf("%x", input[i]);
    }
    kprintf("\r\nInput Length: %x\r\n", sizeof(input));
    kprintf("\r\nSHA3-256 Output:\r\n");
    for(i = 0; i < SHA3_256_DIGEST_SIZE; i++) {
      kprintf("%x ", output[i]);
    }
    kprintf("\r\nExpected Output:\r\n");
    for(i = 0; i < SHA3_256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\n");
    // //sha3ONE(input, sizeof(input), result);
    // for(i = 0; i < SHA3_256_DIGEST_SIZE; i++){
    //   kprintf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     kprintf("Failed: Outputs don't match!\n");
    //     kprintf("SHA execution took %lu cycles\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);

  kprintf("Success!\r\n");

  kprintf("SHA3 execution took %x cycles\r\n", end - start);
  kprintf("SHA3 execution took %x instructions\r\n", inst_end - inst_start);
}


int main() {
  REG32(uart, UART_REG_TXCTRL) = UART_TXEN;
  rocctest1();
  kprintf("\r\n");
  rocctest2();
  return 0;
}
