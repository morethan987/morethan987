"""
AutoClicks - 自动点击脚本集合主程序

提供友好的用户界面，方便选择和运行不同的自动化脚本。
"""

import os
import subprocess
import sys
from pathlib import Path


class AutoClicksManager:
    """AutoClicks管理器，负责模块选择和执行"""

    def __init__(self):
        self.modules = {
            "1": {
                "name": "CourseSelectingSystem",
                "description": "自动选课系统",
                "path": "./CourseSelectingSystem/main.py",
                "readme": "./CourseSelectingSystem/README.md",
            },
            "2": {
                "name": "QiangGuoXianFeng",
                "description": "强国先锋自动播放程序",
                "path": "./qiangguoxianfeng/main.py",
                "readme": "./qiangguoxianfeng/PROJECT_STRUCTURE.md",
            },
        }

    def clear_screen(self):
        """清屏"""
        os.system("cls" if os.name == "nt" else "clear")

    def display_banner(self):
        """显示程序横幅"""
        print("=" * 60)
        print("🤖 AutoClicks - 自动点击脚本集合")
        print("=" * 60)
        print("🎯 管理和运行各种自动化脚本")
        print("=" * 60)
        print()

    def display_modules(self):
        """显示所有可用模块"""
        print("📦 可用模块列表:")
        print("-" * 40)

        for key, module in self.modules.items():
            status = "✅ 可用" if os.path.exists(module["path"]) else "❌ 缺失"
            print(f"{key}. {module['name']}")
            print(f"   📋 {module['description']}")
            print(f"   📁 路径: {module['path']}")
            print(f"   📍 状态: {status}")
            print()

        print("0. 🚪 退出程序")
        print()

    def display_module_info(self, module_key):
        """显示模块详细信息"""
        if module_key not in self.modules:
            print("❌ 无效的模块选择")
            return

        module = self.modules[module_key]
        print(f"\n🔍 模块详情: {module['name']}")
        print("-" * 40)
        print(f"📋 描述: {module['description']}")
        print(f"📁 执行路径: {module['path']}")

        # 显示README文件内容
        readme_path = module["readme"]
        if os.path.exists(readme_path):
            print("\n📖 README 内容:")
            try:
                with open(readme_path, "r", encoding="utf-8") as f:
                    content = f.read()
                    # 只显示前500个字符，避免内容过长
                    if len(content) > 500:
                        content = content[:500] + "\n...(内容过长，已截断)"
                    print(content)
            except Exception as e:
                print(f"⚠️  读取README文件时出错: {e}")
        else:
            print("⚠️  未找到README文件")

        print("-" * 40)

    def run_module(self, module_key):
        """运行选定的模块"""
        if module_key not in self.modules:
            print("❌ 无效的模块选择")
            return False

        module = self.modules[module_key]
        module_path = module["path"]

        if not os.path.exists(module_path):
            print(f"❌ 模块文件不存在: {module_path}")
            return False

        print(f"\n🚀 正在启动 {module['name']}...")
        print("按 Ctrl+C 可以中止运行")
        print("-" * 40)

        try:
            # 使用subprocess运行模块，保持当前环境
            # 使用module_path.dirname作为工作目录
            work_dir = str(Path(module_path).parent)

            result = subprocess.run(
                [sys.executable, module_path], cwd=work_dir, check=True, text=True
            )
            print("\n✅ 模块执行完成")
            return True

        except subprocess.CalledProcessError as e:
            print(f"\n❌ 模块执行失败，返回码: {e.returncode}")
            if e.stdout:
                print(f"输出: {e.stdout}")
            return False
        except KeyboardInterrupt:
            print("\n⚠️  用户中止了模块执行")
            return False
        except Exception as e:
            print(f"\n❌ 执行过程中出现错误: {e}")
            return False

    def handle_user_choice(self, choice):
        """处理用户选择"""
        if choice == "0":
            print("👋 感谢使用AutoClicks！再见！")
            return False

        elif choice in self.modules:
            # 显示模块信息
            self.display_module_info(choice)

            # 询问是否要运行
            run_choice = input("\n🎯 是否要运行此模块? (y/n): ").lower().strip()

            if run_choice == "y" or run_choice == "yes":
                self.run_module(choice)
            else:
                print("🔙 返回主菜单")

        else:
            print("❌ 无效的选择，请重新输入")

        return True

    def show_help(self):
        """显示帮助信息"""
        print("\n📖 使用说明:")
        print("-" * 30)
        print("• 输入数字选择要运行的模块")
        print("• 输入 '0' 退出程序")
        print("• 选择模块后可以查看详细信息")
        print("• 确认后即可运行对应的自动化脚本")
        print("• 按 Ctrl+C 可以中止正在运行的脚本")
        print("-" * 30)
        print()


def main():
    """主函数"""
    manager = AutoClicksManager()

    while True:
        manager.clear_screen()
        manager.display_banner()
        manager.display_modules()

        # 获取用户输入
        choice = input("👉 请选择模块 (输入数字): ").strip()

        # 处理特殊命令
        if choice.lower() in ["help", "h", "?"]:
            manager.show_help()
            input("按回车键继续...")
            continue

        # 处理用户选择
        if not manager.handle_user_choice(choice):
            break

        # 询问是否继续
        continue_choice = input("\n🔄 是否继续使用其他模块? (y/n): ").lower().strip()
        if continue_choice not in ["y", "yes"]:
            print("👋 感谢使用AutoClicks！再见！")
            break


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  程序被用户中断")
    except Exception as e:
        print(f"\n❌ 程序出现未预期的错误: {e}")
        import traceback

        traceback.print_exc()
    finally:
        print("\n程序结束")
