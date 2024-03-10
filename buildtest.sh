#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=~/chipyard # TODO

cp -r sha3acc $chipyard/generators
cp -r sha2acc $chipyard/generators

cd $chipyard
source ./env.sh
cd $chipyard/generators/sha3acc/software
./build.sh
cd $chipyard/generators/sha2acc/software
./build.sh

# marshal test --spike marshal-configs/sha3-bare-rocc.yaml
# marshal test --spike marshal-configs/sha3-bare-sw.yaml
