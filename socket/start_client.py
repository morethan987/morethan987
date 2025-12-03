#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
聊天客户端快速启动脚本
提供客户端选择和连接配置
"""

import os
import subprocess
import sys


def print_banner():
    """显示启动横幅"""
    banner = """
╔══════════════════════════════════════════════╗
║            Python 聊天室客户端               ║
║                v3.0 增强版                   ║
╠══════════════════════════════════════════════╣
║  两种客户端可选:                             ║
║  • 简单版 - 基础命令行界面                   ║
║  • UI版 - 类QQ布局，推荐使用                 ║
╚══════════════════════════════════════════════╝
"""
    print(banner)


def check_files():
    """检查必要文件是否存在"""
    files = ["client.py", "client_ui.py"]
    missing = []

    for file in files:
        if not os.path.exists(file):
            missing.append(file)

    if missing:
        print("❌ 缺少必要文件:")
        for file in missing:
            print(f"   • {file}")
        print("\n请确保所有客户端文件在当前目录下")
        return False
    return True


def get_client_choice():
    """选择客户端类型"""
    print("请选择客户端版本:")
    print("=" * 40)
    print("1. 🎨 UI版客户端 (client_ui.py) - 推荐")
    print("   • 类QQ界面布局")
    print("   • 消息左右对齐显示")
    print("   • 实时用户列表")
    print("   • 消息滚动查看")
    print("   • 更好的输入体验")
    print()
    print("2. 📝 简单版客户端 (client.py)")
    print("   • 基础命令行界面")
    print("   • 轻量级，兼容性好")
    print("   • 适合老旧终端")
    print()

    while True:
        choice = input("请选择客户端版本 (1/2, 默认: 1): ").strip()
        if not choice or choice == "1":
            return "client_ui.py"
        elif choice == "2":
            return "client.py"
        else:
            print("请输入 1 或 2")


def get_connection_config():
    """获取连接配置"""
    print("\n请配置连接参数:")
    print("=" * 30)

    # 获取服务器IP
    print("连接选项:")
    print("1. 连接到本机服务器 (127.0.0.1)")
    print("2. 连接到局域网/手机热点服务器")
    print()

    while True:
        choice = input("请选择连接方式 (1/2, 默认: 1): ").strip()
        if not choice or choice == "1":
            server_ip = "127.0.0.1"
            break
        elif choice == "2":
            while True:
                server_ip = input("请输入服务器IP地址: ").strip()
                if server_ip:
                    # 简单的IP格式验证
                    parts = server_ip.split(".")
                    if len(parts) == 4:
                        try:
                            all(0 <= int(part) <= 255 for part in parts)
                            break
                        except ValueError:
                            pass
                    print("请输入有效的IP地址格式 (例如: 192.168.1.100)")
                else:
                    print("IP地址不能为空")
            break
        else:
            print("请输入 1 或 2")

    return server_ip


def display_client_info(client_file, server_ip):
    """显示客户端信息"""
    client_name = "UI增强版" if "ui" in client_file else "简单版"

    print(f"\n🚀 启动 {client_name} 客户端")
    print("=" * 40)
    print(f"📱 客户端: {client_file}")
    print(f"🌐 服务器: {server_ip}")

    if "ui" in client_file:
        print("\n💡 UI版使用说明:")
        print("  • 在底部输入框输入消息")
        print("  • Enter键发送消息")
        print("  • @用户名 发送私聊")
        print("  • PageUp/PageDown 滚动历史消息")
        print("  • /quit 退出程序")
        print("  • Ctrl+C 强制退出")
    else:
        print("\n💡 简单版使用说明:")
        print("  • 直接输入消息发送群聊")
        print("  • @用户名 消息内容 发送私聊")
        print("  • /help 查看帮助")
        print("  • /users 查看在线用户")
        print("  • /quit 退出程序")

    print("=" * 40)
    print("正在启动客户端...\n")


def start_client():
    """启动客户端"""
    print_banner()

    # 检查文件
    if not check_files():
        return

    # 选择客户端
    client_file = get_client_choice()

    # 获取连接配置
    server_ip = get_connection_config()

    # 显示信息
    display_client_info(client_file, server_ip)

    # 构建启动命令
    if server_ip == "127.0.0.1":
        cmd = [sys.executable, client_file]
    else:
        cmd = [sys.executable, client_file, server_ip]

    try:
        # 启动客户端
        subprocess.run(cmd)
    except KeyboardInterrupt:
        print("\n👋 客户端已退出")
    except FileNotFoundError:
        print(f"❌ 无法找到 {client_file} 文件")
    except Exception as e:
        print(f"❌ 启动客户端时出错: {e}")


def main():
    """主函数"""
    try:
        start_client()
    except KeyboardInterrupt:
        print("\n👋 启动脚本已取消")
    except Exception as e:
        print(f"💥 启动脚本出现错误: {e}")


if __name__ == "__main__":
    main()
