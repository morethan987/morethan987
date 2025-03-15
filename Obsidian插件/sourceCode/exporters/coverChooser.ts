import * as path from 'path';
import * as fs from 'fs';

export class CoverChooser {
    constructor() {}

    async chooseCover( settings: any, slugDir: string) {
        // settings.blogPath和settings.coverPath拼接形成封面图片文件夹的绝对路径
        const exportDir = path.resolve(settings.exportPath);
        const coverDir = path.join(
            exportDir,
            settings.blogPath,
            settings.coverPath
        );

        // 检查目标路径是否已存在background.svg
        const backgroundTarget = path.join(slugDir, "background.svg");
        if (!fs.existsSync(backgroundTarget)) {
            // 先从封面路径中复制一个固定的背景图片background.svg到目标路径下
            const backgroundFile = path.join(coverDir, "background.svg");
            if (fs.existsSync(backgroundFile)) {
                try {
                    await fs.promises.copyFile(backgroundFile, path.join(slugDir, "background.svg"));
                } catch (error) {
                    console.error("复制背景图片失败:", error);
                }
            }
        }

        

        // 检查目标路径是否已存在featured.svg
        const featuredTarget = path.join(slugDir, "featured.svg");
        if (fs.existsSync(featuredTarget)) {
            return; // 如果已存在，直接返回
        }

        // 在封面文件夹中随机选择封面图片，只选择svg格式的图片
        // 复制到目标路径下，并重命名为featured.svg
        try {
            const files = await fs.promises.readdir(coverDir);
            const svgFiles = files.filter(file => 
                file.endsWith('.svg') && file !== 'background.svg'
            );
            
            if (svgFiles.length > 0) {
                const randomSvg = svgFiles[Math.floor(Math.random() * svgFiles.length)];
                await fs.promises.copyFile(path.join(coverDir, randomSvg), featuredTarget);
            }
        } catch (error) {
            console.error("复制封面图片失败:", error);
        }
    }
}