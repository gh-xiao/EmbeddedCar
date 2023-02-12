EmbeddedCar
===
原创嵌入式智能小车Android控制终端

## 版本号: 2.3.0

### 2023/2/11

    #修复部分Bug
    #大幅度重新优化红绿灯识别代码
    #清除最原始的TrafficLight类,并将TrafficLight_fix类改名为TrafficLight

## 版本号: 2.2.0

### 2023/2/10

    #完成基于车型检测的车牌识别

## 版本号: 2.1.0

### 2023/2/9

    #添加车型识别
    #微调TFT智能裁剪参数
    #优化ModuleViewModel的module()方法中的线程启动

## 版本号: alpha2.0.5

### 2023/2/8

    #完善基于颜色过滤的车牌识别模块
    #完善交通标志物识别模块(实验性)

## 版本号: alpha2.0.4

### 2023/2/7

    #更新BaiDuOCR-Mod-debug.arr结果解析方式,现在使用Google提供的Gson进行参数传递,方便对结果进行序列化和反序列化

## 版本号: alpha2.0.3

### 2023/2/7

    #继续向AnalyseFragment添加新功能,现在可以通过导入导出HSV参数控制TFT智能裁剪

## 版本号: alpha2.0.2

### 2023/2/6

    #完善AnalyseFragment

## 版本号: alpha2.0.1

### 2023/2/5

    #恢复大部分注释
    #重新优化大部分代码

## 版本号: alpha2.0.0

### 2023/2/4

    #意外删除源代码，现已恢复...

## 版本号: Dev0.1.0

### 2023/1/7 - 1/16

    #完成大部分功能的添加

### 2023/1/17

    #添加并修复USB转串口通讯

## 版本号: Dev0.0.1

### 2023/1/5 - 1/7

    #更新ConnectFragment使用基于MVVM设计模式的连接方式

### 2023/1/7

    #大幅修改对象实例化方式,目前主要使用的对象将进行单例化实例

# 其他资料:

* Android的[Context](https://www.jianshu.com/p/57220504efd2)
* 了解MVVM设计模式可以前往[这里](https://blog.csdn.net/m0_70748845/article/details/125730730)
  ,或者[这里](https://blog.csdn.net/luoj_616/article/details/121166549)
  ,以及[这里](https://www.bilibili.com/video/BV1ES4y1x7we?p=3&share_source=copy_web)
* 有关AGP与gradle版本之间的关联请前往[这里](https://developer.android.google.cn/studio/releases/gradle-plugin.html)
* 全局异常捕获处理可以前往[这里](https://www.jianshu.com/p/9b2f43d87c9f)
  ,或者[这里](https://blog.csdn.net/shankezh/article/details/79332004)
  ,以及[这里](https://blog.csdn.net/cqn2bd2b/article/details/126435256)
* 了解RecyclerView如何使用请前往[这里](https://www.jianshu.com/p/0bd4bc12c170)
  或者[这里](https://blog.csdn.net/qq_29882585/article/details/108818849)
* 有关WeChatQRCode的学习资料可参阅[这里](https://www.wanandroid.com/blog/show/3041)
* [morphologyEx(形态学操作)](https://www.jianshu.com/p/ee72f5215e07)
* 结构元形状构造函数[getStructuringElement()](https://blog.csdn.net/weixin_41695564/article/details/79928835)
* [FragmentManager](https://blog.csdn.net/azurelaker/article/details/84310053)
* 关于Android横向二级联动菜单的实现可以前往[这里](https://blog.csdn.net/xiaole0313/article/details/53674147)
  ,或者[这里](https://cloud.tencent.com/developer/article/1035815?from=article.detail.1481712)
* [AndroidPicker](https://github.com/gzu-liyujiang/AndroidPicker/blob/master/API.md)
* 自定义Toolbar在[这里](https://blog.csdn.net/baidu_41616022/article/details/117912975)
  ,或[这里](https://blog.csdn.net/qq_43441284/article/details/125438810)