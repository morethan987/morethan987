package color

import "os"

// 颜色变量（根据 TTY 检测动态赋值，与 shell-bin 第 23-35 行一致）
var (
	Red    string
	Green  string
	Yellow string
	Blue   string
	NC     string
)

// Init 检测 stdout 是否为 TTY，并相应设置颜色变量
// 等价于 shell 版的 [ -t 1 ] 条件判断
func Init() {
	fi, err := os.Stdout.Stat()
	if err == nil && (fi.Mode()&os.ModeCharDevice != 0) {
		Red = "\033[0;31m"
		Green = "\033[0;32m"
		Yellow = "\033[0;33m"
		Blue = "\033[1;36m"
		NC = "\033[0m"
	} else {
		Red = ""
		Green = ""
		Yellow = ""
		Blue = ""
		NC = ""
	}
}

// Colorize 返回带颜色包裹的文本，非 TTY 时退化为纯文本
func Colorize(color, text string) string {
	return color + text + NC
}
