/**
 * @file shasum.c
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

#include "sha2.h"

#define TEST

static void help(const char *argv0)
{
  printf("To call: %s [-f] <filename> or <string>\n", argv0);
}

int main(int argc, char *argv[])
{
#ifndef TEST
  const char *input;
  int fd;
  struct stat st;
  void *p;
  unsigned i;
  uint8_t hash[SHA256_SIZE_BYTES];


  if(argc != 2 && argc != 3) {
    help(argv[0]);
    return 1;
  }

  input = argv[1];
  if(input[0]=='-' && input[1]=='f') { /* input is file */
    input = argv[2];
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

  sha256(p, st.st_size, hash);

  for(i = 0; i < SHA256_SIZE_BYTES; i++) {
    printf("%02x", hash[i]);
  }
  printf("  %s\n", input);

  return 0;
#else
  int fd;
  unsigned i;
  uint8_t hash[SHA256_SIZE_BYTES];

  static unsigned char input[150] = { '\0' };
  // static unsigned char input[150] = {'1', '2', '3', '4', '5', '6', '7', '8', '9', '\0'};
  // static unsigned char input[13] = { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a' , 'b', 'c', 'd' };
  // static unsigned char input[11] = { 'b', 'a', '9', '8', '7', '6', '5', '4', '3', '2', '1' };

  sha256(input, sizeof(input), hash);

  for(i = 0; i < SHA256_SIZE_BYTES; i++) {
    printf("%x,", hash[i]);
  }
  printf("\n");

  return 0;
#endif
}
