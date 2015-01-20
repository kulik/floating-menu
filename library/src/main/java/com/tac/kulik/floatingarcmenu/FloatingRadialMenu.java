package com.tac.kulik.floatingarcmenu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;


public class FloatingRadialMenu extends ViewGroup {

    private static final int ANIMATION_DURATION = 300;
    private static final float COLLAPSED_PLUS_ROTATION = 0f;
    private static final float EXPANDED_PLUS_ROTATION = 90f + 45f;

    private int mAddButtonPlusColor;
    private int mAddButtonColorNormal;
    private int mAddButtonColorPressed;
    private int mAddButtonSize;
    private boolean mAddButtonStrokeVisible;

    private int mButtonSpacing;
    private int mLabelsMargin;
    private int mLabelsVerticalOffset;

    private float mFromAngle = 270.0f;
    private float mToAngle = 360.0f;
    private float mPadding = 0f;

    private boolean mExpanded;

    private AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION);
    private AddFloatingActionButton mAddButton;
    private RotatingDrawable mRotatingDrawable;
    private int mMaxButtonWidth;
    private int mMaxButtonHeight;
    private int mLabelsStyle;
    private int mButtonsCount;

    private OnFloatingActionsMenuUpdateListener mListener;
    private boolean mAlignLeft;
    private boolean mAlignDown;
    private double mRadius;

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();

        void onMenuCollapsed();
    }

    public FloatingRadialMenu(Context context) {
        this(context, null);
    }

    public FloatingRadialMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingRadialMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));
        mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
        mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);
//
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
        mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
        mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
        mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
        mAddButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, FloatingActionButton.SIZE_NORMAL);
        mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
        attr.recycle();

//        if (mLabelsStyle != 0 && expandsHorizontally()) {
//            throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
//        }

        createAddButton(context);
    }

    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        mListener = listener;
    }

    private static class RotatingDrawable extends LayerDrawable {
        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[]{drawable});
        }

        private float mRotation;

        @SuppressWarnings("UnusedDeclaration")
        public float getRotation() {
            return mRotation;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    private void createAddButton(Context context) {
        mAddButton = new AddFloatingActionButton(context){
            @Override
            public void updateBackground() {
                mPlusColor = mAddButtonPlusColor;
                mColorNormal = mAddButtonColorNormal;
                mColorPressed = mAddButtonColorPressed;
                mStrokeVisible = mAddButtonStrokeVisible;
                super.updateBackground();
            }

            @Override
            public Drawable getIconDrawable() {
                final RotatingDrawable rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
                mRotatingDrawable = rotatingDrawable;

                final OvershootInterpolator interpolator = new OvershootInterpolator();

                final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", EXPANDED_PLUS_ROTATION, COLLAPSED_PLUS_ROTATION);
                final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", COLLAPSED_PLUS_ROTATION, EXPANDED_PLUS_ROTATION);

                collapseAnimator.setInterpolator(interpolator);
                expandAnimator.setInterpolator(interpolator);

                mExpandAnimation.play(expandAnimator);
                mCollapseAnimation.play(collapseAnimator);

                return rotatingDrawable;
            }
        };

        mAddButton.setId(R.id.fab_expand_menu_button);
        mAddButton.setSize(mAddButtonSize);
        mAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mAddButton, super.generateDefaultLayoutParams());
    }

    public void addButton(FloatingActionButton button) {
        addView(button, mButtonsCount - 1);
        mButtonsCount++;

        if (mLabelsStyle != 0) {
            createLabels();
        }
    }

    public void removeButton(FloatingActionButton button) {
        removeView(button);
        mButtonsCount--;
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        View generalButton = getNonAddButton();
        //calc Radius

        float laproximated = (generalButton.getMeasuredWidth() + mPadding) * (getChildCount() - 1);
        float sector = Math.abs(mFromAngle - mToAngle);
        // perim = Pi * d
        //lsector = Pi * d / (360/sector)
        //lsector = Pi * 2 *r * sector / 360
        // r = lsector *360 / (Pi * 2*sector)
        mRadius = (laproximated * 360) / (2 * Math.PI * sector);

        int width = (int) (mRadius + mAddButton.getMeasuredWidth() / 2 + generalButton.getMeasuredWidth() / 2);

        width = adjustForOvershoot(width);
        //TODO add (mToAngle < 180f && mFromAngle < 180f)
        setMeasuredDimension(width, width);
    }

    private View getNonAddButton() {
        for (int i = 0; i < mButtonsCount; i++) {
            final View child = getChildAt(i);
            if (child == mAddButton || child.getVisibility() == GONE) continue;
            return child;
        }
        return null;
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int addButtonY = (mToAngle > 180f && mFromAngle > 180f) ? b - t - mAddButton.getMeasuredHeight() : 0;
        float sectorFull = mToAngle - mFromAngle;
        float sectorPiceRad = (float) ((sectorFull * Math.PI) / ((mButtonsCount - 2) * 180));

        // Ensure mAddButton is centered on the line where the buttons should be
        int addButtonLeft = 0;//TODO calc depend on1 circle sector
        int centerX = addButtonLeft + mAddButton.getMeasuredWidth() / 2;
        int centerY = addButtonY + mAddButton.getMeasuredHeight() / 2;
        mAddButton.layout(centerX - mAddButton.getMeasuredWidth() / 2, centerY - mAddButton.getMeasuredHeight() / 2, centerX + mAddButton.getMeasuredWidth() / 2, centerY + mAddButton.getMeasuredHeight() / 2);
        for (int i = 0; i < mButtonsCount; i++) {
            final View child = getChildAt(i);

            if (child == mAddButton || child.getVisibility() == GONE) continue;

            int childX = (int) (centerX + mRadius * Math.cos(sectorPiceRad * i));
            int childY = (int) (centerY - mRadius * Math.sin(sectorPiceRad * i));
            child.layout(childX - child.getMeasuredWidth() / 2, childY - child.getMeasuredHeight() / 2, childX + child.getMeasuredWidth() / 2, childY + child.getMeasuredHeight() / 2);

                    float collapsedTranslationX = centerX - childX;
                    float collapsedTranslationY = centerY - childY;
                    float expandedTranslationX = 0;
                    float expandedTranslationY = 0;
//
                    child.setTranslationY(mExpanded ? expandedTranslationY : collapsedTranslationY);
                    child.setTranslationX(mExpanded ? expandedTranslationX : collapsedTranslationX);
                    child.setAlpha(mExpanded ? 1f : 0f);
//
                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.mCollapseDirX.setFloatValues(expandedTranslationX, collapsedTranslationX);
                    params.mCollapseDirY.setFloatValues(expandedTranslationY, collapsedTranslationY);
                    params.mExpandDirX.setFloatValues(collapsedTranslationX, expandedTranslationX);
                    params.mExpandDirY.setFloatValues(collapsedTranslationY, expandedTranslationY);
                    params.setAnimationsTarget(child);

//                    nextY = expandUp ?
//                            childY - mButtonSpacing :
//                            childY + child.getMeasuredHeight() + mButtonSpacing;
        }
    }


    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {
        private ObjectAnimator mExpandDirX = new ObjectAnimator();
        private ObjectAnimator mExpandDirY = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDirX = new ObjectAnimator();
        private ObjectAnimator mCollapseDirY = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();

        private boolean animationsSetToPlay;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            mExpandDirX.setInterpolator(sExpandInterpolator);
            mExpandDirY.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseDirX.setInterpolator(sCollapseInterpolator);
            mCollapseDirY.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);
            mCollapseDirX.setProperty(View.TRANSLATION_X);
            mCollapseDirY.setProperty(View.TRANSLATION_Y);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);
            mExpandDirX.setProperty(View.TRANSLATION_X);
            mExpandDirY.setProperty(View.TRANSLATION_Y);
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDirX.setTarget(view);
            mCollapseDirY.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDirX.setTarget(view);
            mExpandDirY.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                mCollapseAnimation.play(mCollapseAlpha);
                mCollapseAnimation.play(mCollapseDirX);
                mCollapseAnimation.play(mCollapseDirY);
                mExpandAnimation.play(mExpandAlpha);
                mExpandAnimation.play(mExpandDirX);
                mExpandAnimation.play(mExpandDirY);
                animationsSetToPlay = true;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bringChildToFront(mAddButton);
        mButtonsCount = getChildCount();

        if (mLabelsStyle != 0) {
            createLabels();
        }
    }

    private void createLabels() {
        Context context = new ContextThemeWrapper(getContext(), mLabelsStyle);

        for (int i = 0; i < mButtonsCount; i++) {
            FloatingActionButton button = (FloatingActionButton) getChildAt(i);
            String title = button.getTitle();

            if (button == mAddButton || title == null ||
                    button.getTag(R.id.fab_label) != null) continue;

            TextView label = new TextView(context);
            label.setText(button.getTitle());
            addView(label);

            button.setTag(R.id.fab_label, label);
        }
    }

    public void collapse() {
        if (mExpanded) {
            mExpanded = false;
            mCollapseAnimation.start();
            mExpandAnimation.cancel();

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mCollapseAnimation.cancel();
            mExpandAnimation.start();

            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;

//            if (mRotatingDrawable != null) {
//                mRotatingDrawable.setRotation(mExpanded ? EXPANDED_PLUS_ROTATION : COLLAPSED_PLUS_ROTATION);
//            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}