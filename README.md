EmbeddedCar
===
原创嵌入式智能小车Android控制终端

## 版本号: 2.4.4

### 2023/2/21

    #优化布局文件中硬编码的文本,现在使用strings.xml文件引用
    #修复某些控件因为跳转导致的bug

### 2023/2/24

    #添加由ChatGPT生成的内容并加以注释
    #在该页下方添加由ChatGPT生成的知识

## 版本号: 2.4.3

### 2023/2/20

    #微调布局
    #添加红绿灯检测位置配置
    #完善RFID读卡数据获取

## 版本号: 2.4.2

### 2023/2/13

    #调整识别参数

## 版本号: 2.4.1

### 2023/2/13

    #继续修复模块bug

## 版本号: 2.4.0

### 2023/2/13

    #添加设备连接页面点击连接后自动跳转到主页的action
    #继续修复模块bug

## 版本号: 2.3.1

### 2023/2/12

    #大幅度重新优化红绿灯识别代码
    #清除最原始的TrafficLight类,并将TrafficLight_fix类改名为TrafficLight
    #向半自动方法预留结果发送

## 版本号: 2.3.0

### 2023/2/11

    #修复部分Bug

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

# 以下内容基于ChatGPT生成,请自行分辨真伪

* OpenCV库中还有一些用于图像色彩校正的函数，例如cv2.inRange函数，用于将图像中的某些颜色范围转换为白色或黑色；
  cv2.cvtColor函数，用于将图像从一种颜色空间转换为另一种颜色空间；
  cv2.normalize函数，用于将图像的每个像素值转换为指定范围内的值；
  cv2.bilateralFilter函数，用于将图像中的噪声去除；
  cv2.GaussianBlur函数，用于将图像中的噪声去除；
  cv2.adaptiveThreshold函数，用于将图像中的某些区域转换为白色或黑色；
  cv2.equalizeHist函数，用于计算图像的白平衡；
  cv2.dilate函数，用于将图像中的某些区域扩大；
  cv2.erode函数，用于将图像中的某些区域缩小；
  以及cv2.morphologyEx函数，用于执行形态学变换。
* 我可以为您编写一份基于Java语法并使用opencv库函数的白平衡代码。
  首先，您需要在您的代码中导入opencv库，以便您可以使用opencv库中的函数。
  然后，您可以使用opencv库中的函数来计算图像的白平衡。
  您可以使用opencv库中的cv2.cvtColor函数将图像转换为HSV格式，然后使用cv2.equalizeHist函数来计算白平衡。
  最后，您可以使用cv2.cvtColor函数将图像转换回RGB格式，以便您可以显示白平衡后的图像。

# ChatGPT FAQ:

* OpenCV还提供了一些额外的色彩校正工具，比如色彩校正滤镜、色彩增强滤镜等。
* 小目标在模型中有哪些优缺点？
  小目标在模型中有许多优点，例如可以提高模型的准确性和精度，并且可以更有效地处理小目标的检测问题。
  此外，小目标还可以更有效地处理复杂的场景，从而提高模型的性能。
  然而，小目标也有一些缺点，例如它们的检测可能会受到噪声的影响，从而导致模型的准确性降低。
* 标签样例数据量差别非常大会影响到模型吗？
  是的，标签样例数据量差别非常大会影响模型的性能。如果某个类别的样例数据量过少，模型可能无法有效地学习到该类别的特征，从而导致模型的准确性降低。
  因此，建议您尽量保持样例数据量的平衡，以确保模型的性能。
* 每个标签下推荐放置多少张样例？
  建议每个标签下至少放置10张样例，以确保模型能够有效地学习到每个类别的特征。此外，您还可以根据自己的需要添加更多的样例，以提高模型的准确性。