# SHA series hash algorithm hardware Coprocessor based on RISC-V

Key Words：SHA、Rocket Chip、Chipyard、RoCC

<br/>

The repositories related to this project：

[This project](https://github.com/BriMonzZY/sha-acc)

[Chipyard enviroment to run this project](https://github.com/BriMonzZY/chipyard-shaacc) ( fork from chipyard v1.11.0 )

[spike of sha extension for this project](https://github.com/BriMonzZY/riscv-isa-sim-sha-extension) ( fork from riscv-isa-sim v1.1.0 )

[fpga-shell which support VCU108 board](https://github.com/BriMonzZY/rocket-chip-fpga-shells) ( fork from rocket-chip-fpga-shells )

<br/>

<br/>

File hierarchy:

```txt
.
├── sha3sim
├── sha3acc
|   ├── software
|   └── src
├── sha2sim
├── sha2acc
|   ├── software
|   └── src
└── doc
```

<br/>

# Simulation

how to use:

project shoud be used in the [chipyard-shaacc](https://github.com/BriMonzZY/chipyard-shaacc) enviroment.

```shell
git clone https://github.com/BriMonzZY/chipyard-shaacc.git

cd chipyard-shaacc


# This step may take a long time and require more than 3GB of storage space. It is recommended to go online scientifically (if you are in China) and to a location with a better network environment, otherwise initialization may fail.
./init_env.sh # initial chipyard env.
```

<br/>

project codes are in the`chipyard-shaacc/generators/sha-acc` folder.

you can run`build.sh` 、`chiseltest.sh` or `run.sh` scripts to compile or run this project.

```shell
## Enter the directory
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

./build.sh test # compile the test programes

./build.sh clean # execute make clean in sims/verilator、sims/vcs and sims/xcelium to clean the files which generated by compile


./run.sh # run the file after compile


## run units test
./chiseltest.sh
./chiseltest.sh sha3
./chiseltest.sh sha2
```

<br/>

<br/>

# FPGA Prototype

### generate FPGA bitstream file

Default generation for Xilinx VCU108 Bitstream file of the board

First, check if there are any environment variables for the Vivado tool in the script file. Please confirm that the Vivado tool is installed.

```shell
./fpgabuild.sh -h # help
```

After the successful generation of the bitstream, you can find the bitstream file and Vivado checkpoint file in ` fpga/generated src/<long name>/obj `, where you can view information such as resource utilization and timing reports, or use [UltraFast](https://docs.amd.com/r/zh-CN/ug949-vivado-design-methodology) to Insert ILA probes into the process for debugging.

<br/>

<br/>

Physical demonstration video of SHA series hash algorithm hardware Coprocessor based on RISC-V: https://pan.baidu.com/s/1lb5idFS651VYNDrE4HT7Zg?pwd=rq52 (use baidunetdisk)
