package ai.beans.common.widgets.imagedisplay;

import android.graphics.Point;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class BeansGestureDetector implements View.OnTouchListener{

    int SLOP_VALUE = 10;
    BeansGestureDetectorListener listener;
    View view;
    long gestureStartTimestamp;
    long gestureStopTimestamp;

    enum STATE { NONE, SINGLE_TAP_START, MULTI_TAP_START, SINGLE_FINGER_DRAG, MULTI_FINGER_DRAG};
    STATE gesture_state = STATE.NONE;
    int majorPointerId;
    int minorPointerId;
    Point majorStartPoint;
    Point majorPointPrev = new Point();
    Point majorPointCurrent = new Point();

    Point majorPointPrevRaw = new Point();
    Point majorPointCurrentRaw = new Point();

    Point minorPointPrev = new Point();
    Point minorPointCurrent = new Point();
    boolean firstFingerDown = false;
    boolean secondFingerDown = false;
    private GestureDetector longPressDetector;
    boolean inLongPress = false;

    boolean useRawData = false;


    public BeansGestureDetector(View view) {
        this.view = view;
        setup();
    }

    public BeansGestureDetector(View view, BeansGestureDetectorListener listener) {
        this.listener = listener;
        this.view = view;
        setup();
    }

    private void setup() {
        if(this.view != null) {
            this.view.setOnTouchListener(this);
            this.longPressDetector = new GestureDetector(view.getContext(), new MyLongPressListener());
            this.longPressDetector.setIsLongpressEnabled(true);
            ViewConfiguration config = ViewConfiguration.get(view.getContext());
            SLOP_VALUE = config.getScaledTouchSlop();
        }
    }

    public void disconnectTouchHandling() {
        if(this.view != null) {
            this.view.setOnTouchListener(null);
        }
    }

    public void connectTouchHandling(View view) {
        this.view = view;
        setup();
    }

    public void useRawDragData(Boolean flag) {
        useRawData = flag;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (longPressDetector.onTouchEvent(event)) {
            return true;
        } else {
            int action = event.getActionMasked();
            int majorPointerIndex;
            int minorPointerIndex;
            int count = event.getPointerCount();
            Log.d("BeansGesture", String.format("pointerCOunt = %s", Integer.toString(count)));
            if (v != view) {
                Log.d("BeansGesture", "gesture on another view!");
            }
            if(v == null || v.getParent() == null) {
                //Note: There are certain conditions where queued up touch events are delivered here
                // but the view has gone. In such a case, just move on...
                Log.d("BeansGesture", "View = NULL!");
                return true;
            }
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    try {
                        gestureStartTimestamp = System.currentTimeMillis();
                        gesture_state = STATE.SINGLE_TAP_START;
                        majorPointerId = event.getPointerId(0);
                        Log.d("BeansGesture", String.format("Major Index: %s, ID: %s", 0, majorPointerId));
                        majorPointerIndex = event.getActionIndex();
                        majorPointPrev.x = (int) event.getX(majorPointerIndex);
                        majorPointPrev.y = (int) event.getY(majorPointerIndex);

                        majorPointPrevRaw.x = (int) event.getRawX();
                        majorPointPrevRaw.y = (int) event.getRawY();

                        if (majorStartPoint == null) {
                            majorStartPoint = new Point(majorPointPrev.x, majorPointPrev.y);
                        }
                        firstFingerDown = true;
                        //Log.d("BeansGesture", String.format("Primary Point Down : %s, %s", majorPointPrev.x, majorPointPrev.y));
                        return true;
                    } catch (IllegalArgumentException e) {
                        gesture_state = STATE.NONE;
                        return true;
                    }
                case (MotionEvent.ACTION_POINTER_DOWN):
                    try {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        if (!firstFingerDown) {
                            gestureStartTimestamp = System.currentTimeMillis();
                            gesture_state = STATE.SINGLE_TAP_START;
                            majorPointerIndex = event.getActionIndex();
                            majorPointerId = event.getPointerId(majorPointerIndex);
                            majorPointPrev.x = (int) event.getX(majorPointerIndex);
                            majorPointPrev.y = (int) event.getY(majorPointerIndex);
                            firstFingerDown = true;
                            Log.d("BeansGesture", String.format("Minor Index %s, ID %s", majorPointerIndex, minorPointerId));
                            return true;
                        } else if (!secondFingerDown) {
                            gestureStartTimestamp = System.currentTimeMillis();
                            gesture_state = STATE.MULTI_TAP_START;
                            minorPointerIndex = event.getActionIndex();
                            minorPointerId = event.getPointerId(minorPointerIndex);
                            minorPointPrev.x = (int) event.getX(minorPointerIndex);
                            minorPointPrev.y = (int) event.getY(minorPointerIndex);
                            secondFingerDown = true;
                            Log.d("BeansGesture", String.format("Minor Index %s, ID %s", minorPointerIndex, minorPointerId));
                            return true;
                        }
                    } catch (IllegalArgumentException e) {
                        gesture_state = STATE.NONE;
                        return true;
                    }
                    break;
                case (MotionEvent.ACTION_POINTER_UP):
                    Log.d("BeansGesture", "Action Pointer Up");
                    gestureStartTimestamp = System.currentTimeMillis();
                    //gesture_state = STATE.SINGLE_TAP_START;
                    int index = event.getActionIndex();
                    int upPointerId = event.getPointerId(index);
                    event.getPointerCount();

                    if (upPointerId == minorPointerId && secondFingerDown) {
                        Log.d("BeansGesture", "Minor finger up");
                        /*secondFingerDown = false;
                        gesture_state = STATE.SINGLE_TAP_START;*/
                        gesture_state = STATE.NONE;
                        firstFingerDown = false;
                        secondFingerDown = false;

                    } else if (upPointerId == majorPointerId && firstFingerDown) {
                        if (secondFingerDown) {
                            //swap major and minor
                            Log.d("BeansGesture", "Major finger up");
                            /*secondFingerDown = false;
                            gesture_state = STATE.SINGLE_TAP_START;
                            majorPointerId = minorPointerId;
                            majorPointPrev.x = minorPointPrev.x;
                            majorPointPrev.y = minorPointPrev.y;
                            majorPointCurrent.x = minorPointCurrent.x;
                            majorPointCurrent.y = minorPointCurrent.y;*/
                            gesture_state = STATE.NONE;
                            firstFingerDown = false;
                            secondFingerDown = false;

                        } else {
                            //our current major finger is going away...lets find another
                            Log.d("BeansGesture", "Major/Minor fingers up");
                            gesture_state = STATE.NONE;
                            firstFingerDown = false;
                            secondFingerDown = false;
                        }
                    }
                    if (!secondFingerDown) {
                        if (listener != null) {
                            listener.onPinchZoomEnd();
                        }
                    }
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    boolean retVal = true;
                    if (gesture_state == STATE.SINGLE_TAP_START) {
                        majorPointerIndex = event.findPointerIndex(majorPointerId);
                        //drag
                        majorPointCurrent.x = (int) event.getX(majorPointerIndex);
                        majorPointCurrent.y = (int) event.getY(majorPointerIndex);

                        majorPointCurrentRaw.x = (int) event.getRawX();
                        majorPointCurrentRaw.y = (int) event.getRawY();

                        double xDiff = majorPointCurrent.x - majorPointPrev.x;
                        double yDiff = majorPointCurrent.y - majorPointPrev.y;
                        double hypo = Math.hypot(xDiff, yDiff);

                        if (hypo > SLOP_VALUE) {
                            gesture_state = STATE.SINGLE_FINGER_DRAG;
                            if (listener != null) {
                                if(useRawData) {
                                    listener.onDragStart(majorPointPrevRaw);
                                    listener.onDrag(majorPointPrevRaw, majorPointCurrentRaw);
                                } else {
                                    listener.onDragStart(majorPointPrev);
                                    listener.onDrag(majorPointPrev, majorPointCurrent);
                                }
                            }
                            majorPointPrev.x = majorPointCurrent.x;
                            majorPointPrev.y = majorPointCurrent.y;
                        } else {
                            //with slop value...

                        }

                    } else if (gesture_state == STATE.MULTI_TAP_START) {
                        retVal = true;
                        //pinch-zoom
                        if (gesture_state == STATE.SINGLE_FINGER_DRAG) {
                            if (listener != null) {
                                listener.onDragEnd();
                            }
                        }
                        majorPointerIndex = event.findPointerIndex(majorPointerId);
                        minorPointerIndex = event.findPointerIndex(minorPointerId);

                        gesture_state = STATE.MULTI_FINGER_DRAG;
                        majorPointCurrent.x = (int) event.getX(majorPointerIndex);
                        majorPointCurrent.y = (int) event.getY(majorPointerIndex);
                        minorPointCurrent.x = (int) event.getX(minorPointerIndex);
                        minorPointCurrent.y = (int) event.getY(minorPointerIndex);

                        if (listener != null) {
                            listener.onPinchZoomStart(majorPointPrev, minorPointPrev);
                            listener.onPinchZoom(majorPointPrev, majorPointCurrent, minorPointPrev, minorPointCurrent, 0);
                        }


                        majorPointPrev.x = majorPointCurrent.x;
                        majorPointPrev.y = majorPointCurrent.y;
                        minorPointPrev.x = minorPointCurrent.x;
                        minorPointPrev.y = minorPointCurrent.y;

                    } else if (gesture_state == STATE.MULTI_FINGER_DRAG) {
                        retVal = true;
                        //pinch zoom
                        majorPointerIndex = event.findPointerIndex(majorPointerId);
                        minorPointerIndex = event.findPointerIndex(minorPointerId);

                        majorPointCurrent.x = (int) event.getX(majorPointerIndex);
                        majorPointCurrent.y = (int) event.getY(majorPointerIndex);
                        minorPointCurrent.x = (int) event.getX(minorPointerIndex);
                        minorPointCurrent.y = (int) event.getY(minorPointerIndex);

                        double prevXdiff = minorPointPrev.x - majorPointPrev.x;
                        double prevYdiff = minorPointPrev.y - majorPointPrev.y;
                        double prevHypo = Math.hypot(prevXdiff, prevYdiff);

                        double currXdiff = minorPointCurrent.x - majorPointCurrent.x;
                        double currYdiff = minorPointCurrent.y - majorPointCurrent.y;
                        double curHypo = Math.hypot(currXdiff, currYdiff);

                        float pinchDistance = (float) (curHypo - prevHypo);

                        if (listener != null) {
                            listener.onPinchZoom(majorPointPrev, majorPointCurrent, minorPointPrev, minorPointCurrent, pinchDistance);
                        }

                        majorPointPrev.x = majorPointCurrent.x;
                        majorPointPrev.y = majorPointCurrent.y;
                        minorPointPrev.x = minorPointCurrent.x;
                        minorPointPrev.y = minorPointCurrent.y;
                    } else if (gesture_state == STATE.SINGLE_FINGER_DRAG) {
                        majorPointerIndex = event.findPointerIndex(majorPointerId);
                        majorPointCurrent.x = (int) event.getX(majorPointerIndex);
                        majorPointCurrent.y = (int) event.getY(majorPointerIndex);
                        majorPointCurrentRaw.x = (int) event.getRawX();
                        majorPointCurrentRaw.y = (int) event.getRawY();

                        if (listener != null) {
                            if(useRawData) {
                                listener.onDrag(majorPointPrevRaw, majorPointCurrentRaw);
                            } else {
                                listener.onDrag(majorPointPrev, majorPointCurrent);
                            }
                        }
                        majorPointPrev.x = majorPointCurrent.x;
                        majorPointPrev.y = majorPointCurrent.y;
                    }
                    return retVal;
                case (MotionEvent.ACTION_UP):
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    if(!inLongPress) {
                        if (gesture_state == STATE.SINGLE_TAP_START) {
                            //Tap
                            Log.d("BeansGesture", "Single Tap");
                            if (listener != null) {
                                listener.onSingleTap(majorPointPrev);
                            }
                        } else if (gesture_state == STATE.MULTI_TAP_START) {
                            //pinch-zoom
                            Log.d("BeansGesture", "Multi Tap");
                        } else if (gesture_state == STATE.MULTI_FINGER_DRAG) {
                            //pinch zoom
                            Log.d("BeansGesture", "Pinch-Zooming DONE");
                        } else if (gesture_state == STATE.SINGLE_FINGER_DRAG) {
                            //drag
                            if (listener != null) {
                                listener.onDragEnd();
                            }
                            Log.d("BeansGesture", "Dragging DOne");
                        }
                    }
                    inLongPress = false;
                    gesture_state = STATE.NONE;
                    firstFingerDown = false;
                    secondFingerDown = false;
                    if (listener != null) {
                        listener.onActionEnd();
                    }
                    return true;
                case (MotionEvent.ACTION_CANCEL):
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    Log.d("BeansGesture", "Action was CANCEL");
                    return true;
                case (MotionEvent.ACTION_OUTSIDE):
                    Log.d("BeansGesture", "Movement occurred outside bounds " +
                            "of current screen element");
                    return true;
            }
            return true;
        }
    }

    public void removeListener() {
        listener = null;
    }

    public void setListener(BeansGestureDetectorListener listener) {
        this.listener = listener;
    }

    //Build in Gesture listener to listen for long presses...
    class MyLongPressListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public void onLongPress(MotionEvent e) {
            if (listener != null) {
                listener.onLongPress();
                inLongPress = true;
            }

        }
    }

    public void destroy() {
        removeListener();
        this.view = null;
    }
}
