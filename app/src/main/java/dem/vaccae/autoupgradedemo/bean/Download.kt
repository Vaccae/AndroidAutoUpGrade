package dem.vaccae.autoupgradedemo.bean

import java.io.File

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 19:32
 * 功能模块说明：
 */
class Download {
    //下载进度
    var processvalue = 0

    //下载状态 0：未开始  1：下载中  2：下载完  -1：异常
    var state = 0;

    //文件
    var file: File? = null

    //信息
    var msg: String = ""
}