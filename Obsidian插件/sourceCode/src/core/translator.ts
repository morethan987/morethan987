import { App, MarkdownView, Notice } from 'obsidian';
import * as path from 'path';
import * as fs from 'fs';
import HugoBlowfishExporter from './plugin';

export class Translator {
    constructor(
        private app: App,
        private plugin: HugoBlowfishExporter
    ) {}

    async translateCurrentNote() {
        try {
            // 检查API密钥是否配置
            if (!process.env.API_KEY) {
                new Notice('请先配置OpenAI API密钥');
                return;
            }

            // 检查API密钥是否配置
            if (!this.plugin.settings.BaseURL) {
                new Notice('请先在设置中配置BaseURL');
                return;
            }

            // 检查API密钥是否配置
            if (!this.plugin.settings.ModelName) {
                new Notice('请先在设置中配置模型名称');
                return;
            }

            // 检查翻译文件导出路径是否配置
            if (!this.plugin.settings.translatedExportPath) {
                new Notice('请先在设置中配置翻译文件导出路径');
                return;
            }

            const activeView = this.app.workspace.getActiveViewOfType(MarkdownView);
            if (!activeView) {
                new Notice('没有打开的文件');
                return;
            }

            const currentFile = activeView.file;
            if (!currentFile) {
                new Notice('无法获取当前文件');
                return;
            }

            // 获取文件的元数据和内容
            const metadata = this.app.metadataCache.getFileCache(currentFile);
            const content = await this.app.vault.read(currentFile);

            new Notice('开始翻译...');

            // 调用API进行标题的翻译
            const titleCompletion = await this.plugin.client.chat.completions.create({
                model: this.plugin.settings.ModelName,
                messages: [
                    {
                        role: "system",
                        content: `你是一个精准的标题翻译专家。请将以下标题翻译成简洁凝练的${this.plugin.settings.targetLanguage}。`
                    },
                    {
                        role: "user",
                        content: currentFile.basename
                    }
                ],
                temperature: 0.3
            });
            const translatedTitle = titleCompletion.choices[0].message.content || 'Default Title';

            // 调用API进行内容的翻译
            const contentCompletion = await this.plugin.client.chat.completions.create({
                model: this.plugin.settings.ModelName,
                messages: [
                    {
                        role: "system",
                        content: `你是一个专业的文档翻译助手。请将以下Markdown内容翻译成地道流畅的${this.plugin.settings.targetLanguage}，同时遵循一下要求：\n1. 保持所有的Markdown格式、链接和图片引用不变；frontmatter部分需要保持格式不变。\n2. 不要翻译文件名和链接。\n3. 代码块中的注释需要翻译`
                    },
                    {
                        role: "user",
                        content: content
                    }
                ],
                temperature: 0.3
            });
            const translatedContent = contentCompletion.choices[0].message.content || '';

            // 构建翻译文件的保存路径，保持原有目录结构
            const fileName = `${this.plugin.settings.translatedFilePrefix}${translatedTitle}.md`;
            
            const translatedFilePath = path.join(this.plugin.settings.translatedExportPath, fileName);

            // 先导出到目标文件夹中，确保目录存在
            fs.mkdirSync(path.dirname(translatedFilePath), { recursive: true });
            fs.writeFileSync(translatedFilePath, translatedContent, 'utf8');
            new Notice(`✅ 翻译完成！\n文件已保存至:\n${translatedFilePath}`);

            // 检查是否需要直接导出
            if (this.plugin.settings.directExportAfterTranslation) {
                await this.directExport(translatedContent, metadata, translatedTitle);
            }
        } catch (error) {
            new Notice(`❌ 翻译失败: ${error.message}`);
            console.error('Translation error:', error);
        }
    }

    private async directExport(translatedContent: string, metadata: any, translatedTitle: string) {
        new Notice(`正在执行直接导出...`);

        // 检测是否有slug属性
        if (!metadata?.frontmatter?.slug) {
            new Notice('⚠️ 当前文件缺少 slug 属性，请在 frontmatter 中添加 slug 字段');
            return;
        }

        // 根据slug创建目标目录
        let exportDir = path.resolve(this.plugin.settings.exportPath);
        exportDir = path.join(exportDir, this.plugin.settings.blogPath);
        const slugDir = path.join(exportDir, metadata.frontmatter.slug);
        if (!fs.existsSync(slugDir)) {
            fs.mkdirSync(slugDir, { recursive: true });
        }

        const modifiedContent = await this.plugin.exporter.modifyContent(translatedContent, 'single');

        let directExportFileName: string;
        if (this.plugin.settings.targetLanguage === '中文') {
            directExportFileName = 'index.zh-cn';
        } else {
            directExportFileName = 'index.en';
        }

        // 构建完整的输出路径
        const outputPath = path.join(slugDir, `${directExportFileName}.md`);

        // 写入文件
        fs.writeFileSync(outputPath, modifiedContent, 'utf8');

        // 自动选择博客封面
        await this.plugin.coverChooser.chooseCover(this.plugin.settings, slugDir);

        new Notice(`✅ 直接导出成功!\n文件已保存至:\n${outputPath}`, 5000);
    }
}