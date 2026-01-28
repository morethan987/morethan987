use crate::load_env::Config;
use crate::parser::ParsedEmail;
use anyhow::Result;
use regex::Regex;

pub struct EmailMatcher {
    sender_keywords: Vec<String>,
    head_keywords: Vec<String>,
    compiled_body_regexes: Vec<Regex>,
}

impl EmailMatcher {
    /// 初始化匹配引擎：将配置中的字符串编译为正则对象
    pub fn new(config: &Config) -> Result<Self> {
        let mut compiled_body_regexes = Vec::new();

        // 预编译所有的正则表达式
        for re_str in config.body_regex() {
            let re = Regex::new(re_str)
                .map_err(|e| anyhow::anyhow!("正则表达式 '{}' 语法错误: {}", re_str, e))?;
            compiled_body_regexes.push(re);
        }

        Ok(EmailMatcher {
            sender_keywords: config.sender_keywords().to_vec(),
            head_keywords: config.head_keywords().to_vec(),
            compiled_body_regexes,
        })
    }

    /// 核心判断函数：根据规则判定邮件是否命中
    pub fn is_match(&self, email: &ParsedEmail) -> bool {
        // 1. 匹配发件人 (只要包含任意一个关键词即命中)
        if self.sender_keywords.iter().any(|k| email.from.contains(k)) {
            return true;
        }

        // 2. 匹配主题
        if self.head_keywords.iter().any(|k| email.subject.contains(k)) {
            return true;
        }

        // 3. 匹配正文 (正则表达式)
        if self
            .compiled_body_regexes
            .iter()
            .any(|re| re.is_match(&email.body))
        {
            return true;
        }

        false
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_matcher_logic() {
        // 模拟配置
        let sender_keywords = vec!["boss".to_string()];
        let head_keywords = vec!["urgent".to_string()];
        let body_regex = vec![r"\d{6}".to_string()]; // 匹配6位数字

        let matcher = EmailMatcher {
            sender_keywords,
            head_keywords,
            compiled_body_regexes: body_regex.iter().map(|s| Regex::new(s).unwrap()).collect(),
        };

        // 测试邮件 A: 命中发件人
        let email_a = ParsedEmail {
            uid: 1,
            from: "my boss <b@b.com>".into(),
            subject: "hi".into(),
            body: "hello".into(),
        };
        assert!(matcher.is_match(&email_a));

        // 测试邮件 B: 命中正文正则
        let email_b = ParsedEmail {
            uid: 2,
            from: "someone".into(),
            subject: "no".into(),
            body: "Your code is 123456".into(),
        };
        assert!(matcher.is_match(&email_b));

        // 测试邮件 C: 全都不命中
        let email_c = ParsedEmail {
            uid: 3,
            from: "ads".into(),
            subject: "buy now".into(),
            body: "cheap items".into(),
        };
        assert!(!matcher.is_match(&email_c));
    }
}
