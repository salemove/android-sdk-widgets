package com.glia.widgets.view;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Pair;

/**
 * @hide
 */
public class ViewHelpers {

    /**
     * @hide
     */
    public static class OnTouchListener implements View.OnTouchListener {
        private static final int DIFF_THRESHOLD = 20;
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private final OnRequestInitialCoordinates onRequestInitialCoordinates;
        private final OnMoveListener onMoveListener;
        private final View.OnClickListener onChatHeadClickedListener;

        public OnTouchListener(
            OnRequestInitialCoordinates onRequestInitialCoordinates,
            OnMoveListener onMoveListener,
            View.OnClickListener onChatHeadClickedListener
        ) {
            this.onRequestInitialCoordinates = onRequestInitialCoordinates;
            this.onMoveListener = onMoveListener;
            this.onChatHeadClickedListener = onChatHeadClickedListener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //remember the initial position.
                    Pair<Integer, Integer> coordinates = onRequestInitialCoordinates.request();
                    initialX = coordinates.first;
                    initialY = coordinates.second;

                    //get the touch location
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;
                case MotionEvent.ACTION_UP:
                    int xDiff = (int) Math.abs(event.getRawX() - initialTouchX);
                    int yDiff = (int) Math.abs(event.getRawY() - initialTouchY);
                    if (xDiff < DIFF_THRESHOLD && yDiff < DIFF_THRESHOLD) {
                        onChatHeadClickedListener.onClick(v);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.
                    xDiff = initialX + (int) (event.getRawX() - initialTouchX);
                    yDiff = initialY + (int) (event.getRawY() - initialTouchY);
                    onMoveListener.onMove(xDiff, yDiff);
                    return true;
            }
            return false;
        }

        /**
         * @hide
         */
        public interface OnRequestInitialCoordinates {
            /**
             * @return x, y coordinates of the current position of the view when moving starts.
             */
            Pair<Integer, Integer> request();
        }

        /**
         * @hide
         */
        public interface OnMoveListener {
            void onMove(float x, float y);
        }
    }
}
