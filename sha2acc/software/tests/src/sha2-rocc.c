#include <stdio.h>
#include <stdint.h>
#include "rocc.h"
#include "sha2.h"
#include "encoding.h"
#include "compiler.h"

int main() {

  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    printf("Start SHA2 basic test 1.\n");
    // BASIC TEST 1 - 150 zero bytes

    // Setup some test data
    static unsigned char input[150] __aligned(8) = { '\0' };
    unsigned char output[SHA256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    // time_start = rdtime();
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
    // time_end = rdtime();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
    {29,131,81,139,137,123,20,226,148,57,144,239,246,85,131,130,70,204,2,7,167,201,90,95,61,252,204,46,57,95,139,191};
    
    //sha3ONE(input, sizeof(input), result);
    for(i = 0; i < SHA256_DIGEST_SIZE; i++){
      printf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
      if(output[i] != result[i]) {
        printf("Failed: Outputs don't match!\n");
        printf("SHA execution took %lu cycles\n", end - start);
        return 1;
      }
    }
  } while(0);

  printf("Success!\n");

  printf("SHA2 execution took %lu cycles\n", end - start);
  // printf("SHA execution took %lu times\n", time_end - time_start);
  printf("SHA2 execution took %lu instructions\n", inst_end - inst_start);

  return 0;
}
