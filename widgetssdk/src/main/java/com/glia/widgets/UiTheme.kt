package com.glia.widgets

import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import com.glia.widgets.view.configuration.ButtonConfiguration
import com.glia.widgets.view.configuration.ChatHeadConfiguration
import com.glia.widgets.view.configuration.TextConfiguration
import com.glia.widgets.view.configuration.survey.SurveyStyle
import kotlinx.parcelize.Parcelize

@Parcelize
data class UiTheme(
    /**
     * Text to be shown on the top of the app bar in the chat
     */
    val appBarTitle: String? = null,

    /**
     * Primary color for your brand. Used for example to set the color of the appbar
     */
    @ColorRes
    val brandPrimaryColor: Int? = null,

    /**
     * Dark color of the UI widgets. Used for example to change the body text colors
     */
    @ColorRes
    val baseLightColor: Int? = null,

    /**
     * Light color of the UI widgets. Used for example to change the title text colors.
     * Background color of the chat, etc.
     */
    @ColorRes
    val baseDarkColor: Int? = null,

    /**
     * Base normal color of the chat
     */
    @ColorRes
    val baseNormalColor: Int? = null,

    /**
     * Base shade color of the chat
     */
    @ColorRes
    val baseShadeColor: Int? = null,

    /**
     * Color for the operator's chat messages background
     */
    @ColorRes
    val systemAgentBubbleColor: Int? = null,

    /**
     * Color for any error or chat widgets
     */
    @ColorRes
    val systemNegativeColor: Int? = null,

    /**
     * Color for visitor message background
     */
    @ColorRes
    val visitorMessageBackgroundColor: Int? = null,

    /**
     * Color for visitor message text
     */
    @ColorRes
    val visitorMessageTextColor: Int? = null,

    /**
     * Color for operator message background
     */
    @ColorRes
    val operatorMessageBackgroundColor: Int? = null,

    /**
     * Color for operator message text
     */
    @ColorRes
    val operatorMessageTextColor: Int? = null,

    /**
     * The color of "new messages" divider in the chat
     */
    @ColorRes
    val newMessageDividerColor: Int? = null,

    /**
     * The color of "new messages" divider text in the chat
     */
    @ColorRes
    val newMessageDividerTextColor: Int? = null,

    /**
     * Color for bot action button background color
     */
    @ColorRes
    val botActionButtonBackgroundColor: Int? = null,

    /**
     * Color for bot action button text color
     */
    @ColorRes
    val botActionButtonTextColor: Int? = null,

    /**
     * Color for bot action button background color
     */
    @ColorRes
    val botActionButtonSelectedBackgroundColor: Int? = null,

    /**
     * Color for bot action button text color
     */
    @ColorRes
    val botActionButtonSelectedTextColor: Int? = null,

    /**
     * Color for Chat Send message Button
     */
    @ColorRes
    val sendMessageButtonTintColor: Int? = null,

    /**
     * Color for chat background
     */
    @ColorRes
    val gliaChatBackgroundColor: Int? = null,

    /**
     * Color for Chat Header title
     */
    @ColorRes
    val gliaChatHeaderTitleTintColor: Int? = null,

    /**
     * Color for Chat Home Button
     */
    @ColorRes
    val gliaChatHeaderHomeButtonTintColor: Int? = null,

    /**
     * Color for Chat Exit Queue Button
     */
    @ColorRes
    val gliaChatHeaderExitQueueButtonTintColor: Int? = null,

    /**
     * Color for Visitor Code numbers
     */
    @ColorRes
    val visitorCodeTextColor: Int? = null,

    /**
     * Color for Visitor Code number slots background
     */
    @ColorRes
    val visitorCodeBackgroundColor: Int? = null,

    /**
     * Color for Visitor Code number slots border
     */
    @ColorRes
    val visitorCodeBorderColor: Int? = null,

    /**
     * Allow overriding the view's fontFamily
     */
    @FontRes
    val fontRes: Int? = null,

    /**
     * The appbar's back icon resId
     */
    @DrawableRes
    val iconAppBarBack: Int? = null,

    /**
     * The appbar's leave queue icon resId
     */
    @DrawableRes
    val iconLeaveQueue: Int? = null,

    /**
     * The send message icon resId
     */
    @DrawableRes
    val iconSendMessage: Int? = null,

    /**
     * The icon resId used in chat messages to display an ongoing audio upgrade timer
     */
    @DrawableRes
    val iconChatAudioUpgrade: Int? = null,

    /**
     * The icon resId used in the header of an audio upgrade dialog
     */
    @DrawableRes
    val iconUpgradeAudioDialog: Int? = null,

    /**
     * The icon resId used to set the call's audio on
     */
    @DrawableRes
    val iconCallAudioOn: Int? = null,

    /**
     * The icon resId used in chat messages to display an ongoing video upgrade timer
     */
    @DrawableRes
    val iconChatVideoUpgrade: Int? = null,

    /**
     * The icon resId used in the header of an video upgrade dialog
     */
    @DrawableRes
    val iconUpgradeVideoDialog: Int? = null,

    /**
     * The icon resId used in the header of of a screen sharing upgrade dialog
     */
    @DrawableRes
    val iconScreenSharingDialog: Int? = null,

    /**
     * The icon resId used to set the call's video on
     */
    @DrawableRes
    val iconCallVideoOn: Int? = null,

    /**
     * The icon resId used to set the call's audio off
     */
    @DrawableRes
    val iconCallAudioOff: Int? = null,

    /**
     * The icon resId used set the call's video off
     */
    @DrawableRes
    val iconCallVideoOff: Int? = null,

    /**
     * The icon resId used to navigate back to the chat from a call
     */
    @DrawableRes
    val iconCallChat: Int? = null,

    /**
     * The icon resId used to set the call's speaker on
     */
    @DrawableRes
    val iconCallSpeakerOn: Int? = null,

    /**
     * The icon resId used to set the call's speaker off
     */
    @DrawableRes
    val iconCallSpeakerOff: Int? = null,

    /**
     * The icon resId used to minimize a call
     */
    @DrawableRes
    val iconCallMinimize: Int? = null,

    /**
     * The icon resId used in a chat head if an operator's profile image is absent
     */
    @DrawableRes
    val iconPlaceholder: Int? = null,

    /**
     * The icon resId used when visitor is put on hold by the operator
     */
    @DrawableRes
    val iconOnHold: Int? = null,

    /**
     * The icon resId used when visitor is sharing the screen
     */
    @DrawableRes
    val iconEndScreenShare: Int? = null,

    /**
     * The icon tint resId when visitor is sharing the screen
     */
    @DrawableRes
    val endScreenShareTintColor: Int? = null,

    /**
     * Connecting Operator Status Layout Heading Text Color
     */
    @ColorRes
    val gliaChatStartingHeadingTextColor: Int? = null,

    /**
     * Connecting Operator Status Layout Caption Text Color
     */
    @ColorRes
    val gliaChatStartingCaptionTextColor: Int? = null,

    /**
     * Connected Operator Status Layout Heading Text Color
     */
    @ColorRes
    val gliaChatStartedHeadingTextColor: Int? = null,

    /**
     * Connected Operator Status Layout Caption Text Color
     */
    @ColorRes
    val gliaChatStartedCaptionTextColor: Int? = null,

    val whiteLabel: Boolean? = null,

    val gliaAlertDialogButtonUseVerticalAlignment: Boolean? = null,

    @get:Deprecated("Replaced by Unified Ui")
    val gliaChoiceCardContentTextConfiguration: TextConfiguration? = null,

    @get:Deprecated("Replaced by Unified Ui")
    val gliaEndButtonConfiguration: ButtonConfiguration? = null,

    @get:Deprecated("Replaced by Unified Ui")
    val gliaPositiveButtonConfiguration: ButtonConfiguration? = null,

    @get:Deprecated("Replaced by Unified Ui")
    val gliaNegativeButtonConfiguration: ButtonConfiguration? = null,

    @get:Deprecated("Replaced by Unified Ui")
    val gliaNeutralButtonConfiguration: ButtonConfiguration? = null,
    @get:Deprecated("Replaced by Unified Ui")
    val chatHeadConfiguration: ChatHeadConfiguration? = null,
    @get:Deprecated("Replaced by Unified Ui")
    val surveyStyle: SurveyStyle? = null,

    //    GVA
    @ColorRes
    val gvaQuickReplyBackgroundColor: Int? = null,

    @ColorRes
    val gvaQuickReplyStrokeColor: Int? = null,

    @ColorRes
    val gvaQuickReplyTextColor: Int? = null

) : Parcelable {

    private constructor(builder: UiThemeBuilder) : this(
        appBarTitle = builder.appBarTitle,
        brandPrimaryColor = builder.brandPrimaryColor,
        baseLightColor = builder.baseLightColor,
        baseDarkColor = builder.baseDarkColor,
        baseNormalColor = builder.baseNormalColor,
        baseShadeColor = builder.baseShadeColor,
        systemAgentBubbleColor = builder.systemAgentBubbleColor,
        fontRes = builder.fontRes,
        systemNegativeColor = builder.systemNegativeColor,
        visitorMessageBackgroundColor = builder.visitorMessageBackgroundColor,
        visitorMessageTextColor = builder.visitorMessageTextColor,
        visitorCodeTextColor = builder.visitorCodeTextColor,
        visitorCodeBackgroundColor = builder.visitorCodeBackgroundColor,
        visitorCodeBorderColor = builder.visitorCodeBorderColor,
        operatorMessageBackgroundColor = builder.operatorMessageBackgroundColor,
        newMessageDividerColor = builder.newMessageDividerColor,
        newMessageDividerTextColor = builder.newMessageDividerTextColor,
        gliaChatBackgroundColor = builder.gliaChatBackgroundColor,
        operatorMessageTextColor = builder.operatorMessageTextColor,
        botActionButtonBackgroundColor = builder.botActionButtonBackgroundColor,
        botActionButtonTextColor = builder.botActionButtonTextColor,
        botActionButtonSelectedBackgroundColor = builder.botActionButtonSelectedBackgroundColor,
        botActionButtonSelectedTextColor = builder.botActionButtonSelectedTextColor,
        sendMessageButtonTintColor = builder.sendMessageButtonTintColor,
        gliaChatHeaderTitleTintColor = builder.gliaChatHeaderTitleTintColor,
        gliaChatHeaderHomeButtonTintColor = builder.gliaChatHomeButtonTintColor,
        gliaChatHeaderExitQueueButtonTintColor = builder.gliaChatHeaderExitQueueButtonTintColor,
        iconAppBarBack = builder.iconAppBarBack,
        iconLeaveQueue = builder.iconLeaveQueue,
        iconSendMessage = builder.iconSendMessage,
        iconChatAudioUpgrade = builder.iconChatAudioUpgrade,
        iconUpgradeAudioDialog = builder.iconUpgradeAudioDialog,
        iconCallAudioOn = builder.iconCallAudioOn,
        iconChatVideoUpgrade = builder.iconChatVideoUpgrade,
        iconUpgradeVideoDialog = builder.iconUpgradeVideoDialog,
        iconScreenSharingDialog = builder.iconScreenSharingDialog,
        iconCallVideoOn = builder.iconCallVideoOn,
        iconCallAudioOff = builder.iconCallAudioOff,
        iconCallVideoOff = builder.iconCallVideoOff,
        iconCallChat = builder.iconCallChat,
        iconCallSpeakerOn = builder.iconCallSpeakerOn,
        iconCallSpeakerOff = builder.iconCallSpeakerOff,
        iconCallMinimize = builder.iconCallMinimize,
        iconPlaceholder = builder.iconPlaceholder,
        iconOnHold = builder.iconOnHold,
        iconEndScreenShare = builder.iconEndScreenShare,
        endScreenShareTintColor = builder.endScreenShareTintColor,
        whiteLabel = builder.whiteLabel,
        gliaAlertDialogButtonUseVerticalAlignment =
        builder.gliaAlertDialogButtonUseVerticalAlignment,
        gliaEndButtonConfiguration = builder.headerEndButtonConfiguration,
        gliaPositiveButtonConfiguration = builder.positiveButtonConfiguration,
        gliaNegativeButtonConfiguration = builder.negativeButtonConfiguration,
        gliaNeutralButtonConfiguration = builder.neutralButtonConfiguration,
        gliaChatStartingCaptionTextColor = builder.chatStartingCaptionTextColor,
        gliaChatStartingHeadingTextColor = builder.chatStartingHeadingTextColor,
        gliaChatStartedCaptionTextColor = builder.chatStartedCaptionTextColor,
        gliaChatStartedHeadingTextColor = builder.chatStartedHeadingTextColor,
        gliaChoiceCardContentTextConfiguration = builder.choiceCardContentTextConfiguration,
        chatHeadConfiguration = builder.chatHeadConfiguration,
        surveyStyle = builder.surveyStyle,
        gvaQuickReplyBackgroundColor = builder.gvaQuickReplyBackgroundColor,
        gvaQuickReplyStrokeColor = builder.gvaQuickReplyStrokeColor,
        gvaQuickReplyTextColor = builder.gvaQuickReplyTextColor
    )

    class UiThemeBuilder {
        /**
         * Text to be shown on the top of the app bar in the chat
         */
        var appBarTitle: String? = null
            private set

        /**
         * Primary color for your brand. Used for example to set the color of the appbar
         */
        @ColorRes
        var brandPrimaryColor: Int? = null
            private set

        /**
         * Dark color of the UI widgets. Used for example to change the body text colors
         */
        @ColorRes
        var baseLightColor: Int? = null
            private set

        /**
         * Light color of the UI widgets. Used for example to change the title text colors.
         * Background color of the chat, etc.
         */
        @ColorRes
        var baseDarkColor: Int? = null
            private set

        /**
         * Base normal color of the chat
         */
        @ColorRes
        var baseNormalColor: Int? = null
            private set

        /**
         * Base shade color of the chat
         */
        @ColorRes
        var baseShadeColor: Int? = null
            private set

        /**
         * Color for the operator's chat messages background
         */
        @ColorRes
        var systemAgentBubbleColor: Int? = null
            private set

        /**
         * Color for any error or chat widgets
         */
        @ColorRes
        var systemNegativeColor: Int? = null
            private set

        /**
         * Color for visitor message background
         */
        @ColorRes
        var visitorMessageBackgroundColor: Int? = null
            private set

        /**
         * Color for visitor message text
         */
        @ColorRes
        var visitorMessageTextColor: Int? = null
            private set

        /**
         * Color for Visitor Code numbers
         */
        @ColorRes
        var visitorCodeTextColor: Int? = null
            private set

        /**
         * Color for Visitor Code number slots background
         */
        @ColorRes
        var visitorCodeBackgroundColor: Int? = null
            private set

        /**
         * Color for Visitor Code number slots border
         */
        @ColorRes
        var visitorCodeBorderColor: Int? = null
            private set

        /**
         * Color for operator message background
         */
        @ColorRes
        var operatorMessageBackgroundColor: Int? = null
            private set

        /**
         * Color for operator message text
         */
        @ColorRes
        var operatorMessageTextColor: Int? = null
            private set

        /**
         * The color of "new messages" divider in the chat
         */
        @ColorRes
        var newMessageDividerColor: Int? = null
            private set

        /**
         * The color of "new messages" divider text in the chat
         */
        @ColorRes
        var newMessageDividerTextColor: Int? = null
            private set

        /**
         * Color for bot action button background color
         */
        @ColorRes
        var botActionButtonBackgroundColor: Int? = null
            private set

        /**
         * Color for bot action button text color
         */
        @ColorRes
        var botActionButtonTextColor: Int? = null
            private set

        /**
         * Color for bot action button background color
         */
        @ColorRes
        var botActionButtonSelectedBackgroundColor: Int? = null
            private set

        /**
         * Color for bot action button text color
         */
        @ColorRes
        var botActionButtonSelectedTextColor: Int? = null
            private set

        /**
         * Color for Send message Button
         */
        @ColorRes
        var sendMessageButtonTintColor: Int? = null
            private set

        /**
         * Color for chat background
         */
        @ColorRes
        var gliaChatBackgroundColor: Int? = null
            private set

        /**
         * Color for Chat Header Title
         */
        @ColorRes
        var gliaChatHeaderTitleTintColor: Int? = null
            private set

        /**
         * Color for Chat Home Button
         */
        @ColorRes
        var gliaChatHomeButtonTintColor: Int? = null
            private set

        /**
         *
         */
        @ColorRes
        var gliaChatHeaderExitQueueButtonTintColor: Int? = null
            private set

        /**
         * Allow overriding the view's fontFamily
         */
        @FontRes
        var fontRes: Int? = null
            private set

        /**
         * The appbar's back icon resId
         */
        @DrawableRes
        var iconAppBarBack: Int? = null
            private set

        /**
         * The appbar's leave queue icon resId
         */
        @DrawableRes
        var iconLeaveQueue: Int? = null
            private set

        /**
         * The send message icon resId
         */
        @DrawableRes
        var iconSendMessage: Int? = null
            private set

        /**
         * The icon resId used in chat messages to display an ongoing audio upgrade timer
         */
        @DrawableRes
        var iconChatAudioUpgrade: Int? = null
            private set

        /**
         * The icon resId used in the header of an audio upgrade dialog
         */
        @DrawableRes
        var iconUpgradeAudioDialog: Int? = null
            private set

        /**
         * The icon resId used to set the call's audio on
         */
        @DrawableRes
        var iconCallAudioOn: Int? = null
            private set

        /**
         * The icon resId used in chat messages to display an ongoing video upgrade timer
         */
        @DrawableRes
        var iconChatVideoUpgrade: Int? = null
            private set

        /**
         * The icon resId used in the header of an video upgrade dialog
         */
        @DrawableRes
        var iconUpgradeVideoDialog: Int? = null
            private set

        /**
         * The icon resId used in the header of of a screen sharing upgrade dialog
         */
        @DrawableRes
        var iconScreenSharingDialog: Int? = null
            private set

        /**
         * The icon resId used to set the call's video on
         */
        @DrawableRes
        var iconCallVideoOn: Int? = null
            private set

        /**
         * The icon resId used to set the call's audio off
         */
        @DrawableRes
        var iconCallAudioOff: Int? = null
            private set

        /**
         * The icon resId used set the call's video off
         */
        @DrawableRes
        var iconCallVideoOff: Int? = null
            private set

        /**
         * The icon resId used to navigate back to the chat from a call
         */
        @DrawableRes
        var iconCallChat: Int? = null
            private set

        /**
         * The icon resId used to set the call's speaker on
         */
        @DrawableRes
        var iconCallSpeakerOn: Int? = null
            private set

        /**
         * The icon resId used to set the call's speaker off
         */
        @DrawableRes
        var iconCallSpeakerOff: Int? = null
            private set

        /**
         * The icon resId used to minimize a call
         */
        @DrawableRes
        var iconCallMinimize: Int? = null
            private set

        /**
         * The icon resId used in a chat head if an operator's profile image is absent
         */
        @DrawableRes
        var iconPlaceholder: Int? = null
            private set

        /**
         * The icon resId used when visitor is put on hold by operator
         */
        @DrawableRes
        var iconOnHold: Int? = null
            private set

        /**
         * The icon resId used when visitor is sharing the screen
         */
        @DrawableRes
        var iconEndScreenShare: Int? = null
            private set

        /**
         * The icon tint resId when visitor is sharing the screen
         */
        @DrawableRes
        var endScreenShareTintColor: Int? = null
            private set

        /**
         * Connecting Operator Status Layout Heading Text Color
         */
        @ColorRes
        var chatStartingHeadingTextColor: Int? = null
            private set

        /**
         * Connecting Operator Status Layout Caption Text Color
         */
        @ColorRes
        var chatStartingCaptionTextColor: Int? = null
            private set

        /**
         * Connected Operator Status Layout Heading Text Color
         */
        @ColorRes
        var chatStartedHeadingTextColor: Int? = null
            private set

        /**
         * Connected Operator Status Layout Caption Text Color
         */
        @ColorRes
        var chatStartedCaptionTextColor: Int? = null
            private set
        var whiteLabel: Boolean? = null
            private set
        var gliaAlertDialogButtonUseVerticalAlignment: Boolean? = null
            private set
        var headerEndButtonConfiguration: ButtonConfiguration? = null
            private set
        var positiveButtonConfiguration: ButtonConfiguration? = null
            private set
        var negativeButtonConfiguration: ButtonConfiguration? = null
            private set
        var neutralButtonConfiguration: ButtonConfiguration? = null
            private set
        var choiceCardContentTextConfiguration: TextConfiguration? = null
            private set
        var chatHeadConfiguration: ChatHeadConfiguration? = null
            private set
        var surveyStyle: SurveyStyle? = SurveyStyle.Builder().build()
            private set

        //    GVA
        @ColorRes
        var gvaQuickReplyBackgroundColor: Int? = null
            private set

        @ColorRes
        var gvaQuickReplyStrokeColor: Int? = null
            private set

        @ColorRes
        var gvaQuickReplyTextColor: Int? = null
            private set

        fun setAppBarTitle(appBarTitle: String?) {
            this.appBarTitle = appBarTitle
        }

        fun setFontRes(@FontRes fontRes: Int?) {
            this.fontRes = fontRes?.takeIf { it != 0 }
        }

        fun setBrandPrimaryColor(@ColorRes brandPrimaryColor: Int?) {
            this.brandPrimaryColor = brandPrimaryColor
        }

        fun setBaseLightColor(@ColorRes baseLightColor: Int?) {
            this.baseLightColor = baseLightColor
        }

        fun setBaseDarkColor(@ColorRes baseDarkColor: Int?) {
            this.baseDarkColor = baseDarkColor
        }

        fun setBaseNormalColor(baseNormalColor: Int?) {
            this.baseNormalColor = baseNormalColor
        }

        fun setBaseShadeColor(baseShadeColor: Int?) {
            this.baseShadeColor = baseShadeColor
        }

        fun setSystemAgentBubbleColor(@ColorRes systemAgentBubbleColor: Int?) {
            this.systemAgentBubbleColor = systemAgentBubbleColor
        }

        fun setSystemNegativeColor(@ColorRes systemNegativeColor: Int?) {
            this.systemNegativeColor = systemNegativeColor
        }

        fun setVisitorMessageBackgroundColor(@ColorRes color: Int?) {
            visitorMessageBackgroundColor = color
        }

        fun setVisitorMessageTextColor(@ColorRes color: Int?) {
            visitorMessageTextColor = color
        }

        fun setVisitorCodeTextColor(@ColorRes color: Int?) {
            visitorCodeTextColor = color
        }

        fun setVisitorCodeBackgroundColor(@ColorRes color: Int?) {
            visitorCodeBackgroundColor = color
        }

        fun setVisitorCodeBorderColor(@ColorRes color: Int?) {
            visitorCodeBorderColor = color
        }

        fun setOperatorMessageBackgroundColor(@ColorRes color: Int?) {
            operatorMessageBackgroundColor = color
        }

        fun setOperatorMessageTextColor(@ColorRes color: Int?) {
            operatorMessageTextColor = color
        }

        fun setNewMessagesDividerColor(@ColorRes color: Int?) {
            newMessageDividerColor = color
        }

        fun setNewMessagesDividerTextColor(@ColorRes color: Int?) {
            newMessageDividerTextColor = color
        }

        fun setBotActionButtonBackgroundColor(@ColorRes color: Int?) {
            botActionButtonBackgroundColor = color
        }

        fun setBotActionButtonTextColor(@ColorRes color: Int?) {
            botActionButtonTextColor = color
        }

        fun setBotActionButtonSelectedBackgroundColor(@ColorRes color: Int?) {
            botActionButtonSelectedBackgroundColor = color
        }

        fun setBotActionButtonSelectedTextColor(@ColorRes color: Int?) {
            botActionButtonSelectedTextColor = color
        }

        fun setIconAppBarBack(@DrawableRes iconAppBarBack: Int?) {
            this.iconAppBarBack = iconAppBarBack
        }

        fun setIconLeaveQueue(@DrawableRes iconLeaveQueue: Int?) {
            this.iconLeaveQueue = iconLeaveQueue
        }

        fun setIconSendMessage(@DrawableRes iconSendMessage: Int?) {
            this.iconSendMessage = iconSendMessage
        }

        fun setIconChatAudioUpgrade(@DrawableRes iconChatAudioUpgrade: Int?) {
            this.iconChatAudioUpgrade = iconChatAudioUpgrade
        }

        fun setIconUpgradeAudioDialog(@DrawableRes iconUpgradeAudioDialog: Int?) {
            this.iconUpgradeAudioDialog = iconUpgradeAudioDialog
        }

        fun setIconCallAudioOn(@DrawableRes iconCallAudioOn: Int?) {
            this.iconCallAudioOn = iconCallAudioOn
        }

        fun setIconChatVideoUpgrade(@DrawableRes iconChatVideoUpgrade: Int?) {
            this.iconChatVideoUpgrade = iconChatVideoUpgrade
        }

        fun setIconUpgradeVideoDialog(@DrawableRes iconUpgradeVideoDialog: Int?) {
            this.iconUpgradeVideoDialog = iconUpgradeVideoDialog
        }

        fun setIconScreenSharingDialog(@DrawableRes iconScreenSharingDialog: Int?) {
            this.iconScreenSharingDialog = iconScreenSharingDialog
        }

        fun setIconCallVideoOn(@DrawableRes iconCallVideoOn: Int?) {
            this.iconCallVideoOn = iconCallVideoOn
        }

        fun setIconCallAudioOff(@DrawableRes iconCallAudioOff: Int?) {
            this.iconCallAudioOff = iconCallAudioOff
        }

        fun setIconCallVideoOff(@DrawableRes iconCallVideoOff: Int?) {
            this.iconCallVideoOff = iconCallVideoOff
        }

        fun setIconCallChat(@DrawableRes iconCallChat: Int?) {
            this.iconCallChat = iconCallChat
        }

        fun setIconCallSpeakerOn(@DrawableRes iconCallSpeakerOn: Int?) {
            this.iconCallSpeakerOn = iconCallSpeakerOn
        }

        fun setIconCallSpeakerOff(@DrawableRes iconCallSpeakerOff: Int?) {
            this.iconCallSpeakerOff = iconCallSpeakerOff
        }

        fun setIconCallMinimize(@DrawableRes iconCallMinimize: Int?) {
            this.iconCallMinimize = iconCallMinimize
        }

        fun setIconPlaceholder(@DrawableRes iconPlaceholder: Int?) {
            this.iconPlaceholder = iconPlaceholder
        }

        fun setIconOnHold(@DrawableRes iconOnHold: Int?) {
            this.iconOnHold = iconOnHold
        }

        fun setIconEndScreenShare(@DrawableRes iconEndScreenShare: Int?) {
            this.iconEndScreenShare = iconEndScreenShare
        }

        fun setEndScreenShareTintColor(@DrawableRes endScreenShareTint: Int?) {
            this.endScreenShareTintColor = endScreenShareTint
        }

        fun setWhiteLabel(whiteLabel: Boolean?) {
            this.whiteLabel = whiteLabel
        }

        fun setGliaAlertDialogButtonUseVerticalAlignment(value: Boolean?) {
            gliaAlertDialogButtonUseVerticalAlignment = value
        }

        fun setHeaderEndButtonConfiguration(configuration: ButtonConfiguration?) {
            headerEndButtonConfiguration = configuration
        }

        fun setPositiveButtonConfiguration(configuration: ButtonConfiguration?) {
            positiveButtonConfiguration = configuration
        }

        fun setNegativeButtonConfiguration(configuration: ButtonConfiguration?) {
            negativeButtonConfiguration = configuration
        }

        fun setNeutralButtonConfiguration(configuration: ButtonConfiguration?) {
            neutralButtonConfiguration = configuration
        }

        fun setSendMessageButtonTintColor(color: Int?) {
            sendMessageButtonTintColor = color
        }

        fun setGliaChatBackgroundColor(@ColorRes color: Int?) {
            gliaChatBackgroundColor = color
        }

        fun setGliaChatHeaderTitleTintColor(color: Int?) {
            gliaChatHeaderTitleTintColor = color
        }

        fun setGliaChatHeaderHomeButtonTintColor(color: Int?) {
            gliaChatHomeButtonTintColor = color
        }

        fun setGliaChatHeaderExitQueueButtonTintColor(color: Int?) {
            gliaChatHeaderExitQueueButtonTintColor = color
        }

        fun setChatStartingHeadingTextColor(color: Int?) {
            chatStartingHeadingTextColor = color
        }

        fun setChatStartingCaptionTextColor(color: Int?) {
            chatStartingCaptionTextColor = color
        }

        fun setChatStartedHeadingTextColor(color: Int?) {
            chatStartedHeadingTextColor = color
        }

        fun setChatStartedCaptionTextColor(color: Int?) {
            chatStartedCaptionTextColor = color
        }

        fun setChoiceCardContentTextConfiguration(textConfiguration: TextConfiguration?) {
            choiceCardContentTextConfiguration = textConfiguration
        }

        fun setChatHeadConfiguration(chatHeadConfiguration: ChatHeadConfiguration?) {
            this.chatHeadConfiguration = chatHeadConfiguration
        }

        fun setSurveyStyle(surveyStyle: SurveyStyle?) {
            this.surveyStyle = surveyStyle
        }

        fun setGvaQuickReplyBackgroundColor(@ColorRes gvaQuickReplyBackgroundColor: Int?) {
            this.gvaQuickReplyBackgroundColor = gvaQuickReplyBackgroundColor
        }

        fun setGvaQuickReplyStrokeColor(@ColorRes gvaQuickReplyStrokeColor: Int?) {
            this.gvaQuickReplyStrokeColor = gvaQuickReplyStrokeColor
        }

        fun setGvaQuickReplyTextColor(@ColorRes gvaQuickReplyTextColor: Int?) {
            this.gvaQuickReplyTextColor = gvaQuickReplyTextColor
        }

        fun setTheme(theme: UiTheme) {
            appBarTitle = theme.appBarTitle
            brandPrimaryColor = theme.brandPrimaryColor
            baseLightColor = theme.baseLightColor
            baseDarkColor = theme.baseDarkColor
            baseNormalColor = theme.baseNormalColor
            baseShadeColor = theme.baseShadeColor
            systemAgentBubbleColor = theme.systemAgentBubbleColor
            fontRes = theme.fontRes
            systemNegativeColor = theme.systemNegativeColor
            visitorMessageBackgroundColor = theme.visitorMessageBackgroundColor
            visitorMessageTextColor = theme.visitorMessageTextColor
            operatorMessageBackgroundColor = theme.operatorMessageBackgroundColor
            newMessageDividerColor = theme.newMessageDividerColor
            newMessageDividerTextColor = theme.newMessageDividerTextColor
            operatorMessageTextColor = theme.operatorMessageTextColor
            botActionButtonBackgroundColor = theme.botActionButtonBackgroundColor
            botActionButtonTextColor = theme.botActionButtonTextColor
            botActionButtonSelectedBackgroundColor = theme.botActionButtonSelectedBackgroundColor
            botActionButtonSelectedTextColor = theme.botActionButtonSelectedTextColor
            iconAppBarBack = theme.iconAppBarBack
            iconLeaveQueue = theme.iconLeaveQueue
            iconSendMessage = theme.iconSendMessage
            iconChatAudioUpgrade = theme.iconChatAudioUpgrade
            iconUpgradeAudioDialog = theme.iconUpgradeAudioDialog
            iconCallAudioOn = theme.iconCallAudioOn
            iconChatVideoUpgrade = theme.iconChatVideoUpgrade
            iconUpgradeVideoDialog = theme.iconUpgradeVideoDialog
            iconScreenSharingDialog = theme.iconScreenSharingDialog
            iconCallVideoOn = theme.iconCallVideoOn
            iconCallAudioOff = theme.iconCallAudioOff
            iconCallVideoOff = theme.iconCallVideoOff
            iconCallChat = theme.iconCallChat
            iconCallSpeakerOn = theme.iconCallSpeakerOn
            iconCallSpeakerOff = theme.iconCallSpeakerOff
            iconCallMinimize = theme.iconCallMinimize
            iconPlaceholder = theme.iconPlaceholder
            iconOnHold = theme.iconOnHold
            iconEndScreenShare = theme.iconEndScreenShare
            endScreenShareTintColor = theme.endScreenShareTintColor
            whiteLabel = theme.whiteLabel
            headerEndButtonConfiguration = theme.gliaEndButtonConfiguration
            positiveButtonConfiguration = theme.gliaPositiveButtonConfiguration
            negativeButtonConfiguration = theme.gliaNegativeButtonConfiguration
            neutralButtonConfiguration = theme.gliaNeutralButtonConfiguration
            sendMessageButtonTintColor = theme.sendMessageButtonTintColor
            gliaChatBackgroundColor = theme.gliaChatBackgroundColor
            gliaChatHeaderTitleTintColor = theme.gliaChatHeaderTitleTintColor
            gliaChatHomeButtonTintColor = theme.gliaChatHeaderHomeButtonTintColor
            gliaChatHeaderExitQueueButtonTintColor = theme.gliaChatHeaderExitQueueButtonTintColor
            chatStartingCaptionTextColor = theme.gliaChatStartingCaptionTextColor
            chatStartingHeadingTextColor = theme.gliaChatStartingHeadingTextColor
            chatStartedCaptionTextColor = theme.gliaChatStartedCaptionTextColor
            chatStartedHeadingTextColor = theme.gliaChatStartedHeadingTextColor
            choiceCardContentTextConfiguration = theme.gliaChoiceCardContentTextConfiguration
            chatHeadConfiguration = theme.chatHeadConfiguration
            surveyStyle = theme.surveyStyle
            gvaQuickReplyBackgroundColor = theme.gvaQuickReplyBackgroundColor
            gvaQuickReplyStrokeColor = theme.gvaQuickReplyStrokeColor
            gvaQuickReplyTextColor = theme.gvaQuickReplyTextColor
        }

        fun build(): UiTheme {
            return UiTheme(this)
        }
    }
}
