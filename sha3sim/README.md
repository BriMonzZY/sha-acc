# SHA-3的C语言实现

提供一个简单的基于[FIPS_202](http://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf)标准的SHA-3的C语言实现，可以计算文件或字符串的SHA-3哈希值。



编译sha3sum：

```shell
make sha3sum
```

在build文件夹中生成 sha3sum 可执行文件

用法：

sha3sum 256|384|512 [OPTION]... [FILE]...

-f 输入是文件，如果输入是字符串则不需要 -f 选项

例如：

```shell
./sha3sum 256 -f empty.txt # 计算文件的SHA3-256
./sha3sum 512 abcdef # 计算字符串的SHA3-512
```

```shell
sha3sum -a 256 empty.txt # 用Linux自带的sha3sum指令计算文件的SHA3-256来验证我们的实现
```



这个项目用来支持 SHA ACC 项目的实现。

