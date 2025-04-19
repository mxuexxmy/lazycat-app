#!/bin/sh

# 切换到当前目录
cd "$(dirname "$0")"
sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories
apk update
apk add python3 py3-pip
export PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple
pip install -r ./requirements.txt
python3 main.py --db /lzcapp/var/todo.db
