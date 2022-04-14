import java.util.*

class GoodProperties : Properties() {
    operator fun get(key: String) = super.getProperty(key) ?: throw IllegalArgumentException("No property $key")
}

val String.asProperties: GoodProperties
    get() = GoodProperties().apply {
        load(GoodProperties::class.java.getResource(this@asProperties)!!.openStream())
    }