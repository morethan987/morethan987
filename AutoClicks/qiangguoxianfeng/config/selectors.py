"""页面元素选择器配置"""

# 强国先锋页面元素选择器
SELECTORS = {
    # 入口按钮
    "entry_button": '//*[@id="app"]/div/div[2]/div[2]/div[1]/section/div[2]/div/div/div[1]/div',
    # 一级目录项
    "level1_items": '//*[@id="app"]/div/div[2]/div[2]/div[1]/div[2]/ul/li',
    # 二级目录项
    "level2_items": '//*[@id="app"]/div/div[2]/div[2]/div[2]/div[2]/ul/li',
    # 播放列表
    "playlist": '//*[@id="app"]/div/div[2]/div/div[1]/section[2]/div[2]/section/div[2]/div/ul/li',
    # 播放框
    "play_box": '//*[@id="app"]/div/div[2]/div/div[1]/section[1]/div[2]/div/div',
    # 返回按钮
    "back_button": '//*[@id="app"]/div/div[2]/div/div[1]/section[1]/div',
    # 返回到一级目录
    "back_to_level1": '//*[@id="app"]/div/div[2]/div[2]/div[1]/div/span[1]/span[1]',
    # 弹窗按钮
    "popup_buttons": [
        "/html/body/div[5]/div/div/div[3]/button[2]",
        "/html/body/div[5]/div/div/div[3]/button",
        "//button[contains(text(), '确定')]",
        "//button[contains(text(), '关闭')]",
        "//button[contains(@class, 'confirm')]",
    ],
}
