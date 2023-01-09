package com.glia.widgets.core.callvisualizer.domain;

import android.content.Context;
import android.view.LayoutInflater;

import com.glia.widgets.view.VisitorCodeView;

public class VisitorCodeViewRepository {

    public VisitorCodeView createVisitorCodeView(Context context) {
        return new VisitorCodeView(context);
    }
}
