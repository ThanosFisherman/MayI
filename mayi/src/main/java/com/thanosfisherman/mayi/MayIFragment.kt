@file:Suppress("DEPRECATION")

package com.thanosfisherman.mayi

import android.app.Fragment
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi


class MayIFragment : Fragment(), PermissionToken {

    companion object {
        const val TAG = "MayIFragment"
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    private var permissionResultSingleListener: ((PermissionBean) -> Unit)? = null
    private var rationaleSingleListener: ((PermissionBean, PermissionToken) -> Unit)? = null
    private var permissionResultMultiListener: ((List<PermissionBean>) -> Unit)? = null
    private var rationaleMultiListener: ((List<PermissionBean>, PermissionToken) -> Unit)? = null
    private var isShowingNativeDialog: Boolean = false
    private lateinit var rationalePermissions: List<String>
    private lateinit var permissionMatcher: PermissionMatcher

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            isShowingNativeDialog = false
            if (grantResults.isEmpty())
                return
            val beansResultList = mutableListOf<PermissionBean>()

            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permissions[i]))
                        beansResultList.add(PermissionBean(permissions[i], isGranted = false, isPermanentlyDenied = false))
                    else
                        beansResultList.add(PermissionBean(permissions[i], isGranted = false, isPermanentlyDenied = true))
                } else {
                    beansResultList.add(PermissionBean(permissions[i], isGranted = true, isPermanentlyDenied = false))
                }
            }

            permissionResultSingleListener?.invoke(beansResultList[0])

            permissionResultMultiListener?.let {

                val grantedBeans = permissionMatcher.grantedPermissions.map { PermissionBean(it, true) }
                it(beansResultList.plus(grantedBeans))
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    internal fun checkPermissions(permissionMatcher: PermissionMatcher) {
        this.permissionMatcher = permissionMatcher
        rationalePermissions = permissionMatcher.deniedPermissions.filter(this::shouldShowRequestPermissionRationale)
        val rationaleBeanList = rationalePermissions.map { PermissionBean(it) }

        if (rationaleBeanList.isEmpty()) {
            if (!isShowingNativeDialog)
                requestPermissions(permissionMatcher.deniedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            isShowingNativeDialog = true
        } else {
            rationaleSingleListener?.invoke(rationaleBeanList[0], PermissionRationaleToken(this))
            rationaleMultiListener?.invoke(rationaleBeanList, PermissionRationaleToken(this))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun continuePermissionRequest() {
        if (!isShowingNativeDialog)
            requestPermissions(permissionMatcher.deniedPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        isShowingNativeDialog = true
    }

    override fun skipPermissionRequest() {
        isShowingNativeDialog = false
        permissionResultSingleListener?.invoke(PermissionBean(rationalePermissions[0]))


        val totalBeanGranted = permissionMatcher.grantedPermissions.map { PermissionBean(it, true) }
        val totalBeanDenied = permissionMatcher.deniedPermissions.map { PermissionBean(it) }
        val totalBeanPermanentlyDenied = permissionMatcher.permissions
                .filterNot { s -> permissionMatcher.deniedPermissions.contains(s) }
                .filterNot { s -> permissionMatcher.grantedPermissions.contains(s) }
                .map { PermissionBean(it, isGranted = false, isPermanentlyDenied = true) }
        permissionResultMultiListener?.invoke(totalBeanGranted.asSequence()
                .plus(totalBeanDenied)
                .plus(totalBeanPermanentlyDenied)
                .toList())
    }

    internal fun setListeners(listenerResult: ((PermissionBean) -> Unit)?,
                              listenerResultMulti: ((List<PermissionBean>) -> Unit)?,
                              rationaleSingle: ((PermissionBean, PermissionToken) -> Unit)?,
                              rationaleMulti: ((List<PermissionBean>, PermissionToken) -> Unit)?) {
        permissionResultSingleListener = listenerResult
        permissionResultMultiListener = listenerResultMulti
        rationaleSingleListener = rationaleSingle
        rationaleMultiListener = rationaleMulti
    }

    /*  private fun isPermissionsDialogShowing(): Boolean {
          val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
          val cn = am.getRunningTasks(1).get(0).topActivity
          return "com.android.packageinstaller.permission.ui.GrantPermissionsActivity" == cn.className
      }*/
}