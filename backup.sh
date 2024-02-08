#!/bin/bash

cur_date=$(date +%Y-%m-%d-%H-%M)
echo $cur_date
cd ..
tar -cvf - sha-acc | pigz --best -k > sha-acc-$cur_date.tar.gz

