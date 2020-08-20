import kotlin.reflect.full.memberProperties

object ModuleDependency {
    // All consts are accessed via reflection
    const val SAMPLE = ":sample"
    const val LIB = ":mayi"

    fun getAllModules() = ModuleDependency::class.memberProperties
        .filter { it.isConst }
        .map { it.getter.call().toString() }
        .toSet()

    fun getAppModuleDependencies() = setOf(SAMPLE, LIB)

}
