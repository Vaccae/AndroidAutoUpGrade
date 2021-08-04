package dem.vaccae.autoupgradedemo.dl

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dem.vaccae.autoupgradedemo.bean.Download
import dem.vaccae.autoupgradedemo.bean.UpGrade
import dem.vaccae.autoupgradedemo.net.retrofitAPIManager
import dem.vaccae.autoupgradedemo.net.retrofitUpGrade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 18:07
 * 功能模块说明：
 */
class DownloadManager {

    //获取通讯实例
    private fun GetClientAPI(url: String): retrofitUpGrade? {
        //设置通讯的BaseUrl
        retrofitAPIManager.SERVER_URL = "http://$url/"
        return retrofitAPIManager.provideClientApi(retrofitUpGrade::class.java)
    }

    suspend fun download(mUrl: String, mFilepath: String): Flow<Download> {
        //定义下载类
        var dlres = Download()

        lateinit var inputStream : InputStream
        lateinit var outputStream: OutputStream
        val fileReader = ByteArray(4096)
        val apkfile = File(mFilepath)

        return flow {
            //如果存在先删除再下载
            if (apkfile.exists()) {
                var result = apkfile.delete()
                if (!result) {
                    throw Exception("存储路径下的同名文件删除失败！")
                }
            }

            //获取通信实例
            val clientAPI: retrofitUpGrade? = GetClientAPI(mUrl)
            clientAPI?.let {
                //定义系统版本通讯Call
                val callupgrade: Call<ResponseBody> = it.DownLoadFile(mUrl);
                val body = callupgrade.execute().body()

                body?.let {

                    val fileSize: Long = it.contentLength()
                    var fileSizeDownloaded: Long = 0

                    inputStream = it.byteStream()
                    outputStream = FileOutputStream(apkfile)
                    var oldProgress = 0
                    var newprogress = 0
                    while (true) {
                        //此处加了10毫秒延时,可以防止下载没有完成的情况
                        Thread.sleep(10)
                        val read = inputStream.read(fileReader)
                        if (read == -1) {
                            break
                        }
                        outputStream.write(fileReader, 0, read)
                        fileSizeDownloaded += read.toLong()
                        newprogress = (fileSizeDownloaded * 1.0 / fileSize * 100).toInt()
                        Log.i("process", newprogress.toString())
                        if (oldProgress != newprogress) {
                            dlres.state = 1
                            dlres.processvalue = newprogress
                            Log.i("dlresprocess", dlres.processvalue.toString())
                            //发送当前进度
                            emit(dlres)
                        }
                        oldProgress = newprogress
                    }
                    outputStream.flush()

                    inputStream.close()
                    outputStream.close()
                    //下载完成
                    dlres.state = 2
                    dlres.msg = "下载完成"
                    dlres.file = apkfile
                    emit(dlres)
                }
            }
        }.catch {
            dlres.state = -1
            dlres.msg = it.message.toString()
            emit(dlres)
            throw it
        }.conflate()
        //conflate() 对应 LATEST 策略,如果缓存池满了，新数据会覆盖老数据
    }

    //使用协程时需要加关键字suspend
    suspend fun ChkUpGrade(url: String): Flow<UpGrade?> {
        var upgrade: UpGrade? = null

        return flow {
            //获取通信实例
            val clientAPI: retrofitUpGrade? = GetClientAPI(url)
            clientAPI?.let {
                //定义系统版本通讯Call
                val callupgrade: Call<ResponseBody> = it.ChkUpgrade();
                val rsp = callupgrade.execute()
                //判断返回体是否为null,如果是空返回参数Logininfo信息
                if (rsp.body() == null) {
                    throw Exception(rsp.message())
                } else {
                    //解析收到的JSON数据
                    val json = rsp.body()!!.string()
                    upgrade = Gson().fromJson(json, object : TypeToken<UpGrade?>() {}.type)
                }
            }
            emit(upgrade)
        }.catch { throw it }
    }

}