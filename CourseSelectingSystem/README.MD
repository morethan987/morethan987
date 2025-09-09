# Course Selection System

An automated, extensible course selection system built with Python and Seleniumbase.

## Features

- 🔄 **Automated Monitoring**: Continuously monitors course availability
- 🎯 **Flexible Selection**: Support for both general courses and teacher-specific selections
- 🏗️ **Modular Architecture**: Clean, extensible design with clear separation of concerns
- ⚙️ **Highly Configurable**: YAML-based configuration with environment variable support
- 🔄 **Retry Logic**: Built-in retry mechanisms with different strategies
- 📧 **Notifications**: Multiple notification channels (console, email, webhook)
- 📊 **Comprehensive Logging**: Detailed logging with multiple levels and file output
- 🛡️ **Error Handling**: Robust exception handling and recovery mechanisms

## Quick Start

### 1. Setup Environment

```bash
# Clone or download the system
# Install dependencies
pip install -r requirements.txt

# Set environment variables (Linux/Mac)
export CQU_USERNAME="your_username"
export CQU_PASSWORD="your_password"

# Set environment variables (Windows)
set CQU_USERNAME=your_username
set CQU_PASSWORD=your_password
```

### 2. Configure Courses

Edit `config/config.yaml`:

```yaml
monitoring:
  courses:
    - name: "Your Course Name"
      strategy: "first_available"

  course_teacher_pairs:
    - course_name: "Your Course"
      teacher_name: "Preferred Teacher"
```

### 3. Run the System

```bash
python main.py
```

## Configuration

### Course Types

**General Courses**: Selects first available option
```yaml
courses:
  - name: "Course Name"
    strategy: "first_available"
```

**Teacher-Specific Courses**: Prefers specific teacher
```yaml
course_teacher_pairs:
  - course_name: "Course Name"
    teacher_name: "Teacher Name"
    priority: 1
```

### Settings

- `check_interval`: Time between checks (seconds)
- `max_retries`: Maximum retry attempts
- `timeout`: Browser timeout settings
- `headless`: Run browser in background

## Architecture

The system follows a layered architecture:

- **Application Layer**: Main entry point and configuration
- **Business Layer**: Core services and business logic
- **Data Layer**: Web interaction and element location
- **Infrastructure Layer**: WebDriver, logging, error handling

## Advanced Usage

### Custom Strategies

Extend selection behavior:

```python
from core.strategies.selection_strategies import SelectionStrategy

class CustomStrategy(SelectionStrategy):
    def select(self, course, available_teachers):
        # Your custom logic here
        return selected_teacher
```

### Adding Notifications

Implement new notification channels:

```python
from core.services.notification_service import NotificationService

class SlackNotificationService(NotificationService):
    def send_notification(self, message, **kwargs):
        # Send to Slack
        pass
```

### Completely Custom

The system is designed for full customization. The main components are `Monitor` and `Handler`. The `Monitor` checks course availability, while the `Handler` manages the selection process.

See `examples` folder for full custom implementations. Generally, you should define your own `Monitor` and `Handler` classes by extending the base classes:

```python
class CustomCourseMonitor(CourseMonitorInterface):
    pass

class CustomCourseHandler(CourseHandlerInterface):
    pass
```

You are recommended to put your custom classes in a separate module, e.g., `custom_monitor.py` in the folder `core/implementations`

When your customization is complete, you can cite them in `main.py`:

```python
monitor = CustomCourseMonitor()
handler = CustomCourseHandler()
```

### Command Line Options

```bash
python main.py --config custom.yaml --log-level DEBUG
python main.py --help  # Show all options
```

## Troubleshooting

### Common Issues

1. **Login Failed**: Check username/password environment variables
2. **Elements Not Found**: Website structure may have changed
3. **Timeout Errors**: Increase timeout values in config
4. **Browser Issues**: Try different browser settings

### Debug Mode

Run with debug logging:
```bash
python main.py --log-level DEBUG
```

Check log files in `logs/` directory for detailed information.

## Support

For issues and questions:
1. Check the logs in `logs/` directory
2. Review configuration in `config/config.yaml`
3. Enable debug logging for more details
