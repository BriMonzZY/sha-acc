# SHA256的C语言实现

提供一个简单的SHA256算法的C语言实现，可以计算文件或字符串的SHA256哈希值。



编译shasum：

```shell
make shasum
```

在build文件夹中生成 shasum 可执行文件

用法：

shasum  [OPTION]... [FILE]...

-f 输入是文件，如果输入是字符串则不需要 -f 选项

例如：

```shell
./shasum -f empty.txt # 计算文件的SHA256
./shasum abcdef # 计算字符串的SHA256
```

```shell
shasum -a 256 empty.txt # 用Linux自带的sha3sum指令计算文件的SHA3-256来验证我们的实现
```



这个项目用来支持 SHA ACC 项目的实现。

