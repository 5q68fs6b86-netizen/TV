#!/bin/bash

# 确保 apt-get 在非交互模式下运行
export DEBIAN_FRONTEND=noninteractive

# 更新包列表并安装必要的工具
# aapt2 需要 libc6:i386, lib32stdc++6, lib32z1
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install -y wget unzip lib32stdc++6 lib32z1

# 设置安卓命令行工具的版本和下载链接
ANDROID_CMDLINE_TOOLS_VERSION="11076708" # 您可以在安卓开发者官网找到最新版本
ANDROID_CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip"

# 创建安卓 SDK 的目录
SDK_DIR="$HOME/android-sdk"
mkdir -p "$SDK_DIR"

# 下载并解压命令行工具
wget -q "$ANDROID_CMDLINE_TOOLS_URL" -O cmdline-tools.zip
unzip -q cmdline-tools.zip -d "$SDK_DIR/cmdline-tools"
# 将解压后的文件夹重命名为 'latest' 以便管理
mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
rm cmdline-tools.zip

# 设置环境变量到 .bashrc，以便在新的终端会话中也可用
# 注意：JAVA_HOME 是由 devcontainer feature 自动设置的
echo '' >> ~/.bashrc
echo '# Android SDK' >> ~/.bashrc
echo "export ANDROID_HOME=$SDK_DIR" >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc

# 让当前会话也能立即使用这些环境变量
export ANDROID_HOME=$SDK_DIR
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# 使用 sdkmanager 接受许可证并安装必要的包
# 'yes' 命令会自动对所有许可证提示回答 'y'
yes | sdkmanager --licenses > /dev/null
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# 清理 apt 缓存
sudo apt-get clean

echo "Android SDK with Java 17 setup complete."