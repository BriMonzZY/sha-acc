#ifndef __SHA2_H
#define __SHA2_H

#include <stdint.h>

#define SHA256_SIZE_BYTES 32

typedef struct {
    uint8_t  buf[64];
    uint32_t hash[8];
    uint32_t bits[2];
    uint32_t len;
    uint32_t rfu__;
    uint32_t W[64];
} sha256_context;

void sha256Init(sha256_context *ctx);
void sha256Hash(sha256_context *ctx, const void *data, size_t len);
void sha256Done(sha256_context *ctx, uint8_t *hash);

void sha256(const void *data, size_t len, uint8_t *hash);


#endif
