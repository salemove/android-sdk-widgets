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
        private int lastAction;
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

                    lastAction = event.getAction();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        v.performClick();
                        onChatHeadClickedListener.onClick(v);
                    }
                    lastAction = event.getAction();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.
                    float x = initialX + (int) (event.getRawX() - initialTouchX);
                    float y = initialY + (int) (event.getRawY() - initialTouchY);
                    onMoveListener.onMove(x, y);
                    lastAction = event.getAction();
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
