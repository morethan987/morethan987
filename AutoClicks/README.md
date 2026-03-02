# AutoClicks

这里面收集一些简单的自动化操作脚本，主要基于seleniumbase，推荐使用 uv 进行 python 包管理

偶然之间发现了另外一个貌似更加厉害的 Web 自动化工具，同样是基于 python 的：[pydoll](https://github.com/autoscrape-labs/pydoll)，并且使用 Chrome DevTools Protocol，更加类似于你打开浏览器的开发者工具进行操作，直接驱动浏览器进行操作。当然，只能够驱动支持 Chrome DevTools Protocol 这套协议的浏览器，IE什么的别想了😅

> selenium 系列是基于WebDriver 协议 (W3C 标准)，需要一个 driver 来翻译

总的来说，pydoll 更加“极客”，什么异步并发、事件驱动、克制反机器人系统等等，妥妥的 Hacker 风；相比之下，selenium 本来也不是拿来搞这些的，其一开始是用来检测网页是否有缺陷的，是一个 Web 测试工具。总之，对于抢课系统或者大规模爬虫来说，pydoll肯定是更加合适的方案

2026.03.01 又一个偶然之间，我发现了一个Go的浏览器自动化库[Rod](https://github.com/go-rod/rod)，同样是基于CDP的也继承了Go强悍的并发调度，并且依靠Go内置的跨平台编译能力，使用Rod编写的自动化工程可以被直接编译为二进制文件，so cool😄 不仅易于分发，还能够非常容易地部署到服务器上，实现真正的一天24小时不间断工作
