import { App, Notice, TFile } from 'obsidian';
import * as path from 'path';
import * as fs from 'fs';
import { HugoBlowfishExporterSettings } from '../main';
import { ImageExporter } from '../exporters/imageExporter';
import { CoverChooser } from '../exporters/coverChooser';

// 批量导出的模态框
export class BatchExportModal {
    constructor(
        private app: App,
        private settings: HugoBlowfishExporterSettings,
        private modifyContent: (content: string, mode: 'batch' | 'single') => Promise<string>
    ) {}

    async export() {
        const files = this.app.vault.getMarkdownFiles();
        if (files.length === 0) {
            new Notice('没有找到Markdown文件');
            return;
        }

        const progressNotice = new Notice('', 0);
        const exportDir = path.resolve(this.settings.exportPath);
        const contentDir = path.join(exportDir, this.settings.blogPath);
        
        if (!fs.existsSync(contentDir)) {
            fs.mkdirSync(contentDir, { recursive: true });
        }

        let processedCount = 0;
        let successCount = 0;
        let failCount = 0;
        let missingSlugCount = 0;

        for (const file of files) {
            processedCount++;
            const progress = Math.round((processedCount / files.length) * 100);
            progressNotice.setMessage(
                `正在导出: ${progress}%\n` +
                `${file.basename}\n` +
                `(${processedCount}/${files.length})`
            );

            const result = await this.processSingleFile(file, contentDir);
            if (result.success) successCount++;
            if (result.failed) failCount++;
            if (result.missingSlug) missingSlugCount++;
        }

        progressNotice.hide();

        new Notice(
            `导出完成!\n` +
            `✅ 成功: ${successCount}\n` +
            `❌ 失败: ${failCount}\n` +
            `⚠️ 缺少slug: ${missingSlugCount}`,
            10000
        );
    }

    // 批量导出的文件处理部分
    private async processSingleFile(file: TFile, contentDir: string): Promise<{success?: boolean, failed?: boolean, missingSlug?: boolean}> {
        try {
            const metadata = this.app.metadataCache.getFileCache(file);
            if (!metadata?.frontmatter?.slug) {
                console.warn(`文件 ${file.basename} 缺少 slug 属性，已跳过`);
                return { missingSlug: true };
            }

            const slugDir = path.join(contentDir, metadata.frontmatter.slug);
            if (!fs.existsSync(slugDir)) {
                fs.mkdirSync(slugDir, { recursive: true });
            }

            let content = await this.app.vault.read(file);

            // 特有的对于imageExporter的再次调用，这样能够保证在文件夹里面的文件链接的图片也能够被导出
            const imageExporter = new ImageExporter(this.app);
            content = await imageExporter.transformImages(content, 'batch', this.settings, metadata.frontmatter.slug);

            const modifiedContent = await this.modifyContent(content, 'batch');

            let fileName = this.settings.useDefaultExportName
                ? this.settings.defaultExportName.replace('{{title}}', file.basename)
                : file.basename;

            const outputPath = path.join(slugDir, `${fileName}.md`);
            fs.writeFileSync(outputPath, modifiedContent, 'utf8');

            const coverChooser = new CoverChooser();
            await coverChooser.chooseCover(this.settings, slugDir);
            
            return { success: true };
        } catch (error) {
            console.error(`导出失败 ${file.path}:`, error);
            return { failed: true };
        }
    }
}
