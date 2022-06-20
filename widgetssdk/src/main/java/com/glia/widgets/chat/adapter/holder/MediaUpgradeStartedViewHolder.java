package com.glia.widgets.chat.adapter.holder;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.model.history.MediaUpgradeStartedTimerItem;
import com.google.android.material.card.MaterialCardView;

public class MediaUpgradeStartedViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final ImageView iconView;
    private final TextView titleView;
    private final TextView timerView;
    private final @DrawableRes
    Integer upgradeAudioIcon;
    private final @DrawableRes
    Integer upgradeVideoIcon;

    public MediaUpgradeStartedViewHolder(@NonNull View itemView, UiTheme uiTheme) {
        super(itemView);
        context = itemView.getContext();

        MaterialCardView layoutCardView = itemView.findViewById(R.id.card_view);
        iconView = itemView.findViewById(R.id.icon_view);
        titleView = itemView.findViewById(R.id.title_view);
        timerView = itemView.findViewById(R.id.timer_view);

        this.upgradeAudioIcon = uiTheme.getIconChatAudioUpgrade();
        this.upgradeVideoIcon = uiTheme.getIconChatVideoUpgrade();

        ColorStateList baseLightStateList = ContextCompat.getColorStateList(context, uiTheme.getBaseLightColor());
        int baseShadeColor = ContextCompat.getColor(context, uiTheme.getBaseShadeColor());
        int baseNormalColor = ContextCompat.getColor(context, uiTheme.getBaseNormalColor());
        ColorStateList brandPrimaryColorStateList =
                ContextCompat.getColorStateList(context, uiTheme.getBrandPrimaryColor());
        int baseDarkColor = ContextCompat.getColor(context, uiTheme.getBaseDarkColor());

        layoutCardView.setBackgroundTintList(baseLightStateList);
        layoutCardView.setStrokeColor(baseShadeColor);
        iconView.setImageTintList(brandPrimaryColorStateList);
        titleView.setTextColor(baseDarkColor);
        timerView.setTextColor(baseNormalColor);

        if (uiTheme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, uiTheme.getFontRes());
            titleView.setTypeface(fontFamily);
            timerView.setTypeface(fontFamily);
        }
    }

    public void bind(MediaUpgradeStartedTimerItem chatItem) {
        if (chatItem.type == MediaUpgradeStartedTimerItem.Type.AUDIO) {
            iconView.setImageResource(upgradeAudioIcon);
            iconView.setContentDescription(context.getString(R.string.glia_chat_audio_icon_content_description));
            titleView.setText(context.getString(R.string.glia_chat_upgraded_to_audio_call));
        } else {
            iconView.setImageResource(upgradeVideoIcon);
            iconView.setContentDescription(context.getString(R.string.glia_chat_video_icon_content_description));
            titleView.setText(context.getString(R.string.glia_chat_upgraded_to_video_call));
        }
        timerView.setText(chatItem.time);
    }
}
