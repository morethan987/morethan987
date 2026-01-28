use crate::parser::ParsedEmail;
use anyhow::Result;
use std::collections::HashMap;

pub async fn send_notification(url: &str, email: &ParsedEmail) -> Result<()> {
    let client = reqwest::Client::new();

    // 构建 Server酱 要求的参数
    // text: 消息标题
    // desp: 消息内容（支持 Markdown）
    let mut params = HashMap::new();
    params.insert("text", format!("邮件提醒: {}", email.subject));

    let description = format!(
        "### 收到符合规则的邮件\n\n**发件人:** {}\n\n**主题:** {}\n\n---\n\n**正文预览:**\n\n{}",
        email.from, email.subject, email.body
    );
    params.insert("desp", description);

    // 发送 POST 请求
    // reqwest 的 .form() 会自动设置 Content-Type 为 application/x-www-form-urlencoded
    let res = client.post(url).form(&params).send().await?;

    if res.status().is_success() {
        println!("通知发送成功 (UID: {})", email.uid);
    } else {
        let status = res.status();
        let error_body = res.text().await.unwrap_or_default();
        eprintln!("通知发送失败: {} - {}", status, error_body);
    }

    Ok(())
}
