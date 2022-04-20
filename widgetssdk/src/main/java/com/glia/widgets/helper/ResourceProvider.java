package com.glia.widgets.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.lang.ref.WeakReference;

public class ResourceProvider implements IResourceProvider {

    private final WeakReference<Context> weakContext;

    public ResourceProvider(Context context) {
        weakContext = new WeakReference<>(context);
    }

    @Override
    public String getString(int id) {
        return (weakContext.get()).getResources().getString(id);
    }

    @Override
    public String getString(int id, @Nullable Object... formatArgs) {
        return (weakContext.get()).getResources().getString(id, formatArgs);
    }

    @Override
    public Integer getColor(int id) {
        return ResourcesCompat.getColor((weakContext.get()).getResources(), id, null);
    }

    @Override
    public ColorStateList getColorStateList(int id) {
        return ContextCompat.getColorStateList(weakContext.get(), id);
    }

    @Nullable
    @Override
    public Drawable getDrawable(int id) {
        return ResourcesCompat.getDrawable((weakContext.get()).getResources(), id, null);
    }

    public int getDimension(int dimensionId){
        Resources resources = weakContext.get().getResources();
        return (int) (resources.getDimension(dimensionId)/ resources.getDisplayMetrics().density);
    }

    public float convertDpToPixel(int dp){
        return dp * ((float) weakContext.get().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}

interface IResourceProvider {

    String getString(@StringRes int id);

    String getString(@StringRes int id, @Nullable Object... formatArgs);

    @ColorInt
    Integer getColor(@ColorRes int id);

    ColorStateList getColorStateList(int id);

    @Nullable
    Drawable getDrawable(@DrawableRes int id);
}
