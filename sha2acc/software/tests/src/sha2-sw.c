#include <stdio.h>
#include <stdint.h>
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

    // Compute hash in SW
    sha256(input, sizeof(input), output);

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
    {29,131,81,139,137,123,20,226,148,57,144,239,246,85,131,130,70,204,2,7,167,201,90,95,61,252,204,46,57,95,139,191};
    
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
  printf("SHA2 execution took %lu instructions\n", inst_end - inst_start);

  return 0;
}
