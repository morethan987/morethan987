import argparse
import os

from core.models.config import SystemConfig


def check_environment():
    """Check if required environment variables are set"""
    required_vars = ["CQU_USERNAME", "CQU_PASSWORD"]
    missing_vars = []

    for var in required_vars:
        if not os.getenv(var):
            missing_vars.append(var)

    if missing_vars:
        print("❌ Missing required environment variables:")
        for var in missing_vars:
            print(f"   - {var}")
        print("\nPlease set these environment variables before running the system.")
        print("Example (Linux/Mac):")
        print("   export CQU_USERNAME='your_username'")
        print("   export CQU_PASSWORD='your_password'")
        print("\nExample (Windows):")
        print("   set CQU_USERNAME=your_username")
        print("   set CQU_PASSWORD=your_password")
        return False

    return True


def display_config_info(config: SystemConfig):
    """Display configuration information"""
    print("✅ Configuration loaded successfully")
    print(f"🎯 Target URL: {config.login.url}")
    print(f"👤 Username: {config.login.username}")
    print(f"🔍 Monitoring interval: {config.monitoring.check_interval} seconds")
    print(f"📚 General courses: {len(config.monitoring.courses)}")
    print(f"👨‍🏫 Course-teacher pairs: {len(config.monitoring.course_teacher_pairs)}")

    # Display courses being monitored
    if config.monitoring.courses:
        print("\n📋 General courses being monitored:")
        for i, course in enumerate(config.monitoring.courses, 1):
            course_name = course.get("name") if isinstance(course, dict) else course
            print(f"   {i}. {course_name}")

    if config.monitoring.course_teacher_pairs:
        print("\n👨‍🏫 Course-teacher pairs being monitored:")
        for i, pair in enumerate(config.monitoring.course_teacher_pairs, 1):
            print(f"   {i}. {pair.get('course_name')} - {pair.get('teacher_name')}")


def parse_arguments():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(
        description="Automated Course Selection System (Refactored Version)",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Environment Variables:
  CQU_USERNAME    Your university username
  CQU_PASSWORD    Your university password

Examples:
  python main.py
  python main.py --config custom_config.yaml
  python main.py --log-level DEBUG

Refactor Notes:
  This version uses dependency injection for monitoring and handling logic.
  See examples/basic_usage_example.py for custom implementations.
        """,
    )

    parser.add_argument(
        "--config",
        "-c",
        default="config/config.yaml",
        help="Path to configuration file (default: config/config.yaml)",
    )

    parser.add_argument(
        "--log-level",
        "-l",
        choices=["DEBUG", "INFO", "WARNING", "ERROR"],
        default="INFO",
        help="Set logging level (default: INFO)",
    )

    parser.add_argument(
        "--log-dir", default="logs", help="Directory for log files (default: logs)"
    )

    return parser.parse_args()
