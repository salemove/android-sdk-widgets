package com.glia.widgets.callvisualizer.mvpsample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.glia.widgets.R
import kotlinx.parcelize.Parcelize

class MvpSampleActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mvp_sample_activity)
        addFragment(TAG_SAMPLE, MvpSampleFragment.createInstance(), R.id.mvp_sample_fragment_container)
    }

    // Can be extracted to be either base activity function or extension function
    private fun addFragment(tag: String, instance: Fragment, @IdRes parentRes: Int) {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            return
        }
        supportFragmentManager
            .beginTransaction()
            .remove(instance)
            .replace(parentRes, instance, tag)
            .commitAllowingStateLoss()
    }

    companion object {

        const val TAG_SAMPLE = "com.glia.widgets.callvisualizer.mvpsample.TAG_SAMPLE"

        fun getIntent(context: Context?): Intent? {
            return Intent(context, MvpSampleActivity::class.java)
        }
    }
}

@Parcelize
data class SampleState(val counterValue: Int) : Parcelable