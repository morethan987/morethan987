import { App, Modal } from 'obsidian';

export class ExportDispNameModal extends Modal {
    private onSubmit: (fileName: string) => void;
    private selectedLanguage: string = 'zh-cn'; // 默认选择中文

    constructor(app: App, defaultFileName: string, onSubmit: (fileName: string) => void) {
        super(app);
        this.onSubmit = onSubmit;
    }

    onOpen() {
        const {contentEl} = this;
        contentEl.empty();

        contentEl.createEl('h2', {text: '选择语言版本'});

        const inputContainer = contentEl.createDiv();
        inputContainer.style.margin = '1em 0';

        const label = inputContainer.createEl('p', {text: '检测到展示性链接，请选择该链接指向的内容语言版本：'});
        label.style.marginBottom = '1em';

        // 创建单选按钮组
        const radioGroup = inputContainer.createDiv();
        radioGroup.style.display = 'flex';
        radioGroup.style.flexDirection = 'column';
        radioGroup.style.gap = '10px';
        radioGroup.style.marginBottom = '1em';

        // 中文选项
        const zhContainer = radioGroup.createDiv();
        zhContainer.style.display = 'flex';
        zhContainer.style.alignItems = 'center';
        zhContainer.style.gap = '8px';
        const zhRadio = zhContainer.createEl('input', {
            type: 'radio',
            value: 'zh-cn',
            attr: { name: 'language' }
        });
        zhRadio.checked = true;
        zhContainer.createEl('label', {text: '中文版本 (index.zh-cn.md)'});

        // 英文选项
        const enContainer = radioGroup.createDiv();
        enContainer.style.display = 'flex';
        enContainer.style.alignItems = 'center';
        enContainer.style.gap = '8px';
        const enRadio = enContainer.createEl('input', {
            type: 'radio',
            value: 'en',
            attr: { name: 'language' }
        });
        enContainer.createEl('label', {text: '英文版本 (index.en.md)'});

        // 添加事件监听
        zhRadio.addEventListener('change', () => {
            if (zhRadio.checked) this.selectedLanguage = 'zh-cn';
        });
        enRadio.addEventListener('change', () => {
            if (enRadio.checked) this.selectedLanguage = 'en';
        });

        // 按钮容器
        const buttonContainer = contentEl.createDiv();
        buttonContainer.style.display = 'flex';
        buttonContainer.style.justifyContent = 'flex-end';
        buttonContainer.style.gap = '10px';
        buttonContainer.style.marginTop = '1em';

        // 添加按钮
        const cancelButton = buttonContainer.createEl('button', {text: '取消'});
        const confirmButton = buttonContainer.createEl('button', {text: '确认'});
        confirmButton.classList.add('mod-cta');

        cancelButton.onclick = () => this.close();
        confirmButton.onclick = () => {
            const fileName = `index.${this.selectedLanguage}.md`;
            this.onSubmit(fileName);
            this.close();
        };

        // 支持回车确认
        contentEl.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                confirmButton.click();
            }
        });
    }

    onClose() {
        const {contentEl} = this;
        contentEl.empty();
    }
}
