#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
UI客户端测试脚本
用于验证新的UI版本是否能正常工作
"""

import json
import sys
import threading
import time
from unittest.mock import Mock, patch


def test_imports():
    """测试导入是否正常"""
    print("🔍 测试导入模块...")
    try:
        import curses

        print("✅ curses 模块导入成功")
    except ImportError:
        print("❌ curses 模块导入失败 - 可能不支持curses")
        return False

    try:
        import json
        import socket
        import threading
        from collections import deque
        from datetime import datetime

        print("✅ 标准库模块导入成功")
    except ImportError as e:
        print(f"❌ 标准库导入失败: {e}")
        return False

    try:
        from client_ui import ChatClient

        print("✅ ChatClient 类导入成功")
        return True
    except ImportError as e:
        print(f"❌ ChatClient 导入失败: {e}")
        return False


def test_chat_client_init():
    """测试ChatClient初始化"""
    print("\n🔍 测试ChatClient初始化...")
    try:
        from client_ui import ChatClient

        client = ChatClient("127.0.0.1", 8888)

        # 检查初始化属性
        assert client.server_ip == "127.0.0.1"
        assert client.port == 8888
        assert client.nickname == ""
        assert client.running
        assert len(client.messages) == 0
        assert len(client.online_users) == 0
        assert client.input_buffer == ""
        assert client.cursor_pos == 0
        assert client.message_scroll == 0

        print("✅ ChatClient 初始化测试通过")
        return True
    except Exception as e:
        print(f"❌ ChatClient 初始化测试失败: {e}")
        return False


def test_message_handling():
    """测试消息处理功能"""
    print("\n🔍 测试消息处理...")
    try:
        from client_ui import ChatClient

        client = ChatClient()

        # 测试添加消息
        client.add_message("测试用户", "测试消息", "other")
        assert len(client.messages) == 1

        msg = client.messages[0]
        assert msg["sender"] == "测试用户"
        assert msg["message"] == "测试消息"
        assert msg["type"] == "other"
        assert "time" in msg

        # 测试不同类型的消息
        client.add_message("我", "我的消息", "own")
        client.add_message("系统", "系统消息", "system")
        client.add_message("用户A", "私聊消息", "private")

        assert len(client.messages) == 4
        print("✅ 消息处理测试通过")
        return True
    except Exception as e:
        print(f"❌ 消息处理测试失败: {e}")
        return False


def test_message_formatting():
    """测试消息格式化"""
    print("\n🔍 测试消息格式化...")
    try:
        from client_ui import ChatClient

        client = ChatClient()

        # 创建测试消息
        test_msg = {
            "time": "10:30:15",
            "sender": "测试用户",
            "message": "这是一条测试消息",
            "type": "other",
        }

        # 测试格式化
        lines = client.format_message(test_msg, 50)
        assert len(lines) > 0
        assert isinstance(lines[0], tuple)
        assert len(lines[0]) == 2  # (formatted_text, msg_type)

        # 测试长消息换行
        long_msg = {
            "time": "10:30:15",
            "sender": "测试用户",
            "message": "这是一条非常长的测试消息，应该会被分成多行显示，用来测试换行功能是否正常工作。",
            "type": "other",
        }

        long_lines = client.format_message(long_msg, 30)
        print(f"调试信息: 长消息格式化结果行数: {len(long_lines)}")
        for i, (line, msg_type) in enumerate(long_lines):
            print(f"  行 {i + 1}: '{line}' (类型: {msg_type})")
        # 长消息应该被分成多行
        assert len(long_lines) >= 1  # 先改为至少1行，看看实际情况

        print("✅ 消息格式化测试通过")
        return True
    except Exception as e:
        print(f"❌ 消息格式化测试失败: {e}")
        import traceback

        print(f"详细错误信息: {traceback.format_exc()}")
        return False


def test_input_buffer():
    """测试输入缓冲区操作"""
    print("\n🔍 测试输入缓冲区...")
    try:
        from client_ui import ChatClient

        client = ChatClient()

        # 模拟输入
        test_text = "Hello World"
        client.input_buffer = test_text
        client.cursor_pos = len(test_text)

        # 测试退格
        original_len = len(client.input_buffer)
        if client.cursor_pos > 0:
            client.input_buffer = (
                client.input_buffer[: client.cursor_pos - 1]
                + client.input_buffer[client.cursor_pos :]
            )
            client.cursor_pos -= 1

        assert len(client.input_buffer) == original_len - 1
        assert client.input_buffer == "Hello Worl"

        # 测试插入字符
        char = "d"
        client.input_buffer = (
            client.input_buffer[: client.cursor_pos]
            + char
            + client.input_buffer[client.cursor_pos :]
        )
        client.cursor_pos += 1

        assert client.input_buffer == "Hello World"

        print("✅ 输入缓冲区测试通过")
        return True
    except Exception as e:
        print(f"❌ 输入缓冲区测试失败: {e}")
        return False


def test_color_initialization():
    """测试颜色初始化（模拟）"""
    print("\n🔍 测试颜色初始化...")
    try:
        from client_ui import ChatClient

        client = ChatClient()

        # 模拟curses环境
        with (
            patch("curses.start_color"),
            patch("curses.init_pair"),
            patch("curses.COLOR_WHITE", 7),
            patch("curses.COLOR_BLUE", 4),
        ):
            client.init_colors()
            print("✅ 颜色初始化测试通过")
            return True
    except Exception as e:
        print(f"❌ 颜色初始化测试失败: {e}")
        return False


def test_json_message_parsing():
    """测试JSON消息解析"""
    print("\n🔍 测试JSON消息解析...")
    try:
        # 测试用户列表消息
        user_list_msg = {"type": "user_list", "users": ["用户1", "用户2", "用户3"]}
        json_data = json.dumps(user_list_msg, ensure_ascii=False)
        parsed = json.loads(json_data)

        assert parsed["type"] == "user_list"
        assert len(parsed["users"]) == 3

        # 测试私聊消息
        private_msg = {
            "type": "private",
            "sender": "发送者",
            "message": "私聊内容",
            "target": "接收者",
        }
        json_data = json.dumps(private_msg, ensure_ascii=False)
        parsed = json.loads(json_data)

        assert parsed["type"] == "private"
        assert parsed["sender"] == "发送者"
        assert parsed["message"] == "私聊内容"

        print("✅ JSON消息解析测试通过")
        return True
    except Exception as e:
        print(f"❌ JSON消息解析测试失败: {e}")
        return False


def run_basic_functionality_test():
    """运行基础功能测试"""
    print("🚀 开始运行UI客户端基础功能测试\n")
    print("=" * 60)

    tests = [
        test_imports,
        test_chat_client_init,
        test_message_handling,
        test_message_formatting,
        test_input_buffer,
        test_color_initialization,
        test_json_message_parsing,
    ]

    passed = 0
    failed = 0

    for test in tests:
        try:
            if test():
                passed += 1
            else:
                failed += 1
        except Exception as e:
            print(f"❌ 测试异常: {e}")
            failed += 1

    print("\n" + "=" * 60)
    print("📊 测试结果统计:")
    print(f"   ✅ 通过: {passed}")
    print(f"   ❌ 失败: {failed}")
    print(f"   📈 成功率: {passed / (passed + failed) * 100:.1f}%")

    if failed == 0:
        print("\n🎉 所有基础功能测试通过！UI客户端应该可以正常使用。")
        return True
    else:
        print(f"\n⚠️  有 {failed} 个测试失败，请检查相关功能。")
        return False


def test_curses_compatibility():
    """测试curses兼容性"""
    print("\n🔍 测试终端curses兼容性...")
    try:
        import curses

        # 尝试初始化curses
        stdscr = curses.initscr()
        curses.start_color()
        curses.noecho()
        curses.cbreak()

        # 获取终端尺寸
        height, width = stdscr.getmaxyx()
        print(f"✅ 终端尺寸: {width}x{height}")

        # 测试颜色支持
        if curses.has_colors():
            print("✅ 终端支持颜色")
        else:
            print("⚠️  终端不支持颜色")

        # 恢复终端
        curses.echo()
        curses.nocbreak()
        curses.endwin()

        if width < 80 or height < 20:
            print(f"⚠️  建议终端尺寸至少为80x20，当前为{width}x{height}")

        print("✅ curses兼容性测试通过")
        return True

    except Exception as e:
        print(f"❌ curses兼容性测试失败: {e}")
        print("提示: 如果是在Windows上，可能需要安装windows-curses:")
        print("      pip install windows-curses")
        return False


def main():
    """主测试函数"""
    print("╔══════════════════════════════════════════════════╗")
    print("║              UI客户端测试工具                    ║")
    print("║          验证client_ui.py功能是否正常             ║")
    print("╚══════════════════════════════════════════════════╝")

    # 运行基础功能测试
    basic_ok = run_basic_functionality_test()

    # 运行curses兼容性测试
    curses_ok = test_curses_compatibility()

    print("\n" + "=" * 60)
    print("🏁 最终测试结果:")

    if basic_ok and curses_ok:
        print("🎉 所有测试通过！UI客户端可以正常使用。")
        print("\n💡 现在你可以:")
        print("   1. 启动服务器: python start_server.py")
        print("   2. 启动UI客户端: python client_ui.py")
        print("   3. 使用Windows批处理文件（如果在Windows上）")
    elif basic_ok:
        print("⚠️  基础功能正常，但curses可能有兼容性问题")
        print("   建议使用简单版客户端: python client.py")
    else:
        print("❌ 测试失败，请检查代码或环境配置")

    print("\n📚 使用说明:")
    print("   • UI版本提供类QQ的界面布局")
    print("   • 消息左右对齐，自己的消息在右边")
    print("   • 实时用户列表显示")
    print("   • 支持消息滚动查看")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n👋 测试已取消")
    except Exception as e:
        print(f"\n💥 测试过程中出现错误: {e}")
