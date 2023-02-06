package com.xiao.embeddedcar.Utils.Shape;

import java.util.HashMap;

/**
 * 形状统计
 */
public class ShapeStatistics {

    //统计形状数量
    private HashMap<String, Integer> shapeStatistics = new HashMap<>();

    /**
     * 获取指定形状的数量
     *
     * @param shapeName 三角形/矩形/菱形/五角星/圆形/总计
     * @return 数量
     */
    public Integer getCounts(String shapeName) {
        return shapeStatistics.get(shapeName);
    }

    /**
     * 设置形状的数量
     *
     * @param shapeStatistics 包含该形状数量的HashMap对象
     */
    public void setShapeStatistics(HashMap<String, Integer> shapeStatistics) {
        this.shapeStatistics = shapeStatistics;
    }
}
