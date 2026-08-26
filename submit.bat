@echo off
chcp 65001 >nul 2>&1   & REM 切换为 UTF-8 编码，支持中文输入
setlocal enabledelayedexpansion

:: 提示并获取提交信息
set /p "commit_msg=请输入合并信息: "

:: 检查是否为空
if "!commit_msg!"=="" (
    echo 错误：提交信息不能为空。
    pause
    exit /b 1
)

echo 正在执行 git pull ...
git pull
if errorlevel 1 (
    echo 错误：git pull 失败，请检查网络或解决冲突。
    pause
    exit /b 1
)

echo 正在执行 git add ...
git add .
echo 正在执行 git commit -m "!commit_msg!" ...
git commit -m "!commit_msg!"
if errorlevel 1 (
    echo 错误：git commit 失败，可能没有变更需要提交。
    pause
    exit /b 1
)

echo 正在执行 git push ...
git push
if errorlevel 1 (
    echo 错误：git push 失败，请检查网络或权限。
    pause
    exit /b 1
)

echo 所有操作成功完成！
pause
