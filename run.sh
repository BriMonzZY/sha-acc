#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

# config=RocketConfig
config=Sha3RocketConfig
# config=Sha3RocketPrintfConfig
# config=Sha2RocketConfig
# config=Sha2SmallRocketConfig
# config=Sha2RocketPrintfConfig

testdir=../../generators/sha-acc/sha3acc/software/tests/bare
# testdir=../../generators/sha-acc/sha2acc/software/tests/bare

# testname=sha3-rocc
testname=sha3-sw
# testname=sha2-sw
# testname=sha2-rocc


cd ../../
source ./env.sh
verilator -version
cd sims/verilator

# 不输出Log
# ./simulator-chipyard.harness-$config $testdir/$testname.riscv

# 输出Log
# ./simulator-chipyard.harness-$config $testdir/$testname.riscv +verbose

# 运行带debug的仿真
# ./simulator-chipyard.harness-$config-debug $testdir/$testname.riscv

# Fast Memory Loading
make run-binary CONFIG=$config BINARY=$testdir/$testname.riscv LOADMEM=1


# 编译并运行，并生成波形文件
# make CONFIG=$config run-binary-debug BINARY=$testdir/$testname.riscv LOADMEM=1 VERILATOR_THREADS=$(nproc) -j$(nproc)
