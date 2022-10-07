package com.glia.widgets.helper;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.glia.widgets.R;
import com.glia.widgets.di.Dependencies;
import com.glia.widgets.view.configuration.ColorConfiguration;
import com.glia.widgets.view.configuration.LayerConfiguration;
import com.glia.widgets.view.configuration.TextConfiguration;
import com.glia.widgets.view.configuration.call.BarButtonStatesConfiguration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint({"ResourceAsColor", "WrongConstant"})
public class StyleUtils {

    public static void setBackgroundColor(@Nullable View view,
                                          @Nullable ColorConfiguration colorConfiguration) {
        if (view == null || colorConfiguration == null) {
            return;
        }

        GradientDrawable drawable = new GradientDrawable();

        setBackgroundColor(drawable, colorConfiguration);

        view.setBackground(drawable);
    }

    public static void setLayerConfiguration(@Nullable View view,
                                             @Nullable LayerConfiguration layerConfiguration) {
        if (view == null || layerConfiguration == null) {
            return;
        }

        GradientDrawable drawable = new GradientDrawable();

        setBackgroundColor(drawable, layerConfiguration.getBackgroundColorConfiguration());
        setStroke(drawable, layerConfiguration);

        if (layerConfiguration.getCornerRadius() != null) {
            ResourceProvider resourceProvider = Dependencies.getResourceProvider();
            float cornerRadius = resourceProvider.convertDpToPixel(layerConfiguration.getCornerRadius());
            drawable.setCornerRadius(cornerRadius);
        }

        view.setBackground(drawable);
    }

    public static void setTextConfiguration(@Nullable TextView textView,
                                            @Nullable TextConfiguration textConfiguration) {
        if (textView == null || textConfiguration == null) {
            return;
        }

        setInternalTextConfiguration(textView, textConfiguration);
        setRemoteTextConfiguration(textView, textConfiguration);
    }

    public static void setBarButtonStatesConfiguration(@Nullable FloatingActionButton actionButton,
                                                       @Nullable BarButtonStatesConfiguration configuration) {
        if (actionButton == null || configuration == null) {
            return;
        }

        Integer inactiveBackgroundColor = null;
        Integer inactiveIconColor = null;
        if (configuration.getInactive() != null) {
            inactiveBackgroundColor = configuration.getInactive().getBackground();
            inactiveIconColor = configuration.getInactive().getImageColor();
        }
        Integer activeBackgroundColor = null;
        Integer activeIconColor = null;
        Integer activeIconRes = null;
        if (configuration.getActive() != null) {
            activeBackgroundColor = configuration.getActive().getBackground();
            activeIconColor = configuration.getActive().getImageColor();
            activeIconRes = configuration.getActive().getImageRes();
        }
        Integer selectedBackgroundColor = null;
        Integer selectedIconColor = null;
        Integer selectedIconRes = null;
        if (configuration.getSelected() != null) {
            selectedBackgroundColor = configuration.getSelected().getBackground();
            selectedIconColor = configuration.getSelected().getImageColor();
            selectedIconRes = configuration.getSelected().getImageRes();
        }

        ColorStateList backgroundTintList = makeColorStateList(inactiveBackgroundColor, activeBackgroundColor, selectedBackgroundColor);
        actionButton.setBackgroundTintList(backgroundTintList);

        ColorStateList imageTintList = makeColorStateList(inactiveIconColor, activeIconColor, selectedIconColor);
        actionButton.setImageTintList(imageTintList);

        StateListDrawable drawable = makeStateListDrawable(activeIconRes, selectedIconRes);
        actionButton.setImageDrawable(drawable);
    }

    public static void setBarButtonConfiguration(@Nullable TextView textView,
                                                 @Nullable BarButtonStatesConfiguration configuration,
                                                 @NonNull FabButtonStatus status) {
        if (textView == null || configuration == null) {
            return;
        }

        if (status == FabButtonStatus.INACTIVE) {
            if (configuration.getInactive() != null && configuration.getInactive().getTitle() != null) {
                setTextConfiguration(textView, configuration.getInactive().getTitle());
            }
        } else if (status == FabButtonStatus.ACTIVE) {
            if (configuration.getActive() != null && configuration.getActive().getTitle() != null) {
                setTextConfiguration(textView, configuration.getActive().getTitle());
            }
        } else if (status == FabButtonStatus.SELECTED) {
            if (configuration.getSelected() != null && configuration.getSelected().getTitle() != null) {
                setTextConfiguration(textView, configuration.getSelected().getTitle());
            }
        }
    }

    private static void setInternalTextConfiguration(@NonNull TextView textView,
                                                     @NonNull TextConfiguration textConfiguration) {
        if (textConfiguration.getHintColor() != null) {
            textView.setHintTextColor(textConfiguration.getHintColor());
        }
        if (textConfiguration.getTextColorLink() != null) {
            textView.setLinkTextColor(textConfiguration.getTextColorLink());
        }
        if (textConfiguration.getTextColorHighlight() != null) {
            textView.setHighlightColor(textConfiguration.getTextColorHighlight());
        }
        if (textConfiguration.getFontFamily() != null) {
            textView.setTypeface(
                    ResourcesCompat.getFont(textView.getContext(), textConfiguration.getFontFamily())
            );
        }
        Boolean isBold = textConfiguration.isBold();
        if (isBold != null) {
            Typeface typeface = Typeface.create(
                    textView.getTypeface(),
                    isBold ? Typeface.BOLD : Typeface.NORMAL
            );
            textView.setTypeface(typeface);
        }
        Boolean isAllCaps = textConfiguration.isAllCaps();
        if (isAllCaps != null) {
            textView.setAllCaps(isAllCaps);
        }
    }

    private static void setRemoteTextConfiguration(@NonNull TextView textView,
                                                   @NonNull TextConfiguration textConfiguration) {
        if (textConfiguration.getTextColor() != null) {
            textView.setTextColor(textConfiguration.getTextColor());
        }
        if (textConfiguration.getTextSize() != null) {
            textView.setTextSize(textConfiguration.getTextSize());
        }
        if (textConfiguration.getTextTypeFaceStyle() != null) {
            Typeface typeface = Typeface.create(
                    textView.getTypeface(),
                    textConfiguration.getTextTypeFaceStyle()
            );
            textView.setTypeface(typeface);
        }
        if (textConfiguration.getTextAlignment() != null) {
            textView.setTextAlignment(textConfiguration.getTextAlignment());
        }
        if (textConfiguration.getBackgroundColor() != null) {
            setBackgroundColor(textView, textConfiguration.getBackgroundColor());
        }
    }

    public static void setBackgroundColor(@Nullable GradientDrawable drawable,
                                          @Nullable ColorConfiguration colorConfiguration) {
        if (drawable == null || colorConfiguration == null) {
            return;
        }

        List<Integer> colorList = colorConfiguration.getColorList();
        if (colorList.size() > 1) {
            int[] colors = colorList.stream().mapToInt(i -> i).toArray();
            drawable.setColors(colors);
        } else if (!colorList.isEmpty()) {
            drawable.setColor(colorList.get(0));
        }
    }

    public static void setStroke(@Nullable GradientDrawable drawable,
                                 @Nullable LayerConfiguration layerConfiguration) {
        if (drawable == null || layerConfiguration == null) {
            return;
        }

        ColorConfiguration colorConfiguration = layerConfiguration.getBorderColorConfiguration();
        if (colorConfiguration == null) {
            return;
        }

        Integer color = null;

        List<Integer> colorList = colorConfiguration.getColorList();
        if (!colorList.isEmpty()) {
            color = colorList.get(0);
        }

        if (color == null) {
            return;
        }

        int width;
        if (layerConfiguration.getBorderWidth() != null) {
            width = layerConfiguration.getBorderWidth();
        } else {
            width = Dependencies.getResourceProvider().getDimension(R.dimen.glia_px);
        }

        drawable.setStroke(width, color);
    }

    private static ColorStateList makeColorStateList(@Nullable Integer inactive,
                                                     @Nullable Integer active,
                                                     @Nullable Integer selected) {
        List<Integer[]> states = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (inactive != null) {
            states.add(new Integer[] {-android.R.attr.state_enabled});
            colors.add(inactive);
        }

        if (selected != null) {
            states.add(new Integer[] {android.R.attr.state_activated});
            colors.add(selected);
        }

        if (active != null) {
            states.add(new Integer[] {});
            colors.add(active);
        }

        int[][] statesArray = states.stream()
                .map(item -> Arrays.stream(item).mapToInt(state -> state).toArray())
                .toArray(int[][]::new);
        int[] colorsArray = colors.stream().mapToInt(i -> i).toArray();

        return new ColorStateList(
                statesArray,
                colorsArray
        );
    }

    private static StateListDrawable makeStateListDrawable(@Nullable @DrawableRes Integer active,
                                                           @Nullable @DrawableRes Integer selected) {
        ResourceProvider resourceProvider = Dependencies.getResourceProvider();
        StateListDrawable drawable = new StateListDrawable();
        if (selected != null) {
            drawable.addState(
                    new int[]{android.R.attr.state_activated},
                    resourceProvider.getDrawable(selected)
            );
        }
        if (active != null) {
            drawable.addState(
                    new int[]{},
                    resourceProvider.getDrawable(active)
            );
        }
        return drawable;
    }

    public enum FabButtonStatus {
        INACTIVE, ACTIVE, SELECTED
    }
}
