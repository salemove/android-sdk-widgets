package com.glia.widgets.view;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Pair;

public class ViewHelpers {

    public static class ChatHeadOnTouchListener implements View.OnTouchListener {

        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private final OnRequestInitialCoordinates onRequestInitialCoordinates;
        private final OnMoveListener onMoveListener;
        private final View.OnClickListener onChatHeadClickedListener;

        public ChatHeadOnTouchListener(
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
                    int xDiff = (int) (event.getRawX() - initialTouchX);
                    int yDiff = (int) (event.getRawY() - initialTouchY);

                    //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                    //So that is click event.
                    if (xDiff < 10 && yDiff < 10) {
                        onChatHeadClickedListener.onClick(v);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.
                    float x = initialX + (int) (event.getRawX() - initialTouchX);
                    float y = initialY + (int) (event.getRawY() - initialTouchY);
                    onMoveListener.onMove(x, y);
                    return true;
            }
            return false;
        }

        public interface OnRequestInitialCoordinates {
            /**
             * @return x, y coordinates of the current position of the view when moving starts.
             */
            Pair<Integer, Integer> request();
        }

        public interface OnMoveListener {
            void onMove(float x, float y);
        }
    }
}
