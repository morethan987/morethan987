import json
import socket
import threading

clients = {}  # {conn: {'addr': addr, 'nickname': str}}


def get_ip_address():
    """获取本机IP地址"""
    try:
        # 连接到外部地址获取本机IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"


def broadcast(msg, sender_conn, msg_type="group", target_nickname=None):
    """广播消息"""
    # 处理系统消息
    if sender_conn is None:
        sender_name = "系统"
    elif sender_conn in clients:
        sender_name = clients[sender_conn]["nickname"]
    else:
        sender_name = "系统"

    msg_data = {
        "type": msg_type,
        "sender": sender_name,
        "message": msg,
        "target": target_nickname,
    }
    msg_json = json.dumps(msg_data, ensure_ascii=False).encode("utf-8")

    for conn, client_info in clients.items():
        if msg_type == "private":
            # 私聊消息发送给发送者和目标用户
            if client_info["nickname"] == target_nickname or conn == sender_conn:
                try:
                    conn.send(msg_json)
                except Exception:
                    pass
        elif msg_type == "system":
            # 系统消息发送给所有人
            try:
                conn.send(msg_json)
            except Exception:
                pass
        else:
            # 群聊发送给除发送者外的所有人
            if conn != sender_conn:
                try:
                    conn.send(msg_json)
                except Exception:
                    pass


def send_user_list():
    """发送在线用户列表给所有客户端"""
    nicknames = [info["nickname"] for info in clients.values()]
    user_list_msg = {"type": "user_list", "users": nicknames}
    msg_json = json.dumps(user_list_msg, ensure_ascii=False).encode("utf-8")

    for conn in clients.keys():
        try:
            conn.send(msg_json)
        except Exception:
            pass


def handle_client(conn, addr):
    print(f"[+] 用户连接: {addr}")

    # 等待客户端发送昵称
    try:
        nickname_data = conn.recv(1024).decode("utf-8")
        nickname_info = json.loads(nickname_data)
        nickname = nickname_info.get("nickname", f"用户_{addr[1]}")
    except Exception:
        nickname = f"用户_{addr[1]}"

    clients[conn] = {"addr": addr, "nickname": nickname}
    print(f"[+] {nickname} 上线")

    # 发送欢迎消息和用户列表
    welcome_msg = f"欢迎 {nickname} 加入聊天室！"
    broadcast(welcome_msg, conn, "group")
    send_user_list()

    while True:
        try:
            data = conn.recv(1024)
            if not data:
                break

            try:
                msg_info = json.loads(data.decode("utf-8"))
                msg_type = msg_info.get("type", "group")
                message = msg_info.get("message", "")
                target = msg_info.get("target", "")

                if msg_type == "private" and target:
                    # 私聊消息
                    # 检查目标用户是否在线
                    target_user_online = False
                    for info in clients.values():
                        if info["nickname"] == target:
                            target_user_online = True
                            break

                    if target_user_online:
                        broadcast(
                            message,
                            conn,
                            "private",
                            target,
                        )
                    else:
                        error_msg = {
                            "type": "error",
                            "message": f"用户 {target} 不在线",
                        }
                        conn.send(
                            json.dumps(error_msg, ensure_ascii=False).encode("utf-8")
                        )
                else:
                    # 群聊消息
                    broadcast(message, conn, "group")

            except json.JSONDecodeError:
                # 兼容旧版本的纯文本消息
                broadcast(data.decode("utf-8"), conn, "group")

        except Exception as e:
            print(f"处理客户端消息时出错: {e}")
            break

    # 用户离线
    nickname = clients[conn]["nickname"]
    print(f"[-] {nickname} 离线")
    del clients[conn]
    conn.close()

    # 广播离线消息和更新用户列表
    # 使用特殊值表示系统消息，避免KeyError
    broadcast(f"{nickname} 离开了聊天室", None, "system")
    send_user_list()


if __name__ == "__main__":
    server_ip = "0.0.0.0"  # 监听所有网络接口，支持手机热点连接
    port = 8888

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((server_ip, port))
    server.listen()

    local_ip = get_ip_address()
    print("[*] 聊天服务器启动")
    print(f"[*] 监听端口: {port}")
    print(f"[*] 本机IP: {local_ip}")
    print(f"[*] 手机热点用户请连接IP: {local_ip}")
    print("[*] 等待用户连接...")

    try:
        while True:
            conn, addr = server.accept()
            threading.Thread(
                target=handle_client, args=(conn, addr), daemon=True
            ).start()
    except KeyboardInterrupt:
        print("\n[*] 服务器关闭")
        server.close()
