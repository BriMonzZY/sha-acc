#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=../../ # TODO
pwd=$(pwd)

source $chipyard/./env.sh
cd $pwd/sha3acc/software
./build.sh
cd ../../sha2acc/software
./build.sh
