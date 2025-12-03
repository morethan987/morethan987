import json
import socket
import sys
import threading

server_ip = "127.0.0.1"  # 默认本地IP，可修改为服务器IP
port = 8888


def get_nickname():
    """获取用户昵称"""
    while True:
        nickname = input("请输入您的昵称: ").strip()
        if nickname and len(nickname) <= 20:
            return nickname
        print("昵称不能为空且不超过20个字符")


def connect_to_server():
    """连接到服务器"""
    global sock
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((server_ip, port))
        return True
    except Exception as e:
        print(f"连接服务器失败: {e}")
        return False


def send_nickname(nickname):
    """发送昵称给服务器"""
    try:
        nickname_data = json.dumps({"nickname": nickname}, ensure_ascii=False)
        sock.send(nickname_data.encode("utf-8"))
        return True
    except Exception as e:
        print(f"发送昵称失败: {e}")
        return False


def recv_msg():
    """接收服务器消息"""
    while True:
        try:
            data = sock.recv(1024)
            if not data:
                break

            try:
                # 尝试解析JSON格式的消息
                msg_info = json.loads(data.decode("utf-8"))
                msg_type = msg_info.get("type", "group")

                if msg_type == "user_list":
                    # 显示在线用户列表
                    users = msg_info.get("users", [])
                    print("\n===== 在线用户 =====")
                    for i, user in enumerate(users, 1):
                        print(f"{i}. {user}")
                    print("====================")
                elif msg_type == "error":
                    # 显示错误消息
                    print(f"\n[错误] {msg_info.get('message', '')}")
                elif msg_type == "private":
                    # 私聊消息
                    sender = msg_info.get("sender", "未知用户")
                    message = msg_info.get("message", "")
                    print(f"\n[私聊] {sender}: {message}")
                else:
                    # 群聊消息
                    sender = msg_info.get("sender", "系统")
                    message = msg_info.get("message", "")
                    print(f"\n{sender}: {message}")

            except json.JSONDecodeError:
                # 兼容旧版本的纯文本消息
                print(f"\n{data.decode('utf-8')}")

        except Exception:
            break
    print("\n与服务器连接断开")
    sock.close()


def show_help():
    """显示帮助信息"""
    print("\n===== 帮助信息 =====")
    print("1. 群聊: 直接输入消息并发送")
    print("2. 私聊: @昵称 消息内容 (例如: @张三 你好)")
    print("3. 查看在线用户: /users")
    print("4. 显示帮助: /help")
    print("5. 退出: /quit 或 Ctrl+C")
    print("==================")


def main():
    global sock

    # 如果命令行参数提供了服务器IP，则使用它
    if len(sys.argv) > 1:
        global server_ip
        server_ip = sys.argv[1]
        print(f"连接到服务器: {server_ip}")

    # 连接服务器
    if not connect_to_server():
        return

    # 获取并发送昵称
    nickname = get_nickname()
    if not send_nickname(nickname):
        return

    print(f"\n欢迎 {nickname} 加入聊天室！")
    print("连接成功！可以开始聊天了。")
    show_help()

    # 启动接收消息线程
    recv_thread = threading.Thread(target=recv_msg, daemon=True)
    recv_thread.start()

    # 主消息循环
    try:
        while True:
            msg = input("\n> ").strip()

            if not msg:
                continue

            if msg.lower() in ["/quit", "exit", "q"]:
                print("再见！")
                break
            elif msg.lower() in ["/help", "h"]:
                show_help()
                continue
            elif msg.lower() in ["/users", "u"]:
                # 请求用户列表
                request = {"type": "get_users"}
                sock.send(json.dumps(request, ensure_ascii=False).encode("utf-8"))
                continue
            elif msg.startswith("@"):
                # 私聊消息
                parts = msg.split(" ", 1)
                if len(parts) == 2:
                    target_nickname = parts[0][1:]  # 去掉@符号
                    private_msg = parts[1]
                    private_request = {
                        "type": "private",
                        "message": private_msg,
                        "target": target_nickname,
                    }
                    sock.send(
                        json.dumps(private_request, ensure_ascii=False).encode("utf-8")
                    )
                else:
                    print("[错误] 私聊格式: @昵称 消息内容")
            else:
                # 群聊消息
                group_request = {"type": "group", "message": msg}
                sock.send(json.dumps(group_request, ensure_ascii=False).encode("utf-8"))

    except KeyboardInterrupt:
        print("\n\n再见！")
    except Exception as e:
        print(f"\n发送消息时出错: {e}")
    finally:
        if sock:
            sock.close()


if __name__ == "__main__":
    main()
