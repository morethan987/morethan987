use crate::load_env::Config;
use anyhow::{Context, Result};
use chrono::{Duration, Utc};
use native_tls::TlsConnector;

/// 定义邮件抓取结果
pub struct RawEmail {
    pub uid: u32,
    pub body: Vec<u8>,
}

/// 抓取符合日期条件的原始邮件
pub fn fetch_emails(config: &Config) -> Result<Vec<RawEmail>> {
    let domain = "imap.gmail.com";
    let port = 993;

    // 1. 建立 TLS 连接
    let tls = TlsConnector::builder()
        .build()
        .context("Failed to build TLS connector")?;

    let client = imap::connect((domain, port), domain, &tls)
        .context("Failed to connect to Gmail IMAP server")?;

    // 2. 登录 (使用 Config 中的应用专用密码)
    let mut imap_session = client
        .login(config.user(), config.pass())
        .map_err(|e| anyhow::anyhow!("IMAP login failed: {}", e.0))?;

    // 3. 选择收件箱 (只读模式即可)
    imap_session
        .examine("INBOX")
        .context("Failed to select INBOX")?;

    // 4. 构造搜索条件：过去 24 小时
    // IMAP 的 SINCE 格式严格要求为: DD-Mon-YYYY (例如: 29-Jan-2024)
    let yesterday = (Utc::now() - Duration::days(1))
        .format("%d-%b-%Y")
        .to_string();
    let search_query = format!("SINCE {}", yesterday);

    // 执行搜索得到邮件 UID 列表
    let msg_uids = imap_session
        .uid_search(search_query)
        .context("Search failed")?;

    if msg_uids.is_empty() {
        return Ok(vec![]);
    }

    // 5. 抓取邮件内容 (RFC822 是包含 Header 和 Body 的完整格式)
    let mut results = Vec::new();

    // 为了效率，我们可以一次性 fetch 所有的 UID
    let uid_query: String = msg_uids
        .iter()
        .map(|id| id.to_string())
        .collect::<Vec<_>>()
        .join(",");
    let fetches = imap_session
        .uid_fetch(uid_query, "RFC822")
        .context("Fetch failed")?;

    for fetch in fetches.iter() {
        if let Some(body) = fetch.body() {
            results.push(RawEmail {
                uid: fetch.uid.unwrap_or(0),
                body: body.to_vec(),
            });
        }
    }

    // 6. 登出
    imap_session.logout().ok();

    Ok(results)
}
