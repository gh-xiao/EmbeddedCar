# 使用注意事项

* 等小车初始化完毕后(摄像头停止转动),再连接ntvu无线局域网,ntvu密码:NTVUntvu
* 在连接页面点击连接后将自动跳转到主页,建议再从侧边栏中选择配置页初始化配置
* assets文件夹中的模型文件,VID代表车型,TS代表交通标志物,各模型略有差别,请酌情使用

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

# ChatGPT/newBing FAQ:

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
* 我知道矩形和菱形的区别。矩形和菱形都是平行四边形，但是它们有不同的性质。
  矩形的特点是有一个角是直角，而且对边相等、平行。
  菱形的特点是邻边相等，而且对角线互相垂直。
* 我明白了。如果你想要判断一个矩形的边长和角度，你可以参考以下的步骤¹²：
    1. 加载你想要判断的图像，并将其转换为灰度图。
    2. 使用**Imgproc.Canny**方法来检测图像中的边缘，并得到一个二值化的图像。
    3. 使用**Imgproc.findContours**方法来寻找图像中的轮廓，并得到一个轮廓列表。
    4. 遍历轮廓列表，对每个轮廓使用**Imgproc.approxPolyDP**方法来近似一个多边形，并判断它是否是一个矩形。如果是，就获取它的顶点坐标。
    5. 对每个矩形，使用**Core.norm**方法来计算两个顶点之间的距离，也就是边长。然后，使用**Math.atan2**方法来计算两条相邻边之间的夹角。

    1) [Java中使用opencv_我啥都会的博客-CSDN博客_java opencv.](https://blog.csdn.net/qianlixiaomage/article/details/108416400)
       访问时间 2023/2/27.
    2) [Intro to OpenCV with Java | Baeldung.](https://www.baeldung.com/java-opencv) 访问时间 2023/2/27.
    3) [史上最全 java 集成 opencv 教程 - 掘金.](https://juejin.cn/post/6870109208730140686) 访问时间 2023/2/27.
* Core.norm()是一个用来计算向量或矩阵的范数（norm）的方法¹。范数是一种衡量向量或矩阵的大小或长度的函数。有不同类型的范数，例如L1范数，L2范数，无穷范数等。
  你可以通过指定**norm_type**参数来选择你想要使用的范数类型¹。
  在我们的示例代码中，我们使用了Core.norm()方法来计算两个顶点之间的距离，也就是边长。我们使用了默认的L2范数，也就是欧几里得距离²。这个方法接受两个**Scalar**
  对象作为参数，分别表示两个顶点的坐标，并返回一个**double**值作为结果。 访问时间 2023/2/27.
    1) Core (OpenCV 3.4.19 Java documentation)
       https://docs.opencv.org/3.4/javadoc/org/opencv/core/Core.html
    2) Java Examples & Tutorials of Core.normalize (org.opencv.core) - Tabnine
    3) https://www.tabnine.com/code/java/methods/org.opencv.core.Core/normalize
    4) Java Code Examples for org.opencv.core.core # norm()
       https://www.programcreek.com/java-api-examples/docs/?class=org.opencv.core.core&method=norm
* double epsilon = 0.01 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);这段代码有什么用？
  你好，这是Bing。这段代码的作用是计算一个轮廓的近似多边形¹。它使用了Imgproc.arcLength方法来求出轮廓的周长，然后乘以一个0.01的系数作为epsilon参数，表示近似多边形和原始轮廓之间的最大距离¹。如果最后一个参数是true，表示轮廓是闭合的¹。
    1) cv2.approxPolyDP() , cv2.arcLength() How these works.
       https://stackoverflow.com/questions/62274412/cv2-approxpolydp-cv2-arclength-how-these-works
    2) Android+Opencv图形的形状颜色识别_陈有乐的博客-CSDN博客_android 矩形识别,并提取矩形区域.
       https://blog.csdn.net/chenyouledashen/article/details/118067143
    3) Android+Opencv+Tesseract-ocr识别不同底色的车牌，蓝色，绿色(新能源)车牌.
       https://blog.csdn.net/chenyouledashen/article/details/118067702
    4) 0.01这个系数是一个精度因子，用来控制近似多边形的形状¹。它越小，近似多边形就越接近原始轮廓²。它越大，近似多边形就越简单，有更少的顶点³。你可以根据你的需要调整这个系数。
    5) cv2.approxPolyDP () , cv2.arcLength () How these works.
       https://stackoverflow.com/questions/62274412/cv2-approxpolydp-cv2-arclength-how-these-works
    6) OpenCV Contour Approximation - PyImageSearch.
       https://pyimagesearch.com/2021/10/06/opencv-contour-approximation/
    7) OpenCV ApproxPolyDP | Learn the Examples of OpenCV ApproxPolyDP - EDUCBA.
       https://www.educba.com/opencv-approxpolydp/