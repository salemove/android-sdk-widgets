package com.glia.widgets;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.FontRes;

import com.glia.widgets.view.configuration.ButtonConfiguration;
import com.glia.widgets.view.configuration.ChatHeadConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;

public class UiTheme implements Parcelable {
    /**
     * Text to be shown on the top of the app bar in the chat
     **/
    private final String appBarTitle;
    /**
     * Primary color for your brand. Used for example to set the color of the appbar
     **/
    private @ColorRes
    final Integer brandPrimaryColor;
    /**
     * Dark color of the UI widgets. Used for example to change the body text colors
     **/
    private @ColorRes
    final Integer baseLightColor;
    /**
     * Light color of the UI widgets. Used for example to change the title text colors.
     * Background color of the chat, etc.
     **/
    private @ColorRes
    final Integer baseDarkColor;
    /**
     * Base normal color of the chat
     */
    private @ColorRes
    final Integer baseNormalColor;
    /**
     * Base shade color of the chat
     */
    private @ColorRes
    final Integer baseShadeColor;
    /**
     * Color for the operator's chat messages background
     */
    private @ColorRes
    final Integer systemAgentBubbleColor;
    /**
     * Color for any error or chat widgets
     */
    private @ColorRes
    final Integer systemNegativeColor;
    /**
     * Color for visitor message background
     */
    private @ColorRes
    final Integer visitorMessageBackgroundColor;
    /**
     * Color for visitor message text
     */
    private @ColorRes
    final Integer visitorMessageTextColor;
    /**
     * Color for operator message background
     */
    private @ColorRes
    final Integer operatorMessageBackgroundColor;
    /**
     * Color for operator message text
     */
    private @ColorRes
    final Integer operatorMessageTextColor;
    /**
     * Color for bot action button background color
     */
    private @ColorRes
    final Integer botActionButtonBackgroundColor;
    /**
     * Color for bot action button text color
     */
    private @ColorRes
    final Integer botActionButtonTextColor;
    /**
     * Color for bot action button background color
     */
    private @ColorRes
    final Integer botActionButtonSelectedBackgroundColor;
    /**
     * Color for bot action button text color
     */
    private @ColorRes
    final Integer botActionButtonSelectedTextColor;
    /**
     * Color for Chat Send message Button
     */
    private @ColorRes
    final Integer sendMessageButtonTintColor;
    /**
     * Color for chat background
     */
    private @ColorRes
    final Integer gliaChatBackgroundColor;
    /**
     * Color for Chat Header title
     */
    private @ColorRes
    final Integer gliaChatHeaderTitleTintColor;
    /**
     * Color for Chat Home Button
     */
    private @ColorRes
    final Integer gliaChatHeaderHomeButtonTintColor;
    /**
     * Color for Chat Exit Queue Button
     */
    private @ColorRes
    final Integer gliaChatHeaderExitQueueButtonTintColor;
    /**
     * Allow overriding the view's fontFamily
     */
    private @FontRes
    final Integer fontRes;
    /**
     * The appbar's back icon resId
     */
    private @DrawableRes
    final Integer iconAppBarBack;
    /**
     * The appbar's leave queue icon resId
     */
    private @DrawableRes
    final Integer iconLeaveQueue;
    /**
     * The send message icon resId
     */
    private @DrawableRes
    final Integer iconSendMessage;
    /**
     * The icon resId used in chat messages to display an ongoing audio upgrade timer
     */
    private @DrawableRes
    final Integer iconChatAudioUpgrade;
    /**
     * The icon resId used in the header of an audio upgrade dialog
     */
    private @DrawableRes
    final Integer iconUpgradeAudioDialog;
    /**
     * The icon resId used to set the call's audio on
     */
    private @DrawableRes
    final Integer iconCallAudioOn;
    /**
     * The icon resId used in chat messages to display an ongoing video upgrade timer
     */
    private @DrawableRes
    final Integer iconChatVideoUpgrade;
    /**
     * The icon resId used in the header of an video upgrade dialog
     */
    private @DrawableRes
    final Integer iconUpgradeVideoDialog;
    /**
     * The icon resId used in the header of of a screen sharing upgrade dialog
     */
    private @DrawableRes
    final Integer iconScreenSharingDialog;
    /**
     * The icon resId used to set the call's video on
     */
    private @DrawableRes
    final Integer iconCallVideoOn;
    /**
     * The icon resId used to set the call's audio off
     */
    private @DrawableRes
    final Integer iconCallAudioOff;
    /**
     * The icon resId used set the call's video off
     */
    private @DrawableRes
    final Integer iconCallVideoOff;
    /**
     * The icon resId used to navigate back to the chat from a call
     */
    private @DrawableRes
    final Integer iconCallChat;
    /**
     * The icon resId used to set the call's speaker on
     */
    private @DrawableRes
    final Integer iconCallSpeakerOn;
    /**
     * The icon resId used to set the call's speaker off
     */
    private @DrawableRes
    final Integer iconCallSpeakerOff;
    /**
     * The icon resId used to minimize a call
     */
    private @DrawableRes
    final Integer iconCallMinimize;
    /**
     * The icon resId used in a chat head if an operator's profile image is absent
     */
    private @DrawableRes
    final Integer iconPlaceholder;

    /**
     * The icon resId used when visitor is put on hold by the operator
     */
    private @DrawableRes
    final Integer iconOnHold;

    /**
     * Connecting Operator Status Layout Heading Text Color
     */
    private @ColorRes
    final Integer chatStartingHeadingTextColor;

    /**
     * Connecting Operator Status Layout Caption Text Color
     */
    private @ColorRes
    final Integer chatStartingCaptionTextColor;

    /**
     * Connected Operator Status Layout Heading Text Color
     */
    private @ColorRes
    final Integer chatStartedHeadingTextColor;

    /**
     * Connected Operator Status Layout Caption Text Color
     */
    private @ColorRes
    final Integer chatStartedCaptionTextColor;

    private final Boolean whiteLabel;
    private final Boolean gliaAlertDialogButtonUseVerticalAlignment;

    private final TextConfiguration choiceCardContentTextConfiguration;

    private final ButtonConfiguration headerEndButtonConfiguration;
    private final ButtonConfiguration positiveButtonConfiguration;
    private final ButtonConfiguration negativeButtonConfiguration;
    private final ButtonConfiguration neutralButtonConfiguration;

    private final ChatHeadConfiguration chatHeadConfiguration;

    private UiTheme(UiThemeBuilder builder) {
        this.appBarTitle = builder.appBarTitle;
        this.brandPrimaryColor = builder.brandPrimaryColor;
        this.baseLightColor = builder.baseLightColor;
        this.baseDarkColor = builder.baseDarkColor;
        this.baseNormalColor = builder.baseNormalColor;
        this.baseShadeColor = builder.baseShadeColor;
        this.systemAgentBubbleColor = builder.systemAgentBubbleColor;
        this.fontRes = builder.fontRes;
        this.systemNegativeColor = builder.systemNegativeColor;
        this.visitorMessageBackgroundColor = builder.visitorMessageBackgroundColor;
        this.visitorMessageTextColor = builder.visitorMessageTextColor;
        this.operatorMessageBackgroundColor = builder.operatorMessageBackgroundColor;
        this.gliaChatBackgroundColor = builder.gliaChatBackgroundColor;
        this.operatorMessageTextColor = builder.operatorMessageTextColor;
        this.botActionButtonBackgroundColor = builder.botActionButtonBackgroundColor;
        this.botActionButtonTextColor = builder.botActionButtonTextColor;
        this.botActionButtonSelectedBackgroundColor = builder.botActionButtonSelectedBackgroundColor;
        this.botActionButtonSelectedTextColor = builder.botActionButtonSelectedTextColor;
        this.sendMessageButtonTintColor = builder.sendMessageButtonTintColor;
        this.gliaChatHeaderTitleTintColor = builder.gliaChatHeaderTitleTintColor;
        this.gliaChatHeaderHomeButtonTintColor = builder.gliaChatHomeButtonTintColor;
        this.gliaChatHeaderExitQueueButtonTintColor = builder.gliaChatHeaderExitQueueButtonTintColor;
        this.iconAppBarBack = builder.iconAppBarBack;
        this.iconLeaveQueue = builder.iconLeaveQueue;
        this.iconSendMessage = builder.iconSendMessage;
        this.iconChatAudioUpgrade = builder.iconChatAudioUpgrade;
        this.iconUpgradeAudioDialog = builder.iconUpgradeAudioDialog;
        this.iconCallAudioOn = builder.iconCallAudioOn;
        this.iconChatVideoUpgrade = builder.iconChatVideoUpgrade;
        this.iconUpgradeVideoDialog = builder.iconUpgradeVideoDialog;
        this.iconScreenSharingDialog = builder.iconScreenSharingDialog;
        this.iconCallVideoOn = builder.iconCallVideoOn;
        this.iconCallAudioOff = builder.iconCallAudioOff;
        this.iconCallVideoOff = builder.iconCallVideoOff;
        this.iconCallChat = builder.iconCallChat;
        this.iconCallSpeakerOn = builder.iconCallSpeakerOn;
        this.iconCallSpeakerOff = builder.iconCallSpeakerOff;
        this.iconCallMinimize = builder.iconCallMinimize;
        this.iconPlaceholder = builder.iconPlaceholder;
        this.iconOnHold = builder.iconOnHold;
        this.whiteLabel = builder.whiteLabel;
        this.gliaAlertDialogButtonUseVerticalAlignment = builder.gliaAlertDialogButtonUseVerticalAlignment;
        this.headerEndButtonConfiguration = builder.headerEndButtonConfiguration;
        this.positiveButtonConfiguration = builder.positiveButtonConfiguration;
        this.negativeButtonConfiguration = builder.negativeButtonConfiguration;
        this.neutralButtonConfiguration = builder.neutralButtonConfiguration;
        this.chatStartingCaptionTextColor = builder.chatStartingCaptionTextColor;
        this.chatStartingHeadingTextColor = builder.chatStartingHeadingTextColor;
        this.chatStartedCaptionTextColor = builder.chatStartedCaptionTextColor;
        this.chatStartedHeadingTextColor = builder.chatStartedHeadingTextColor;
        this.choiceCardContentTextConfiguration = builder.choiceCardContentTextConfiguration;
        this.chatHeadConfiguration = builder.chatHeadConfiguration;
    }

    public static class UiThemeBuilder {
        /**
         * Text to be shown on the top of the app bar in the chat
         **/
        private String appBarTitle;
        /**
         * Primary color for your brand. Used for example to set the color of the appbar
         **/
        private @ColorRes
        Integer brandPrimaryColor;
        /**
         * Dark color of the UI widgets. Used for example to change the body text colors
         **/
        private @ColorRes
        Integer baseLightColor;
        /**
         * Light color of the UI widgets. Used for example to change the title text colors.
         * Background color of the chat, etc.
         **/
        private @ColorRes
        Integer baseDarkColor;
        /**
         * Base normal color of the chat
         */
        private @ColorRes
        Integer baseNormalColor;
        /**
         * Base shade color of the chat
         */
        private @ColorRes
        Integer baseShadeColor;
        /**
         * Color for the operator's chat messages background
         */
        private @ColorRes
        Integer systemAgentBubbleColor;
        /**
         * Color for any error or chat widgets
         */
        private @ColorRes
        Integer systemNegativeColor;
        /**
         * Color for visitor message background
         */
        private @ColorRes
        Integer visitorMessageBackgroundColor;
        /**
         * Color for visitor message text
         */
        private @ColorRes
        Integer visitorMessageTextColor;
        /**
         * Color for operator message background
         */
        private @ColorRes
        Integer operatorMessageBackgroundColor;
        /**
         * Color for operator message text
         */
        private @ColorRes
        Integer operatorMessageTextColor;
        /**
         * Color for bot action button background color
         */
        private @ColorRes
        Integer botActionButtonBackgroundColor;
        /**
         * Color for bot action button text color
         */
        private @ColorRes
        Integer botActionButtonTextColor;
        /**
         * Color for bot action button background color
         */
        private @ColorRes
        Integer botActionButtonSelectedBackgroundColor;
        /**
         * Color for bot action button text color
         */
        private @ColorRes
        Integer botActionButtonSelectedTextColor;
        /**
         * Color for Send message Button
         */
        private @ColorRes
        Integer sendMessageButtonTintColor;
        /**
         * Color for chat background
         */
        private @ColorRes
        Integer gliaChatBackgroundColor;
        /**
         * Color for Chat Header Title
         */
        private @ColorRes
        Integer gliaChatHeaderTitleTintColor;
        /**
         * Color for Chat Home Button
         */
        private @ColorRes
        Integer gliaChatHomeButtonTintColor;
        /**
         *
         */
        private @ColorRes
        Integer gliaChatHeaderExitQueueButtonTintColor;
        /**
         * Allow overriding the view's fontFamily
         */
        private @FontRes
        Integer fontRes;
        /**
         * The appbar's back icon resId
         */
        private @DrawableRes
        Integer iconAppBarBack;
        /**
         * The appbar's leave queue icon resId
         */
        private @DrawableRes
        Integer iconLeaveQueue;
        /**
         * The send message icon resId
         */
        private @DrawableRes
        Integer iconSendMessage;
        /**
         * The icon resId used in chat messages to display an ongoing audio upgrade timer
         */
        private @DrawableRes
        Integer iconChatAudioUpgrade;
        /**
         * The icon resId used in the header of an audio upgrade dialog
         */
        private @DrawableRes
        Integer iconUpgradeAudioDialog;
        /**
         * The icon resId used to set the call's audio on
         */
        private @DrawableRes
        Integer iconCallAudioOn;
        /**
         * The icon resId used in chat messages to display an ongoing video upgrade timer
         */
        private @DrawableRes
        Integer iconChatVideoUpgrade;
        /**
         * The icon resId used in the header of an video upgrade dialog
         */
        private @DrawableRes
        Integer iconUpgradeVideoDialog;
        /**
         * The icon resId used in the header of of a screen sharing upgrade dialog
         */
        private @DrawableRes
        Integer iconScreenSharingDialog;
        /**
         * The icon resId used to set the call's video on
         */
        private @DrawableRes
        Integer iconCallVideoOn;
        /**
         * The icon resId used to set the call's audio off
         */
        private @DrawableRes
        Integer iconCallAudioOff;
        /**
         * The icon resId used set the call's video off
         */
        private @DrawableRes
        Integer iconCallVideoOff;
        /**
         * The icon resId used to navigate back to the chat from a call
         */
        private @DrawableRes
        Integer iconCallChat;
        /**
         * The icon resId used to set the call's speaker on
         */
        private @DrawableRes
        Integer iconCallSpeakerOn;
        /**
         * The icon resId used to set the call's speaker off
         */
        private @DrawableRes
        Integer iconCallSpeakerOff;
        /**
         * The icon resId used to minimize a call
         */
        private @DrawableRes
        Integer iconCallMinimize;
        /**
         * The icon resId used in a chat head if an operator's profile image is absent
         */
        private @DrawableRes
        Integer iconPlaceholder;

        /**
         * The icon resId used when visitor is put on hold by operator
         */
        private @DrawableRes
        Integer iconOnHold;

        /**
         * Connecting Operator Status Layout Heading Text Color
         */
        private @ColorRes
        Integer chatStartingHeadingTextColor;

        /**
         * Connecting Operator Status Layout Caption Text Color
         */
        private @ColorRes
        Integer chatStartingCaptionTextColor;

        /**
         * Connected Operator Status Layout Heading Text Color
         */
        private @ColorRes
        Integer chatStartedHeadingTextColor;

        /**
         * Connected Operator Status Layout Caption Text Color
         */
        private @ColorRes
        Integer chatStartedCaptionTextColor;

        private
        Boolean whiteLabel;

        private
        Boolean gliaAlertDialogButtonUseVerticalAlignment;

        private
        ButtonConfiguration headerEndButtonConfiguration;

        private
        ButtonConfiguration positiveButtonConfiguration;

        private
        ButtonConfiguration negativeButtonConfiguration;

        private
        ButtonConfiguration neutralButtonConfiguration;

        private
        TextConfiguration choiceCardContentTextConfiguration;

        private
        ChatHeadConfiguration chatHeadConfiguration;

        public void setAppBarTitle(String appBarTitle) {
            this.appBarTitle = appBarTitle;
        }

        public void setFontRes(@FontRes Integer fontRes) {
            if (fontRes != null && fontRes != 0) {
                this.fontRes = fontRes;
            } else {
                this.fontRes = null;
            }
        }

        public void setBrandPrimaryColor(@ColorRes Integer brandPrimaryColor) {
            this.brandPrimaryColor = brandPrimaryColor;
        }

        public void setBaseLightColor(@ColorRes Integer baseLightColor) {
            this.baseLightColor = baseLightColor;
        }

        public void setBaseDarkColor(@ColorRes Integer baseDarkColor) {
            this.baseDarkColor = baseDarkColor;
        }

        public void setBaseNormalColor(Integer baseNormalColor) {
            this.baseNormalColor = baseNormalColor;
        }

        public void setBaseShadeColor(Integer baseShadeColor) {
            this.baseShadeColor = baseShadeColor;
        }

        public void setSystemAgentBubbleColor(@ColorRes Integer systemAgentBubbleColor) {
            this.systemAgentBubbleColor = systemAgentBubbleColor;
        }

        public void setSystemNegativeColor(@ColorRes Integer systemNegativeColor) {
            this.systemNegativeColor = systemNegativeColor;
        }

        public void setVisitorMessageBackgroundColor(@ColorRes Integer color) {
            this.visitorMessageBackgroundColor = color;
        }

        public void setVisitorMessageTextColor(@ColorRes Integer color) {
            this.visitorMessageTextColor = color;
        }

        public void setOperatorMessageBackgroundColor(@ColorRes Integer color) {
            this.operatorMessageBackgroundColor = color;
        }

        public void setOperatorMessageTextColor(@ColorRes Integer color) {
            this.operatorMessageTextColor = color;
        }

        public void setBotActionButtonBackgroundColor(@ColorRes Integer color) {
            this.botActionButtonBackgroundColor = color;
        }

        public void setBotActionButtonTextColor(@ColorRes Integer color) {
            this.botActionButtonTextColor = color;
        }

        public void setBotActionButtonSelectedBackgroundColor(@ColorRes Integer color) {
            this.botActionButtonSelectedBackgroundColor = color;
        }

        public void setBotActionButtonSelectedTextColor(@ColorRes Integer color) {
            this.botActionButtonSelectedTextColor = color;
        }

        public void setIconAppBarBack(@DrawableRes Integer iconAppBarBack) {
            this.iconAppBarBack = iconAppBarBack;
        }

        public void setIconLeaveQueue(@DrawableRes Integer iconLeaveQueue) {
            this.iconLeaveQueue = iconLeaveQueue;
        }

        public void setIconSendMessage(@DrawableRes Integer iconSendMessage) {
            this.iconSendMessage = iconSendMessage;
        }

        public void setIconChatAudioUpgrade(@DrawableRes Integer iconChatAudioUpgrade) {
            this.iconChatAudioUpgrade = iconChatAudioUpgrade;
        }

        public void setIconUpgradeAudioDialog(@DrawableRes Integer iconUpgradeAudioDialog) {
            this.iconUpgradeAudioDialog = iconUpgradeAudioDialog;
        }

        public void setIconCallAudioOn(@DrawableRes Integer iconCallAudioOn) {
            this.iconCallAudioOn = iconCallAudioOn;
        }

        public void setIconChatVideoUpgrade(@DrawableRes Integer iconChatVideoUpgrade) {
            this.iconChatVideoUpgrade = iconChatVideoUpgrade;
        }

        public void setIconUpgradeVideoDialog(@DrawableRes Integer iconUpgradeVideoDialog) {
            this.iconUpgradeVideoDialog = iconUpgradeVideoDialog;
        }

        public void setIconScreenSharingDialog(@DrawableRes Integer iconScreenSharingDialog) {
            this.iconScreenSharingDialog = iconScreenSharingDialog;
        }

        public void setIconCallVideoOn(@DrawableRes Integer iconCallVideoOn) {
            this.iconCallVideoOn = iconCallVideoOn;
        }

        public void setIconCallAudioOff(@DrawableRes Integer iconCallAudioOff) {
            this.iconCallAudioOff = iconCallAudioOff;
        }

        public void setIconCallVideoOff(@DrawableRes Integer iconCallVideoOff) {
            this.iconCallVideoOff = iconCallVideoOff;
        }

        public void setIconCallChat(@DrawableRes Integer iconCallChat) {
            this.iconCallChat = iconCallChat;
        }

        public void setIconCallSpeakerOn(@DrawableRes Integer iconCallSpeakerOn) {
            this.iconCallSpeakerOn = iconCallSpeakerOn;
        }

        public void setIconCallSpeakerOff(@DrawableRes Integer iconCallSpeakerOff) {
            this.iconCallSpeakerOff = iconCallSpeakerOff;
        }

        public void setIconCallMinimize(@DrawableRes Integer iconCallMinimize) {
            this.iconCallMinimize = iconCallMinimize;
        }

        public void setIconPlaceholder(@DrawableRes Integer iconPlaceholder) {
            this.iconPlaceholder = iconPlaceholder;
        }

        public void setIconOnHold(@DrawableRes Integer iconOnHold) {
            this.iconOnHold = iconOnHold;
        }

        public void setWhiteLabel(Boolean whiteLabel) {
            this.whiteLabel = whiteLabel;
        }

        public void setGliaAlertDialogButtonUseVerticalAlignment(Boolean value) {
            this.gliaAlertDialogButtonUseVerticalAlignment = value;
        }

        public void setHeaderEndButtonConfiguration(ButtonConfiguration configuration) {
            this.headerEndButtonConfiguration = configuration;
        }

        public void setPositiveButtonConfiguration(ButtonConfiguration configuration) {
            this.positiveButtonConfiguration = configuration;
        }

        public void setNegativeButtonConfiguration(ButtonConfiguration configuration) {
            this.negativeButtonConfiguration = configuration;
        }

        public void setNeutralButtonConfiguration(ButtonConfiguration configuration) {
            this.neutralButtonConfiguration = configuration;
        }

        public void setSendMessageButtonTintColor(Integer color) {
            this.sendMessageButtonTintColor = color;
        }

        public void setGliaChatBackgroundColor(@ColorRes Integer color) {
            this.gliaChatBackgroundColor = color;
        }

        public void setGliaChatHeaderTitleTintColor(Integer color) {
            this.gliaChatHeaderTitleTintColor = color;
        }

        public void setGliaChatHeaderHomeButtonTintColor(Integer color) {
            this.gliaChatHomeButtonTintColor = color;
        }

        public void setGliaChatHeaderExitQueueButtonTintColor(Integer color) {
            this.gliaChatHeaderExitQueueButtonTintColor = color;
        }

        public void setChatStartingHeadingTextColor(Integer color) {
            this.chatStartingHeadingTextColor = color;
        }

        public void setChatStartingCaptionTextColor(Integer color) {
            this.chatStartingCaptionTextColor = color;
        }

        public void setChatStartedHeadingTextColor(Integer color) {
            this.chatStartedHeadingTextColor = color;
        }

        public void setChatStartedCaptionTextColor(Integer color) {
            this.chatStartedCaptionTextColor = color;
        }

        public void setChoiceCardContentTextConfiguration(TextConfiguration textConfiguration) {
            this.choiceCardContentTextConfiguration = textConfiguration;
        }

        public void setChatHeadConfiguration(ChatHeadConfiguration chatHeadConfiguration) {
            this.chatHeadConfiguration = chatHeadConfiguration;
        }

        public void setTheme(UiTheme theme) {
            this.appBarTitle = theme.appBarTitle;
            this.brandPrimaryColor = theme.brandPrimaryColor;
            this.baseLightColor = theme.baseLightColor;
            this.baseDarkColor = theme.baseDarkColor;
            this.baseNormalColor = theme.baseNormalColor;
            this.baseShadeColor = theme.baseShadeColor;
            this.systemAgentBubbleColor = theme.systemAgentBubbleColor;
            this.fontRes = theme.fontRes;
            this.systemNegativeColor = theme.systemNegativeColor;
            this.visitorMessageBackgroundColor = theme.visitorMessageBackgroundColor;
            this.visitorMessageTextColor = theme.visitorMessageTextColor;
            this.operatorMessageBackgroundColor = theme.operatorMessageBackgroundColor;
            this.operatorMessageTextColor = theme.operatorMessageTextColor;
            this.botActionButtonBackgroundColor = theme.botActionButtonBackgroundColor;
            this.botActionButtonTextColor = theme.botActionButtonTextColor;
            this.botActionButtonSelectedBackgroundColor = theme.botActionButtonSelectedBackgroundColor;
            this.botActionButtonSelectedTextColor = theme.botActionButtonSelectedTextColor;
            this.iconAppBarBack = theme.iconAppBarBack;
            this.iconLeaveQueue = theme.iconLeaveQueue;
            this.iconSendMessage = theme.iconSendMessage;
            this.iconChatAudioUpgrade = theme.iconChatAudioUpgrade;
            this.iconUpgradeAudioDialog = theme.iconUpgradeAudioDialog;
            this.iconCallAudioOn = theme.iconCallAudioOn;
            this.iconChatVideoUpgrade = theme.iconChatVideoUpgrade;
            this.iconUpgradeVideoDialog = theme.iconUpgradeVideoDialog;
            this.iconScreenSharingDialog = theme.iconScreenSharingDialog;
            this.iconCallVideoOn = theme.iconCallVideoOn;
            this.iconCallAudioOff = theme.iconCallAudioOff;
            this.iconCallVideoOff = theme.iconCallVideoOff;
            this.iconCallChat = theme.iconCallChat;
            this.iconCallSpeakerOn = theme.iconCallSpeakerOn;
            this.iconCallSpeakerOff = theme.iconCallSpeakerOff;
            this.iconCallMinimize = theme.iconCallMinimize;
            this.iconPlaceholder = theme.iconPlaceholder;
            this.iconOnHold = theme.iconOnHold;
            this.whiteLabel = theme.whiteLabel;
            this.headerEndButtonConfiguration = theme.headerEndButtonConfiguration;
            this.positiveButtonConfiguration = theme.positiveButtonConfiguration;
            this.negativeButtonConfiguration = theme.negativeButtonConfiguration;
            this.neutralButtonConfiguration = theme.neutralButtonConfiguration;
            this.sendMessageButtonTintColor = theme.sendMessageButtonTintColor;
            this.gliaChatBackgroundColor = theme.gliaChatBackgroundColor;
            this.gliaChatHeaderTitleTintColor = theme.gliaChatHeaderTitleTintColor;
            this.gliaChatHomeButtonTintColor = theme.gliaChatHeaderHomeButtonTintColor;
            this.gliaChatHeaderExitQueueButtonTintColor = theme.gliaChatHeaderExitQueueButtonTintColor;
            this.chatStartingCaptionTextColor = theme.chatStartingCaptionTextColor;
            this.chatStartingHeadingTextColor = theme.chatStartingHeadingTextColor;
            this.chatStartedCaptionTextColor = theme.chatStartedCaptionTextColor;
            this.chatStartedHeadingTextColor = theme.chatStartedHeadingTextColor;
            this.choiceCardContentTextConfiguration = theme.choiceCardContentTextConfiguration;
            this.chatHeadConfiguration = theme.chatHeadConfiguration;
        }

        public UiTheme build() {
            return new UiTheme(this);
        }
    }

    protected UiTheme(Parcel in) {
        appBarTitle = in.readString();
        if (in.readByte() == 0) {
            brandPrimaryColor = null;
        } else {
            brandPrimaryColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseLightColor = null;
        } else {
            baseLightColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseDarkColor = null;
        } else {
            baseDarkColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseNormalColor = null;
        } else {
            baseNormalColor = in.readInt();
        }
        if (in.readByte() == 0) {
            baseShadeColor = null;
        } else {
            baseShadeColor = in.readInt();
        }
        if (in.readByte() == 0) {
            systemAgentBubbleColor = null;
        } else {
            systemAgentBubbleColor = in.readInt();
        }
        if (in.readByte() == 0) {
            systemNegativeColor = null;
        } else {
            systemNegativeColor = in.readInt();
        }
        if (in.readByte() == 0) {
            visitorMessageBackgroundColor = null;
        } else {
            visitorMessageBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            visitorMessageTextColor = null;
        } else {
            visitorMessageTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            operatorMessageBackgroundColor = null;
        } else {
            operatorMessageBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            operatorMessageTextColor = null;
        } else {
            operatorMessageTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            botActionButtonBackgroundColor = null;
        } else {
            botActionButtonBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            botActionButtonTextColor = null;
        } else {
            botActionButtonTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            botActionButtonSelectedBackgroundColor = null;
        } else {
            botActionButtonSelectedBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            botActionButtonSelectedTextColor = null;
        } else {
            botActionButtonSelectedTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            sendMessageButtonTintColor = null;
        } else {
            sendMessageButtonTintColor = in.readInt();
        }
        if (in.readByte() == 0) {
            gliaChatBackgroundColor = null;
        } else {
            gliaChatBackgroundColor = in.readInt();
        }
        if (in.readByte() == 0) {
            gliaChatHeaderTitleTintColor = null;
        } else {
            gliaChatHeaderTitleTintColor = in.readInt();
        }
        if (in.readByte() == 0) {
            gliaChatHeaderHomeButtonTintColor = null;
        } else {
            gliaChatHeaderHomeButtonTintColor = in.readInt();
        }
        if (in.readByte() == 0) {
            gliaChatHeaderExitQueueButtonTintColor = null;
        } else {
            gliaChatHeaderExitQueueButtonTintColor = in.readInt();
        }
        if (in.readByte() == 0) {
            fontRes = null;
        } else {
            fontRes = in.readInt();
        }
        if (in.readByte() == 0) {
            iconAppBarBack = null;
        } else {
            iconAppBarBack = in.readInt();
        }
        if (in.readByte() == 0) {
            iconLeaveQueue = null;
        } else {
            iconLeaveQueue = in.readInt();
        }
        if (in.readByte() == 0) {
            iconSendMessage = null;
        } else {
            iconSendMessage = in.readInt();
        }
        if (in.readByte() == 0) {
            iconChatAudioUpgrade = null;
        } else {
            iconChatAudioUpgrade = in.readInt();
        }
        if (in.readByte() == 0) {
            iconUpgradeAudioDialog = null;
        } else {
            iconUpgradeAudioDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallAudioOn = null;
        } else {
            iconCallAudioOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconChatVideoUpgrade = null;
        } else {
            iconChatVideoUpgrade = in.readInt();
        }
        if (in.readByte() == 0) {
            iconUpgradeVideoDialog = null;
        } else {
            iconUpgradeVideoDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconScreenSharingDialog = null;
        } else {
            iconScreenSharingDialog = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallVideoOn = null;
        } else {
            iconCallVideoOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallAudioOff = null;
        } else {
            iconCallAudioOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallVideoOff = null;
        } else {
            iconCallVideoOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallChat = null;
        } else {
            iconCallChat = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallSpeakerOn = null;
        } else {
            iconCallSpeakerOn = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallSpeakerOff = null;
        } else {
            iconCallSpeakerOff = in.readInt();
        }
        if (in.readByte() == 0) {
            iconCallMinimize = null;
        } else {
            iconCallMinimize = in.readInt();
        }
        if (in.readByte() == 0) {
            iconPlaceholder = null;
        } else {
            iconPlaceholder = in.readInt();
        }
        if (in.readByte() == 0) {
            iconOnHold = null;
        } else {
            iconOnHold = in.readInt();
        }
        if (in.readByte() == 0) {
            chatStartingHeadingTextColor = null;
        } else {
            chatStartingHeadingTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            chatStartingCaptionTextColor = null;
        } else {
            chatStartingCaptionTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            chatStartedHeadingTextColor = null;
        } else {
            chatStartedHeadingTextColor = in.readInt();
        }
        if (in.readByte() == 0) {
            chatStartedCaptionTextColor = null;
        } else {
            chatStartedCaptionTextColor = in.readInt();
        }
        byte tmpWhiteLabel = in.readByte();
        whiteLabel = tmpWhiteLabel == 0 ? null : tmpWhiteLabel == 1;
        byte tmpGliaAlertDialogButtonUseVerticalAlignment = in.readByte();
        gliaAlertDialogButtonUseVerticalAlignment = tmpGliaAlertDialogButtonUseVerticalAlignment == 0 ? null : tmpGliaAlertDialogButtonUseVerticalAlignment == 1;
        choiceCardContentTextConfiguration = in.readParcelable(TextConfiguration.class.getClassLoader());
        headerEndButtonConfiguration = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        positiveButtonConfiguration = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        negativeButtonConfiguration = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        neutralButtonConfiguration = in.readParcelable(ButtonConfiguration.class.getClassLoader());
        chatHeadConfiguration = in.readParcelable(ChatHeadConfiguration.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appBarTitle);
        if (brandPrimaryColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(brandPrimaryColor);
        }
        if (baseLightColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseLightColor);
        }
        if (baseDarkColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseDarkColor);
        }
        if (baseNormalColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseNormalColor);
        }
        if (baseShadeColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(baseShadeColor);
        }
        if (systemAgentBubbleColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(systemAgentBubbleColor);
        }
        if (systemNegativeColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(systemNegativeColor);
        }
        if (visitorMessageBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(visitorMessageBackgroundColor);
        }
        if (visitorMessageTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(visitorMessageTextColor);
        }
        if (operatorMessageBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(operatorMessageBackgroundColor);
        }
        if (operatorMessageTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(operatorMessageTextColor);
        }
        if (botActionButtonBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(botActionButtonBackgroundColor);
        }
        if (botActionButtonTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(botActionButtonTextColor);
        }
        if (botActionButtonSelectedBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(botActionButtonSelectedBackgroundColor);
        }
        if (botActionButtonSelectedTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(botActionButtonSelectedTextColor);
        }
        if (sendMessageButtonTintColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(sendMessageButtonTintColor);
        }
        if (gliaChatBackgroundColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gliaChatBackgroundColor);
        }
        if (gliaChatHeaderTitleTintColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gliaChatHeaderTitleTintColor);
        }
        if (gliaChatHeaderHomeButtonTintColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gliaChatHeaderHomeButtonTintColor);
        }
        if (gliaChatHeaderExitQueueButtonTintColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gliaChatHeaderExitQueueButtonTintColor);
        }
        if (fontRes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(fontRes);
        }
        if (iconAppBarBack == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconAppBarBack);
        }
        if (iconLeaveQueue == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconLeaveQueue);
        }
        if (iconSendMessage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconSendMessage);
        }
        if (iconChatAudioUpgrade == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconChatAudioUpgrade);
        }
        if (iconUpgradeAudioDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconUpgradeAudioDialog);
        }
        if (iconCallAudioOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallAudioOn);
        }
        if (iconChatVideoUpgrade == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconChatVideoUpgrade);
        }
        if (iconUpgradeVideoDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconUpgradeVideoDialog);
        }
        if (iconScreenSharingDialog == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconScreenSharingDialog);
        }
        if (iconCallVideoOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallVideoOn);
        }
        if (iconCallAudioOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallAudioOff);
        }
        if (iconCallVideoOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallVideoOff);
        }
        if (iconCallChat == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallChat);
        }
        if (iconCallSpeakerOn == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallSpeakerOn);
        }
        if (iconCallSpeakerOff == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallSpeakerOff);
        }
        if (iconCallMinimize == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconCallMinimize);
        }
        if (iconPlaceholder == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconPlaceholder);
        }
        if (iconOnHold == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(iconOnHold);
        }
        if (chatStartingHeadingTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chatStartingHeadingTextColor);
        }
        if (chatStartingCaptionTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chatStartingCaptionTextColor);
        }
        if (chatStartedHeadingTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chatStartedHeadingTextColor);
        }
        if (chatStartedCaptionTextColor == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(chatStartedCaptionTextColor);
        }
        dest.writeByte((byte) (whiteLabel == null ? 0 : whiteLabel ? 1 : 2));
        dest.writeByte((byte) (gliaAlertDialogButtonUseVerticalAlignment == null ? 0 : gliaAlertDialogButtonUseVerticalAlignment ? 1 : 2));
        dest.writeParcelable(choiceCardContentTextConfiguration, flags);
        dest.writeParcelable(headerEndButtonConfiguration, flags);
        dest.writeParcelable(positiveButtonConfiguration, flags);
        dest.writeParcelable(negativeButtonConfiguration, flags);
        dest.writeParcelable(neutralButtonConfiguration, flags);
        dest.writeParcelable(chatHeadConfiguration, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UiTheme> CREATOR = new Creator<UiTheme>() {
        @Override
        public UiTheme createFromParcel(Parcel in) {
            return new UiTheme(in);
        }

        @Override
        public UiTheme[] newArray(int size) {
            return new UiTheme[size];
        }
    };

    public String getAppBarTitle() {
        return appBarTitle;
    }

    public Integer getBrandPrimaryColor() {
        return brandPrimaryColor;
    }

    public Integer getBaseLightColor() {
        return baseLightColor;
    }

    public Integer getBaseDarkColor() {
        return baseDarkColor;
    }

    public Integer getBaseNormalColor() {
        return baseNormalColor;
    }

    public Integer getBaseShadeColor() {
        return baseShadeColor;
    }

    public Integer getSystemAgentBubbleColor() {
        return systemAgentBubbleColor;
    }

    public Integer getFontRes() {
        return fontRes;
    }

    public Integer getSystemNegativeColor() {
        return systemNegativeColor;
    }

    public Integer getVisitorMessageBackgroundColor() {
        return visitorMessageBackgroundColor;
    }

    public Integer getVisitorMessageTextColor() {
        return visitorMessageTextColor;
    }

    public Integer getOperatorMessageBackgroundColor() {
        return operatorMessageBackgroundColor;
    }

    public Integer getOperatorMessageTextColor() {
        return operatorMessageTextColor;
    }

    public Integer getBotActionButtonBackgroundColor() {
        return botActionButtonBackgroundColor;
    }

    public Integer getBotActionButtonTextColor() {
        return botActionButtonTextColor;
    }

    public Integer getBotActionButtonSelectedBackgroundColor() {
        return botActionButtonSelectedBackgroundColor;
    }

    public Integer getBotActionButtonSelectedTextColor() {
        return botActionButtonSelectedTextColor;
    }

    public Integer getSendMessageButtonTintColor() {
        return sendMessageButtonTintColor;
    }

    public Integer getGliaChatBackgroundColor() {
        return gliaChatBackgroundColor;
    }

    public Integer getGliaChatHeaderTitleTintColor() {
        return gliaChatHeaderTitleTintColor;
    }

    public Integer getGliaChatHeaderHomeButtonTintColor() {
        return gliaChatHeaderHomeButtonTintColor;
    }

    public Integer getGliaChatHeaderExitQueueButtonTintColor() {
        return gliaChatHeaderExitQueueButtonTintColor;
    }

    public Integer getIconAppBarBack() {
        return iconAppBarBack;
    }

    public Integer getIconLeaveQueue() {
        return iconLeaveQueue;
    }

    public Integer getIconSendMessage() {
        return iconSendMessage;
    }

    public Integer getIconChatAudioUpgrade() {
        return iconChatAudioUpgrade;
    }

    public Integer getIconUpgradeAudioDialog() {
        return iconUpgradeAudioDialog;
    }

    public Integer getIconCallAudioOn() {
        return iconCallAudioOn;
    }

    public Integer getIconChatVideoUpgrade() {
        return iconChatVideoUpgrade;
    }

    public Integer getIconUpgradeVideoDialog() {
        return iconUpgradeVideoDialog;
    }

    public Integer getIconScreenSharingDialog() {
        return iconScreenSharingDialog;
    }

    public Integer getIconCallVideoOn() {
        return iconCallVideoOn;
    }

    public Integer getIconCallAudioOff() {
        return iconCallAudioOff;
    }

    public Integer getIconCallVideoOff() {
        return iconCallVideoOff;
    }

    public Integer getIconCallChat() {
        return iconCallChat;
    }

    public Integer getIconCallSpeakerOn() {
        return iconCallSpeakerOn;
    }

    public Integer getIconCallSpeakerOff() {
        return iconCallSpeakerOff;
    }

    public Integer getIconCallMinimize() {
        return iconCallMinimize;
    }

    public Integer getIconPlaceholder() {
        return iconPlaceholder;
    }

    public Integer getIconOnHold() {
        return iconOnHold;
    }

    public Boolean getWhiteLabel() {
        return whiteLabel;
    }

    public Boolean getGliaAlertDialogButtonUseVerticalAlignment() {
        return gliaAlertDialogButtonUseVerticalAlignment;
    }

    public ButtonConfiguration getGliaEndButtonConfiguration() {
        return headerEndButtonConfiguration;
    }

    public ButtonConfiguration getGliaPositiveButtonConfiguration() {
        return positiveButtonConfiguration;
    }

    public ButtonConfiguration getGliaNegativeButtonConfiguration() {
        return negativeButtonConfiguration;
    }

    public ButtonConfiguration getGliaNeutralButtonConfiguration() {
        return neutralButtonConfiguration;
    }

    public TextConfiguration getGliaChoiceCardContentTextConfiguration() {
        return choiceCardContentTextConfiguration;
    }

    public Integer getGliaChatStartingHeadingTextColor() {
        return chatStartingHeadingTextColor;
    }

    public Integer getGliaChatStartingCaptionTextColor() {
        return chatStartingCaptionTextColor;
    }

    public Integer getGliaChatStartedHeadingTextColor() {
        return chatStartedHeadingTextColor;
    }

    public Integer getGliaChatStartedCaptionTextColor() {
        return chatStartedCaptionTextColor;
    }

    public ChatHeadConfiguration getChatHeadConfiguration() {
        return chatHeadConfiguration;
    }
}
