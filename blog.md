### 前言

前几天在github发现一个蛮不错的Android Studio插件[ECTranslation](https://github.com/Skykai521/ECTranslation)，
在一些源码注释中遇到不认识的英文单词可以很方便地查看中文翻译。当时怀着好奇心也想试着开发一个小插件，在网上查资料发现插件开发的
资料很少，大部分blog都只是简单地搭建了个开发环境然后弹出个Hello World的对话框就完了，而jetbrains也只提供了一份[DevGuide](http://www.jetbrains.org/intellij/sdk/docs/index.html)
并没有比较详细的API文档。因此遇到大部分都只能啃它的那份英文指导手册和参考别人发布在github的插件源码。现在这个小插件完成得差不多
了，想把自己这几天的开发过程整理一下分享出来，希望能给其他有兴趣尝试plugin开发的朋友一点帮助。

### 明确需求

开发一款插件最先要考虑的当然是它要实现什么功能了。比如我想做的是varname-go-die主要功能就是让开发者有时候遇到起变量名但是不知道
英文怎么拼时，不需要切换到翻译软件去查找再copy过来，只需要在编辑器中输入中文就可以实现联网翻译，并且可以通过一个列表选择自己设置的
常用变量格式。
##### 这是我考虑实现的功能：
1. 在Android Studio设置界面有VarNameGoDie的设置选项，开发者可以根据自己对变量名的命名风格进行设置
2. 在编辑器输入并选取要转换的中文，快捷键启动一个ChangeVar的Action，联网查找翻译并弹出设置中的变量名格式列表，选择后替换编辑器中的中文
3. 在编辑器中输入英文单词也可以进行格式转换

具体的效果参见下图：



### 环境准备及项目创建
Android Studio是基于Intellij IDEA，网上查找后发现好像可以在Intellij IDEA中进行插件开发，Android Studio中new project是没有plugin选项的。

1. [下载Intellij IDEA](https://www.jetbrains.com/idea/#chooseYourEdition),Community是免费的
2. 创建plugin项目，File->New Project，然后按照下图操作，注意：关于Project SDK，如果没有需要创建一个，点击New，然后指向Intellij IDEA的安装目录就OK了。


###



