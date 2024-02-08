# 基于RISC-V的SHA系列哈希算法硬件加速器

关键词：SHA、Rocket Chip、Chipyard、RoCC



安装 [chipyard](https://github.com/ucb-bar/chipyard) 环境用于编译、运行、仿真本项目



sha3sim SHA3算法的C语言实现

sha3acc SHA3算法硬件加速器

sha2sim SHA2算法的C语言实现

sha2acc SHA2算法硬件加速器





通过将加速器代码复制到 chipyard 环境下进行编译和仿真





```shell
./build.sh -h

./build.sh sha3acc
./build.sh sha3accprint
./build.sh sha3test

./build.sh sha2acc
./build.sh sha2accprint
./build.sh sha2test
```

