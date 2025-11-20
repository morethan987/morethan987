class CourseSelectionException(Exception):
    """Base exception for course selection system"""
    pass


class LoginFailedException(CourseSelectionException):
    """Raised when login fails"""
    pass


class ElementNotFoundException(CourseSelectionException):
    """Raised when required element is not found"""
    pass


class CourseNotAvailableException(CourseSelectionException):
    """Raised when course is not available for selection"""
    pass


class NetworkException(CourseSelectionException):
    """Raised when network issues occur"""
    pass


class ConfigurationException(CourseSelectionException):
    """Raised when configuration is invalid"""
    pass
