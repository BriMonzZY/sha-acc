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

./build.sh sha3acc # 将sha3复制到chipyard编译,并编译bare测试
./build.sh sha3accprint
./build.sh sha3test # 在spike运行软件测试

./build.sh sha2acc
./build.sh sha2accprint
./build.sh sha2test
```



编译测试程序：

直接运行 sha2(3)acc/softwave 中的 build.sh 即可编译测试文件（注意需要在 chipyard 环境中编译）

也可以在 chipyard 环境中用 marshal 命令编译以及用 spike 运行测试文件

```shell
./buildtest.sh # 会把 sha2acc 和 sha3acc 复制到 chipyard 并切换到chipyard环境编译其中的软件测试
```

