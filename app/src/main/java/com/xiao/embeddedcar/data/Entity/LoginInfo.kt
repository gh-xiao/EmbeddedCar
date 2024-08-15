package com.xiao.embeddedcar.data.Entity

data class LoginInfo(
    //设备IP
    var iP: String? = null,

    //摄像头IP
    var iPCamera: String? = null,

    @JvmField
    var pureCameraIP: String? = null
) {
    override fun toString(): String {
        return """
               LoginInfo{IP='${iP}', IPCamera='${iPCamera}', pureCameraIP='$pureCameraIP'}
               """.trimIndent()
    }
}