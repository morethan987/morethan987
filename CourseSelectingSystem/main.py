"""
选课系统主入口文件

重构后的版本支持依赖注入，用户可以：
1. 使用默认实现（与原始系统相同）
2. 实现自定义的监控和处理逻辑
3. 直接使用底层服务组装
"""

import argparse
import sys
import os
import signal
from pathlib import Path

# Add the project root to Python path
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from core.course_selection_system import CourseSelectionSystem
from core.implementations.default_course_monitor import DefaultCourseMonitor
from core.implementations.default_course_handler import DefaultCourseHandler
from utils.config_loader import ConfigLoader
from utils.logging_config import setup_logging
from utils.chore import display_config_info, parse_arguments, check_environment
from core.exceptions.exceptions import ConfigurationException
import logging


def main():
    """主函数：使用默认实现启动选课系统"""
    args = parse_arguments()

    # Setup logging
    setup_logging(args.log_level, args.log_dir)
    logger = logging.getLogger(__name__)

    # Check environment variables
    if not check_environment():
        sys.exit(1)

    try:
        # Load configuration
        print(f"📄 Loading configuration from: {args.config}")
        config = ConfigLoader.load_from_file(args.config)

        # Display configuration information
        display_config_info(config)

        # Create default implementations
        print("\n🔧 Using default monitoring and handling implementations")
        print("   💡 Tip: See examples/basic_usage_example.py for custom implementations")
        monitor = DefaultCourseMonitor()
        handler = DefaultCourseHandler()

        # Create system with dependency injection
        system = CourseSelectionSystem(config, monitor, handler)

        # Add signal handler for graceful shutdown
        def signal_handler(signum, frame):
            print("\n🛑 Received shutdown signal, stopping system...")
            system.stop()
            sys.exit(0)

        signal.signal(signal.SIGINT, signal_handler)
        signal.signal(signal.SIGTERM, signal_handler)

        # Start the system
        print("\n🚀 Starting Course Selection System (Refactored Version)...")
        logger.info("Starting course selection system with default implementation")
        system.start()

    except ConfigurationException as e:
        print(f"❌ Configuration error: {e}")
        logger.error(f"Configuration error: {e}")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n🛑 System stopped by user")
        logger.info("System stopped by user")
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        logger.exception("Unexpected error in main")
        sys.exit(1)


if __name__ == "__main__":
    main()
