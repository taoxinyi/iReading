
- [1. iReading](#1-ireading)
    - [1.1. demo](#11-demo)
- [2. MyApplication](#2-myapplication)
    - [2.1. MainActivity](#21-mainactivity)
        - [2.1.1. ArticleListFragment  阅读](#211-articlelistfragment)
        - [2.1.2. WordSearchFragment 查词](#212-wordsearchfragment)
        - [2.1.3. CollectionFragment 收藏](#213-collectionfragment)
        - [2.1.4. AboutFragment 关于](#214-aboutfragment)
    - [2.2. ArticleDetailActivity](#22-articledetailactivity)
    - [2.3. WordDetailActivity](#23-worddetailactivity)
    - [2.4. SettingsActivity](#24-settingsactivity)
- [3. 技术重点](#3)
    - [3.1. 进程](#31)
    - [3.2. 进程创建过程](#32)
    - [3.3. 收藏/取消收藏的过程](#33)
    - [3.4. 主要API](#34-api)

<!-- /TOC -->
# 1. iReading
iReading, an English Learning Android App aims to improve people's English reading experience, which features browsing news/articles, single tapping to fetch the meaning and providing offline/online dictionary.
## 1.1. demo


![Demo](/demo/demo.gif "Demo")

# 2. MyApplication

App类，每个进程开启时会调用

- 加载数据库
  - 文章数据库 `daoArticle`
  - 离线单词数据库 `daoDictionary`(默认不可更改)
  - 用户单词收藏数据库`daoCollection`
- 加载用户设置文件（目前主要只有单词刷新的个数）
- 注册`EventBus`进行单进程内收发事件
- 注册广播用来多进程通信
## 2.1. MainActivity

打开软件开启的Activity，开启新的进程`main`，主要完成初始化各Fragment的操作
内含Fragment:
- `ArticleListFragmen`(默认显示) 显示文章列表
- `WordSearchFragment` 单词搜索
- `CollectionFragment` 收藏
    - `WordCollectionNestedFragment` 单词收藏
    - `ArticleCollectionNestedFragment` 文章收藏
- `AboutFragment`关于
### 2.1.1. ArticleListFragment  阅读
打开应用后默认显示的Fragment，用于接受文章
- 侧滑选择源（默认为所有源）
    - 所有源(`EventRegistry`所提供的全球源，每分钟刷新均能收到全球英文最新资讯)
    - 精品源
        - ("National Geographic", "国家地理");
        - ("Nature", "自然");
        - ("The Economist", "经济学人");
        - ("TIME", "时代");
        - ("The New York Times", "纽约时报");
        - ("Bloomberg Business", "彭博商业");
        - ("CNN", "有线电视新闻网");
        - ("Fox News", "福克斯新闻");
        - ("Forbes", "福布斯");
        - ("Washington Post", "华盛顿邮报");
        - ("The Guardian", "卫报");
        - ("The Times", "泰晤士报");
        - ("Mail Online", "每日邮报");
        - ("BBC", "英国广播公司");
        - ("PEOPLE", "人物");
- 获取阅读简要信息（下拉刷新，上滑加载）
- 显示缩略图（缺省为logo),名称，发布的源以及发布时间，其中发布时间由独立线程每分钟更新一次UI
- 侧滑每一个简要信息可以收藏/取消收藏
- 点击右上角可以搜索，选择不同的源进行搜索（默认为当前源开始搜索）
- 双击标题栏返回顶部
- 点击某个文章进入文章详情(`ArticleDetailActivity`,为新的进程)

### 2.1.2. WordSearchFragment 查词
- 使用离线词库，每页最多显示20个，默认显示从a开始，显示单词（词组）的收藏情况，名称，词性，解释
- 搜索栏输入可以进行查词，下方会动态更新候选，不用点搜索
- 可以侧滑每个单词进行收藏/取消收藏
- 点击某个单词进入单词详情(`WordDetailActivity`,为新的进程)

### 2.1.3. CollectionFragment 收藏
显示文章和单词的收藏，分为2个子Fragment
- `WordCollectionNestedFragment` 单词收藏
    - 显示已收藏的单词，内容与单词搜索一致
    - 理想情况按时间分类，显示在可折叠窗口。今天新增的在"今天"类中，历史收藏可分为"一周内"、"一月内"、"三月内"、"半年内"、"一年内"、"一年外"……
    - 单词侧滑收藏/取消收藏
    - 右上角点击显示释义/取消释义，方便背单词
    - 点击某个单词进入单词详情(`WordDetailActivity`,为新的进程)
- `ArticleCollectionNestedFragment` 文章收藏
    - 显示已收藏文章，内容与阅读一致(可以考虑时间改为收藏时间)
    - 理想情况按时间分类，显示在可折叠窗口。今天新增的在"今天"类中，历史收藏可分为"一周内"、"一月内"、"三月内"、"半年内"、"一年内"、"一年外"……
    - 单词侧滑收藏/取消收藏
    - 点击某个进入文章详情(`ArticleDetailActivity`,为新的进程)
### 2.1.4. AboutFragment 关于
显示关于信息和提供设置
- 软件名
- 版本号
- 作者
- 设置,点击后跳入`SettingsActivity`,注意不是新的进程，而与当前`MainActivity`同一进程
- (捐赠）

## 2.2. ArticleDetailActivity

点击文章后(刷新，加载，搜索，收藏)后跳转的Activity，开启新的进程`article`，显示文章内容。在加载未完毕时出现加载页面，无网络时出现无网络页面，从而进行过渡。

- ToolBar包括返回上一个Activity(进程)，收藏该文章，同时支持侧滑返回
- 正文内容
    - 文章标题
    - 文章分类
    - 文章源
    - 文章发布时间
    - 图片(如果有)
    - 正文
- 点击查词：弹出单词简要概述窗口(理想情况可以让用户选择偏好：优先显示离线词库/在线词库/全显示)。在弹出窗口点击任意区域跳转到该单词详情页`WordDetailActivity`,为新进程
    -  单词
    -  收藏情况(点击可收藏/取消收藏)
    -  音标(如果存在)
    -  朗读(如果存在)
    -  词性
    -  含义
## 2.3. WordDetailActivity
点击单词后(搜索，收藏，阅读中的弹窗)跳转到的Activity，开启新的进程`word`或者在`word`中共享(如果这不是第一个点击的单词详情页)
- ToolBar包括返回上一个Activity(进程)，该单词，收藏该单词，同时支持侧滑返回
- 折叠区
    - 金山词霸源
        - 单词
        - 音标
        - 发音
        - 词性
        - 例句
    - 离线词典源
        - 单词
        - 词性
        - 释义
        - 例句及翻译（如果有）
- 点击查词，同`ActicleDetailActivity`,目前只有金山词霸源可以点击，理想情况在折叠区所有单词均可点击
- 注意，在此界面，弹出任意单词（非本单词）的弹窗后会跳入此单词的`WordDetailActivity`，即可无限开启单词嵌套查询，注意每次都在该进程中，内存容量会随着Activity的增加而逐渐增大(约每次150Mb)，但是由于进程的原因，每次返回均会销毁该进程，因此不会内存泄露。

## 2.4. SettingsActivity
设置区，点击关于界面中的设置中进入,可以侧滑返回(Toolbar中的返回正在做)，注意并非新的进程，而是和`MainActivity`同一进程
- 显示当前缓存并可清除(清除的大多为图片缓存)，注意，此举不会删除用户任何收藏的数据库。
- 设置每次刷新/加载的数量，主要用于节省流量用


# 3. 技术重点
## 3.1. 进程
    总共有三个进程
- `main`
    - `MainActivity` 一打开app即存在，一直在main中存活直到完整退出应用
    - `SettingActivity` 进入设置后存在，退出后结束，共享main这个进程
- `word`
    - `WordDetailActivity` 所有的单词详情页均在此进程中
- `article`
    - `ArticleDetailActivity` 文章详情页
## 3.2. 进程创建过程
每个进程的创建过程为
- 启动新的`MyApplication`,分配独立的内存区域
- 启动各自的Activity(注意单词详情页`WordDetailActivity`开启n个，即该n个Activity共享进程`word`)
- 退出后回收所有内存
## 3.3. 收藏/取消收藏的过程
前提条件是各个进程创建时，`MyApplication`注册广播接收器(用于多进程)
各个Activity/Fragment创建时,注册`EventBus`(用于单进程)
- 点击收藏/取消收藏或者图标
- 先更新UI(图标切换，字切换)
- 通过`EventBus`发出需要更改数据库的事件
    - `changeArticleCollectionDBEvent`
    - `changeWordCollectionDBEvent`
- 当前进程的`MyApplication`接收，修改数据库
- 向各个进程广播数据库已被更改，以及更改的内容
- 的所有存活的不同进程中各自的`MyApplication`(包括先前发出广播的)的广播接收器通过`EventBus`告知与收藏相关的Activity/Fragment数据库已被更改，并告知更改的内容
    - `ArticleDataSetChangedEvent`
    - `WordDataSetChangedEvent`
- 与收藏相关的Activity/Fragment数据库通过`EventBus`得知数据库更改，获取更改的内容，更新UI
## 3.4. 主要API
- `EventRegistry` 获取新闻数据
- `金山词霸` 获取在线词库
<!-- TOC -->
