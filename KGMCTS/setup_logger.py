"""
日志配置模块：为整个项目提供统一的日志配置
"""

import logging
import sys
from typing import Optional


def setup_logger(name: str,
                level: str = "INFO",
                log_file: Optional[str] = None,
                format_string: Optional[str] = None) -> logging.Logger:
    """
    设置并返回配置好的logger

    Args:
        name: logger名称
        level: 日志级别 ("DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL")
        log_file: 日志文件路径（可选）
        format_string: 自定义格式字符串（可选）

    Returns:
        配置好的logger实例
    """
    logger = logging.getLogger(name)

    # 避免重复添加handler
    if logger.handlers:
        return logger

    # 设置日志级别
    numeric_level = getattr(logging, level.upper(), logging.INFO)
    logger.setLevel(numeric_level)

    # 默认格式
    if format_string is None:
        format_string = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"

    formatter = logging.Formatter(format_string)

    # 控制台处理器
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(numeric_level)
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)

    # 文件处理器（如果指定了文件路径）
    if log_file:
        try:
            file_handler = logging.FileHandler(log_file, encoding='utf-8')
            file_handler.setLevel(numeric_level)
            file_handler.setFormatter(formatter)
            logger.addHandler(file_handler)
        except Exception as e:
            logger.warning(f"Failed to setup file handler for {log_file}: {e}")

    return logger


def set_global_log_level(level: str):
    """
    设置全局日志级别

    Args:
        level: 日志级别 ("DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL")
    """
    numeric_level = getattr(logging, level.upper(), logging.INFO)
    logging.root.setLevel(numeric_level)

    # 更新所有已存在的logger
    for logger_name in logging.Logger.manager.loggerDict:
        logger = logging.getLogger(logger_name)
        logger.setLevel(numeric_level)

        # 更新所有handler的级别
        for handler in logger.handlers:
            handler.setLevel(numeric_level)


def disable_external_loggers():
    """禁用一些外部库的详细日志输出"""
    # 禁用httpx的详细日志
    logging.getLogger("httpx").setLevel(logging.WARNING)

    # 禁用urllib3的详细日志
    logging.getLogger("urllib3").setLevel(logging.WARNING)

    # 禁用openai的详细日志
    logging.getLogger("openai").setLevel(logging.WARNING)


# 项目启动时的默认配置
def configure_project_logging(log_level: str = "INFO",
                            log_file: Optional[str] = None,
                            disable_external: bool = True):
    """
    配置整个项目的日志系统

    Args:
        log_level: 全局日志级别
        log_file: 全局日志文件（可选）
        disable_external: 是否禁用外部库的详细日志
    """
    # 设置全局日志级别
    set_global_log_level(log_level)

    # 禁用外部库日志
    if disable_external:
        disable_external_loggers()

    # 设置根logger的格式
    root_logger = logging.getLogger()

    # 清除现有的handlers
    root_logger.handlers.clear()

    # 重新配置根logger
    formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")

    # 控制台处理器
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(getattr(logging, log_level.upper()))
    console_handler.setFormatter(formatter)
    root_logger.addHandler(console_handler)

    # 文件处理器
    if log_file:
        try:
            file_handler = logging.FileHandler(log_file, encoding='utf-8')
            file_handler.setLevel(getattr(logging, log_level.upper()))
            file_handler.setFormatter(formatter)
            root_logger.addHandler(file_handler)
        except Exception as e:
            print(f"Warning: Failed to setup global log file {log_file}: {e}")
