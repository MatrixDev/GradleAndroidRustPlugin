package dev.matrix.agp.rust.utils

import java.io.OutputStream

internal object NullOutputStream : OutputStream() {
    override fun write(value: Int) = Unit
}
