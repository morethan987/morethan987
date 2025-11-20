import time
from selenium.common.exceptions import TimeoutException
from core.models.config import LoginConfig
from core.exceptions.exceptions import LoginFailedException, ElementNotFoundException
import logging


class LoginService:
    """Service for handling user login"""

    def __init__(self, config: LoginConfig):
        self.config = config
        self.logger = logging.getLogger(__name__)

    def login(self, sb) -> bool:
        """
        Perform login operation

        Args:
            sb: SeleniumBase driver instance

        Returns:
            bool: True if login successful, False otherwise

        Raises:
            LoginFailedException: If login fails
        """
        try:
            self.logger.info("Starting login process")
            sb.open(self.config.url)
            time.sleep(5)

            # Input username
            sb.type('input[name="username"]', self.config.username)
            self.logger.debug("Username entered")

            # Input password
            sb.type('input[type="password"]', self.config.password)
            self.logger.debug("Password entered")

            # Click login button
            sb.click('button.login-button')
            self.logger.debug("Login button clicked")

            # Wait for successful login (main page element appears)
            sb.wait_for_element('div.table-title', timeout=self.config.timeout)
            self.logger.info("Login successful")

            return True

        except TimeoutException as e:
            error_msg = f"Login timeout after {self.config.timeout} seconds"
            self.logger.error(error_msg)
            raise LoginFailedException(error_msg) from e
        except Exception as e:
            error_msg = f"Login failed: {str(e)}"
            self.logger.error(error_msg)
            raise LoginFailedException(error_msg) from e
