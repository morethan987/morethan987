import { App, Modal, Notice } from 'obsidian';

export class ConfirmationModal extends Modal {
    constructor(app: App, private onConfirm: () => void) {
        super(app);
    }

    onOpen() {
        const {contentEl} = this;
        contentEl.createEl('h2', {text: '确认导出'});
        contentEl.createEl('p', {text: '是否确认导出所有文件？提前请检查被笔记中是否有slug属性'});

        const buttonContainer = contentEl.createDiv();
        buttonContainer.style.display = 'flex';
        buttonContainer.style.justifyContent = 'flex-end';
        buttonContainer.style.gap = '10px';

        const cancelButton = buttonContainer.createEl('button', {text: '取消'});
        const confirmButton = buttonContainer.createEl('button', {text: '确认'});
        confirmButton.classList.add('mod-cta');

        cancelButton.onclick = () => this.close();
        confirmButton.onclick = () => {
            this.onConfirm();
            this.close();
        };
    }

    onClose() {
        const {contentEl} = this;
        contentEl.empty();
    }
}