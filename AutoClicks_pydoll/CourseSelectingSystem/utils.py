import asyncio

from pydoll.browser.tab import Tab
from pydoll.protocol.network.events import NetworkEvent


async def wait_for_network_idle(
    tab: Tab, idle_time: float = 1.0, timeout: float = 15.0
):
    """
    等待当前标签页的网络请求静默（在 idle_time 秒内没有任何活跃的网络请求）
    这是解决前端框架（如 Angular/Vue/React）动态渲染导致元素失效的终极方案。
    """
    # 启用 Pydoll 的网络事件监听
    await tab.enable_network_events()

    inflight_requests = set()
    idle_event = asyncio.Event()
    timer_task = None

    async def reset_timer():
        """重置静默倒计时"""
        nonlocal timer_task
        if timer_task and not timer_task.done():
            timer_task.cancel()

        # 如果当前没有挂起的请求了，就开始启动静默倒计时
        if not inflight_requests:
            timer_task = asyncio.create_task(trigger_idle())

    async def trigger_idle():
        try:
            await asyncio.sleep(idle_time)
            # 成功熬过了 idle_time 都没有被新请求打断，说明网络真的空闲了！
            idle_event.set()
        except asyncio.CancelledError:
            # 倒计时被新的网络请求打断了，默默退出
            pass

    # ============ 定义 Pydoll 网络事件回调 ============
    async def on_request_sent(event):
        req_id = event["params"]["requestId"]
        inflight_requests.add(req_id)
        # 有新请求发出，马上打破现有的静默倒计时
        if timer_task and not timer_task.done():
            timer_task.cancel()

    async def on_request_finished(event):
        req_id = event["params"]["requestId"]
        inflight_requests.discard(req_id)
        # 请求完成或失败，尝试重新开启倒计时
        await reset_timer()

    # 注册事件监听器
    await tab.on(NetworkEvent.REQUEST_WILL_BE_SENT, on_request_sent)
    await tab.on(NetworkEvent.LOADING_FINISHED, on_request_finished)
    await tab.on(NetworkEvent.LOADING_FAILED, on_request_finished)

    # 初始触发一次检查（万一页面当前本来就是完全空闲的）
    await reset_timer()

    try:
        # 阻塞等待，直到 idle_event 被 set，或者整体超时
        await asyncio.wait_for(idle_event.wait(), timeout=timeout)
        print("✅ 网络请求已静默，页面数据加载及前端框架渲染彻底完成！")
    except asyncio.TimeoutError:
        print(
            "⚠️ 网络空闲等待超时（可能是页面存在长连接如 WebSocket/心跳包），直接继续..."
        )
    finally:
        # 清理工作：如果后续不需要监控网络了，最好关掉它以节省资源
        await tab.disable_network_events()
