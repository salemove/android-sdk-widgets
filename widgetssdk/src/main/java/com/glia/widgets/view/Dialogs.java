package com.glia.widgets.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.UiTheme;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Dialogs {

    public static AlertDialog showOptionsDialog(Context context,
                                         UiTheme theme,
                                         @StringRes int title,
                                         @StringRes int message,
                                         @StringRes int positiveButtonText,
                                         @StringRes int neutralButtonText,
                                         DialogInterface.OnClickListener positiveButtonClickListener,
                                         DialogInterface.OnClickListener neutralButtonClickListener,
                                         DialogInterface.OnCancelListener cancelListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, positiveButtonClickListener)
                .setNeutralButton(neutralButtonText, neutralButtonClickListener)
                .setOnCancelListener(cancelListener);
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            TextView titleView = new TextView(context);
            titleView.setTextColor(ContextCompat.getColor(context, theme.getBaseDarkColor()));
            TypedValue typedValue = new TypedValue();
            Resources.Theme resourceTheme = context.getTheme();
            resourceTheme.resolveAttribute(R.attr.materialAlertDialogTitleTextStyle, typedValue, true);
            titleView.setTextAppearance(typedValue.data);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int horizontalPadding = (int) context.getResources().getDimension(R.dimen.large_x_large);
            int verticalPadding = (int) context.getResources().getDimension(R.dimen.medium);
            titleView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0);
            titleView.setLayoutParams(lp);
            titleView.setText(title);
            titleView.setTypeface(fontFamily);
            builder.setCustomTitle(titleView);
        } else {
            builder.setTitle(title);
        }

        AlertDialog dialog = builder.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setBackgroundTintList(
                ContextCompat.getColorStateList(context, theme.getBrandPrimaryColor()));
        positiveButton.setTextColor(ContextCompat.getColor(
                context,
                theme.getBaseLightColor()
        ));
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setBackgroundTintList(
                ContextCompat.getColorStateList(context, theme.getSystemNegativeColor()));
        neutralButton.setTextColor(ContextCompat.getColor(
                context,
                theme.getBaseLightColor()
        ));
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
            messageView.setTypeface(fontFamily);
            positiveButton.setTypeface(fontFamily);
            neutralButton.setTypeface(fontFamily);
        }
        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                context, theme.getBaseLightColor()));
        return dialog;
    }

    public static AlertDialog showAlertDialog(Context context,
                                       UiTheme theme,
                                       @StringRes int title,
                                       @StringRes int message,
                                       @StringRes int buttonText,
                                       DialogInterface.OnClickListener buttonClickListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(buttonText, buttonClickListener);
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());

            TextView titleView = new TextView(context);
            titleView.setTextColor(ContextCompat.getColor(context, theme.getBaseDarkColor()));
            TypedValue typedValue = new TypedValue();
            Resources.Theme resourceTheme = context.getTheme();
            resourceTheme.resolveAttribute(R.attr.materialAlertDialogTitleTextStyle, typedValue, true);
            titleView.setTextAppearance(typedValue.data);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int horizontalPadding = (int) context.getResources().getDimension(R.dimen.large_x_large);
            int verticalPadding = (int) context.getResources().getDimension(R.dimen.medium);
            titleView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, 0);
            titleView.setLayoutParams(lp);
            titleView.setText(title);
            titleView.setTypeface(fontFamily);
            builder.setCustomTitle(titleView);
        } else {
            builder.setTitle(title);
        }

        AlertDialog dialog = builder.show();
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setBackgroundTintList(
                ContextCompat.getColorStateList(context, theme.getBrandPrimaryColor()));
        negativeButton.setTextColor(ContextCompat.getColor(
                context,
                theme.getBaseLightColor()
        ));
        TextView messageView = dialog.getWindow().findViewById(android.R.id.message);
        if (theme.getFontRes() != null) {
            Typeface fontFamily = ResourcesCompat.getFont(context, theme.getFontRes());
            messageView.setTypeface(fontFamily);
            negativeButton.setTypeface(fontFamily);
        }
        dialog.getWindow().getDecorView().getBackground().setTint(ContextCompat.getColor(
                context, theme.getBaseLightColor()));
        return dialog;
    }
}
