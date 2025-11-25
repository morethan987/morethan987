"""装饰器工具模块"""

from functools import wraps
from time import sleep


def retry_on_failure(max_attempts=3, delay=2, exceptions=(Exception,)):
    """重试装饰器

    Args:
        max_attempts: 最大尝试次数
        delay: 重试间隔（秒）
        exceptions: 需要捕获的异常类型元组
    """

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            last_exception: Exception | None = None
            for attempt in range(1, max_attempts + 1):
                try:
                    return func(*args, **kwargs)
                except exceptions as e:
                    last_exception = e
                    if attempt < max_attempts:
                        print(
                            f"⚠️  操作失败 (尝试 {attempt}/{max_attempts}): {func.__name__}"
                        )
                        print(f"   错误: {str(e)[:100]}")
                        sleep(delay)
                    else:
                        print(f"❌ 操作最终失败: {func.__name__}")

            assert last_exception is not None
            raise last_exception

        return wrapper

    return decorator
