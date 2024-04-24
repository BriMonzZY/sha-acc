#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=../../
pwd=$(pwd)

COLOR_RED="\033[1;31m"
COLOR_GREEN="\033[1;32m"
COLOR_NONE="\033[0m"


# 检查vivado是否安装
vivadooutput=$(vivado -version)
if [ -n "$vivadooutput" ]; then
  echo "Vivado tools is installed"
  echo $vivadooutput
else
  echo "Vivado tools no found, please install vivado or check environment variable"
  exit 1
fi


usage() {
  echo "Usage: please see README"
  echo ""
  echo "Options"
  echo "  --help -h : Display this message"
  echo "  sha3acc : Build SHA3 Accelerator bitstream (VCU108)"
  echo "  sha2acc : Build SHA2 Accelerator bitstream (VCU108)"
  echo "  clean   : Clean all generated files in fpga directory"
  exit "$1"
}

BUILD_TYPE="sha3acc"

while [ "$1" != "" ];
do
  case $1 in
    -h | --help)
      usage 3 ;;
    sha3acc | sha2acc | clean)
      BUILD_TYPE=$1 ;;

    * )
      error "invalid option $1"
      usage 1 ;;

  esac
  shift
done


exit_build() {
  if [ $? -eq 0 ]; then
    echo -e "$COLOR_GREEN BUILD PASS $COLOR_NONE"
  else
    echo -e "$COLOR_RED BUILD FAIL $COLOR_NONE"
  fi
  exit 0
}

if [ $BUILD_TYPE == "sha3acc" ]; then
  cd $chipyard
  source ./env.sh
  cd fpga
  exit_build
fi

if [ $BUILD_TYPE == "sha2acc" ]; then
  cd $chipyard
  source ./env.sh
  cd fpga
  exit_build
fi

if [ $BUILD_TYPE == "clean" ]; then
  cd $chipyard/fpga
  make clean
fi
