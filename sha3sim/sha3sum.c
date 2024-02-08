/**
 * @file sha3sum.c
 * @author brimonzzy (zzybrimon@gmail.com)
 * @brief 
 * @version 0.1
 * @date 2024-02-03
 * 
 * @copyright Copyright (c) 2024
 * 
 */

#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <assert.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>

#include "sha3.h"

// #define TEST

static void help(const char *argv0)
{
  printf("To call: %s 256|384|512 [-f] <filename> or <string>\n", argv0);
  printf("The sums are computed as described in the FIPS 202 SHA-3 submission.\n");
}

static void ByteToHex(uint8_t b, char *s)
{
  unsigned i=1;
  s[0] = s[1] = '0';
  s[2] = '\0';
  while(b) {
    unsigned t = b & 0x0f;
    if( t < 10 ) {
      s[i] = '0' + t;
    } else {
      s[i] = 'a' + t - 10;
    }
    i--;
    b >>= 4;
  }
}

int main(int argc, char *argv[])
{
#ifndef TEST
  const char *input;
  unsigned image_size;
  int fd;
  struct stat st;
  void *p;
  unsigned i;
  sha3_context c;
  const uint8_t *hash;


  if(argc != 3 && argc != 4) {
    help(argv[0]);
    return 1;
  }

  image_size = atoi(argv[1]);
  switch (image_size) {
    case 256:
    case 384:
    case 512:
      break;
    default:
      help(argv[0]);
      return 1;
    break;
  }

  input = argv[2];
  if(input[0]=='-' && input[1]=='f') { /* input is file */
    input = argv[3];
    if(access(input, R_OK)!=0) {
      printf("Cannot read file '%s'", input);
      return 2;
    }
    fd = open(input, O_RDONLY);
    if(fd == -1) {
      printf("Cannot open file '%s' for reading", input);
	    return 2;
    }
    i = fstat(fd, &st);
    if(i) {
	    close(fd);
	    printf("Cannot determine the size of file '%s'", input);
	    return 2;
    }
    p = mmap(NULL, st.st_size, PROT_READ, MAP_SHARED, fd, 0);
    close(fd);
    if(p == NULL) {
	    printf("Cannot memory-map file '%s'", input);
	    return 2;
    }
  }
  else { /* input is string */
    p = (void *) input;
    st.st_size = strlen(input);
  }

  switch(image_size) {
    case 256:
      sha3Init256(&c);
      break;
    case 384:
      sha3Init384(&c);
      break;
    case 512:
      sha3Init512(&c);
      break;
  }

  sha3Update(&c, p, st.st_size);
  hash = sha3Finalize(&c);

  munmap(p, st.st_size);

  for(i = 0; i < image_size/8; i++) {
    char s[3];
    ByteToHex(hash[i], s);
    printf("%s", s);
  }
  printf("  %s\n", input);

  return 0;
#else
  void *p;
  unsigned i;
  sha3_context c;
  const uint8_t *hash;

  static unsigned char input[150] = { '\0' };
  p = (void *)input;

  sha3Init256(&c);
  sha3Update(&c, p, 150);
  hash = sha3Finalize(&c);

  munmap(p, 150);

  for(i = 0; i < 256/8; i++) {
    char s[3];
    ByteToHex(hash[i], s);
    printf("%s", s);
  }

  return 0;
#endif
}
