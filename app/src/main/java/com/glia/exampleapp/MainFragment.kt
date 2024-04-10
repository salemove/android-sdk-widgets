package com.glia.exampleapp

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.glia.androidsdk.Engagement
import com.glia.androidsdk.Glia
import com.glia.androidsdk.GliaException
import com.glia.androidsdk.fcm.GliaPushMessage
import com.glia.androidsdk.omnibrowse.Omnibrowse
import com.glia.androidsdk.screensharing.ScreenSharing
import com.glia.androidsdk.visitor.Authentication
import com.glia.exampleapp.ExampleAppConfigManager.createDefaultConfig
import com.glia.exampleapp.Utils.getAuthenticationBehaviorFromPrefs
import com.glia.widgets.GliaWidgets
import com.glia.widgets.UiTheme
import com.glia.widgets.call.CallActivity
import com.glia.widgets.chat.ChatActivity
import com.glia.widgets.chat.ChatType
import com.glia.widgets.core.notification.NotificationActionReceiver
import com.glia.widgets.messagecenter.MessageCenterActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlin.concurrent.thread
import kotlin.properties.Delegates

class MainFragment : Fragment() {
    private var containerView: ConstraintLayout? = null
    private var authentication: Authentication? = null

    private var pauseItem: MenuItem by Delegates.notNull()
    private var resumeItem: MenuItem by Delegates.notNull()

    private var engagementEndedItem: MenuItem by Delegates.notNull()
    private var ongoingEngagementItem: MenuItem by Delegates.notNull()

    private val authToken: String
        get() {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val authTokenFromPrefs = getAuthTokenFromPrefs(sharedPreferences)
            return authTokenFromPrefs.ifEmpty { getString(R.string.glia_jwt) }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.main_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerView = view.findViewById(R.id.constraint_layout)
        val navController = NavHostFragment.findNavController(this)

        setupAuthButtonsVisibility()

        view.findViewById<View>(R.id.settings_button)
            .setOnClickListener { navController.navigate(R.id.settings) }
        view.findViewById<View>(R.id.chat_activity_button)
            .setOnClickListener { navigateToChat(ChatType.LIVE_CHAT) }
        view.findViewById<View>(R.id.audio_call_button)
            .setOnClickListener { navigateToCall(GliaWidgets.MEDIA_TYPE_AUDIO) }
        view.findViewById<View>(R.id.video_call_button)
            .setOnClickListener { navigateToCall(GliaWidgets.MEDIA_TYPE_VIDEO) }
        view.findViewById<View>(R.id.message_center_activity_button)
            .setOnClickListener { navigateToMessageCenter() }
        view.findViewById<View>(R.id.end_engagement_button)
            .setOnClickListener { GliaWidgets.endEngagement() }
        view.findViewById<View>(R.id.visitor_info_button)
            .setOnClickListener { navController.navigate(R.id.visitor_info) }
        view.findViewById<View>(R.id.initGliaWidgetsButton).setOnClickListener {
            thread { initGliaWidgets() }
        }
        view.findViewById<View>(R.id.authenticationButton)
            .setOnClickListener { showAuthenticationDialog(null) }
        view.findViewById<View>(R.id.deauthenticationButton)
            .setOnClickListener { deAuthenticate() }
        view.findViewById<View>(R.id.clear_session_button)
            .setOnClickListener { clearSession() }
        view.findViewById<View>(R.id.visitor_code_button).setOnClickListener {
            if ((view.findViewById<View>(R.id.visitor_code_switch) as SwitchCompat).isChecked) {
                showVisitorCodeInADedicatedView()
            } else {
                showVisitorCode()
            }
        }

        view.findViewById<MaterialToolbar>(R.id.top_app_bar).menu.apply {
            pauseItem = findItem(R.id.lo_pause).setOnMenuItemClickListener {
                it.isVisible = false
                resumeItem.isVisible = true
                Glia.getLiveObservation().pause()
                true
            }
            resumeItem = findItem(R.id.lo_resume).setOnMenuItemClickListener {
                it.isVisible = false
                pauseItem.isVisible = true
                Glia.getLiveObservation().resume()
                true
            }
            ongoingEngagementItem = findItem(R.id.menu_engagement_ongoing)
            engagementEndedItem = findItem(R.id.menu_no_engagement)

            findItem(R.id.menu_open_legacy_activity).setOnMenuItemClickListener {
                startActivity(Intent(requireContext(), LegacyActivity::class.java))
                true
            }
            findItem(R.id.menu_broadcast_end_screen_sharing_event).setOnMenuItemClickListener {
                simulateEndScreenSharingFromNotification()
                true
            }
        }

        handleOpensFromPushNotification()
    }

    private fun simulateEndScreenSharingFromNotification() {
        val intent = Intent(requireContext(), NotificationActionReceiver::class.java)
            .setAction("com.glia.widgets.core.notification.NotificationActionReceiver.ACTION_ON_SCREEN_SHARING_END_PRESSED")
        requireContext().sendBroadcast(intent)
    }

    private fun initMenu() {
        if (Glia.getCurrentEngagement().isPresent) {
            pauseItem.isVisible = true
            resumeItem.isVisible = false
            ongoingEngagementItem.isVisible = true
            engagementEndedItem.isVisible = false
        } else {
            pauseItem.isVisible = false
            resumeItem.isVisible = false
            ongoingEngagementItem.isVisible = false
            engagementEndedItem.isVisible = true
        }


        Glia.on(Glia.Events.ENGAGEMENT) {
            onEngagementStarted()

            it.on(Engagement.Events.END) {
                onEngagementEnded()
            }
        }

        Glia.omnibrowse.on(Omnibrowse.Events.ENGAGEMENT) {
            onEngagementStarted()

            it.on(Engagement.Events.END) {
                onEngagementEnded()
            }
        }
    }

    private fun onEngagementStarted() {
        view?.post {
            ongoingEngagementItem.isVisible = true
            engagementEndedItem.isVisible = false
            pauseItem.isVisible = true
            resumeItem.isVisible = false
        }
    }

    private fun onEngagementEnded() {
        view?.post {
            ongoingEngagementItem.isVisible = false
            engagementEndedItem.isVisible = true
            pauseItem.isVisible = false
            resumeItem.isVisible = false
        }
    }

    private fun handleOpensFromPushNotification() {
        val push = Glia.getPushNotifications()
            .handleOnMainActivityCreate(requireActivity().intent.extras) ?: return

        if (push.type == GliaPushMessage.PushType.QUEUED_MESSAGE) {
            authenticate { navigateToChat(ChatType.SECURE_MESSAGING) }
        } else {
            navigateToChat(ChatType.LIVE_CHAT)
        }
    }

    private fun authenticate(callback: OnAuthCallback) {
        if (Glia.isInitialized() && authentication == null) {
            prepareAuthentication()
            setupAuthButtonsVisibility()
        }
        if (!Glia.isInitialized()) {
            thread {
                initGliaWidgets()
                requireActivity().runOnUiThread { showAuthenticationDialog(callback) }
            }
        } else if (authentication != null && authentication!!.isAuthenticated) {
            callback.onAuthenticated()
        } else {
            showAuthenticationDialog(callback)
        }
    }

    override fun onResume() {
        super.onResume()

        if (Glia.isInitialized()) {
            initMenu()

            if (authentication == null) {
                prepareAuthentication()
                setupAuthButtonsVisibility()
            }
        }
    }

    private fun setupAuthButtonsVisibility() {
        if (activity == null || containerView == null) return
        if (!Glia.isInitialized()) {
            requireActivity().runOnUiThread {
                containerView!!.findViewById<View>(R.id.initGliaWidgetsButton).visibility = View.VISIBLE
                containerView!!.findViewById<View>(R.id.authenticationButton).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.deauthenticationButton).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.visitor_info_button).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.visitor_code_button).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.visitor_code_switch_container).visibility = View.GONE
            }
            return
        }
        requireActivity().runOnUiThread {
            containerView!!.findViewById<View>(R.id.visitor_info_button).visibility = View.VISIBLE
            containerView!!.findViewById<View>(R.id.visitor_code_button).visibility = View.VISIBLE
            containerView!!.findViewById<View>(R.id.visitor_code_switch_container).visibility = View.VISIBLE
        }
        if (authentication == null) return
        if (authentication!!.isAuthenticated) {
            requireActivity().runOnUiThread {
                containerView!!.findViewById<View>(R.id.initGliaWidgetsButton).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.authenticationButton).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.deauthenticationButton).visibility = View.VISIBLE
            }
        } else {
            requireActivity().runOnUiThread {
                containerView!!.findViewById<View>(R.id.initGliaWidgetsButton).visibility = View.GONE
                containerView!!.findViewById<View>(R.id.authenticationButton).visibility = View.VISIBLE
                containerView!!.findViewById<View>(R.id.deauthenticationButton).visibility = View.GONE
            }
        }
    }

    private fun listenForCallVisualizerEngagements() {
        // If a Visitor Code is displayed as embedded view, then it should be hidden on engagement start
        GliaWidgets.getCallVisualizer().onEngagementStart {
            activity?.runOnUiThread { removeVisitorCodeFromDedicatedView() }
        }
    }

    private fun navigateToChat(chatType: ChatType) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val intent = ChatActivity.getIntent(
            context,
            getContextAssetIdFromPrefs(sharedPreferences),
            getQueueIdFromPrefs(sharedPreferences),
            chatType
        )
        startActivity(intent)
    }

    private fun navigateToMessageCenter() {
        val intent = Intent(requireContext(), MessageCenterActivity::class.java)
        setNavigationIntentData(intent)
        startActivity(intent)
    }

    private fun navigateToCall(mediaType: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val intent = Intent(context, CallActivity::class.java)
            .putExtra(GliaWidgets.QUEUE_ID, getQueueIdFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, getContextAssetIdFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.UI_THEME, getRuntimeThemeFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.USE_OVERLAY, getUseOverlay(sharedPreferences))
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, getScreenSharingModeFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.MEDIA_TYPE, Utils.toMediaType(mediaType))
        startActivity(intent)
    }

    private fun setNavigationIntentData(intent: Intent) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        intent.putExtra(GliaWidgets.QUEUE_ID, getQueueIdFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.CONTEXT_ASSET_ID, getContextAssetIdFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.UI_THEME, getRuntimeThemeFromPrefs(sharedPreferences))
            .putExtra(GliaWidgets.USE_OVERLAY, getUseOverlay(sharedPreferences))
            .putExtra(GliaWidgets.SCREEN_SHARING_MODE, getScreenSharingModeFromPrefs(sharedPreferences))
    }

    private fun getUseOverlay(sharedPreferences: SharedPreferences): Boolean {
        return Utils.getUseOverlay(sharedPreferences, resources)
    }

    private fun getScreenSharingModeFromPrefs(sharedPreferences: SharedPreferences): ScreenSharing.Mode {
        return Utils.getScreenSharingModeFromPrefs(sharedPreferences, resources)
    }

    private fun getRuntimeThemeFromPrefs(sharedPreferences: SharedPreferences): UiTheme? {
        return Utils.getRunTimeThemeByPrefs(sharedPreferences, resources)
    }

    private fun getQueueIdFromPrefs(sharedPreferences: SharedPreferences): String {
        return Utils.getStringFromPrefs(
            R.string.pref_queue_id,
            getString(R.string.glia_queue_id),
            sharedPreferences,
            resources
        )
    }

    private fun getContextAssetIdFromPrefs(sharedPreferences: SharedPreferences): String? {
        return Utils.getStringFromPrefs(
            R.string.pref_context_asset_id,
            null,
            sharedPreferences,
            resources
        )
    }

    private fun getCompanyNameFromPrefs(sharedPreferences: SharedPreferences): String {
        return Utils.getStringFromPrefs(
            R.string.pref_company_name,
            "",
            sharedPreferences,
            resources
        )
    }

    private fun saveAuthToken(jwt: String) {
        if (jwt != getString(R.string.glia_jwt)) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            putAuthTokenToPrefs(sharedPreferences, jwt)
        }
    }

    private fun clearAuthToken() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        putAuthTokenToPrefs(sharedPreferences, null)
    }

    private fun getAuthTokenFromPrefs(sharedPreferences: SharedPreferences): String {
        return Utils.getStringFromPrefs(
            R.string.pref_auth_token,
            "",
            sharedPreferences,
            resources
        )
    }

    private fun putAuthTokenToPrefs(sharedPreferences: SharedPreferences, jwt: String?) {
        Utils.putStringToPrefs(
            R.string.pref_auth_token,
            jwt,
            sharedPreferences,
            resources
        )
    }

    private fun showAuthenticationDialog(callback: OnAuthCallback?) {
        if (context == null) return
        val builder = AlertDialog.Builder(requireContext())
        val jwtInput = prepareJwtInputViewEditText(builder)
        val externalTokenInput = prepareExternalTokenInputViewEditText(builder)
        jwtInput.setText(authToken)
        builder.setPositiveButton(
            getString(R.string.authentication_dialog_authenticate_button)
        ) { _, _ ->
            authenticate(jwtInput, externalTokenInput, callback)
        }
        builder.setNeutralButton(getString(R.string.authentication_dialog_clear_button), null)
        builder.setNegativeButton(
            R.string.authentication_dialog_cancel_button
        ) { dialog: DialogInterface, _ -> dialog.cancel() }
        builder.setView(prepareDialogLayout(jwtInput, externalTokenInput))
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            val button = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            button.setOnClickListener {
                jwtInput.setText("")
                externalTokenInput.setText("")
                clearAuthToken()
            }
        }
        alertDialog.show()
    }

    private fun prepareDialogLayout(
        jwtInput: EditText,
        externalTokenInput: EditText
    ): LinearLayout {
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val marginInDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            resources.displayMetrics
        ).toInt()
        layoutParams.setMargins(marginInDp, 0, marginInDp, 0)
        jwtInput.layoutParams = layoutParams
        jwtInput.gravity = Gravity.TOP or Gravity.START
        externalTokenInput.layoutParams = layoutParams
        externalTokenInput.gravity = Gravity.TOP or Gravity.START
        container.addView(jwtInput, layoutParams)
        container.addView(externalTokenInput, layoutParams)
        return container
    }

    private fun prepareJwtInputViewEditText(builder: AlertDialog.Builder): EditText {
        val input = EditText(context)
        input.setHint(R.string.authentication_dialog_jwt_input_hint)
        input.setSingleLine()
        input.maxLines = 10
        input.setHorizontallyScrolling(false)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setTitle(R.string.authentication_dialog_title)
        builder.setView(input)
        return input
    }

    private fun prepareExternalTokenInputViewEditText(builder: AlertDialog.Builder): EditText {
        val input = EditText(context)
        input.setHint(R.string.authentication_dialog_external_token_input_hint)
        input.setSingleLine()
        input.maxLines = 10
        input.setHorizontallyScrolling(false)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setTitle(R.string.authentication_dialog_title)
        builder.setView(input)
        return input
    }

    private fun initGliaWidgets() {
        if (Glia.isInitialized()) {
            setupAuthButtonsVisibility()
            listenForCallVisualizerEngagements()
            return
        }

        GliaWidgets.init(
            createDefaultConfig(
                context = requireActivity().applicationContext,
//                uiJsonRemoteConfig = UnifiedUiConfigurationLoader.fetchLocalGlobalColors(requireContext()),
//                runtimeConfig = createSampleRuntimeConfig(),
//                region = "us"
            )
        )
        prepareAuthentication()
        setupAuthButtonsVisibility()
        listenForCallVisualizerEngagements()

        view?.post {
            initMenu()
        }
    }

    private fun createSampleRuntimeConfig(): UiTheme = UiTheme(
        gvaQuickReplyTextColor = android.R.color.holo_green_dark,
        gvaQuickReplyStrokeColor = android.R.color.holo_green_dark
    )

    private fun prepareAuthentication() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        authentication =
            GliaWidgets.getAuthentication(getAuthenticationBehaviorFromPrefs(sharedPreferences, resources))
    }

    private fun authenticate(
        jwtInput: EditText,
        externalTokenInput: EditText,
        callback: OnAuthCallback?
    ) {
        if (activity == null || containerView == null) return
        prepareAuthentication()
        val jwt = jwtInput.text.toString()
        var externalAccessToken: String? = externalTokenInput.text.toString()
        if (externalAccessToken!!.isEmpty()) externalAccessToken = null
        authentication!!.authenticate(
            jwt,
            externalAccessToken
        ) { _, exception: GliaException? ->
            if (exception == null && authentication!!.isAuthenticated) {
                setupAuthButtonsVisibility()
                callback?.onAuthenticated()
            } else {
                if (exception?.cause == GliaException.Cause.AUTHENTICATION_ERROR) {
                    showToast(exception.message.toString())
                } else {
                    showToast("Error: $exception")
                }
            }
        }
        saveAuthToken(jwt)
    }

    private fun deAuthenticate() {
        if (activity == null || containerView == null) return
        authentication!!.deauthenticate { _, exception: GliaException? ->
            if (exception == null && !authentication!!.isAuthenticated) {
                setupAuthButtonsVisibility()
            } else {
                showToast("Error: $exception")
            }
        }
    }

    private fun clearSession() {
        GliaWidgets.clearVisitorSession()
        setupAuthButtonsVisibility()
    }

    private fun showVisitorCode() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val visitorContext = getContextAssetIdFromPrefs(sharedPreferences)
        val cv = GliaWidgets.getCallVisualizer()
        if (!visitorContext.isNullOrBlank()) {
            cv.addVisitorContext(visitorContext)
        }
        cv.showVisitorCodeDialog()
    }

    // For testing the integrated Visitor Code solution
    private fun showVisitorCodeInADedicatedView() {
        val visitorCodeView = GliaWidgets.getCallVisualizer().createVisitorCodeView(requireContext())
        val cv = containerView!!.findViewById<CardView>(R.id.container)
        cv.removeAllViews()
        cv.addView(visitorCodeView)
        cv.visibility = View.VISIBLE
    }

    private fun removeVisitorCodeFromDedicatedView() {
        val cv = containerView!!.findViewById<CardView>(R.id.container)
        cv.removeAllViews()
        cv.visibility = View.GONE
    }

    private fun showToast(message: String) {
        if (activity == null) return
        requireActivity().runOnUiThread { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    private fun interface OnAuthCallback {
        fun onAuthenticated()
    }
}
