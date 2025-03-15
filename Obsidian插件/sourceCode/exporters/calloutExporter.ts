export class CalloutExporter {
    transformCallouts(content: string): string {
        // 识别代码块的位置
        const codeBlockPositions: {start: number, end: number}[] = [];
        const codeBlockRegex = /```[\s\S]*?```/g;
        let match: RegExpExecArray | null;
        while ((match = codeBlockRegex.exec(content)) !== null) {
            codeBlockPositions.push({
                start: match.index,
                end: match.index + match[0].length
            });
        }

        const calloutRegex = /^>\s*\[!(\w+)\]\s*(.*)?\n((?:>[^\n]*\n?)*)/gm;
        let result = '';
        let lastIndex = 0;

        while ((match = calloutRegex.exec(content)) !== null) {
            // 检查当前匹配是否在任何代码块内
            const isInCodeBlock = codeBlockPositions.some(pos => 
                match !== null && match.index >= pos.start && match.index < pos.end
            );

            if (isInCodeBlock) {
                // 如果在代码块内，保持原样
                result += content.slice(lastIndex, match.index + match[0].length);
            } else {
                // 如果不在代码块内，进行转换
                result += content.slice(lastIndex, match.index);
                const type = match[1];
                const contents = match[3];
                const cleanContents = this.cleanCalloutContent(contents);
                const contributes = this.getCalloutAttributes(type);
                result += this.generateCalloutHtml(cleanContents, contributes);
            }
            lastIndex = match.index + match[0].length;
        }

        // 添加剩余内容
        result += content.slice(lastIndex);
        return result;
    }

    private cleanCalloutContent(contents: string): string {
        return contents
            .split('\n')
            .map((line: string) => line.replace(/^>\s?/, '').trim())
            .filter((line: string) => line.length > 0)
            .join('\n');
    }

    private getCalloutAttributes(type: string): string {
        switch (type.toLowerCase()) {
            case 'note':
                return 'icon="pencil" cardColor="#1E3A8A" textColor="#E0E7FF"';
            case 'info':
                return 'icon="circle-info" cardColor="#b0c4de" textColor="#333333"';
            case 'todo':
                return 'icon="square-check" iconColor="#4682B4" cardColor="#e0ffff" textColor="#333333"';
            case 'tip':
            case 'hint':
            case 'important':
                return 'icon="lightbulb" cardColor="#fff5b7" textColor="#333333"';
            case 'success':
            case 'check':
            case 'done':
                return 'icon="check" cardColor="#32CD32" textColor="#fff" iconColor="#ffffff"';
            case 'warning':
            case 'caution':
            case 'attention':
                return 'icon="triangle-exclamation" cardColor="#ffcc00" textColor="#333333" iconColor="#8B6914"';
            case 'question':
            case 'help':
            case 'faq':
                return 'icon="circle-question" cardColor="#ffeb3b" textColor="#333333" iconColor="#3b3b3b"';
            case 'danger':
            case 'error':
                return 'icon="fire" cardColor="#e63946" iconColor="#ffffff" textColor="#ffffff"';
            case 'example':
                return 'icon="list" cardColor="#d8bfd8" iconColor="#8B008B" textColor="#333333"';
            default:
                return '';

        }
    }

    private generateCalloutHtml(content: string, attributes: string): string {
        return `{{< alert ${attributes} >}}
${content}
{{< /alert >}}`;
	}
}
