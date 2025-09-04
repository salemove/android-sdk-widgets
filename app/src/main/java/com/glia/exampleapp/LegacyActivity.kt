package com.glia.exampleapp

import android.app.Activity
import android.os.Bundle
import android.widget.ImageButton

class LegacyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legacy)
        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            finish()
        }
    }
}
