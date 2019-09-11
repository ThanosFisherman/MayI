package com.thanosfisherman.mayi

interface IPermissionBuilder {
    fun onErrorListener(errorListener: (Exception) -> Unit): IPermissionBuilder
    fun check()

    interface Permission {
        fun withPermission(permission: String): SinglePermissionBuilder

        fun withPermissions(vararg permissions: String): MultiPermissionBuilder

        //IPermissionBuilder.MultiPermissionBuilder withPermissions(Collection<String> permissions);
    }

    interface SinglePermissionBuilder : IPermissionBuilder {
        fun onResult(response: (PermissionBean) -> Unit): SinglePermissionBuilder
        fun onRationale(rationale: (PermissionBean, PermissionToken) -> Unit): SinglePermissionBuilder
    }

    interface MultiPermissionBuilder : IPermissionBuilder {
        fun onResult(response: (List<PermissionBean>) -> Unit): MultiPermissionBuilder
        fun onRationale(rationale: (List<PermissionBean>, PermissionToken) -> Unit): MultiPermissionBuilder
    }
}

interface Consumer<T> {
    fun accept(t: T)
}

interface BiConsumer<T, U> {
    fun accept(t: T, u: U)
}