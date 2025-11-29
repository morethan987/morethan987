# pydoll 暂时不能与这个项目兼容，其使用的websocket-client版本过低，等待pydoll更新后再行尝试
import asyncio
import os

from pydoll.browser.chromium import Chrome
from pydoll.browser.options import ChromiumOptions


async def main():
    options = ChromiumOptions()
    options.binary_location = "/usr/bin/google-chrome-stable"
    options.add_argument("--headless=new")
    options.add_argument("--start-maximized")
    options.add_argument("--disable-notifications")
    options.add_argument("--proxy-server=127.0.0.1:7890")

    async with Chrome(options=options) as browser:
        tab = await browser.start()
        await tab.go_to("https://github.com/autoscrape-labs/pydoll")

        star_button = await tab.find(tag_name="button", timeout=5, raise_exc=False)
        if not star_button:
            print("Ops! The button was not found.")
            return

        # await star_button.click()
        await asyncio.sleep(3)

        screenshot_path = os.path.join(os.getcwd(), "pydoll_repo.png")
        await tab.take_screenshot(path=screenshot_path)
        print(f"Screenshot saved to: {screenshot_path}")

        base64_screenshot = await tab.take_screenshot(as_base64=True)

        repo_description_element = await tab.find(class_name="f4.my-3")
        repo_description = await repo_description_element.text
        print(f"Repository description: {repo_description}")


if __name__ == "__main__":
    asyncio.run(main())
