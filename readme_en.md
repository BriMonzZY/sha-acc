# SHA series hash algorithm hardware Coprocessor based on RISC-V

Key Words：SHA、Rocket Chip、Chipyard、RoCC



The repositories related to this project：

[This project](https://github.com/BriMonzZY/sha-acc)

[Chipyard enviroment to run this project](https://github.com/BriMonzZY/chipyard-shaacc) ( fork from chipyard v1.11.0 )

[spike of sha extension for this project](https://github.com/BriMonzZY/riscv-isa-sim-sha-extension) ( fork from riscv-isa-sim v1.1.0 )

[fpga-shell which support VCU108 board](https://github.com/BriMonzZY/rocket-chip-fpga-shells) ( fork from rocket-chip-fpga-shells )







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





```shell
git clone https://github.com/BriMonzZY/chipyard-shaacc.git

cd chipyard-shaacc

./init_env.sh
```



```shell
cd chipyard-shaacc/generators/sha-acc

./build.sh -h # help

./build.sh sha3acc
./build.sh sha3accprint

./build.sh sha2acc
./build.sh sha2accprint

./build.sh test

./build.sh clean

./run.sh

./chiseltest.sh

```



