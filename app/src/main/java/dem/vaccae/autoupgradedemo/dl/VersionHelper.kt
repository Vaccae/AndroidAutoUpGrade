package dem.vaccae.autoupgradedemo.dl

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 11:06
 * 功能模块说明：
 */
class VersionHelper {

    companion object {
        //获取当前版本Code
        fun getAppVersionCode(context: Context): Long {
            var versionCode: Long = 0
            try {
                var packageInfo: PackageInfo = context.applicationContext
                    .packageManager
                    .getPackageInfo(context.packageName, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    versionCode = packageInfo.longVersionCode;
                } else {
                    versionCode = packageInfo.versionCode.toLong();
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("", e.message!!);
            }
            return versionCode;
        }

        //获取当前版本名称
        fun getAppVersionName(context: Context): String {
            var versionName = ""
            try {
                var packageInfo: PackageInfo = context.applicationContext
                    .packageManager
                    .getPackageInfo(context.packageName, 0);
                versionName = packageInfo.versionName;
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("", e.message!!);
            }
            return versionName;
        }
    }
}