import { HugoBlowfishExporterSettings } from '../types/settings';

export const DEFAULT_SETTINGS: HugoBlowfishExporterSettings = {
    exportPath: '',
    imageExportPath: 'img',
    translatedExportPath: '',
    BaseURL: 'https://api.deepseek.com/v1',
    ApiKey: '',
    ModelName: 'deepseek-chat',
    targetLanguage: '英文',
    directExportAfterTranslation: false,
    translatedFilePrefix: '',
    blogPath: 'posts',
    coverPath: '.featured',
    useDefaultExportName: false,
    defaultExportName: '{{title}}',  // 支持使用 {{title}} 作为文件名占位符
    useDefaultDispName: false,
    defaultDispName: 'index.zh-cn.md'
}