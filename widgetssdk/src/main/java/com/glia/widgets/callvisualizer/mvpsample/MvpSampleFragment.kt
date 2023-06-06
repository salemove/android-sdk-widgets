package com.glia.widgets.callvisualizer.mvpsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.glia.widgets.R
import com.glia.widgets.mvp.MvpFragment

class MvpSampleFragment : MvpFragment<MvpSamplePresenter, SampleState>(), MvpSampleContract.View {

    companion object {
        fun createInstance(): MvpSampleFragment {
            return MvpSampleFragment()
        }
    }

    private lateinit var label: AppCompatTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mvp_sample_fragment, container, false)
        view.findViewById<AppCompatButton>(R.id.mvp_sample_fragment_increment_button).setOnClickListener { presenter.onButtonClicked() }
        label = view.findViewById(R.id.mvp_sample_fragment_label)
        return view
    }

    override fun createPresenter(): MvpSamplePresenter {
        return MvpSamplePresenter(this, MvpSampleModel)
    }

    override fun setCounterValue(value: String) {
        label.text = value
    }

    override fun finish() {
        onDestroy()
    }

    override fun showToast() {
        context?.let {
            Toast.makeText(it, "Failed", Toast.LENGTH_LONG)
        }
    }
}