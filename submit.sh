#!/bin/bash

# 提示并获取提交信息
read -p "请输入合并信息: " commit_msg

# 检查是否为空
if [ -z "$commit_msg" ]; then
    echo "错误：提交信息不能为空。"
    exit 1
fi

echo "正在执行 git pull ..."
git pull
if [ $? -ne 0 ]; then
    echo "错误：git pull 失败，请检查网络或解决冲突。"
    exit 1
fi

echo "正在执行 git add ..."
git add .
echo "正在执行 git commit -m \"$commit_msg\" ..."
git commit -m "$commit_msg"
if [ $? -ne 0 ]; then
    echo "错误：git commit 失败，可能没有变更需要提交。"
    exit 1
fi

echo "正在执行 git push ..."
git push
if [ $? -ne 0 ]; then
    echo "错误：git push 失败，请检查网络或权限。"
    exit 1
fi

echo "所有操作成功完成！"
