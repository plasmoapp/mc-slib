package su.plo.slib.api.resource

import java.io.IOException
import java.io.InputStream

interface ResourceLoader {

    @Throws(IOException::class)
    fun load(resourcePath: String): InputStream?
}
