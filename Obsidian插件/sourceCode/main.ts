import { Plugin } from 'obsidian';
import HugoBlowfishExporter from './src/core/plugin';

export default class MainPlugin extends Plugin {
    private exporter: HugoBlowfishExporter;

    async onload() {
        this.exporter = new HugoBlowfishExporter(this.app, this);
        await this.exporter.initialize();
    }

    async onunload() {
        // 清理工作可以在这里进行
    }
}
