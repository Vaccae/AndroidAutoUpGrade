package dem.vaccae.autoupgradedemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dem.vaccae.autoupgradedemo.dl.DownloadManager
import dem.vaccae.autoupgradedemo.dl.VersionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var tv: TextView
    private lateinit var btn: Button

    //下载地址
    private var url = "192.168.3.207:8027"//"192.168.3.207:8027"
    private val dlfilename = "TestUpgrade.apk"
    //版本号
    private var versionCode:Long =0
    //版本名称
    private var versionName =""

    //region 动态申请权限
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                CheckUpGrade(url)
            } else {
                Toast.makeText(this, "未开启权限.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //endregion

    fun CheckUpGrade(url:String){
        GlobalScope.launch(Dispatchers.Main) {
            try {
                var item = DownloadManager().ChkUpGrade(url)
                    .flowOn(Dispatchers.IO)
                    .first()

                item?.let {
                    if(it.versionCode > versionCode) {
                        val updateIntent =
                            Intent(this@MainActivity, DownloadActivity::class.java)
                        updateIntent.putExtra("url", it.appdownloadurl)
                        updateIntent.putExtra("filename", dlfilename)
                        updateIntent.putExtra("msg", "新版本：${it.versionCode}")
                        startActivity(updateIntent)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,e.message.toString(),Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv = findViewById(R.id.textView)
        btn = findViewById(R.id.button)


        if (allPermissionsGranted()) {
            //读取设备当前版本号
            versionCode = VersionHelper.getAppVersionCode(this)
            versionName = VersionHelper.getAppVersionName(this)
            val showmsg = "当前版本号:$versionCode 当前版本名称：$versionName"
            tv.text = showmsg

            btn.text = "检测升级"
            btn.setOnClickListener {
                CheckUpGrade(url)
            }
        }else {
            val showmsg = "需要先动态申请权限"
            tv.text = showmsg

            btn.text = "申请权限"
            btn.setOnClickListener {
                allPermissionsGranted()
            }
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }
}