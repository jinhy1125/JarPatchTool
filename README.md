# JarPatchTool

使用方法 `java -jar JarPathTool-{version}.jar`。

简约的目录结构如下：

```
+---20240712
|   \---update001
|       +---clzx-dzjz-bp
|       |   +---BOOT-INF
|       +---dossier
|       |   +---BOOT-INF
|       \---settings
|           +---BOOT-INF
\---20240713
    \---update001
        +---clzx-dzjz-bp
        |   +---BOOT-INF
        +---dossier
        |   +---BOOT-INF
        \---settings
            +---BOOT-INF
```

以上如 dossier 都是代码构建打包解压后完整的文件夹。

工具第一个输入框输入的是系统文件夹名称 `clzx-dzjz-bp,dossier,dist,settings`（多填不会有问题，不过新旧目录中如果不存在对应的目录则不会生成相应的增量文件）

工具第二个输入框输入的是基础版本程序所在的目录，如 `...\20240712\update001`。

工具第三个输入框输入的是升级版本程序所在的目录，如 `...\20240713\update001`。

点击按钮后会在升级版本所在的目录生成一个以时间戳命名的文件夹，里面即增量文件。（可能会包含 .needDelete 文件，用于删除旧包里的内容）

打包命令：

mvn clean compile assembly:single