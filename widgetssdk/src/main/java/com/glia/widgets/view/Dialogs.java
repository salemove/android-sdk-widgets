package com.glia.widgets.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.glia.widgets.helper.Utils;
import com.glia.widgets.view.button.GliaNegativeButton;
import com.glia.widgets.view.button.GliaPositiveButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Dialogs {

    public static AlertDialog showOptionsDialog(Context context,
                                                UiTheme theme,
                                                String title,
                                                String message,
                                                String positiveButtonText,
                                                String negativeButtonText,
                                                View.OnClickListener positiveButtonClickListener,
                                                View.OnClickListener negativeButtonClickListener,
                                                DialogInterface.OnCancelListener cancelListener) {

        boolean isUseAlertDialogButtonVerticalAlignment = Utils.getGliaAlertDialogButtonUseVerticalAlignment(theme);

        int layout =
                isUseAlertDialogButtonVerticalAlignment ?
                        R.layout.options_dialog_vertical :
                        R.layout.options_dialog;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setOnCancelListener(cancelListener);
        View customLayout = LayoutInflater.from(context).inflate(layout, null, false);

        TextView titleView = customLayout.findViewById(R.id.dialog_title_view);
        TextView messageView = customLayout.findViewById(R.id.dialog_message_view);

        GliaNegativeButton negativeButton = customLayout.findViewById(R.id.negative_button);
        negativeButton.setTheme(theme);

        GliaPositiveButton positiveButton = customLayout.findViewById(R.id.positive_button);
        positiveButton.setTheme(theme);

        ImageView logoView = customLayout.findViewById(R.id.logo_view);

        int baseDarkColor = ContextCompat.getColor(context, theme.getBaseDarkColor());

        titleView.setTextColor(baseDarkColor);
        messageView.setTextColor(baseDarkColor);
        logoView.setImageTintList(ContextCompat.getColorStateList(context, theme.getBaseShadeColor()));

        logoView.setVisibility(theme.getWhiteLabel() ? View.GONE : View.VISIBLE);

        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            titleView.setTypeface(fontFamily);
            messageView.setTypeface(fontFamily);
            positiveButton.setTypeface(fontFamily);
            negativeButton.setTypeface(fontFamily);
        }

        titleView.setText(title);
        messageView.setText(message);
        negativeButton.setText(negativeButtonText);
        positiveButton.setText(positiveButtonText);

        negativeButton.setOnClickListener(negativeButtonClickListener);
        positiveButton.setOnClickListener(positiveButtonClickListener);

        builder.setView(customLayout);

        builder.setCancelable(false);

        AlertDialog dialog = builder.show();
        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                context, theme.getBaseLightColor()));
        return dialog;
    }

    public static AlertDialog showAlertDialog(Context context,
                                              UiTheme theme,
                                              @StringRes int title,
                                              @StringRes int message,
                                              View.OnClickListener buttonClickListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCancelable(false);
        View customLayout = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null, false);
        TextView titleView = customLayout.findViewById(R.id.dialog_title_view);
        TextView messageView = customLayout.findViewById(R.id.dialog_message_view);
        ImageButton closeImageButton = customLayout.findViewById(R.id.close_dialog_button);

        int baseDarkColor = ContextCompat.getColor(context, theme.getBaseDarkColor());
        ColorStateList baseNormalColorStateList = ContextCompat.getColorStateList(context, theme.getBaseNormalColor());

        titleView.setTextColor(baseDarkColor);
        messageView.setTextColor(baseDarkColor);
        closeImageButton.setImageTintList(baseNormalColorStateList);

        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            titleView.setTypeface(fontFamily);
            messageView.setTypeface(fontFamily);
        }

        titleView.setText(title);
        messageView.setText(message);
        closeImageButton.setOnClickListener(buttonClickListener);
        builder.setView(customLayout);

        builder.setCancelable(false);

        AlertDialog dialog = builder.show();

        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(context, theme.getBaseLightColor()));
        return dialog;
    }

    public static AlertDialog showOperatorEndedEngagementDialog(Context context, UiTheme theme, View.OnClickListener buttonClickListener) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCancelable(false);
        View customLayout = LayoutInflater.from(context).inflate(R.layout.operator_ended_engagement_dialog, null, false);
        TextView titleView = customLayout.findViewById(R.id.dialog_title_view);
        TextView messageView = customLayout.findViewById(R.id.dialog_message_view);
        GliaPositiveButton okButton = customLayout.findViewById(R.id.ok_button);
        okButton.setTheme(theme);

        int baseDarkColor = ContextCompat.getColor(context, theme.getBaseDarkColor());

        titleView.setTextColor(baseDarkColor);
        messageView.setTextColor(baseDarkColor);

        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            titleView.setTypeface(fontFamily);
            messageView.setTypeface(fontFamily);
        }

        okButton.setOnClickListener(buttonClickListener);
        builder.setView(customLayout);

        builder.setCancelable(false);

        AlertDialog dialog = builder.show();

        dialog.getWindow()
                .getDecorView()
                .getBackground()
                .setTint(ContextCompat.getColor(context, theme.getBaseLightColor()));

        return dialog;
    }

    public static AlertDialog showUpgradeDialog(Context context,
                                                UiTheme theme,
                                                DialogOfferType type,
                                                View.OnClickListener onAcceptOfferClickListener,
                                                View.OnClickListener onCloseClickListener) {
        boolean isUseAlertDialogButtonVerticalAlignment = Utils.getGliaAlertDialogButtonUseVerticalAlignment(theme);

        int layout =
                isUseAlertDialogButtonVerticalAlignment ?
                        R.layout.upgrade_dialog_vertical :
                        R.layout.upgrade_dialog;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCancelable(false);
        View customLayout = LayoutInflater.from(context).inflate(layout, null, false);
        ImageView titleIconView = customLayout.findViewById(R.id.chat_title_icon);
        TextView titleView = customLayout.findViewById(R.id.dialog_title_view);
        GliaNegativeButton negativeButton = customLayout.findViewById(R.id.negative_button);
        negativeButton.setTheme(theme);
        GliaPositiveButton positiveButton = customLayout.findViewById(R.id.positive_button);
        positiveButton.setTheme(theme);
        ImageView logoView = customLayout.findViewById(R.id.logo_view);

        int baseDarkColor = ContextCompat.getColor(context, theme.getBaseDarkColor());
        ColorStateList primaryBrandColorStateList =
                ContextCompat.getColorStateList(context, theme.getBrandPrimaryColor());

        titleIconView.setImageTintList(primaryBrandColorStateList);
        titleView.setTextColor(baseDarkColor);
        logoView.setImageTintList(ContextCompat.getColorStateList(context, theme.getBaseShadeColor()));

        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            titleView.setTypeface(fontFamily);
            positiveButton.setTypeface(fontFamily);
            negativeButton.setTypeface(fontFamily);
        }

        logoView.setVisibility(theme.getWhiteLabel() ? View.GONE : View.VISIBLE);

        if (type instanceof DialogOfferType.AudioUpgradeOffer) {
            titleView.setText(
                    context.getString(R.string.glia_dialog_upgrade_audio_title, type.getOperatorName())
            );
            titleIconView.setImageResource(theme.getIconUpgradeAudioDialog());
        } else if (type instanceof DialogOfferType.VideoUpgradeOffer2Way) {
            titleView.setText(
                    context.getString(R.string.glia_dialog_upgrade_video_2_way_title, type.getOperatorName())
            );
            titleIconView.setImageResource(theme.getIconUpgradeVideoDialog());
        } else if (type instanceof DialogOfferType.VideoUpgradeOffer1Way) {
            titleView.setText(
                    context.getString(R.string.glia_dialog_upgrade_video_1_way_title, type.getOperatorName())
            );
            titleIconView.setImageResource(theme.getIconUpgradeVideoDialog());
        }

        positiveButton.setOnClickListener(onAcceptOfferClickListener);
        negativeButton.setOnClickListener(onCloseClickListener);
        builder.setView(customLayout);

        builder.setCancelable(false);

        AlertDialog dialog = builder.show();

        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                context, theme.getBaseLightColor()));
        return dialog;
    }

    public static AlertDialog showScreenSharingDialog(
            Context context,
            UiTheme theme,
            String title,
            String message,
            @StringRes int positiveButtonText,
            @StringRes int negativeButtonText,
            View.OnClickListener positiveButtonClickListener,
            View.OnClickListener negativeButtonClickListener
    ) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setCancelable(false);

        boolean isUseAlertDialogButtonVerticalAlignment = Utils.getGliaAlertDialogButtonUseVerticalAlignment(theme);

        int layout =
                isUseAlertDialogButtonVerticalAlignment ?
                        R.layout.screensharing_dialog_vertical :
                        R.layout.screensharing_dialog;

        View customLayout = LayoutInflater.from(context).inflate(layout, null, false);
        ImageView titleIconView = customLayout.findViewById(R.id.title_icon);
        TextView titleView = customLayout.findViewById(R.id.dialog_title_view);
        TextView messageView = customLayout.findViewById(R.id.dialog_message_view);
        GliaNegativeButton negativeButton = customLayout.findViewById(R.id.negative_button);
        negativeButton.setTheme(theme);
        GliaPositiveButton positiveButton = customLayout.findViewById(R.id.positive_button);
        positiveButton.setTheme(theme);
        ImageView logoView = customLayout.findViewById(R.id.logo_view);

        int baseDarkColor = ContextCompat.getColor(context, theme.getBaseDarkColor());
        ColorStateList primaryBrandColorStateList =
                ContextCompat.getColorStateList(context, theme.getBrandPrimaryColor());

        titleIconView.setImageTintList(primaryBrandColorStateList);
        titleView.setTextColor(baseDarkColor);
        messageView.setTextColor(baseDarkColor);
        logoView.setImageTintList(ContextCompat.getColorStateList(context, theme.getBaseShadeColor()));

        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            titleView.setTypeface(fontFamily);
            messageView.setTypeface(fontFamily);
            positiveButton.setTypeface(fontFamily);
            negativeButton.setTypeface(fontFamily);
        }

        logoView.setVisibility(theme.getWhiteLabel() ? View.GONE : View.VISIBLE);

        titleView.setText(title);
        messageView.setText(message);
        negativeButton.setText(negativeButtonText);
        positiveButton.setText(positiveButtonText);

        builder.setView(customLayout);

        builder.setCancelable(false);

        AlertDialog dialog = builder.show();
        negativeButton.setOnClickListener(view -> {
            dialog.dismiss();
            negativeButtonClickListener.onClick(view);
        });
        positiveButton.setOnClickListener(view -> {
            dialog.dismiss();
            positiveButtonClickListener.onClick(view);
        });
        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                context, theme.getBaseLightColor()));
        return dialog;
    }
}
