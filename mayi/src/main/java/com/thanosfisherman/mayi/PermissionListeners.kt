package com.thanosfisherman.mayi

/**
 * Functional interfaces for better compatibility with Java
 */

fun interface PermissionResultSingleListener {
    fun onPermissionResultAction(permissionBean: PermissionBean)
}

fun interface RationaleSingleListener {
    fun onRationaleAction(permissionBean: PermissionBean, permissionToken: PermissionToken)
}

fun interface PermissionResultMultiListener {
    fun onPermissionResultsAction(permissionBeans: List<PermissionBean>)
}

fun interface RationaleMultiListener {
    fun onRationalesAction(permissionBeans: List<PermissionBean>, permissionToken: PermissionToken)
}

fun interface ErrorListener {
    fun onErrorAction(e: Exception)
}
