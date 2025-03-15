import { App, TFile, Notice } from 'obsidian';
import * as path from 'path';
import * as fs from 'fs';

export class ImageExporter {
    constructor(private app: App) {}

    async transformImages(content: string, mode: 'batch' | 'single', settings: any, slug: string): Promise<string> {
        const imgLinkRegex = /!\[\[(.*?)\]\]/g;
        const matches = Array.from(content.matchAll(imgLinkRegex));
        let modifiedContent = content;

        for (const match of matches) {
            const wikiPath = match[1];
            try {
                const attachmentFile = this.app.metadataCache.getFirstLinkpathDest(wikiPath, '');
                if (attachmentFile) {
                    const isImage = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(
                        path.extname(attachmentFile.path).toLowerCase()
                    );
                    if (!isImage) {
                        continue;
                    }
                }
                
                if (attachmentFile instanceof TFile) {
                    // 获取相对于vault根目录的路径
                    const relativePath = attachmentFile.path.replace(/\\/g, '/');
                    
                    // 构建目标路径
                    const exportDir = path.resolve(settings.exportPath);
                    const imagesDir = path.join(
                        exportDir,
                        settings.blogPath,
                        slug,
                        settings.imageExportPath
                    );
                    
                    // 确保图片导出目录存在
                    if (!fs.existsSync(imagesDir)) {
                        fs.mkdirSync(imagesDir, { recursive: true });
                    }
                    
                    // 获取文件内容并复制
                    const imageData = await this.app.vault.readBinary(attachmentFile);
                    const targetPath = path.join(imagesDir, attachmentFile.name);
                    fs.writeFileSync(targetPath, Buffer.from(imageData));
                    
                    // 生成新的图片引用路径（使用相对路径）
                    const hugoImagePath = `${settings.imageExportPath}/${attachmentFile.name}`;
                    
                    // 替换原始wiki链接
                    modifiedContent = modifiedContent.replace(
                        `![[${wikiPath}]]`,
                        this.generateImageHtml(hugoImagePath, attachmentFile.name)
                    );
                }
            } catch (error) {
                console.error(`Failed to process image ${wikiPath}:`, error);
                if (mode === 'single') {
                    new Notice(`❌ 处理图片失败: ${wikiPath}\n${error.message}`);
                }
            }
        }
        
        return modifiedContent;
    }

    private generateImageHtml(imagePath: string, imageTitle: string): string {
        return `![${imageTitle}](${imagePath})`;
    }
}
