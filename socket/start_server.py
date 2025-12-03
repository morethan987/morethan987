#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
聊天服务器快速启动脚本
提供更友好的启动界面和配置选项
"""

import json
import os
import socket
import sys
import threading

# 导入原始服务器模块
try:
    from server import broadcast, clients, get_ip_address, handle_client, send_user_list
except ImportError:
    print("错误: 无法找到 server.py 文件！")
    print("请确保 start_server.py 和 server.py 在同一目录下")
    sys.exit(1)


def print_banner():
    """显示启动横幅"""
    banner = """
╔══════════════════════════════════════════════╗
║            Python 聊天室服务器               ║
║                v3.0 增强版                   ║
╠══════════════════════════════════════════════╣
║  功能特性:                                   ║
║  • 支持群聊和私聊                            ║
║  • 多用户同时在线                            ║
║  • 跨设备连接支持                            ║
║  • 实时用户列表                              ║
║  • 友好的UI界面                              ║
╚══════════════════════════════════════════════╝
"""
    print(banner)


def get_config():
    """获取服务器配置"""
    print("请配置服务器参数:")
    print("=" * 50)

    # 端口配置
    while True:
        try:
            port_input = input("请输入监听端口 (默认: 8888): ").strip()
            if not port_input:
                port = 8888
                break
            port = int(port_input)
            if 1024 <= port <= 65535:
                break
            else:
                print("端口号应该在 1024-65535 范围内")
        except ValueError:
            print("请输入有效的端口号")

    # IP配置
    print("\n网络配置:")
    print("1. 监听所有网络接口 (推荐) - 支持局域网和热点连接")
    print("2. 仅监听本地 - 只允许本机连接")

    while True:
        choice = input("请选择 (1/2, 默认: 1): ").strip()
        if not choice or choice == "1":
            bind_ip = "0.0.0.0"
            break
        elif choice == "2":
            bind_ip = "127.0.0.1"
            break
        else:
            print("请输入 1 或 2")

    # 最大连接数
    while True:
        try:
            max_conn_input = input("最大连接数 (默认: 50): ").strip()
            if not max_conn_input:
                max_connections = 50
                break
            max_connections = int(max_conn_input)
            if 1 <= max_connections <= 1000:
                break
            else:
                print("最大连接数应该在 1-1000 范围内")
        except ValueError:
            print("请输入有效的数字")

    return {"bind_ip": bind_ip, "port": port, "max_connections": max_connections}


def check_port_available(port):
    """检查端口是否可用"""
    try:
        test_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        test_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        test_socket.bind(("0.0.0.0", port))
        test_socket.close()
        return True
    except OSError:
        return False


def display_connection_info(bind_ip, port):
    """显示连接信息"""
    local_ip = get_ip_address()

    print("\n" + "=" * 60)
    print("🚀 服务器启动成功！")
    print("=" * 60)
    print(f"📡 监听端口: {port}")
    print(f"🌐 绑定地址: {bind_ip}")

    if bind_ip == "0.0.0.0":
        print(f"🏠 本机IP: {local_ip}")
        print(f"📱 手机热点用户请连接: {local_ip}")
        print(f"💻 局域网用户请连接: {local_ip}")
    else:
        print(f"🏠 仅本机访问: 127.0.0.1")

    print("\n客户端连接方式:")
    print("=" * 30)

    if bind_ip == "0.0.0.0":
        print("简单版客户端:")
        print(f"  python client.py {local_ip}")
        print("改进UI版客户端 (推荐):")
        print(f"  python client_ui.py {local_ip}")
        print("\n本机用户:")
        print("  python client.py")
        print("  python client_ui.py")
    else:
        print("本机连接:")
        print("  python client.py")
        print("  python client_ui.py")

    print("\n控制命令:")
    print("  Ctrl+C - 停止服务器")
    print("=" * 60)


def display_statistics():
    """显示服务器统计信息"""
    online_count = len(clients)
    if online_count > 0:
        print(f"\n📊 在线用户数: {online_count}")
        nicknames = [info["nickname"] for info in clients.values()]
        print(f"👥 在线用户: {', '.join(nicknames)}")
    else:
        print("\n📊 当前无用户在线")


def start_server():
    """启动服务器"""
    print_banner()

    # 获取配置
    config = get_config()
    bind_ip = config["bind_ip"]
    port = config["port"]
    max_connections = config["max_connections"]

    # 检查端口是否可用
    print(f"\n🔍 检查端口 {port} 可用性...")
    if not check_port_available(port):
        print(f"❌ 端口 {port} 已被占用！")
        print("请选择其他端口或关闭占用该端口的程序")
        return

    # 创建服务器socket
    try:
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((bind_ip, port))
        server.listen(max_connections)
    except Exception as e:
        print(f"❌ 服务器启动失败: {e}")
        return

    # 显示连接信息
    display_connection_info(bind_ip, port)

    # 启动统计信息显示线程
    def show_stats():
        import time

        while True:
            time.sleep(30)  # 每30秒显示一次统计
            if len(clients) > 0:  # 只有在有用户时才显示
                print(f"\n[{time.strftime('%H:%M:%S')}] ", end="")
                display_statistics()

    stats_thread = threading.Thread(target=show_stats, daemon=True)
    stats_thread.start()

    print(f"\n⏳ 等待用户连接...")

    try:
        while True:
            conn, addr = server.accept()
            # 检查连接数限制
            if len(clients) >= max_connections:
                print(f"⚠️  达到最大连接数限制 ({max_connections})，拒绝新连接: {addr}")
                conn.close()
                continue

            print(f"🔗 新连接来自: {addr[0]}:{addr[1]}")
            threading.Thread(
                target=handle_client, args=(conn, addr), daemon=True
            ).start()

    except KeyboardInterrupt:
        print("\n\n🛑 收到停止信号...")
    except Exception as e:
        print(f"\n❌ 服务器运行错误: {e}")
    finally:
        print("📊 最终统计信息:")
        display_statistics()

        # 通知所有客户端服务器关闭
        if clients:
            print("📢 通知所有客户端服务器即将关闭...")
            broadcast("服务器即将关闭，感谢使用！", None, "system")

        print("🔐 关闭服务器...")
        server.close()
        print("👋 服务器已关闭，再见！")


if __name__ == "__main__":
    try:
        start_server()
    except Exception as e:
        print(f"\n💥 启动脚本出现错误: {e}")
        print("请检查 server.py 文件是否存在且无语法错误")
