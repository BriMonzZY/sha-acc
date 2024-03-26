#! /bin/bash
# brimonzzy
# zzybrimon@gmail.com


cd ../../
source ./env.sh
# sbt "project sha3" "test" # TODO:不知道为什么运行结束后会报段错误
# sbt "project sha3" "testOnly sha3.ChiTests"
# sbt "project sha3" "testOnly sha3.IotaTests"
# sbt "project sha3" "testOnly sha3.ThetaTests"

sbt "project sha2" "test"
