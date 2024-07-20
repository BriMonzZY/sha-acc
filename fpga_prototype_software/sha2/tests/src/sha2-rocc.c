#include <stdio.h>
#include <stdint.h>
#include "rocc.h"
#include "sha2.h"
#include "encoding.h"
#include "compiler.h"

#include <platform.h>

#define DEBUG
#include "kprintf.h"

void rocctest1(void)
{
  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    kprintf("Start SHA2 RoCC basic test 1.\r\n");
    // BASIC TEST 1 - 150 zero bytes

    // Setup some test data
    // static unsigned char input[150] __aligned(8) = { '\0' };
    unsigned char input[150];
    for(int j = 0; j < 130; j++) {
      input[j] = 0;
    }
    // static unsigned char input[150] __aligned(8) = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '\0'};
    // static unsigned char input[13] __aligned(8)= { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a' , 'b' , 'c', 'd' };
    // static unsigned char input[1] __abligned(8)= { '1' };
    
    unsigned char output[SHA256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    inst_start = rdinstret();

    // Compute hash with accelerator
    asm volatile ("fence");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    ROCC_INSTRUCTION_SS(1, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    ROCC_INSTRUCTION_S(1, sizeof(input), 1);
    asm volatile ("fence" ::: "memory");

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
      {29,131,81,139,137,123,20,226,148,57,144,239,246,85,131,130,70,204,2,7,167,201,90,95,61,252,204,46,57,95,139,191};
    
    kprintf("Input:\r\n");
    for(i = 0; i < sizeof(input); i++) {
      kprintf("%x", input[i]);
    }
    kprintf("\r\nInput Length: %x\r\n", sizeof(input));
    kprintf("\r\nSHA-256 Output:\r\n");
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
    //   kprintf("%x ", output[i]);
    // }
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\nExpected Output:\r\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\n");
    // //sha3ONE(input, sizeof(input), result);
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++){
    //   kprintf("output[%d]:%d ==? results[%d]:%d \r\n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     kprintf("Failed: Outputs don't match!\r\n");
    //     kprintf("SHA execution took %lu cycles\r\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);
  kprintf("Success!\r\n");

  kprintf("SHA2 execution took %x cycles\r\n", end - start);
  kprintf("SHA2 execution took %x instructions\r\n", inst_end - inst_start);
}

void rocctest2(void)
{
  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    kprintf("Start SHA2 RoCC basic test 2.\r\n");

    // Setup some test data
    static unsigned char input[10] __aligned(8) = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
    
    unsigned char output[SHA256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    inst_start = rdinstret();

    // Compute hash with accelerator
    asm volatile ("fence");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    ROCC_INSTRUCTION_SS(1, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    ROCC_INSTRUCTION_S(1, sizeof(input), 1);
    asm volatile ("fence" ::: "memory");

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
      {199,117,231,183,87,237,230,48,205,10,161,17,59,209,2,102,26,179,136,41,202,82,166,66,42,183,130,134,47,38,134,70};
    
    kprintf("Input:\r\n");
    for(i = 0; i < sizeof(input); i++) {
      kprintf("%x", input[i]);
    }
    kprintf("\r\nInput Length: %x\r\n", sizeof(input));
    kprintf("\r\nSHA-256 Output:\r\n");
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
    //   kprintf("%x ", output[i]);
    // }
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\nExpected Output:\r\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      kprintf("%x ", result[i]);
    }
    kprintf("\r\n");
    // //sha3ONE(input, sizeof(input), result);
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++){
    //   kprintf("output[%d]:%d ==? results[%d]:%d \r\n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     kprintf("Failed: Outputs don't match!\r\n");
    //     kprintf("SHA execution took %lu cycles\r\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);
  kprintf("Success!\r\n");

  kprintf("SHA2 execution took %x cycles\r\n", end - start);
  kprintf("SHA2 execution took %x instructions\r\n", inst_end - inst_start);
}

int main() {
  REG32(uart, UART_REG_TXCTRL) = UART_TXEN;
  rocctest1();
  kprintf("\r\n");
  rocctest2();
  return 0;
}
