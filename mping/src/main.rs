mod imap_client;
mod load_env;
mod notification;
mod parser;
mod policy;

use crate::load_env::Config;
use anyhow::Context;
use imap_client::fetch_emails;
use tokio::task::spawn_blocking;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // 1. 从环境变量获取配置并初始化匹配引擎
    let config = Config::from_env().context("Fail to load environment variables")?;
    let matcher = policy::EmailMatcher::new(&config)?;

    // 2. 获取并解析邮件 (之前的逻辑)
    let config_for_imap = config.clone();
    println!("正在抓取邮件...");
    let raw_emails = spawn_blocking(move || fetch_emails(&config_for_imap)).await??;

    // 3. 解析并过滤邮件
    let matched_emails: Vec<_> = raw_emails
        .iter()
        .filter_map(crate::parser::parse_email) // 解析成 ParsedEmail
        .filter(|email| matcher.is_match(email)) // 根据 Policy 过滤
        .collect();

    println!("匹配到 {} 封符合规则的邮件。", matched_emails.len());

    for email in matched_emails {
        notification::send_notification(config.notify_url(), &email).await?;
    }

    println!("任务处理完成");

    Ok(())
}
