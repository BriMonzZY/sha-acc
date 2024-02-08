#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com

chipyard=~/chipyard

cd $chipyard
source ./env.sh
cd generators/sha3acc/software
# marshal test --spike marshal-configs/sha3-bare-rocc.yaml
marshal test --spike marshal-configs/sha3-bare-sw.yaml

