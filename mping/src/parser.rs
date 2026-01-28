use crate::imap_client::RawEmail;
use mail_parser::{Address, MessageParser};
use std::borrow::Cow;

#[derive(Debug)]
pub struct ParsedEmail {
    pub uid: u32,
    pub from: String,
    pub subject: String,
    pub body: String,
}

pub fn parse_email(raw: &RawEmail) -> Option<ParsedEmail> {
    // 1. 解析原始字节
    let msg = MessageParser::new().parse(&raw.body)?;

    // 2. 提取发件人
    // msg.from() 直接返回 Option<&Address>，不需要再经过 HeaderValue
    let from_str = if let Some(addr_enum) = msg.from() {
        match addr_enum {
            // 情况 A: 直接的地址列表
            Address::List(list) => list.first(),
            // 情况 B: 地址组 (如 Work: a@b.com, c@d.com;)
            Address::Group(groups) => groups.first().and_then(|g| g.addresses.first()),
        }
        .map(|addr| {
            let name = addr.name.as_deref().unwrap_or("");
            let email = addr.address.as_deref().unwrap_or("");
            if name.is_empty() {
                email.to_string()
            } else {
                format!("{} <{}>", name, email)
            }
        })
    } else {
        None
    };

    let from = from_str.unwrap_or_else(|| "Unknown".to_string());

    // 3. 提取主题
    let subject = msg.subject().unwrap_or("No Subject").to_string();

    // 4. 提取正文内容
    let body = msg
        .body_text(0)
        .map(|c: Cow<'_, str>| c.to_string())
        .unwrap_or_default();

    Some(ParsedEmail {
        uid: raw.uid,
        from,
        subject,
        body,
    })
}
