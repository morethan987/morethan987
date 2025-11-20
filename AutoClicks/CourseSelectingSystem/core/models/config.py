from dataclasses import dataclass
from typing import Any, Dict, List, Optional


@dataclass
class LoginConfig:
    url: str
    username: str
    password: str
    max_retries: int = 5
    timeout: int = 15


@dataclass
class WebDriverConfig:
    driver_type: str = "chrome"
    headless: bool = False
    timeout: int = 15
    user_agent: Optional[str] = None
    additional_options: List[str] = []

    def __post_init__(self):
        if self.additional_options is None:
            self.additional_options = []


@dataclass
class MonitoringConfig:
    check_interval: int = 8
    max_retries: int = 10
    courses: List[Dict[str, Any]] = []
    course_teacher_pairs: List[Dict[str, str]] = []

    def __post_init__(self):
        if self.courses is None:
            self.courses = []
        if self.course_teacher_pairs is None:
            self.course_teacher_pairs = []


@dataclass
class NotificationConfig:
    console: bool = True
    email_enabled: bool = False
    webhook_url: Optional[str] = None


@dataclass
class SystemConfig:
    login: LoginConfig
    webdriver: WebDriverConfig
    monitoring: MonitoringConfig
    notifications: NotificationConfig
