#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=../../
pwd=$(pwd)

COLOR_RED="\033[1;31m"
COLOR_GREEN="\033[1;32m"
COLOR_NONE="\033[0m"

usage() {
  echo "Usage: please see README"
  echo ""
  echo "Options"
  echo "  --help -h : Display this message"
  echo "  sha2 : Test sha2"
  echo "  sha3 : Test sha3"
  exit "$1"
}


TEST_TYPE="sha3"

while [ "$1" != "" ];
do
  case $1 in
    -h | --help)
      usage 3 ;;
    sha2 | sha3)
      TEST_TYPE=$1 ;;

    * )
      error "invalid option $1"
      usage 1 ;;

  esac
  shift
done

if [ $TEST_TYPE == "sha3" ]; then
  echo -e "$COLOR_GREEN== test sha3 ==$COLOR_NONE"
  cd $chipyard
  source ./env.sh
  sbt "project sha3" "test"
fi

if [ $TEST_TYPE == "sha2" ]; then
  echo -e "$COLOR_GREEN== test sha2 ==$COLOR_NONE"
  cd $chipyard
  source ./env.sh
  sbt "project sha2" "test"
fi

# sbt "project sha3" "testOnly sha3.ChiTests"
# sbt "project sha3" "testOnly sha3.IotaTests"
# sbt "project sha3" "testOnly sha3.ThetaTests"
