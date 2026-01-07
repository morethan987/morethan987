# AGENTS.md - AutoClicks_pydoll

> Guidelines for AI agents working in this repository.

## Project Overview

A collection of web automation scripts built with **pydoll-python**, a 100% typed async-native Python library using Chrome DevTools Protocol (CDP) for bot evasion and high-performance scraping.

- **Language**: Python 3.13+
- **Package Manager**: uv (uses `pyproject.toml` and `uv.lock`)
- **Core Dependencies**: pydoll-python (>=2.14.0), requests (>=2.32.5)

---

## Build & Run Commands

```bash
# Install dependencies
uv sync

# Run main entry point
uv run python main.py

# Run course selection automation
uv run python -m CourseSelectingSystem.main
```

### Testing (not yet configured)
```bash
# When adding tests, use pytest:
uv add pytest pytest-asyncio --dev

uv run pytest                                        # Run all tests
uv run pytest tests/test_file.py                     # Single test file
uv run pytest tests/test_file.py::test_func -v       # Single test function
uv run pytest --asyncio-mode=auto                    # Async support (required)
```

### Type Checking
```bash
uv add mypy --dev && uv run mypy .
```

---

## Code Style Guidelines

### Imports
Order: 1) Standard library, 2) Third-party, 3) Local modules (blank line between groups)

```python
import asyncio
import re

import requests
from pydoll.browser.chromium import Chrome
from pydoll.browser.tab import Tab

from .config import USERNAME, PASSWORD
```

### Formatting
- **Indentation**: 4 spaces
- **Line length**: ~88-100 characters (Black defaults)
- **Quotes**: Double quotes
- **Trailing commas**: Use in multi-line structures

### Type Hints
Pydoll is 100% typed. Always annotate function signatures:

```python
async def login(tab: Tab) -> None: ...
async def find_target_courses(tab: Tab) -> list[WebElement]: ...

# Use union for nullable (Python 3.10+)
element: WebElement | None = await tab.find(..., raise_exc=False)
```

### Naming Conventions
| Type | Convention | Example |
|------|------------|---------|
| Functions/methods | snake_case | `find_target_courses` |
| Variables | snake_case | `course_list` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY` |
| Classes | PascalCase | `TargetCourses` |
| Private helpers | _prefixed | `_is_available` |

### Async Patterns
All browser operations are async:

```python
# Context manager for browser lifecycle
async with Chrome(options=options) as browser:
    tab = await browser.start()
    await tab.go_to(url)

# Element finding
element = await tab.find(tag_name="input", name="username", timeout=10)
element = await tab.query("div.class-name button", timeout=10)
elements = await tab.find(tag_name="tr", class_name="row", find_all=True)

# Non-throwing find (returns None if not found)
element = await tab.find(tag_name="span", raise_exc=False)
```

### Error Handling
Use pydoll's retry decorator for resilience:

```python
from pydoll.decorators import retry
from pydoll.exceptions import NetworkError, PageLoadTimeout, WaitElementTimeout

@retry(max_retries=MAX_RETRY, exceptions=[WaitElementTimeout, NetworkError, PageLoadTimeout])
async def main(): ...
```

For optional element checks:
```python
element = await row.find(tag_name="span", class_name="error", raise_exc=False)
if element:
    # handle case
```

### Configuration Pattern
Keep selectors in dedicated config classes:

```python
class LoginSelectors:
    username_input = {"tag_name": "input", "name": "username"}
    password_input = {"tag_name": "input", "type": "password"}
```

---

## Project Structure

```
AutoClicks_pydoll/
├── main.py                    # Main entry point
├── pyproject.toml             # Project config & dependencies
├── CourseSelectingSystem/     # Course selection automation
│   ├── main.py               # Main automation script
│   ├── config.py             # Configuration & selectors
│   └── notification.py       # Server酱 notification service
└── .venv/                    # Virtual environment (uv managed)
```

---

## Important Notes

1. **Credentials**: Never commit credentials. Use `.env` files (in `.gitignore`).

2. **Human-like Behavior**: Use `interval` for realistic typing:
   ```python
   await element.type_text(text, interval=0.05)
   ```

3. **Timeouts**: Always specify timeouts for `find()` and `query()` to avoid hanging.

4. **Chinese Comments**: This codebase uses Chinese comments. Maintain this convention.

5. **Browser Options**: Configure headless mode, proxy, and optimizations in `config.py`.
