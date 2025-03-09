import requests
import os
import re
import json
import time
from queue import Queue
import threading

api_key = "app-kRDtsW9d92YBFRGJTLgUubqT"

# 创建一个队列用于存储待处理的图片
image_queue = Queue()
# 用于存储已处理过的文件名
processed_files = set()
# 处理计数器
processed_count = 0
# 计数器锁
count_lock = threading.Lock()

def upload_file(file_path, user):
    upload_url = "http://localhost/v1/files/upload"
    headers = {
        "Authorization": f"Bearer {api_key}",
    }
    
    try:
        with open(file_path, 'rb') as file:
            mime_type = 'image/png' if file_path.endswith('.png') else 'image/jpeg'
            files = {
                'file': (file_path, file, mime_type)
            }
            data = {
                "user": user,
                "type": "image"
            }
            
            response = requests.post(upload_url, headers=headers, files=files, data=data)
            if response.status_code == 201:
                return response.json().get("id")
            else:
                print(f"文件上传失败，状态码: {response.status_code}")
                print(f"响应内容: {response.text}")
                return None
    except Exception as e:
        print(f"发生错误: {str(e)}")
        return None

def run_workflow(file_id, user, response_mode="blocking"):
    workflow_url = "http://localhost/v1/workflows/run"
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json"
    }

    data = {
        "inputs": {},
        "response_mode": response_mode,
        "user": user,
        "files": [
            {
                "type": "image",
                "transfer_method": "local_file",
                "upload_file_id": file_id
            }
        ]
    }

    try:
        response = requests.post(workflow_url, headers=headers, json=data)
        if response.status_code == 200:
            return response.json()
        else:
            print(f"工作流执行失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text}")
            return {"status": "error", "message": f"Failed to execute workflow, status code: {response.status_code}"}
    except Exception as e:
        print(f"发生错误: {str(e)}")
        return {"status": "error", "message": str(e)}

def getLogs():
    log_url = "http://localhost/v1/workflows/logs"
    headers = {
        "Authorization": f"Bearer {api_key}",
    }
    logs = requests.get(log_url,headers=headers)
    return logs

def process_new_files(folder_path):
    """处理新文件并返回新的文件列表"""
    image_files = []
    non_standard_files = []
    counter = 1
    
    for file in os.listdir(folder_path):
        if file.lower().endswith(('.png', '.jpg', '.jpeg')):
            if file not in processed_files:
                # 从文件名中提取日期时间
                match = re.search(r'(\d{8}_\d{6})', file)
                if match:
                    datetime_str = match.group(1)
                    datetime_num = int(datetime_str.replace('_', ''))
                    image_files.append((datetime_num, file))
                else:
                    # 对于非标准文件名，使用计数器作为排序依据
                    non_standard_files.append((counter, file))
                    counter += 1
                processed_files.add(file)
    
    # 处理标准格式文件
    image_files.sort(key=lambda x: x[0])
    
    # 合并两个列表，非标准格式文件按发现顺序排在标准格式文件后面
    return image_files + non_standard_files

def monitor_folder(folder_path, interval=1):
    """监控文件夹中的新图片"""
    while True:
        new_files = process_new_files(folder_path)
        for _, file_name in new_files:
            # 将新文件加入队列
            full_path = os.path.join(folder_path, file_name)
            image_queue.put(full_path)
        time.sleep(interval)  # 等待指定的时间间隔

def process_queue(user):
    """处理队列中的图片"""
    global processed_count
    while True:
        if not image_queue.empty():
            file_path = image_queue.get()
            # 更新计数器
            with count_lock:
                processed_count += 1
                current_count = processed_count
            
            # 上传文件
            file_id = upload_file(file_path, user)
            if file_id:
                # 文件上传成功，继续运行工作流
                result = run_workflow(file_id, user)
                print(f"题目答案{current_count}: {result['data']['outputs']['output']}")
            else:
                print(f"文件 {file_path} 上传失败，无法执行工作流")
            image_queue.task_done()
        time.sleep(0.5)  # 避免过度占用CPU

def run(base_file_path, user):
    """启动监控和处理线程"""
    # 创建并启动文件监控线程
    monitor_thread = threading.Thread(
        target=monitor_folder, 
        args=(base_file_path,),
        daemon=True
    )
    monitor_thread.start()

    # 创建并启动队列处理线程
    process_thread = threading.Thread(
        target=process_queue, 
        args=(user,),
        daemon=True
    )
    process_thread.start()

    try:
        # 保持主线程运行
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n程序终止")

# 使用示例
if __name__ == "__main__":
    base_file_path = "D:/QQ下载/"
    user = "morethan"
    run(base_file_path, user)