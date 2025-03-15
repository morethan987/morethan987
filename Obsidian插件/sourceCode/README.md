## 不再进行专门的功能更新，毕竟功能上[Quartz](https://github.com/jackyzha0/quartz)已经可以说登峰造极了。

## 并且Quartz主创团队也表达了Hugo很难开发这个观点，从目前的实践看来确实如此。

## 因此这个插件仅为Blowfish的深度用户提供简单的Obsidian转换支持

# Hugo-blowfish-exporter

## 概述
这是一个简单的插件，用于将标准的 Obsidian markdown 文件转换为适用于 Hugo 格式，特别是 [Blowfish](https://blowfish.page/) 主题。

## 主要功能

目前该插件只支持 Obsidian 的一小部分功能，因为它已经覆盖了我自己的使用需求：
  - **callout**（支持所有官方的 callout 名称）
  - **内联数学公式**（Blowfish 支持代码块）
  - **mermaid**（支持 mermaid 图表）
  - **图片插入**（自动导出图片）
  - **Wiki链接导出**，非展示性链接支持段落链接和全文引用链接，展示性链接仅支持全文引用链接，并且尽量避免使用展示性引用链接

## 使用方法

### 设置说明

1. 在 Obsidian 设置中设置输出文件路径，该路径为导出文件保存的位置。
  
2. 设置图片导出路径，包含图片链接的 Obsidian 文件将使用此设置。

3. 设置网站的博客路径，即 Hugo 项目 `content` 文件夹下的相对路径。
   - 例如，我将设置为 `blog`，这意味着所有博客文件将存储在 `content/blog` 文件夹中。

4. 设置导出文件的名称，如果需要频繁改动文件名可以不启用默认文件名

5. 如果你的文章内部包含**展示性**的wiki链接，即类似于 `![[yourfile|你的文件]]` ，需要指定链接指向的语言版本。如果没有你需要的语言版本或者你并没有多语言需求，请在设置中配置默认语言版本并启用

### 导出当前文件
1. 打开命令面板，输入 `hugo`，即可看到相关命令。

### 导出所有已打开的 Vault 中的 md 文件
1. 点击页面上的一个按钮（如果没有禁用的话）。

## 注意事项

- Wiki 链接导出依赖于元数据 `slug`，即指向包含引用文件的文件夹名称。例如，如果我将文件的 `slug` 设置为 `pytips`，则表示在网站的根目录下，`content` 文件夹中应该有一个名为 `pytips` 的文件夹。

- Wiki链接导出，支持非展示性的段落和全文引用；展示性的仅支持全文引用，并且尽量避免使用展示性引用，原因是：为了避免展示性引用之间循环嵌套，嵌入的文本中会有部分Hugo简码未翻译，可能影响观感

## 样例仓库
我上传了一个Obsidian的 `exampleVault` 在我的源代码中，可以在Obsidian的沙箱里面进行测试

## 进一步开发

> 你可能会觉得：这个插件的功能有点简单！

**是的，我也这么认为！**

如果你愿意添加更多功能，欢迎克隆该仓库并进行修改！  
主文件 `main.ts` 中有详细的说明。

> 如果你能将修改后的代码上传给我，我将非常感激。🫡

---

# Hugo-blowfish-exporter

## Summary
This is a simple plugin to convert your standard Obisidian md file to a Hugo-friendly format, especially the [Blowfish](https://blowfish.page/) theme.

## Main Function

Now the plugin only support a little function of Obisidian since it already cover my own usage.
  - callout(support all the offical callout name)
  - inline math formular(blowfish supports the code block)
  - mermaid
  - image insert(auto export the images)
  - **Wiki link export**, non-display links support paragraph links and full-text citation links, display links only support full-text citation links, and try to avoid the use of display citation links

## How to use

### Settings explaination

1. set the output file path in the settings of Obisidian, this is the path that the output files are put.

2. set the image export path, the Obsidian files that contain image links will use this setting.

3. set the blog path of your website, the reletive path to the `content` folder.(There should be a `content` folder in your Hugo project)
  - For example, I set the settings to the `blog` ,which means all the blogs are stored in the folder `content/blog`.

4. set your default export name

5. If there are some **Displayed** Wiki Links in your file(`![[yourfile|text]]`), you may need to choose the language mode of your link. If your website is single-language, you can set the default language link mode in the settings.

### Export the opened file
1. call the command palette and type `hugo`, then you can see the relevant command.

### Export all the md files in the opend vault
1. There is a ribbon button you can click.(If you didn't ban that)

## Attention

- Wiki Link exportion relies on the meta data `slug` , which stands for the folder's name that contains the cited file. For example, now I set a file's `slug` as `pytips`, that means in your website root there should be a real folder named `pytips` in the `content` folder.

- Wiki link export supports non-display paragraphs and full-text citations; display paragraphs only support full-text citations. It is recommended to avoid using display citations as much as possible. The reason is to prevent circular nesting between display citations. Some Hugo shortcodes in the embedded text may not be translated, which may affect the appearance.

## exampleVault
There is an `exampleVaul` of Obsidian in my source code, you can test the plugin in the sandbox of Obsidian.

## Further develop

> You may think: How shallow the plugin is!

**Yes! I think so!**

If you are willing to add more function, feel free to clone the repository and modify it!
There are detailed explaination through the main file `main.ts`

> It's nice for you to upload your own modified code to me. My sincerely gratitude for that. 🫡
