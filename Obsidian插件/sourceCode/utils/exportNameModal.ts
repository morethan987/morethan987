import { App, Modal, Notice } from 'obsidian';

export class ExportNameModal extends Modal {
    private fileName: string;
    private onSubmit: (fileName: string) => void;
    private inputEl: HTMLInputElement;

    constructor(app: App, defaultFileName: string, onSubmit: (fileName: string) => void) {
        super(app);
        this.fileName = defaultFileName;
        this.onSubmit = onSubmit;
    }

    onOpen() {
        const {contentEl} = this;
        contentEl.empty();

        contentEl.createEl('h2', {text: '导出文件'});

        // 创建输入框
        const inputContainer = contentEl.createDiv();
        inputContainer.style.margin = '1em 0';

        const label = inputContainer.createEl('label', {text: '请输入导出文件名：'});
        label.style.display = 'block';
        label.style.marginBottom = '0.5em';

        this.inputEl = inputContainer.createEl('input', {
            type: 'text',
            value: this.fileName
        });
        this.inputEl.style.width = '100%';
        this.inputEl.style.marginBottom = '1em';

        // 创建按钮容器
        const buttonContainer = contentEl.createDiv();
        buttonContainer.style.display = 'flex';
        buttonContainer.style.justifyContent = 'flex-end';
        buttonContainer.style.gap = '10px';

        // 添加取消和确认按钮
        const cancelButton = buttonContainer.createEl('button', {text: '取消'});
        const confirmButton = buttonContainer.createEl('button', {text: '确认'});
        confirmButton.classList.add('mod-cta');

        // 绑定事件
        cancelButton.onclick = () => this.close();
        confirmButton.onclick = () => {
            const fileName = this.inputEl.value.trim();
            if (fileName) {
                this.onSubmit(fileName);
                this.close();
            } else {
                new Notice('文件名不能为空');
            }
        };

        // 支持回车确认
        this.inputEl.addEventListener('keydown', (event) => {
            if (event.key === 'Enter') {
                confirmButton.click();
            }
        });

        // 自动聚焦输入框并选中文本
        this.inputEl.focus();
        this.inputEl.select();
    }

    onClose() {
        const {contentEl} = this;
        contentEl.empty();
    }
}
