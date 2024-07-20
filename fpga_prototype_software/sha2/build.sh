#!/bin/bash
set -e

# Build bare-metal tests
echo "Building bare-metal tests"
make -C tests/bare bin
