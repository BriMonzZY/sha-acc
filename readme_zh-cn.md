# 基于RISC-V的SHA系列哈希算法硬件加速器

关键词：SHA、Rocket Chip、Chipyard、RoCC

<br/>

<br/>

本项目相关的仓库：

[本仓库](https://github.com/BriMonzZY/sha-acc)

[运行本项目的chipyard环境](https://github.com/BriMonzZY/chipyard-shaacc) ( fork自chipyard v1.11.0 )

[支持本项目sha扩展的spike](https://github.com/BriMonzZY/riscv-isa-sim-sha-extension) ( fork自riscv-isa-sim v1.1.0 )

[支持VCU108的fpga-shell](https://github.com/BriMonzZY/rocket-chip-fpga-shells) ( fork自rocket-chip-fpga-shells )

<br/>

<br/>

文件结构：

```txt
.
├── sha3sim // SHA3算法的C语言实现
├── sha3acc // SHA3算法硬件加速器
|   ├── software
|   └── src
├── sha2sim // SHA2算法的C语言实现
├── sha2acc // SHA2算法硬件加速器
|   ├── software
|   └── src
└── doc // 文档
```

<br/>

<br/>

使用方式：

本项目需要在配套的 [chipyard-shaacc](https://github.com/BriMonzZY/chipyard-shaacc) 环境中运行：

```shell
git clone https://github.com/BriMonzZY/chipyard-shaacc.git

cd chipyard-shaacc

# 这一步可能需要较长的时间，需要占用>3GB的存储空间，建议科学上网
./init_env.sh # 初始化chipyard环境
```

<br/>

项目代码在 `chipyard-shaacc/generators/sha-acc` 目录中。

可以执行 `build.sh` 、`chiseltest.sh` 或者 `run.sh` 脚本来编译或者运行本项目：

```shell
## 进入目录
cd chipyard-shaacc/generators/sha-acc

## help
./build.sh -h 
./run.sh -h
./fpgabuild.sh -h
./chiseltest.sh -h


./build.sh sha3acc
./build.sh sha3accprint

./build.sh sha2acc
./build.sh sha2accprint

./build.sh test # 编译测试程序

./build.sh clean # 在sims/verilator和sims/vcs和sims/xcelium中执行 make clean 清除编译产生的文件



./run.sh # 运行编译的文件或者编译并运行(TODO还没有完善)



## 运行单元测试
./chiseltest.sh
./chiseltest.sh sha3
./chiseltest.sh sha2
```

<br/>

<br/>

生成FPGA比特流文件

默认生成VCU108板卡的比特流文件

```shell
./fpgabuild.sh
```

