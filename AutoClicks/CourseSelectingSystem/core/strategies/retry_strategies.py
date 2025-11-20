import time
import random
from abc import ABC, abstractmethod
from typing import Callable, Any
from core.exceptions.exceptions import CourseSelectionException


class RetryStrategy(ABC):
    """Abstract base class for retry strategies"""

    @abstractmethod
    def execute(self, func: Callable, *args, **kwargs) -> Any:
        """Execute function with retry logic"""
        pass


class FixedIntervalRetry(RetryStrategy):
    """Retry with fixed interval between attempts"""

    def __init__(self, max_retries: int = 3, delay: float = 1.0):
        self.max_retries = max_retries
        self.delay = delay

    def execute(self, func: Callable, *args, **kwargs) -> Any:
        last_exception = None

        for attempt in range(self.max_retries + 1):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                last_exception = e
                if attempt < self.max_retries:
                    print(f"Attempt {attempt + 1} failed: {e}, retrying in {self.delay}s...")
                    time.sleep(self.delay)
                else:
                    print(f"All {self.max_retries + 1} attempts failed")

        if last_exception:
            raise last_exception


class ExponentialBackoffRetry(RetryStrategy):
    """Retry with exponential backoff"""

    def __init__(self, max_retries: int = 3, base_delay: float = 1.0, max_delay: float = 60.0):
        self.max_retries = max_retries
        self.base_delay = base_delay
        self.max_delay = max_delay

    def execute(self, func: Callable, *args, **kwargs) -> Any:
        last_exception = None

        for attempt in range(self.max_retries + 1):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                last_exception = e
                if attempt < self.max_retries:
                    delay = min(self.base_delay * (2 ** attempt), self.max_delay)
                    # Add jitter
                    delay *= (0.5 + random.random() * 0.5)
                    print(f"Attempt {attempt + 1} failed: {e}, retrying in {delay:.2f}s...")
                    time.sleep(delay)
                else:
                    print(f"All {self.max_retries + 1} attempts failed")

        if last_exception:
            raise last_exception
