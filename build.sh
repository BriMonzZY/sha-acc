#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=../../
pwd=$(pwd)

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
    sha3acc | sha3accprint | sha2acc | sha2accprint | test | clean)
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
fi

if [ $BUILD_TYPE == "sha3accprint" ]; then
  cd $chipyard
  source ./env.sh
  cd sims/verilator
  make CONFIG=Sha3RocketPrintfConfig VERILATOR_THREADS=$(nproc) -j$(nproc)
fi

if [ $BUILD_TYPE == "test" ]; then
  echo " ========== BUILD SOFTWAR TESTS =========="
  source $chipyard/./env.sh
  cd $pwd/sha3acc/software
  ./build.sh
  cd ../../sha2acc/software
  ./build.sh
fi

if [ $BUILD_TYPE == "sha2acc" ]; then
  cd $chipyard
  source ./env.sh
  cd sims/verilator
  make CONFIG=Sha2RocketConfig VERILATOR_THREADS=$(nproc) -j$(nproc)
fi

if [ $BUILD_TYPE == "clean" ]; then
echo " ========== CLEAN VERILATOR BUILD FILES =========="
  cd $chipyard/sims/verilator
  make clean
fi

echo "Build complete!"
