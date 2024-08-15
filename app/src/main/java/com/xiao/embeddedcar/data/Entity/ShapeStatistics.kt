package com.xiao.embeddedcar.data.Entity

/**
 * 形状统计
 */
class ShapeStatistics {
    //统计形状数量
    private var shapeStatistics = HashMap<String, Int>()

    /**
     * 获取指定形状的数量
     *
     * @param shapeName 三角形/矩形/菱形/五角星/圆形/总计
     * @return 数量
     */
    fun getCounts(shapeName: String): Int? {
        return shapeStatistics[shapeName]
    }

    /**
     * 设置形状的数量
     *
     * @param shapeStatistics 包含该形状数量的HashMap对象
     */
    fun setShapeStatistics(shapeStatistics: HashMap<String, Int>) {
        this.shapeStatistics = shapeStatistics
    }
}