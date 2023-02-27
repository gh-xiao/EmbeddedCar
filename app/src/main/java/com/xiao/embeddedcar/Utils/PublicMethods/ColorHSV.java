package com.xiao.embeddedcar.Utils.PublicMethods;

/**
 * <p>Hsv色彩空间范围</p>
 * <p>以下排序使用HSV的H(色彩度数)排序</p>
 * <p>数组下标为2,4,6时是最小值,下标为1,3,5时是最大值</p>
 * <p>由于HSV模型的特性,红色被划分为了两个部分,因而使用了两个数组</p>
 */
@SuppressWarnings("ALL")
public final class ColorHSV {
    /* 图形识别色彩数据 */
    //所有H(色彩度)
    public static final int[] allHSV = new int[]{0, 180, 0, 255, 150, 255, 110};
    //红色:0-20
    public static final int[] redDownHSV = new int[]{0, 25, 0, 255, 110, 255, 110};
    public static final int[] redDownHSV1 = new int[]{0, 25, 0, 255, 150, 255, 110};
    //红色:160-180
    public static final int[] redUpHSV = new int[]{0, 180, 160, 255, 150, 255, 110};
    public static final int[] redUpHSV1 = new int[]{0, 180, 150, 255, 110, 255, 110};
    //黄色
    public static final int[] yellowHSV = new int[]{0, 43, 25, 255, 150, 255, 110};
    public static final int[] yellowHSV1 = new int[]{0, 43, 25, 255, 110, 255, 225};
    //绿色
    public static final int[] greenHSV = new int[]{0, 70, 55, 255, 150, 255, 110};
    public static final int[] greenHSV1 = new int[]{0, 70, 40, 255, 150, 255, 110};
    //青色
    public static final int[] cyanHSV = new int[]{0, 95, 85, 255, 150, 255, 110};
    //蓝色
    public static final int[] blueHSV = new int[]{0, 120, 110, 255, 150, 255, 110};
    public static final int[] blueHSV1 = new int[]{0, 130, 95, 255, 225, 255, 110};
    public static final int[] blueHSV2 = new int[]{0, 120, 85, 255, 210, 255, 195};
    public static final int[] blueHSV3 = new int[]{0, 120, 85, 255, 210, 255, 110};
    //蓝色 - 红色取反
    public static final int[] red2blueHSV = new int[]{0, 140, 110, 255, 150, 255, 110};
    //紫色
    public static final int[] purpleHSV = new int[]{0, 160, 125, 255, 150, 255, 110};
    public static final int[] purpleHSV1 = new int[]{0, 155, 125, 255, 150, 255, 110};
    public static final int[] purpleHSV2 = new int[]{0, 155, 125, 255, 125, 255, 110};
    public static final int[] purpleHSV3 = new int[]{0, 155, 125, 255, 125, 255, 200};
    //实验性 - 白
    public static final int[] whiteHSV = new int[]{0, 110, 0, 60, 0, 255, 225};
    //实验性 - 黑
    public static final int[] blackHSV = new int[]{0, 100, 47, 225, 50, 60, 0};

    /* 车牌识别数据 */
    //浅蓝0、//黄色1、//品红2、//浅红色3、//蓝色4、//青色5、// 深红色6、//黑色7      车牌蓝底9  车牌绿底10
    public static double[][] PlateDetector_HSV_VALUE_LOW = {
            {10, 163, 147}, //浅蓝0
            {77, 163, 147}, //黄色1
            {146, 212, 140},//品红2
            {126, 155, 160},//浅红色3
            {0, 204, 178},  //蓝色4
            {35, 163, 147}, //青色5
            {110, 155, 160},//深红色6
            {0, 0, 0},      //黑色7
            {0, 0, 192},    //标准蓝8
            {0, 190, 190},  //车牌蓝底9     暗的TFT：0,190,190   亮的：0,180,190
            {22, 195, 158}, //车牌绿底10    暗的TFT H:21 S要调高一点:210  V:211  亮的TFT S值要调底一点：110    10,100,148
            {65, 0, 200},   //新能源车牌白变绿渐变
    };

    public static double[][] PlateDetector_HSV_VALUE_HIGH = {
            {47, 255, 255},     //浅蓝0
            {111, 255, 255},    //黄色1
            {241, 255, 255.0},  //品红2
            {150, 255, 255},    //浅红色3
            {21, 255, 255},     //蓝色4
            {75, 255.0, 255},   //青色5
            {150, 255, 255},    //深红色6
            {180, 255, 120},    //黑色7
            {45, 238, 255},     //标准蓝8
            {28, 255, 255},     //车牌蓝底9   亮暗一样
            {73, 255, 255},     //车牌绿底10   暗H:66     亮H:83
            {110, 255, 255},    //新能源车牌白变绿渐变
    };

    //浅蓝0、//黄色1、//品红2、//浅红色3、//蓝色4、//青色5、// 深红色6、//黑色7
    //暗 S、V=214,211     亮 S、V=176,160
    //浅蓝0、//黄色1、//品红2、//浅红色3、//蓝色4、//青色5、// 深红色6、//黑色7      车牌蓝底9  车牌绿底10
    public static double[][] HSV_VALUE_LOW = {
            {13, 176, 160},//浅蓝0  12,214,211
            {67, 176, 160},//黄色1
            {130, 176, 160},//品红2  暗：100, 176,160   亮：130,176,160
            {126, 176, 160},//浅红色3
            {0, 176, 160},//蓝色4
            {30, 176, 160},//青色5   35
            {103, 176, 160},// 深红色6
            {0, 0, 0},//黑色7   暗：0,187,0   亮：0,0,0
            {0, 0, 192},//标准蓝8
            {0, 150, 190},//车牌蓝底9      暗的TFT：0,190,190   亮的：0,180,190
            {22, 104, 161},//车牌绿底10    暗的TFT H:21 S要调高一点:210  V:211  亮的TFT S值要调底一点：110    10,100,148
    };

    public static double[][] HSV_VALUE_HIGH = {
            {30, 255, 255},//浅蓝0
            {111, 255, 255},//黄色1
            {241, 255, 255.0},//品红2
            {150, 255, 255},//浅红色3
            {12, 255, 255},//蓝色4
            {70, 255.0, 255},//青色5   90
            {150, 255, 255},// 深红色6
            {255, 255, 150},//黑色7   暗：28,255,184    亮：255,255,150
            {45, 238, 255},//标准蓝8
            {126, 255, 255},//车牌蓝底9   亮暗一样
            {120, 255, 255},//车牌绿底10   暗H:66     亮H:83
    };
}