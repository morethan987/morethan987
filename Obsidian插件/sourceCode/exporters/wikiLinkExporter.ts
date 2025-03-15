import { App, Notice, TFile } from 'obsidian';
import { ExportDispNameModal } from '../utils/exportDispNameModal';
import { HugoBlowfishExporterSettings } from '../main';

export class WikiLinkExporter {
    constructor(private app: App) {}

    async transformWikiLinks(
        content: string, 
        mode: 'batch' | 'single',
        settings: HugoBlowfishExporterSettings
    ): Promise<string> {
        // 匹配所有wiki链接：展示性(![[file]])和非展示性([[file|text]])
        const wikiLinkRegex = /(!?\[\[(.*?)(?:\|(.*?))?\]\])/g;
        let modifiedContent = content;
        
        const promises = Array.from(content.matchAll(wikiLinkRegex)).map(async match => {
            const [fullMatch, _, targetFile, displayText] = match;
            const isDisplayLink = fullMatch.startsWith('!');
            
            // 分离文件名和段落引用
            const [filePath, fragment] = targetFile.split('#');
            const actualTarget = filePath.split('|')[0].trim();
            
            try {
                let hugoLink: string;
                // 如果没有文件名，说明是一个内部链接
                if (!actualTarget) {
                    const linkText = displayText || fragment;
                    const formated_fragment = fragment.replace(/[A-Z]/g, (char) => char.toLowerCase()).replace(/\s+/g, "-");
                    hugoLink = `[${linkText}]({{< relref "#${formated_fragment}" >}})`;
                    modifiedContent = modifiedContent.replace(fullMatch, hugoLink);
                } else {
                    const file = this.app.metadataCache.getFirstLinkpathDest(actualTarget, '');
                    if (!file) {
                        if (mode === 'single') {
                            new Notice(`❌ 未找到文件: ${actualTarget}`);
                        } else {
                            console.warn(`未找到文件: ${actualTarget}`);
                        }
                        return;
                    }

                    // 检查如果是展示性链接且为图片，则跳过处理
                    if (isDisplayLink) {
                        const isImage = ['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp'].includes(
                            file.extension.toLowerCase()
                        );
                        if (isImage) return;
                    }

                    const metadata = this.app.metadataCache.getFileCache(file);
                
                    if (!metadata?.frontmatter?.slug) {
                        if (mode === 'single') {
                            new Notice(`⚠️ 警告: ${file.basename} 缺少slug属性\n请在文件frontmatter中添加slug字段`, 20000);
                        } else {
                            console.warn(`文件 ${file.basename} 缺少slug属性`);
                        }
                        return;
                    }

                    if (isDisplayLink) {
                        // 处理展示性链接
                        let fileName: string;
                        if (settings.useDefaultDispName) {
                            fileName = settings.defaultDispName;
                        } else {
                            fileName = await new Promise((resolve) => {
                                new ExportDispNameModal(this.app, 'index.zh-cn.md', (name) => {
                                    resolve(name);
                                }).open();
                            });
                        }
                        hugoLink = `{{< mdimporter url="content/${settings.blogPath}/${metadata.frontmatter.slug}/${fileName}" >}}`;
                    } else {
                        // 处理非展示性链接
                        const linkText = displayText || (fragment || file.basename);
                        const fragmentPart = fragment ? `#${fragment}` : '';
                        hugoLink = `[${linkText}]({{< ref "/${settings.blogPath}/${metadata.frontmatter.slug}/${fragmentPart}" >}})`;
                    }

                    modifiedContent = modifiedContent.replace(fullMatch, hugoLink);
                }
            } catch (error) {
                if (mode === 'single') {
                    new Notice(`❌ 处理链接失败: ${actualTarget}\n${error.message}`);
                }
                console.error(`处理wiki链接时出错:`, error);
            }
        });

        await Promise.all(promises);
        return modifiedContent;
    }
}
