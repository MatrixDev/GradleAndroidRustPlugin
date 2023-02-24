package dev.matrix.rust

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private external fun callRustCode(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.loadLibrary("rust_library")

        Toast.makeText(this, callRustCode(), Toast.LENGTH_LONG).show()

        finish()
    }
}
