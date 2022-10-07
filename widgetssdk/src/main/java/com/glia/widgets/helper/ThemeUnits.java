package com.glia.widgets.helper;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.glia.widgets.UiTheme;
import com.glia.widgets.view.configuration.ColorConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.glia.widgets.view.configuration.call.BarButtonConfiguration;
import com.glia.widgets.view.configuration.call.BarButtonStatesConfiguration;
import com.glia.widgets.view.configuration.call.ButtonBarConfiguration;
import com.glia.widgets.view.configuration.call.CallStyle;
import com.glia.widgets.view.configuration.chat.ChatStyle;
import com.glia.widgets.view.unifieduiconfig.component.ColorLayer;
import com.glia.widgets.view.unifieduiconfig.component.Font;
import com.glia.widgets.view.unifieduiconfig.component.Layer;
import com.glia.widgets.view.unifieduiconfig.component.RemoteConfiguration;
import com.glia.widgets.view.unifieduiconfig.component.Text;
import com.glia.widgets.view.unifieduiconfig.component.base.Alignment;
import com.glia.widgets.view.unifieduiconfig.component.base.Color;
import com.glia.widgets.view.unifieduiconfig.component.call.BarButtonStates;
import com.glia.widgets.view.unifieduiconfig.component.call.BarButtonStyle;
import com.glia.widgets.view.unifieduiconfig.component.call.ButtonBar;
import com.glia.widgets.view.unifieduiconfig.component.call.CallScreenStyle;
import com.glia.widgets.view.unifieduiconfig.component.chat.ChatScreenStyle;

import java.util.List;
import java.util.stream.Collectors;

public class ThemeUnits {

    @Nullable
    public static UiTheme apply(@Nullable UiTheme uiTheme, @Nullable RemoteConfiguration remoteConfiguration) {
        if (remoteConfiguration == null) {
            return uiTheme;
        }
        UiTheme.UiThemeBuilder builder = new UiTheme.UiThemeBuilder();
        ChatStyle chatStyle = null;
        CallStyle callStyle = null;
        if (uiTheme != null) {
            builder.setTheme(uiTheme);
            chatStyle = uiTheme.getChatStyle();
            callStyle = uiTheme.getCallStyle();
        }

        applyChatScreen(chatStyle, remoteConfiguration, builder);
        applyCallScreen(callStyle, remoteConfiguration, builder);

        return builder.build();
    }

    private static void applyChatScreen(@Nullable ChatStyle chatStyle,
                                        @NonNull RemoteConfiguration remoteConfiguration,
                                        @NonNull UiTheme.UiThemeBuilder uiThemeBuilder) {
        ChatScreenStyle chatScreenStyle = remoteConfiguration.getChatScreenStyle();
        if (chatScreenStyle == null) {
            return;
        }
        // TODO: will be done in the next task
    }

    private static void applyCallScreen(@Nullable CallStyle callStyle,
                                        @NonNull RemoteConfiguration remoteConfiguration,
                                        @NonNull UiTheme.UiThemeBuilder uiThemeBuilder) {
        CallScreenStyle callScreenStyle = remoteConfiguration.getCallScreenStyle();
        if (callScreenStyle == null) {
            return;
        }

        CallStyle.Builder builder = new CallStyle.Builder();
        LayerConfiguration backgroundConfiguration = null;
        TextConfiguration bottomTextConfiguration = null;
        ButtonBarConfiguration buttonBarConfiguration = null;
        TextConfiguration durationConfiguration = null;
        TextConfiguration operatorConfiguration = null;
        TextConfiguration topTextConfiguration = null;
        if (callStyle != null) {
            builder.setCallStyle(callStyle);
            backgroundConfiguration = callStyle.getBackground();
            bottomTextConfiguration = callStyle.getBottomText();
            buttonBarConfiguration = callStyle.getButtonBar();
            durationConfiguration = callStyle.getDuration();
            operatorConfiguration = callStyle.getOperator();
            topTextConfiguration = callStyle.getTopText();
        }

        if (callScreenStyle.getBackground() != null) {
            builder.setBackground(
                    makeLayerConfiguration(backgroundConfiguration, callScreenStyle.getBackground())
            );
        }
        if (callScreenStyle.getBottomText() != null) {
            builder.setBottomText(
                    makeTextConfiguration(bottomTextConfiguration, callScreenStyle.getBottomText())
            );
        }
        if (callScreenStyle.getButtonBar() != null) {
            builder.setButtonBar(
                    makeButtonBarConfiguration(buttonBarConfiguration, callScreenStyle.getButtonBar())
            );
        }
        if (callScreenStyle.getDuration() != null) {
            builder.setDuration(
                    makeTextConfiguration(durationConfiguration, callScreenStyle.getDuration())
            );
        }
        if (callScreenStyle.getOperator() != null) {
            builder.setOperator(
                    makeTextConfiguration(operatorConfiguration, callScreenStyle.getOperator())
            );
        }
        if (callScreenStyle.getTopText() != null) {
            builder.setTopText(
                    makeTextConfiguration(topTextConfiguration, callScreenStyle.getTopText())
            );
        }

        uiThemeBuilder.setCallStyle(builder.build());
    }

    private static ButtonBarConfiguration makeButtonBarConfiguration(
            @Nullable ButtonBarConfiguration buttonBarConfiguration,
            @NonNull ButtonBar buttonBar
    ) {
        ButtonBarConfiguration.Builder builder = new ButtonBarConfiguration.Builder();

        BarButtonStatesConfiguration chatButtonConfiguration = null;
        BarButtonStatesConfiguration minimizeButtonConfiguration = null;
        BarButtonStatesConfiguration muteButtonConfiguration = null;
        BarButtonStatesConfiguration speakerButtonConfiguration = null;
        BarButtonStatesConfiguration videoButtonConfiguration = null;
        if (buttonBarConfiguration != null) {
            builder.setButtonBarConfiguration(buttonBarConfiguration);
            chatButtonConfiguration = buttonBarConfiguration.getChatButton();
            minimizeButtonConfiguration = buttonBarConfiguration.getMinimizeButton();
            muteButtonConfiguration = buttonBarConfiguration.getMuteButton();
            speakerButtonConfiguration = buttonBarConfiguration.getSpeakerButton();
            videoButtonConfiguration = buttonBarConfiguration.getVideoButton();
        }

        if (buttonBar.getChatButton() != null) {
            builder.setChatButton(
                    makeBarButtonConfiguration(chatButtonConfiguration, buttonBar.getChatButton())
            );
        }
        if (buttonBar.getMinimizeButton() != null) {
            builder.setMinimizeButton(
                    makeBarButtonConfiguration(minimizeButtonConfiguration, buttonBar.getMinimizeButton())
            );
        }
        if (buttonBar.getMuteButton() != null) {
            builder.setMuteButton(
                    makeBarButtonConfiguration(muteButtonConfiguration, buttonBar.getMuteButton())
            );
        }
        if (buttonBar.getSpeakerButton() != null) {
            builder.setSpeakerButton(
                    makeBarButtonConfiguration(speakerButtonConfiguration, buttonBar.getSpeakerButton())
            );
        }
        if (buttonBar.getVideoButton() != null) {
            builder.setVideoButton(
                    makeBarButtonConfiguration(videoButtonConfiguration, buttonBar.getVideoButton())
            );
        }

        return builder.build();
    }

    private static BarButtonStatesConfiguration makeBarButtonConfiguration(
            @Nullable BarButtonStatesConfiguration barButtonStatesConfiguration,
            @NonNull BarButtonStates barButtonStates
    ) {
        BarButtonStatesConfiguration.Builder builder = new BarButtonStatesConfiguration.Builder();

        BarButtonConfiguration inactiveConfiguration = null;
        BarButtonConfiguration activeConfiguration = null;
        BarButtonConfiguration selectedConfiguration = null;
        if (barButtonStatesConfiguration != null) {
            builder.setBarButtonStatesConfiguration(barButtonStatesConfiguration);
            inactiveConfiguration = barButtonStatesConfiguration.getInactive();
            activeConfiguration = barButtonStatesConfiguration.getActive();
            selectedConfiguration = barButtonStatesConfiguration.getSelected();
        }

        if (barButtonStates.getInactive() != null) {
            builder.setInactive(
                    makeBarButtonConfiguration(inactiveConfiguration, barButtonStates.getInactive())
            );
        }
        if (barButtonStates.getActive() != null) {
            builder.setActive(
                    makeBarButtonConfiguration(activeConfiguration, barButtonStates.getActive())
            );
        }
        if (barButtonStates.getSelected() != null) {
            builder.setSelected(
                    makeBarButtonConfiguration(selectedConfiguration, barButtonStates.getSelected())
            );
        }

        return builder.build();
    }

    private static BarButtonConfiguration makeBarButtonConfiguration(
            @Nullable BarButtonConfiguration barButtonConfiguration,
            @NonNull BarButtonStyle barButtonStyle
    ) {
        BarButtonConfiguration.Builder builder = new BarButtonConfiguration.Builder();

        TextConfiguration titleConfiguration = null;
        if (barButtonConfiguration != null) {
            builder.setBarButtonConfiguration(barButtonConfiguration);
            titleConfiguration = barButtonConfiguration.getTitle();
        }

        if (barButtonStyle.getBackground() != null) {
            builder.setBackground(barButtonStyle.getBackground().getPrimaryColor());
        }
        if (barButtonStyle.getImageColor() != null) {
            builder.setImageColor(barButtonStyle.getImageColor().getPrimaryColor());
        }
        if (barButtonStyle.getTitle() != null) {
            builder.setTitle(makeTextConfiguration(titleConfiguration, barButtonStyle.getTitle()));
        }

        return builder.build();
    }

    private static LayerConfiguration makeLayerConfiguration(@Nullable LayerConfiguration layerConfiguration,
                                                             @NonNull Layer layer) {
        LayerConfiguration.Builder builder = new LayerConfiguration.Builder();

        ColorConfiguration backgroundColorConfiguration = null;
        ColorConfiguration borderColorConfiguration = null;
        if (layerConfiguration != null) {
            builder.layerConfiguration(layerConfiguration);
            backgroundColorConfiguration = layerConfiguration.getBackgroundColorConfiguration();
            borderColorConfiguration = layerConfiguration.getBorderColorConfiguration();
        }

        if (layer.getColor() != null) {
            builder.backgroundColor(
                    makeColorConfiguration(backgroundColorConfiguration, layer.getColor())
            );
        }
        if (layer.getBorderColor() != null) {
            builder.borderColor(
                    makeColorConfiguration(borderColorConfiguration, layer.getBorderColor())
            );
        }
        if (layer.getBorderWidth() != null) {
            builder.borderWidth(layer.getBorderWidth().getIntSizeDimension());
        }
        if (layer.getCornerRadius() != null) {
            builder.cornerRadius(layer.getCornerRadius().getIntSizeDimension());
        }

        return builder.build();
    }

    private static ColorConfiguration makeColorConfiguration(@Nullable ColorConfiguration colorConfiguration,
                                                             @NonNull ColorLayer colorLayer) {
        ColorConfiguration.Builder builder = new ColorConfiguration.Builder();

        if (colorConfiguration != null) {
            builder.seColorConfiguration(colorConfiguration);
        }

        if (ColorLayer.TYPE_FILL.equals(colorLayer.getType())) {
            builder.setColor(colorLayer.getPrimaryColor());
        } else if (ColorLayer.TYPE_GRADIENT.equals(colorLayer.getType())) {
            List<Integer> colorList = colorLayer.getValues().stream()
                    .map(Color::getColor)
                    .collect(Collectors.toList());
            builder.setGradientColors(colorList);
        }
        return builder.build();
    }

    private static TextConfiguration makeTextConfiguration(@Nullable TextConfiguration textConfiguration,
                                                           @NonNull Text text) {
        TextConfiguration.Builder builder = new TextConfiguration.Builder();

        ColorConfiguration backgroundColorConfiguration = null;
        if (textConfiguration != null) {
            builder.textConfiguration(textConfiguration);
            backgroundColorConfiguration = textConfiguration.getBackgroundColor();
        }

        if (text.getTextColor() != null) {
            builder.textColor(prepareColorStateList(text.getTextColor()));
        }
        if (text.getBackgroundColor() != null) {
            builder.backgroundColor(
                    makeColorConfiguration(backgroundColorConfiguration, text.getBackgroundColor())
            );
        }
        Font font = text.getFont();
        if (font != null) {
            if (font.getSize() != null) {
                builder.textSize(font.getSize().getSizeDimension());
            }
            if (font.getStyle() != null) {
                builder.textTypeFaceStyle(font.getStyle().getStyle());
            }
        }
        if (text.getAlignment() != null) {
            builder.textAlignment(prepareIntAlignment(text.getAlignment()));
        }
        return builder.build();
    }

    private static ColorStateList prepareColorStateList(@NonNull ColorLayer colorLayer) {
        return ColorStateList.valueOf(colorLayer.getPrimaryColor());
    }

    private static Integer prepareIntAlignment(Alignment alignment) {
        if (Alignment.TYPE_LEADING.equals(alignment.getType())) {
            return View.TEXT_ALIGNMENT_TEXT_START;
        } else if (Alignment.TYPE_CENTER.equals(alignment.getType())) {
            return View.TEXT_ALIGNMENT_GRAVITY;
        } else if (Alignment.TYPE_TRAILING.equals(alignment.getType())) {
            return View.TEXT_ALIGNMENT_TEXT_END;
        }
        return null;
    }
}
