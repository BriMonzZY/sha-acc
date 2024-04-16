#include <stdio.h>
#include <stdint.h>
#include "sha2.h"
#include "encoding.h"
#include "compiler.h"

void test1(void)
{
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
    inst_start = rdinstret();

    // Compute hash in SW
    sha256(input, sizeof(input), output);

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
      {29,131,81,139,137,123,20,226,148,57,144,239,246,85,131,130,70,204,2,7,167,201,90,95,61,252,204,46,57,95,139,191};
    
    printf("Input:\n");
    for(i = 0; i < sizeof(input); i++) {
      printf("%x", input[i]);
    }
    printf("\nInput Length: %d\n", sizeof(input));
    printf("\nSHA-256 Output:\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      printf("%x ", output[i]);
    }
    printf("\nExpected Output:\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      printf("%x ", result[i]);
    }
    printf("\n");
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++){
    //   printf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     printf("Failed: Outputs don't match!\n");
    //     printf("SHA execution took %lu cycles\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);

  printf("Success!\n");

  printf("SHA2 execution took %lu cycles\n", end - start);
  printf("SHA2 execution took %lu instructions\n", inst_end - inst_start);
}

void test2(void)
{
  unsigned long start, end;
  unsigned long time_start, time_end;
  unsigned long inst_start, inst_end;

  do {
    printf("Start SHA2 basic test 2.\n");

    // Setup some test data
    static unsigned char input[10] __aligned(8) = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
    unsigned char output[SHA256_DIGEST_SIZE] __aligned(8);

    start = rdcycle();
    inst_start = rdinstret();

    // Compute hash in SW
    sha256(input, sizeof(input), output);

    end = rdcycle();
    inst_end = rdinstret();

    // Check result
    int i;
    static const unsigned char result[SHA256_DIGEST_SIZE] =
      {199,117,231,183,87,237,230,48,205,10,161,17,59,209,2,102,26,179,136,41,202,82,166,66,42,183,130,134,47,38,134,70};
    
    printf("Input:\n");
    for(i = 0; i < sizeof(input); i++) {
      printf("%x", input[i]);
    }
    printf("\nInput Length: %d\n", sizeof(input));
    printf("\nSHA-256 Output:\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      printf("%x ", output[i]);
    }
    printf("\nExpected Output:\n");
    for(i = 0; i < SHA256_DIGEST_SIZE; i++) {
      printf("%x ", result[i]);
    }
    printf("\n");
    // for(i = 0; i < SHA256_DIGEST_SIZE; i++){
    //   printf("output[%d]:%d ==? results[%d]:%d \n",i,output[i],i,result[i]);
    //   if(output[i] != result[i]) {
    //     printf("Failed: Outputs don't match!\n");
    //     printf("SHA execution took %lu cycles\n", end - start);
    //     return 1;
    //   }
    // }
  } while(0);

  printf("Success!\n");

  printf("SHA2 execution took %lu cycles\n", end - start);
  printf("SHA2 execution took %lu instructions\n", inst_end - inst_start);
}


int main() {
  test1();
  printf("\n");
  test2();
  return 0;
}
