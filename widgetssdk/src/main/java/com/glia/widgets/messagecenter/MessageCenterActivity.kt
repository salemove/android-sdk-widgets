package com.glia.widgets.messagecenter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.core.configuration.GliaSdkConfiguration
import com.glia.widgets.databinding.MessageCenterActivityBinding
import com.glia.widgets.di.Dependencies

class MessageCenterActivity : AppCompatActivity(),
        MessageCenterView.OnFinishListener, MessageCenterView.OnNavigateToMessagingListener {

    private lateinit var binding: MessageCenterActivityBinding
    private var configuration: GliaSdkConfiguration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessageCenterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configuration = createConfiguration(intent)
        val messageCenterView = binding.messageCenterView
        messageCenterView.onFinishListener = this
        messageCenterView.onNavigateToMessagingListener = this

        val controller = Dependencies.getControllerFactory().getMessageCenterController(configuration?.queueId)
        messageCenterView.setController(controller)
    }

    override fun navigateToMessaging() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtras(getIntent())
        startActivity(intent)
        finish()
    }

    private fun createConfiguration(intent: Intent): GliaSdkConfiguration? {
        return GliaSdkConfiguration.Builder()
            .intent(intent)
            .build()
    }
}
