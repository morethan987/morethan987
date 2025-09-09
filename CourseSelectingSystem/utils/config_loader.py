import yaml
import os
from typing import Dict, Any
from core.models.config import SystemConfig, LoginConfig, WebDriverConfig, MonitoringConfig, NotificationConfig
from core.exceptions.exceptions import ConfigurationException


class ConfigLoader:
    """Utility class for loading and parsing configuration"""

    @staticmethod
    def load_from_file(config_path: str) -> SystemConfig:
        """
        Load configuration from YAML file

        Args:
            config_path: Path to configuration file

        Returns:
            SystemConfig: Parsed configuration object

        Raises:
            ConfigurationException: If configuration is invalid
        """
        try:
            if not os.path.exists(config_path):
                raise ConfigurationException(f"Configuration file not found: {config_path}")

            with open(config_path, 'r', encoding='utf-8') as f:
                config_data = yaml.safe_load(f)

            return ConfigLoader._parse_config(config_data)

        except yaml.YAMLError as e:
            raise ConfigurationException(f"Invalid YAML format: {e}")
        except Exception as e:
            raise ConfigurationException(f"Error loading configuration: {e}")

    @staticmethod
    def _parse_config(config_data: Dict[str, Any]) -> SystemConfig:
        """Parse configuration dictionary into SystemConfig object"""
        try:
            # Parse login configuration
            login_data = config_data.get('login', {})
            login_config = LoginConfig(
                url=login_data.get('url', ''),
                username=ConfigLoader._expand_env_vars(login_data.get('username', '')),
                password=ConfigLoader._expand_env_vars(login_data.get('password', '')),
                timeout=login_data.get('timeout', 15)
            )

            # Parse webdriver configuration
            webdriver_data = config_data.get('webdriver', {})
            webdriver_config = WebDriverConfig(
                driver_type=webdriver_data.get('type', 'chrome'),
                headless=webdriver_data.get('headless', False),
                timeout=webdriver_data.get('timeout', 10),
                additional_options=webdriver_data.get('options', [])
            )

            # Parse monitoring configuration
            monitoring_data = config_data.get('monitoring', {})
            monitoring_config = MonitoringConfig(
                check_interval=monitoring_data.get('check_interval', 8),
                max_retries=monitoring_data.get('max_retries', 10),
                courses=monitoring_data.get('courses', []),
                course_teacher_pairs=monitoring_data.get('course_teacher_pairs', [])
            )

            # Parse notification configuration
            notification_data = config_data.get('notifications', {})
            notification_config = NotificationConfig(
                console=notification_data.get('console', True),
                email_enabled=notification_data.get('email', {}).get('enabled', False),
                webhook_url=notification_data.get('webhook_url')
            )

            return SystemConfig(
                login=login_config,
                webdriver=webdriver_config,
                monitoring=monitoring_config,
                notifications=notification_config
            )

        except Exception as e:
            raise ConfigurationException(f"Error parsing configuration: {e}")

    @staticmethod
    def _expand_env_vars(value: str) -> str:
        """Expand environment variables in configuration values"""
        if isinstance(value, str) and value.startswith('${') and value.endswith('}'):
            env_var = value[2:-1]
            return os.getenv(env_var, value)
        return value
