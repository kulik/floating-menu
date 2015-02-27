package com.tac.kulik.floatingarcmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * Created by dima on 2/27/15.
 */
public class SizableFloatingActionButton extends FloatingActionButton {

    private float mDpSize;

    public SizableFloatingActionButton(Context context) {
        super(context);
    }

    public SizableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SizableFloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    void init(Context context, AttributeSet attributeSet) {
        super.init(context, attributeSet);
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
        mDpSize = attr.getDimensionPixelSize(R.styleable.FloatingActionsMenu_fab_dp_size, 0);
        if(mDpSize != 0){
            mCircleSize = (int) mDpSize;
            updateDrawableSize();
            updateBackground();
        }
    }
}
