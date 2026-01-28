use anyhow::Context;
use std::env;

/// 配置结构体，包含从环境变量加载的所有配置项
#[derive(Debug, Clone)]
pub struct Config {
    pub user: String,
    pub pass: String,
    pub notify_url: String,
    pub sender_keywords: Vec<String>, // 发件人关键词列表
    pub head_keywords: Vec<String>,   // 主题关键词列表
    pub body_regex: Vec<String>,      // 正文正则表达式列表
}

impl Config {
    pub fn from_env() -> anyhow::Result<Self> {
        // 使用 anyhow::Context 提供更有意义的错误信息
        let user = env::var("MP_GMAIL_USER").context("MP_GMAIL_USER not set")?;
        let pass = env::var("MP_GMAIL_PASS").context("MP_GMAIL_PASS not set")?;
        let notify_url = env::var("MP_NOTIFY_URL").context("MP_NOTIFY_URL not set")?;

        // 辅助函数：从环境变量获取逗号分隔的字符串并转换为 Vec<String>
        let get_vec = |var_name: &str| -> Vec<String> {
            env::var(var_name)
                .ok()
                .map(|val| {
                    val.split(',')
                        .map(|s| s.trim().to_string())
                        .filter(|s| !s.is_empty())
                        .collect()
                })
                .unwrap_or_default() // 如果变量不存在，直接给一个空的 Vec
        };

        // 获取关键词和正则表达式列表
        let sender_keywords = get_vec("MP_SENDER_KEYWORDS");
        let head_keywords = get_vec("MP_HEAD_KEYWORDS");
        let body_regex = get_vec("MP_BODY_REGEX");

        // 检测至少有一个匹配规则被设置
        if sender_keywords.is_empty() && head_keywords.is_empty() && body_regex.is_empty() {
            anyhow::bail!(
                "At least one of MP_SENDER_KEYWORDS, MP_HEAD_KEYWORDS, or MP_BODY_REGEX must be set"
            );
        }

        Ok(Config {
            user,
            pass,
            notify_url,
            sender_keywords,
            head_keywords,
            body_regex,
        })
    }

    pub fn head_keywords(&self) -> &[String] {
        &self.head_keywords
    }

    pub fn user(&self) -> &str {
        &self.user
    }

    pub fn pass(&self) -> &str {
        &self.pass
    }

    pub fn notify_url(&self) -> &str {
        &self.notify_url
    }

    pub fn sender_keywords(&self) -> &[String] {
        &self.sender_keywords
    }

    pub fn body_regex(&self) -> &[String] {
        &self.body_regex
    }
}

#[cfg(test)]
mod tests {
    use super::*; // 导入父作用域（即 Config 结构体等）
    use std::env;

    #[test]
    fn test_config_load_success() {
        // 1. 模拟设置环境变量
        unsafe {
            // 防御性清理：确保没有旧值干扰
            env::remove_var("MP_BODY_REGEX");
            env::set_var("MP_GMAIL_USER", "test@gmail.com");
            env::set_var("MP_GMAIL_PASS", "password123");
            env::set_var("MP_NOTIFY_URL", "https://api.test.com");
            env::set_var("MP_HEAD_KEYWORDS", "urgent,bank");
            env::set_var("MP_SENDER_KEYWORDS", "boss@work.com");
        }
        // 留空 MP_BODY_REGEX 看看是否工作正常

        // 2. 调用加载函数
        let config = Config::from_env().expect("应该成功加载配置");

        // 3. 断言验证
        assert_eq!(config.user(), "test@gmail.com");
        assert_eq!(config.pass(), "password123");
        assert_eq!(config.head_keywords().len(), 2);
        assert!(config.head_keywords().contains(&"urgent".to_string()));
        assert!(config.body_regex().is_empty());

        // 4. 清理环境变量（防止影响其他测试）
        unsafe {
            env::remove_var("MP_GMAIL_USER");
            env::remove_var("MP_GMAIL_PASS");
            env::remove_var("MP_NOTIFY_URL");
            env::remove_var("MP_HEAD_KEYWORDS");
            env::remove_var("MP_SENDER_KEYWORDS");
        }
    }

    #[test]
    fn test_config_missing_required_var() {
        // 确保环境变量是空的
        unsafe { env::remove_var("MP_GMAIL_USER") };

        // 尝试加载，应该返回错误
        let result = Config::from_env();
        assert!(result.is_err());

        // 验证错误信息是否包含我们期望的内容
        let err_msg = format!("{:?}", result.err().unwrap());
        assert!(err_msg.contains("MP_GMAIL_USER not set"));
    }

    #[test]
    fn test_config_no_rules_error() {
        // 设置基本信息，但不设置任何规则
        unsafe {
            env::set_var("MP_GMAIL_USER", "a");
            env::set_var("MP_GMAIL_PASS", "b");
            env::set_var("MP_NOTIFY_URL", "c");
            env::remove_var("MP_SENDER_KEYWORDS");
            env::remove_var("MP_HEAD_KEYWORDS");
            env::remove_var("MP_BODY_REGEX");
        }

        let result = Config::from_env();
        assert!(result.is_err());
        let err_msg = format!("{:?}", result.err().unwrap());
        assert!(err_msg.contains("At least one of MP_SENDER_KEYWORDS"));
    }
}
