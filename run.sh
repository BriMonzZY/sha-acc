#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

config=RocketConfig
# config=Sha3RocketConfig
# config=Sha3RocketPrintfConfig
# testname=sha3-rocc
testname=sha3-sw

cd ../../
source ./env.sh
verilator -version
cd sims/verilator
./simulator-chipyard.harness-$config ../../generators/sha-acc/sha3acc/software/tests/bare/$testname.riscv
