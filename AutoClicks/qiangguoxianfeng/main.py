"""强国先锋自动播放程序 - 主入口

重构后的模块化版本
"""

import traceback
from time import sleep

from core import QiangGuoPlayer
from seleniumbase import SB
from utils import detect_available_browser


def print_welcome():
    """打印欢迎信息"""
    print("=" * 60)
    print("🎓 欢迎来到'强国先锋'自动播放程序！(增强版)")
    print("=" * 60)
    print("\n📢 使用说明：")
    print("1. 目前程序只能手动登录")
    print("2. 浏览器窗口打开后会有40秒的时间来登录")
    print("3. 完成登录后请不要有多余的点击操作")
    print("4. 程序将自动播放所有未完成的视频")
    print("5. 新增：智能等待和自动重试机制\n")


def main():
    """主函数"""
    print_welcome()

    # 自动检测可用浏览器
    detected_browser = detect_available_browser()

    start = input("👉 是否开始运行？(yes/no): ").strip().lower()

    if start != "yes":
        print("程序已取消")
        return

    browser_config = {"browser": detected_browser} if detected_browser else {}

    # 使用 SeleniumBase 启动浏览器
    try:
        with SB(
            **browser_config,
            headed=True,
            undetectable=True,
            chromium_arg="proxy-server=127.0.0.1:7890",
        ) as sb:
            print("\n🌐 正在打开浏览器...")
            sb.open("https://cqu.qiangguoxianfeng.com/")

            print("⏳ 等待40秒供您登录，请尽快完成登录...")
            sleep(40)

            # 创建播放器实例并开始播放
            player = QiangGuoPlayer(sb)
            player.play_videos()

            print("\n✅ 程序执行完毕！浏览器将保持打开状态供您检查。")
            input("按回车键关闭程序...")
    except Exception as e:
        print(f"\n❌ 程序执行出错: {e}")
        print("💡 建议：检查网络连接或稍后重试")
        input("按回车键退出...")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  程序被用户中断")
    except Exception as e:
        print(f"\n❌ 程序出错: {e}")
        traceback.print_exc()
        input("按回车键退出...")
