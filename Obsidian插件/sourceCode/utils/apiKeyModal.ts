import { App, Modal, Setting } from 'obsidian';
import { exec } from 'child_process';

export class ApiKeyModal extends Modal {
    private apiKey: string;

    constructor(app: App) {
        super(app);
    }

    onOpen() {
        const { contentEl } = this;

        contentEl.createEl('h2', { text: '设置API密钥' });

        new Setting(contentEl)
            .setName('API密钥')
            .setDesc('输入您的API密钥，它将被保存为系统环境变量API_KEY')
            .addText(text => text
                .setPlaceholder('sk-...')
                .onChange(value => {
                    this.apiKey = value;
                }));

        new Setting(contentEl)
            .addButton(btn => btn
                .setButtonText('保存')
                .setCta()
                .onClick(() => {
                    if (!this.apiKey) {
                        return;
                    }

                    // 使用Windows的setx命令设置持久化的环境变量
                    exec(`setx API_KEY "${this.apiKey}"`, (error) => {
                        if (error) {
                            console.error('Error setting environment variable:', error);
                            return;
                        }
                        
                        // 设置当前进程的环境变量
                        process.env.API_KEY = this.apiKey;
                        this.close();
                    });
                }))
            .addButton(btn => btn
                .setButtonText('取消')
                .onClick(() => {
                    this.close();
                }));
    }

    onClose() {
        const { contentEl } = this;
        contentEl.empty();
    }
}