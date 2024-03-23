#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=../../


usage() {
  echo "Usage: please see README"
  echo ""
  echo "Options"
  echo "  --help -h : Display this message"
  exit "$1"
}

BUILD_TYPE="sha3acc"

while [ "$1" != "" ];
do
  case $1 in
    -h | --help)
      usage 3 ;;
    sha3acc | sha3accprint | sha2acc | sha2accprint | sha3test | sha2test)
      BUILD_TYPE=$1 ;;

    * )
      error "invalid option $1"
      usage 1 ;;

  esac
  shift
done



## build sha3 and rocket chip
echo " ========== BUILD SHA ACC =========="
if [ $BUILD_TYPE == "sha3acc" ]; then
  cd $chipyard
  source ./env.sh
  cd sims/verilator
  make CONFIG=Sha3RocketConfig VERILATOR_THREADS=$(nproc) -j$(nproc)
  ## build software tests
  echo " ========== BUILD SOFTWAR TESTS =========="
  cd $chipyard/generators/sha3acc/software/tests/bare
  make
fi

if [ $BUILD_TYPE == "sha3accprint" ]; then
  cd $chipyard
  source ./env.sh
  cd sims/verilator
  make CONFIG=Sha3RocketPrintfConfig VERILATOR_THREADS=$(nproc) -j$(nproc)
  ## build software tests
  echo " ========== BUILD SOFTWAR TESTS =========="
  cd $chipyard/generators/sha3acc/software/tests/bare
  make
fi

if [ $BUILD_TYPE == "sha3test" ]; then
  echo " ========== BUILD SOFTWAR TESTS =========="
  cd $chipyard
  source ./env.sh
  cd generators/sha3acc/software
  marshal build marshal-configs/sha3-bare-rocc.yaml
  marshal build marshal-configs/sha3-bare-sw.yaml
fi

if [ $BUILD_TYPE == "sha2acc" ]; then
  cd $chipyard
  source ./env.sh
  cd sims/verilator
  make CONFIG=Sha2RocketConfig VERILATOR_THREADS=$(nproc) -j$(nproc)
  ## build software tests
  echo " ========== BUILD SOFTWAR TESTS =========="
  cd $chipyard/generators/sha2acc/software/tests/bare
  make
fi

echo "Build complete!"
