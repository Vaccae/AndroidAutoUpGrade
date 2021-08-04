package dem.vaccae.autoupgradedemo

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import dem.vaccae.autoupgradedemo.dl.DownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.File

class DownloadActivity : AppCompatActivity() {

    lateinit var btndo: Button
    lateinit var progress: ProgressBar
    lateinit var tvstatus: TextView

    private var downloadurl: String = ""
    private var filename: String = "demo.apk"
    private var downloadmsg: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        downloadurl = intent.getStringExtra("url").toString()
        filename = intent.getStringExtra("filename").toString()
        downloadmsg = intent.getStringExtra("msg").toString()

        //加载控件
        tvstatus = findViewById(R.id.tvstatus)
        progress = findViewById(R.id.progressbar)
        btndo = findViewById(R.id.btndo)

        startdownload()
    }

    private fun startdownload() {
        try {
            val path =
                Environment.getExternalStorageDirectory().absolutePath + File.separator + "SUM" + File.separator
            //安装包路径
            val updateDir = File(path)
            //创建文件夹
            if (!updateDir.exists()) {
                updateDir.mkdirs()
            }

            val localpath: String = path + filename

            GlobalScope.launch(Dispatchers.Main) {
                DownloadManager().download(downloadurl, localpath)
                    .flowOn(Dispatchers.IO)
                    .onStart {
                        btndo.visibility = View.GONE
                        progress.progress = 0
                    }
                    .collect {
                        when (it.state) {
                            //下载中
                            1 -> {
                                tvstatus.text =
                                    "正在下载" + downloadmsg + "\r\n当前进度..... ${it.processvalue}%"
                                progress.progress = it.processvalue
                            }
                            //下载完成
                            2 -> {
                                val file = it.file
                                tvstatus.text = downloadmsg + it.msg
                                btndo.visibility = View.VISIBLE
                                btndo.text = "点击安装"
                                btndo.setOnClickListener {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    val uri: Uri
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        uri = FileProvider.getUriForFile(
                                            applicationContext,
                                            applicationContext.packageName + ".provider",
                                            file!!
                                        )
                                    } else {
                                        uri = Uri.fromFile(file)
                                    }

                                    intent.setDataAndType(
                                        uri,
                                        "application/vnd.android.package-archive"
                                    )

                                    startActivity(intent)
                                    finish()
                                }
                            }
                            //下载出错
                            -1 -> {
                                tvstatus.text = "下载" + downloadmsg + "失败！\r\n" + it.msg
                                btndo.visibility = View.VISIBLE
                                btndo.text = "关闭窗口"
                                btndo.setOnClickListener {
                                    finish()
                                }
                            }
                            else -> {
                                tvstatus.text = "异常"
                                btndo.visibility = View.VISIBLE
                                btndo.text = "关闭窗口"
                                btndo.setOnClickListener {
                                    finish()
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(this@DownloadActivity, e.message.toString(), Toast.LENGTH_SHORT)
                .show()
        }
    }
}