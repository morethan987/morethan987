import curses
import json
import socket
import sys
import threading
import time
from collections import deque
from datetime import datetime


class ChatClient:
    def __init__(self, server_ip="127.0.0.1", port=8888):
        self.server_ip = server_ip
        self.port = port
        self.sock = None
        self.nickname = ""
        self.running = True
        self.messages = deque(maxlen=1000)  # 消息历史
        self.online_users = []
        self.input_buffer = ""
        self.cursor_pos = 0
        self.message_scroll = 0

    def connect_to_server(self):
        """连接到服务器"""
        try:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sock.connect((self.server_ip, self.port))
            return True
        except Exception as e:
            return False

    def send_nickname(self, nickname):
        """发送昵称给服务器"""
        try:
            nickname_data = json.dumps({"nickname": nickname}, ensure_ascii=False)
            self.sock.send(nickname_data.encode("utf-8"))
            self.nickname = nickname
            return True
        except Exception:
            return False

    def recv_msg(self):
        """接收服务器消息"""
        while self.running:
            try:
                data = self.sock.recv(1024)
                if not data:
                    break

                try:
                    msg_info = json.loads(data.decode("utf-8"))
                    msg_type = msg_info.get("type", "group")

                    if msg_type == "user_list":
                        self.online_users = msg_info.get("users", [])
                    elif msg_type == "error":
                        self.add_message("系统", msg_info.get("message", ""), "error")
                    elif msg_type == "private":
                        sender = msg_info.get("sender", "未知用户")
                        message = msg_info.get("message", "")
                        # 只显示接收到的私聊消息，发送的已在本地显示
                        if sender != self.nickname:
                            self.add_message(sender, message, "private")
                    else:
                        sender = msg_info.get("sender", "系统")
                        message = msg_info.get("message", "")
                        # 只显示别人的群聊消息和系统消息，自己的已在本地显示
                        if sender != self.nickname:
                            self.add_message(sender, message, "other")
                        elif sender == "系统":
                            self.add_message(sender, message, "system")

                except json.JSONDecodeError:
                    self.add_message("系统", data.decode("utf-8"), "system")

            except Exception:
                break

        self.running = False

    def add_message(self, sender, message, msg_type):
        """添加消息到消息列表"""
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.messages.append(
            {"time": timestamp, "sender": sender, "message": message, "type": msg_type}
        )

    def send_message(self, message):
        """发送消息"""
        try:
            if message.startswith("@"):
                parts = message.split(" ", 1)
                if len(parts) == 2:
                    target_nickname = parts[0][1:]
                    private_msg = parts[1]
                    request = {
                        "type": "private",
                        "message": private_msg,
                        "target": target_nickname,
                    }
                    self.sock.send(
                        json.dumps(request, ensure_ascii=False).encode("utf-8")
                    )
                    # 立即显示发送的私聊消息
                    self.add_message(
                        f"我 -> {target_nickname}", private_msg, "private_sent"
                    )
                else:
                    self.add_message("系统", "私聊格式: @昵称 消息内容", "error")
            else:
                request = {"type": "group", "message": message}
                self.sock.send(json.dumps(request, ensure_ascii=False).encode("utf-8"))
                # 立即显示发送的群聊消息
                self.add_message("我", message, "own")
        except Exception:
            self.add_message("系统", "发送消息失败", "error")

    def init_colors(self):
        """初始化颜色"""
        curses.start_color()
        curses.init_pair(1, curses.COLOR_WHITE, curses.COLOR_BLUE)  # 标题栏
        curses.init_pair(2, curses.COLOR_GREEN, curses.COLOR_BLACK)  # 自己的消息
        curses.init_pair(3, curses.COLOR_CYAN, curses.COLOR_BLACK)  # 别人的消息
        curses.init_pair(4, curses.COLOR_YELLOW, curses.COLOR_BLACK)  # 私聊消息
        curses.init_pair(5, curses.COLOR_RED, curses.COLOR_BLACK)  # 错误消息
        curses.init_pair(6, curses.COLOR_MAGENTA, curses.COLOR_BLACK)  # 系统消息
        curses.init_pair(7, curses.COLOR_WHITE, curses.COLOR_BLACK)  # 普通文本
        curses.init_pair(8, curses.COLOR_BLACK, curses.COLOR_WHITE)  # 输入框

    def draw_title_bar(self, stdscr):
        """绘制标题栏"""
        height, width = stdscr.getmaxyx()
        title = f" 聊天室 - {self.nickname} "
        stdscr.addstr(0, 0, " " * width, curses.color_pair(1))
        stdscr.addstr(0, (width - len(title)) // 2, title, curses.color_pair(1))

    def draw_user_list(self, stdscr):
        """绘制用户列表"""
        height, width = stdscr.getmaxyx()
        user_width = 20
        start_x = width - user_width

        # 绘制用户列表标题
        stdscr.addstr(1, start_x, "─" * user_width)
        stdscr.addstr(
            2, start_x, f" 在线用户 ({len(self.online_users)}) ".center(user_width)
        )
        stdscr.addstr(3, start_x, "─" * user_width)

        # 绘制用户列表
        for i, user in enumerate(self.online_users[: height - 6]):
            y = 4 + i
            if y >= height - 3:
                break
            display_name = (
                user[: user_width - 2] if len(user) > user_width - 2 else user
            )
            if user == self.nickname:
                stdscr.addstr(
                    y, start_x, f" {display_name}", curses.color_pair(2) | curses.A_BOLD
                )
            else:
                stdscr.addstr(y, start_x, f" {display_name}")

    def format_message(self, msg, max_width):
        """格式化消息，处理长消息换行"""
        lines = []
        message = msg.get("message", "")
        sender = msg.get("sender", "")
        time_str = msg.get("time", "00:00:00")
        msg_type = msg.get("type", "other")

        # 确保最小宽度
        if max_width < 30:
            max_width = 30

        # 根据消息类型调整格式
        if msg_type == "own":
            prefix = f"[{time_str}] 我: "
        elif msg_type == "private":
            prefix = f"[{time_str}] [私聊] {sender}: "
        elif msg_type == "private_sent":
            prefix = f"[{time_str}] {sender}: "
        elif msg_type == "system" or msg_type == "error":
            prefix = f"[{time_str}] [系统] "
        else:
            prefix = f"[{time_str}] {sender}: "

        # 计算消息内容的最大宽度
        content_width = max_width - len(prefix)
        if content_width < 10:
            content_width = 10  # 最小保证
            prefix = f"[{time_str}] "
            content_width = max_width - len(prefix)
            if content_width < 5:
                content_width = 5

        # 处理空消息
        if not message:
            lines.append((prefix, msg_type))
            return lines

        # 分行处理
        words = message.split()
        if not words:
            lines.append((prefix, msg_type))
            return lines

        current_line = ""
        original_prefix = prefix

        for word in words:
            test_line = (
                current_line + word if not current_line else current_line + " " + word
            )
            if len(test_line) <= content_width:
                current_line = test_line
            else:
                if current_line:
                    lines.append((prefix + current_line, msg_type))
                    prefix = " " * len(original_prefix)  # 续行时用空格对齐
                current_line = word

        if current_line:
            lines.append((prefix + current_line, msg_type))

        return lines

    def draw_messages(self, stdscr):
        """绘制消息区域"""
        height, width = stdscr.getmaxyx()
        user_width = 20
        msg_width = width - user_width - 1
        msg_height = height - 5  # 顶部标题栏 + 底部输入框

        # 清空消息区域
        for y in range(1, height - 4):
            stdscr.addstr(y, 0, " " * (msg_width))

        # 格式化所有消息
        formatted_messages = []
        for msg in self.messages:
            formatted_lines = self.format_message(msg, msg_width)
            formatted_messages.extend(formatted_lines)

        # 计算需要显示的消息范围
        total_lines = len(formatted_messages)
        visible_lines = msg_height - 1

        if total_lines <= visible_lines:
            start_idx = 0
        else:
            start_idx = max(0, total_lines - visible_lines - self.message_scroll)

        # 显示消息
        y = 1
        for i in range(start_idx, min(start_idx + visible_lines, total_lines)):
            if y >= height - 4:
                break

            line, msg_type = formatted_messages[i]

            # 根据消息类型选择颜色和对齐方式
            if msg_type == "own":
                # 自己的消息右对齐
                x = max(0, msg_width - len(line))
                stdscr.addstr(y, x, line, curses.color_pair(2))
            elif msg_type == "private":
                stdscr.addstr(y, 0, line, curses.color_pair(4))
            elif msg_type == "private_sent":
                x = max(0, msg_width - len(line))
                stdscr.addstr(y, x, line, curses.color_pair(4))
            elif msg_type == "error":
                stdscr.addstr(y, 0, line, curses.color_pair(5))
            elif msg_type == "system":
                center_x = max(0, (msg_width - len(line)) // 2)
                stdscr.addstr(y, center_x, line, curses.color_pair(6))
            else:
                # 别人的消息左对齐
                stdscr.addstr(y, 0, line, curses.color_pair(3))

            y += 1

    def draw_input_box(self, stdscr):
        """绘制输入框"""
        height, width = stdscr.getmaxyx()
        user_width = 20
        input_width = width - user_width - 1

        # 绘制分隔线
        stdscr.addstr(height - 4, 0, "─" * input_width)
        stdscr.addstr(height - 4, width - user_width, "─" * user_width)

        # 绘制输入提示
        stdscr.addstr(height - 3, 0, "输入消息 (Enter发送, Ctrl+C退出):")

        # 绘制输入框
        input_y = height - 2
        stdscr.addstr(input_y, 0, " " * input_width, curses.color_pair(8))

        # 显示输入内容
        display_text = self.input_buffer
        if len(display_text) > input_width - 2:
            display_text = display_text[-(input_width - 2) :]

        stdscr.addstr(input_y, 1, display_text)

        # 显示光标
        cursor_x = min(len(display_text) + 1, input_width - 1)
        if cursor_x < input_width:
            try:
                stdscr.addstr(
                    input_y, cursor_x, " ", curses.color_pair(8) | curses.A_REVERSE
                )
            except:
                pass

    def draw_help(self, stdscr):
        """绘制帮助信息"""
        height, width = stdscr.getmaxyx()
        help_text = "快捷键: @用户名 私聊 | PageUp/PageDown 滚动消息"
        if len(help_text) < width:
            stdscr.addstr(height - 1, 0, help_text, curses.color_pair(7))

    def handle_input(self, stdscr):
        """处理用户输入"""
        while self.running:
            try:
                key = stdscr.getch()

                if key == ord("\n") or key == ord("\r"):  # Enter键
                    if self.input_buffer.strip():
                        message = self.input_buffer.strip()
                        if message == "/quit":
                            self.running = False
                            break
                        else:
                            self.send_message(message)
                        self.input_buffer = ""
                        self.cursor_pos = 0

                elif key == curses.KEY_BACKSPACE or key == 127 or key == 8:  # 退格键
                    if self.cursor_pos > 0:
                        self.input_buffer = (
                            self.input_buffer[: self.cursor_pos - 1]
                            + self.input_buffer[self.cursor_pos :]
                        )
                        self.cursor_pos -= 1

                elif key == curses.KEY_DC:  # Delete键
                    if self.cursor_pos < len(self.input_buffer):
                        self.input_buffer = (
                            self.input_buffer[: self.cursor_pos]
                            + self.input_buffer[self.cursor_pos + 1 :]
                        )

                elif key == curses.KEY_LEFT:  # 左箭头
                    if self.cursor_pos > 0:
                        self.cursor_pos -= 1

                elif key == curses.KEY_RIGHT:  # 右箭头
                    if self.cursor_pos < len(self.input_buffer):
                        self.cursor_pos += 1

                elif key == curses.KEY_PPAGE:  # Page Up
                    self.message_scroll = min(self.message_scroll + 5, 100)

                elif key == curses.KEY_NPAGE:  # Page Down
                    self.message_scroll = max(self.message_scroll - 5, 0)

                elif key == 3:  # Ctrl+C
                    self.running = False
                    break

                elif 32 <= key <= 126:  # 可打印字符
                    char = chr(key)
                    self.input_buffer = (
                        self.input_buffer[: self.cursor_pos]
                        + char
                        + self.input_buffer[self.cursor_pos :]
                    )
                    self.cursor_pos += 1

            except KeyboardInterrupt:
                self.running = False
                break
            except:
                continue

    def run_ui(self, stdscr):
        """运行主界面"""
        curses.curs_set(0)  # 隐藏光标
        stdscr.timeout(100)  # 设置超时，让界面能定期刷新
        self.init_colors()

        # 启动消息接收线程
        recv_thread = threading.Thread(target=self.recv_msg, daemon=True)
        recv_thread.start()

        # 启动输入处理线程
        input_thread = threading.Thread(
            target=self.handle_input, args=(stdscr,), daemon=True
        )
        input_thread.start()

        # 主界面循环
        while self.running:
            try:
                stdscr.clear()
                self.draw_title_bar(stdscr)
                self.draw_messages(stdscr)
                self.draw_user_list(stdscr)
                self.draw_input_box(stdscr)
                self.draw_help(stdscr)
                stdscr.refresh()
                time.sleep(0.05)  # 降低CPU使用率
            except KeyboardInterrupt:
                break
            except:
                continue

    def start(self):
        """启动客户端"""
        # 连接服务器
        if not self.connect_to_server():
            print("连接服务器失败！")
            return

        # 获取昵称
        nickname = input("请输入您的昵称: ").strip()
        while not nickname or len(nickname) > 20:
            nickname = input("昵称不能为空且不超过20个字符，请重新输入: ").strip()

        if not self.send_nickname(nickname):
            print("发送昵称失败！")
            return

        print(f"连接成功！正在进入聊天室...")
        time.sleep(1)

        try:
            # 启动curses界面
            curses.wrapper(self.run_ui)
        except KeyboardInterrupt:
            pass
        finally:
            self.running = False
            if self.sock:
                self.sock.close()
            print("\n再见！")


def main():
    server_ip = "127.0.0.1"

    # 如果命令行参数提供了服务器IP，则使用它
    if len(sys.argv) > 1:
        server_ip = sys.argv[1]

    client = ChatClient(server_ip)
    client.start()


if __name__ == "__main__":
    main()
