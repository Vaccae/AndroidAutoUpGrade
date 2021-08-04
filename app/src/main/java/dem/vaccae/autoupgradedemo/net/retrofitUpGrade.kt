package dem.vaccae.autoupgradedemo.net

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 12:46
 * 功能模块说明：
 */
interface retrofitUpGrade {
    //检测服务器系统版本号
    @GET("download/upgrade.txt")
    fun ChkUpgrade(): Call<ResponseBody>

    //下载更新包
    @Streaming
    @GET
    fun DownLoadFile(@Url fileUrl:String):Call<ResponseBody>
}