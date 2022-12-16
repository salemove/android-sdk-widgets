package com.glia.widgets.helper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.lang.ref.WeakReference;

interface IResourceProvider {

    String getString(@StringRes int id);

    String getString(@StringRes int id, @Nullable Object... formatArgs);

    @ColorInt
    Integer getColor(@ColorRes int id);

    ColorStateList getColorStateList(int id);

    int getDimension(int dimensionId);

    float convertDpToPixel(float dp);

    float convertSpToPixel(float sp);

    int convertDpToIntPixel(float dp);
}

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

    @Override
    public int getDimension(int dimensionId) {
        Resources resources = weakContext.get().getResources();
        return (int) (resources.getDimension(dimensionId) / resources.getDisplayMetrics().density);
    }

    @Override
    public float convertDpToPixel(float dp) {
        return applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, weakContext.get());
    }

    @Override
    public int convertDpToIntPixel(float dp) {
        return Math.round(convertDpToPixel(dp));
    }

    @Override
    public float convertSpToPixel(float sp) {
        return applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, weakContext.get());
    }

    private float applyDimension(int unit, float value, Context context) {
        return TypedValue.applyDimension(unit, value, context.getResources().getDisplayMetrics());
    }

}
