package dev.matrix.agp.rust.utils

import java.io.OutputStream

object NullOutputStream : OutputStream() {
    override fun write(value: Int) = Unit
}
