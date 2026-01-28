# mping 🦀

**mping** (Mail Ping) is a lightweight, stateless Gmail monitor written in Rust. It scans your inbox for specific patterns (sender, subject, or body regex) and sends instant notifications to your phone via Webhook (e.g., ServerChan, Bark).

Designed to be triggered by `cron`, it is environment-driven and requires no local database.

## ✨ Features

- **Gmail Optimized**: Specifically designed for Gmail IMAP with App Password support.
- **Rule-based Filtering**: 
  - Match by Sender keywords.
  - Match by Subject keywords.
  - Match by Body content using **Regular Expressions**.
- **Stateless & Secure**: No local storage or database. Configurations are managed via system environment variables.
- **Webhook Integration**: Built-in support for ServerChan (Server酱) and easily adaptable for other HTTP-based notification services.
- **Lightweight**: Blazing fast performance with minimal memory footprint, thanks to Rust.

## 🚀 Getting Started

### Prerequisites

1. **Gmail App Password**: 
   - Enable 2-Factor Authentication on your Google Account.
   - Generate an [App Password](https://myaccount.google.com/apppasswords) for `mping`.
2. **Notification Key**: Obtain a SendKey from [ServerChan](https://sct.ftqq.com/) or a similar service.
3. **Rust Environment**: To build from source.

### Installation

```bash
git clone https://github.com/your-username/mping.git
cd mping
cargo build --release
```

The binary will be available at `target/release/mping`. You can move it to `/usr/local/bin`:
```bash
sudo cp target/release/mping /usr/local/bin/
```

## ⚙️ Configuration

`mping` is configured entirely through environment variables.

| Variable | Description | Example |
| :--- | :--- | :--- |
| `MP_GMAIL_USER` | Your Gmail address | `example@gmail.com` |
| `MP_GMAIL_PASS` | 16-character App Password | `abcd efgh ijkl mnop` |
| `MP_NOTIFY_URL` | Webhook URL for notifications | `https://sctapi.ftqq.com/SCTxxx.send` |
| `MP_SENDER_KEYWORDS` | Keywords to match sender (comma separated) | `boss,hr,bank` |
| `MP_HEAD_KEYWORDS` | Keywords to match subject (comma separated) | `Urgent,Alert,Verification` |
| `MP_BODY_REGEX` | Regular expressions for body matching | `\d{6},(?i)password` |

## 🛠 Usage

Run it manually to test your configuration:

```bash
export MP_GMAIL_USER="your-email@gmail.com"
export MP_GMAIL_PASS="your-app-password"
export MP_NOTIFY_URL="https://sctapi.ftqq.com/SCTxxx.send"
export MP_HEAD_KEYWORDS="Security,Bank"

# just run it
mping
```

### Automation (Cron)

To run `mping` once a day at 9:00 AM, add the following to your `crontab -e`:

```cron
0 9 * * * MP_GMAIL_USER="..." MP_GMAIL_PASS="..." MP_NOTIFY_URL="..." MP_HEAD_KEYWORDS="..." /usr/local/bin/mping >> /tmp/mping.log 2>&1
```

## 🔍 Troubleshooting

### Unexpected EOF / Connection Timeout
If you encounter `unexpected EOF` or timeouts:
- **Proxy Issues**: Gmail's IMAP (Port 993) might be blocked in your region. Ensure your proxy (Clash/Mihomo) is not bypassing 993 port or set to `DIRECT`.
- **Global Mode**: Try switching your proxy to Global mode to verify if it's a rule-based issue.
- **App Password**: Ensure you are not using your standard Google password.
