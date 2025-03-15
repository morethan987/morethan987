export interface HugoBlowfishExporterSettings {
    exportPath: string; // 导出路径配置
    imageExportPath: string; // 图片导出路径配置
    translatedExportPath: string; // 翻译文件导出路径配置
    BaseURL: string; // 大模型BaseURL
    ApiKey: string;  // API密钥
    ModelName: string; // 模型名称
    directExportAfterTranslation: boolean; // 翻译后直接导出
    targetLanguage: string; // 目标翻译语言
    translatedFilePrefix: string; // 翻译文件前缀
    blogPath: string; // 博客文章存放文件夹配置
    coverPath: string; // 封面图片文件夹配置
    useDefaultExportName: boolean;  // 是否使用默认导出文件名
    defaultExportName: string;      // 默认导出文件名
    useDefaultDispName: boolean;    // 是否使用默认展示性链接文件名
    defaultDispName: string;        // 默认展示性链接文件名
}