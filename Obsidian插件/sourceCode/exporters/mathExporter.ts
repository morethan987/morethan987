export class MathExporter {
    transformMath(content: string): string {
        // 匹配单个 $，但不匹配 $$ 的情况
        const segments = content.split(/(\$\$[\s\S]+?\$\$|\$[^\$]+?\$)/g);
        
        return segments.map((segment, index) => {
            // 如果是双美元符号包裹的内容，保持原样
            if (segment.startsWith('$$')) {
                return segment;
            }
            // 如果是单美元符号包裹的内容
            if (segment.startsWith('$') && segment.endsWith('$')) {
                const mathContent = segment.slice(1, -1);
                const cleanMathContent = this.cleanMathContent(mathContent);
                return this.generateKatexHtml(cleanMathContent);
            }
            // 其他内容保持不变
            return segment;
        }).join('');
    }

    private cleanMathContent(mathContent: string): string {
        return mathContent
            .trim()
            .replace(/\s+/g, ' ')  // 将多个空格替换为单个空格
            .replace(/\\/g, '\\\\'); // 单斜杠变双斜杠
    }

    private generateKatexHtml(content: string): string {
        return `{{< katex >}}\\\\(${content}\\\\)`;
    }
}
