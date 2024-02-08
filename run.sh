#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=~/chipyard
config=Sha3RocketConfig
# config=Sha3RocketPrintfConfig
# testname=sha3-rocc
testname=sha3-sw

cd $chipyard
source ./env.sh
verilator -version
cd sims/verilator
./simulator-chipyard.harness-$config $chipyard/generators/sha3acc/software/tests/bare/$testname.riscv
