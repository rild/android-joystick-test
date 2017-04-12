package rimp.rild.com.android.android_joystick_controler_test;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rild on 2017/04/10.
 */

public class JoyStickSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final int TIME_LONG_ACTIVATE = 1500;
    private final int DENO_RATE_STICK_TALL_TO_SIZE = 25;

    private final int DENO_RATE_STICK_SIZE_TO_PAD = 2;
    private final int MARGIN_SHADOW = 32;
    private final int DENO_RATE_OFFSET_TO_PAD = 3;
    private final int DENO_RATE_MIN_DISTANCE_TO_PAD = 12;
    private int ALPHA_STICK = 200;
    private int ALPHA_LAYOUT = 200;
    private int ALPHA_SIGNAL = 200;
    private int OFFSET = 0;

    /**
     * {@code isFixedInterval == false} add weight to `loopInterval`
     * the weight depends on distance (getDistance return value)
     *
     * @param range range would be
     * PAD SIZE (prams.height, params.width) - OFFSET
     * it limits stick movement
     */
    private int RANGE = 100;

    private SurfaceHolder surfaceHolder;

    //    private Context mContext;
    private ViewGroup.LayoutParams params;
    private int stickTall;
    private int stickWidth, stickHeight;
    private int shadowWidth, shadowHeight;
    private int positionX = 0, positionY = 0, minDistance = 0;
    private float distance = 0, angle = 0;

    private JoyStick jsEntity; // joy stick entity

    private Paint alphaSignal;
    private Paint alphaBackground;
    private Paint alphaStick;

    private Resources res;
    private Bitmap background;
    private Bitmap stick;
    private Bitmap shadow; // joy stick shadow
    private Bitmap signalUp;
    private Bitmap signalRight;
    private Bitmap signalDown;
    private Bitmap signalLeft;

    private boolean isTouched = false;
    private boolean shouldDrawShadow = true;
    private boolean canUseSignal = true;

    private JoyStickState stickState = JoyStickState.NONE;

    private On8DirectListener on8DirectListener;
    private On4DirectListener on4DirectListener;
    private OnChangeStateListener onChangeStateListener;

    private final long LOOP_INTERVAL_DEFAULT = 800; // original 100 ms
    public final static long LOOP_INTERVAL_SLOW = 800; // original 100 ms
    public final static long LOOP_INTERVAL_FAST = 100;
    private boolean hasFastLoop = false;

    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private Thread thread = new Thread(this);
    private long loopInterval = LOOP_INTERVAL_DEFAULT;
    private long loopFastInterval = LOOP_INTERVAL_DEFAULT;


    public JoyStickSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mContext = context;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        if (!isInEditMode()) setZOrderOnTop(true);

        res = context.getResources();
        loadImages(res);

        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();
        if (shadow != null) {
            shadowWidth = shadow.getWidth();
            shadowHeight = shadow.getHeight();
        }

        alphaSignal = new Paint();
        alphaBackground = new Paint();
        alphaStick = new Paint();
        jsEntity = new JoyStick();

        registerOnTouchEvent();
    }

    private void registerScreenSize() {
        params = new ViewGroup.LayoutParams(getWidth(), getHeight());
    }

    private void registerOnTouchEvent() {
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawBackground(canvas);
                drawStick(canvas, event);
                surfaceHolder.unlockCanvasAndPost(canvas);

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {

                    if (on4DirectListener != null)
                        on4DirectListener.onDirect(getPosX(), getPosY(), getAngle(), getDistance());

                    if (distance > minDistance && isTouched) {
                        onChangeState(judge8DirectionEvent(angle));
                    } else if (distance <= minDistance && isTouched) {
                        // STICK_NONE;
                        if (on4DirectListener != null) on4DirectListener.onNone();
                        stickState = JoyStickState.NONE;
//                        handlerLongDirection.removeCallbacks(mLongPressed);

                        thread.interrupt();
                        if (onJoystickMoveListener != null)
                            onJoystickMoveListener.onValueChanged(getAngle(), getDistance(),
                                    getStickState());

                    }

                } else {
                    if (on4DirectListener != null) on4DirectListener.onFinish();
                    stickState = JoyStickState.NONE;
//                    handlerLongDirection.removeCallbacks(mLongPressed);

                    thread.interrupt();
                    if (onJoystickMoveListener != null)
                        onJoystickMoveListener.onValueChanged(getAngle(), getDistance(),
                                getStickState());
                }
                return true;
            }
        });
    }

    private JoyStickState judge8DirectionEvent(float angle) {
        JoyStickState event = JoyStickState.NONE;
        if (angle >= 247.5 && angle < 292.5) {
            // STICK_UP;
            return JoyStickState.UP;
        } else if (angle >= 292.5 && angle < 337.5) {
            // STICK_UPRIGHT;
            return JoyStickState.UPRIGHT;
        } else if (angle >= 337.5 || angle < 22.5) {
            // STICK_RIGHT;
            return JoyStickState.RIGHT;
        } else if (angle >= 22.5 && angle < 67.5) {
            // STICK_DOWNRIGHT;
            return JoyStickState.DOWNRIGHT;
        } else if (angle >= 67.5 && angle < 112.5) {
            // STICK_DOWN;
            return JoyStickState.DOWN;
        } else if (angle >= 112.5 && angle < 157.5) {
            // STICK_DOWNLEFT;
            return JoyStickState.DOWNLEFT;
        } else if (angle >= 157.5 && angle < 202.5) {
            // STICK_LEFT;
            return JoyStickState.LEFT;
        } else if (angle >= 202.5 && angle < 247.5) {
            // STICK_UPLEFT;
            return JoyStickState.UPLEFT;
        }
        return event;
    }

    private void onChangeState(JoyStickState state) {
        if (stickState != state) {
            // change from other state
            if (on4DirectListener != null) {
                switch (state) {
                    case UP:
                        on4DirectListener.onUp();
                        break;
                    case RIGHT:
                        on4DirectListener.onRight();
                        break;
                    case DOWN:
                        on4DirectListener.onDown();
                        break;
                    case LEFT:
                        on4DirectListener.onLeft();
                        break;
                }
                on4DirectListener.onUp();
            }
            if (on8DirectListener != null) {
                switch (state) {
                    case UPRIGHT:
                        on8DirectListener.onUpRight();
                        break;
                    case DOWNRIGHT:
                        on8DirectListener.onDownRight();
                        break;
                    case DOWNLEFT:
                        on8DirectListener.onDownLeft();
                        break;
                    case UPLEFT:
                        on8DirectListener.onUpLeft();
                        break;
                }
            }

            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(JoyStickSurfaceView.this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getDistance(),
                        getStickState());
        }
        stickState = state;
    }

    private void onUp() {
        if (stickState != JoyStickState.UP) {
            if (on4DirectListener != null) on4DirectListener.onUp();

            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(JoyStickSurfaceView.this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getDistance(),
                        getStickState());
        }
        stickState = JoyStickState.UP;
    }

    private void onUpRight() {
        if (on8DirectListener != null && stickState != JoyStickState.UPRIGHT) {
            on8DirectListener.onUpRight();
        }
        stickState = JoyStickState.UPRIGHT;
    }

    private void onRight() {
        if (stickState != JoyStickState.RIGHT) {
            if (on4DirectListener != null) on4DirectListener.onRight();
        }
        stickState = JoyStickState.RIGHT;
    }

    private void onDownRight() {
        if (on8DirectListener != null && stickState != JoyStickState.DOWNRIGHT) {
            on8DirectListener.onDownRight();
        }
        stickState = JoyStickState.DOWNRIGHT;
    }

    private void onDown() {
        if (stickState != JoyStickState.DOWN) {
            if (on4DirectListener != null) on4DirectListener.onDown();
        }
        stickState = JoyStickState.DOWN;
    }

    private void onDownLeft() {
        if (on8DirectListener != null && stickState != JoyStickState.DOWNLEFT) {
            on8DirectListener.onDownLeft();
        }
        stickState = JoyStickState.DOWNLEFT;
    }

    private void onLeft() {
        if (stickState != JoyStickState.LEFT) {
            if (on4DirectListener != null) on4DirectListener.onLeft();
        }
        stickState = JoyStickState.LEFT;
    }

    private void onUpLeft() {
        if (on8DirectListener != null && stickState != JoyStickState.UPLEFT) {
            on8DirectListener.onUpLeft();
        }
        stickState = JoyStickState.UPLEFT;
    }

    public void registerLayoutCenter(int width, int height) {
        jsEntity.center_x = width / 2;
        jsEntity.center_y = height / 2;
    }

    private void loadImages(Resources res) {
        if (background != null) background.recycle();
        if (stick != null) stick.recycle();
        if (shadow != null) shadow.recycle();

        background = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_base);
        stick = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_stick);
        shadow = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_shadow); // if you remove shadow, you should also remove "stickTall" : stickTall = 0

        if (canUseSignal) loadSignalImages(res);
    }

    private void loadSignalImages(Resources res) {
        if (signalUp != null) signalUp.recycle();
        if (signalRight != null) signalRight.recycle();
        if (signalDown != null) signalDown.recycle();
        if (signalLeft != null) signalLeft.recycle();

        signalUp = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_up);
        signalRight = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_right);
        signalDown = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_down);
        signalLeft = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_left);
    }

    private void releaseSignalImages() {
        signalUp.recycle();
        signalRight.recycle();
        signalDown.recycle();
        signalLeft.recycle();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        init();
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawBackground(canvas);
        drawStick(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void init() {
//        Log.d("Params", "" + getWidth() + ", " + getHeight());
        registerScreenSize();
        registerLayoutCenter(params.width, params.height);

        // default
        // pad (params) 504 // 126 * 4
        // stick size   220 // 55 * 4
        // shadow size  252 // 63 * 4
        // offset       180 // 45 * 4
        // min distance 40  // 10 * 4
        // pad alpha    150
        // stick alpha  180

        stickTall = stickHeight / DENO_RATE_STICK_TALL_TO_SIZE; // make user feel sticky
        setStickSize(params.width / DENO_RATE_STICK_SIZE_TO_PAD, params.height / DENO_RATE_STICK_SIZE_TO_PAD);
        setShadowSize(params.width / DENO_RATE_STICK_SIZE_TO_PAD + MARGIN_SHADOW,
                params.height / DENO_RATE_STICK_SIZE_TO_PAD + MARGIN_SHADOW);
        setLayoutAlpha(150);
        setStickAlpha(180);
        setSignalAlpha(120);
        setOffset(params.width / DENO_RATE_OFFSET_TO_PAD);
        setMinimumDistance(params.width / DENO_RATE_MIN_DISTANCE_TO_PAD);

        RANGE = params.height - OFFSET;
        resizeImages();
    }

    public void drawStick(Canvas canvas, MotionEvent event) {
        positionX = (int) (event.getX() - (params.width / 2));
        positionY = (int) (event.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(positionX, 2) + Math.pow(positionY, 2));
        angle = (float) calAngle(positionX, positionY);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (params.width / 2) - OFFSET) {
                jsEntity.position(event.getX(), event.getY());
                isTouched = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && isTouched) {
            if (distance <= (params.width / 2) - OFFSET) {
                jsEntity.position(event.getX(), event.getY());
                drawSignal(canvas);
            } else if (distance > (params.width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(calAngle(positionX, positionY))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(calAngle(positionX, positionY))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                jsEntity.position(x, y);
                drawDarkenSignal(canvas);
            } else {
                // reset stick pad
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                drawBackground(canvas);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // reset stick pad
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawBackground(canvas);
            isTouched = false;
        }
        // reset stick
        if (isTouched) drawDarkenStick(canvas); // darken stick on touched
        else drawStick(canvas);
    }

    private void drawStick(Canvas canvas) {
        if (isTouched) {
            if (shadow != null && shouldDrawShadow)
                canvas.drawBitmap(shadow, jsEntity.s_x, jsEntity.s_y, alphaStick);
            canvas.drawBitmap(stick, jsEntity.x, jsEntity.y, alphaStick);
        } else {
            if (shadow != null && shouldDrawShadow) {
                canvas.drawBitmap(shadow,
                        jsEntity.center_x - (shadowWidth / 2),
                        jsEntity.center_y - (shadowHeight / 2) + stickTall,
                        alphaStick);
                canvas.drawBitmap(stick, jsEntity.center_x - (stickWidth / 2), jsEntity.center_y - (stickHeight / 2) - stickTall, alphaStick);
            } else {
                canvas.drawBitmap(stick, jsEntity.center_x - (stickWidth / 2), jsEntity.center_y - (stickHeight / 2), alphaStick);
            }
        }
    }

    private void drawDarkenStick(Canvas canvas) {
        drawStick(canvas);
        drawStick(canvas);
    }

    public void drawBackground(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, alphaBackground);
    }

    private void drawSignal(Canvas canvas) {
        if (!canUseSignal) return;
        switch (stickState) {
            case UP:
                canvas.drawBitmap(signalUp, 0, 0, alphaSignal);
                break;
            case RIGHT:
                canvas.drawBitmap(signalRight, 0, 0, alphaSignal);
                break;
            case DOWN:
                canvas.drawBitmap(signalDown, 0, 0, alphaSignal);
                break;
            case LEFT:
                canvas.drawBitmap(signalLeft, 0, 0, alphaSignal);
                break;
        }
    }

    private void drawDarkenSignal(Canvas canvas) {
        drawSignal(canvas);
        drawSignal(canvas);
        drawSignal(canvas);
    }

    public int getPosX() {
        if (distance > minDistance && isTouched) {
            return positionX;
        }
        return 0;
    }

    public int getPosY() {
        if (distance > minDistance && isTouched) {
            return positionY;
        }
        return 0;
    }

    public float getAngle() {
        if (distance > minDistance && isTouched) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if (distance > minDistance && isTouched) {
            return distance;
        }
        return 0;
    }

    public int getMinimumDistance() {
        return minDistance;
    }

    public int getOffset() {
        return OFFSET;
    }

    public int getLayoutAlpha() {
        return ALPHA_LAYOUT;
    }

    public int getStickAlpha() {
        return ALPHA_STICK;
    }

    public int getALPHA_SIGNAL() {
        return ALPHA_SIGNAL;
    }

    public JoyStickState getStickState() {
        return stickState;
    }

    public boolean isShadowVisible() {
        return shouldDrawShadow;
    }

    private void resizeImages() {
        if (shadow != null) {
            shadow = resizeImage(shadow, shadowWidth, shadowHeight);
        }

        stick = resizeImage(stick, stickWidth, stickHeight);
        background = resizeImage(background, params.width, params.height);

        if (canUseSignal) {
            resizeSignalImages();
        }
    }

    private void resizeSignalImages() {
        signalUp = resizeImage(signalUp, params.width, params.height);
        signalRight = resizeImage(signalRight, params.width, params.height);
        signalDown = resizeImage(signalDown, params.width, params.height);
        signalLeft = resizeImage(signalLeft, params.width, params.height);
    }


    private Bitmap resizeImage(final Bitmap original, int targetWidth, int targetHeight) {
        return Bitmap.createScaledBitmap(original, targetWidth, targetHeight, false);
    }

    public void setStickSize(int width, int height) {
        stickWidth = width;
        stickHeight = height;
    }

    public void setMinimumDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public void setLayoutAlpha(int alpha) {
        ALPHA_LAYOUT = alpha;
        alphaBackground.setAlpha(alpha);
    }

    public void setStickAlpha(int alpha) {
        ALPHA_STICK = alpha;
        alphaStick.setAlpha(alpha);
    }

    public void setSignalAlpha(int alpha) {
        ALPHA_SIGNAL = alpha;
        alphaSignal.setAlpha(alpha);
    }

    public void setShadowSize(int width, int height) {
        this.shadowWidth = width;
        this.shadowHeight = height;
    }

    public void setShadowVisibility(boolean shouldDrawShadow) {
        this.shouldDrawShadow = shouldDrawShadow;
    }

    public void setSignalAvailability(boolean canUseSignal) {
        this.canUseSignal = canUseSignal;
        if (canUseSignal) {
            loadImages(res);
            resizeSignalImages();
        } else {
            releaseSignalImages();
        }
    }

    public void setStickState(JoyStickState stickState) {
        this.stickState = stickState;
    }

    private double calAngle(float x, float y) {
        if (x >= 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x));
        else if (x < 0 && y >= 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x < 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 180;
        else if (x >= 0 && y < 0)
            return Math.toDegrees(Math.atan(y / x)) + 360;
        return 0;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null)
                        onJoystickMoveListener.onValueChanged(getAngle(),
                                getDistance(), getStickState());
                }
            });
            try {
                long interval = loopInterval;
                if (hasFastLoop)
                    interval = calCurrentInterval();
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void setOn4DirectListener(On4DirectListener on4DirectListener) {
        this.on4DirectListener = on4DirectListener;
    }

    public void setOn8DirectListener(On8DirectListener on8DirectListener) {
        this.on8DirectListener = on8DirectListener;
        this.on4DirectListener = on8DirectListener;
    }

    public void setOnJoyStickMoveListener(OnJoystickMoveListener listener,
                                          long loopInterval) {
        this.onJoystickMoveListener = listener;
        this.loopInterval = loopInterval;
        this.hasFastLoop = false;
    }


    public void setOnJoyStickMoveListener(OnJoystickMoveListener listener,
                                          long loopSlowInterval, long loopFastInterval) {
        setOnJoyStickMoveListener(listener, loopSlowInterval);
        this.loopFastInterval = loopFastInterval;
        this.hasFastLoop = true;
    }

    private long calCurrentInterval() {
        long in = loopInterval;
//        if (OFFSET >= distance) in = loopInterval;
        if (distance <= (params.width / 2) - OFFSET) in = loopInterval;
//        else if (distance >= RANGE - params.height / 5) in = LOOP_INTERVAL_FASTEST;
        else if (distance > (params.width / 2) - OFFSET) in = loopFastInterval;
//        else {
//
//            float xx_per_aa = (distance - OFFSET) * (distance - OFFSET) / RANGE / RANGE;
//            Log.d("cal", "x^2/a^2:" + xx_per_aa);
//            Log.d("cal", "1-x^2/a^2:" +
//                    (1 - xx_per_aa));
//            Log.d("cal", "root:" + Math.sqrt(
//                    1 - xx_per_aa));
//            Log.d("cal", "F:" + ((loopInterval - LOOP_INTERVAL_FASTEST)
//                    * Math.sqrt(1 - xx_per_aa)
//                    + LOOP_INTERVAL_FASTEST));
//            in = (long) (
//                    (loopInterval - LOOP_INTERVAL_FASTEST)
//                            * Math.sqrt(1 - xx_per_aa)
//                            + LOOP_INTERVAL_FASTEST);
//
//        }
        return in;
    }

    interface On8DirectListener extends On4DirectListener {
        void onUpRight();

        void onDownRight();

        void onDownLeft();

        void onUpLeft();
    }

    interface On4DirectListener {
        void onDirect(int posX, int posY, float angle, float distance);

        void onNone();

        void onUp();

        void onRight();

        void onDown();

        void onLeft();

        void onFinish();
    }

    interface OnLongPushListener {
        void onLongPush();
    }

    interface OnChangeStateListener {
        void onChangeState(JoyStickState next, JoyStickState previous);
    }

    public interface OnJoystickMoveListener {
        void onValueChanged(float angle, float power, JoyStickState direction);
    }

    enum JoyStickState {
        NONE,
        UP,
        UPRIGHT,
        RIGHT,
        DOWNRIGHT,
        DOWN,
        DOWNLEFT,
        LEFT,
        UPLEFT;
    }

    class JoyStick {
        float x, y;
        float s_x, s_y; // shadow
        float center_x, center_y; // center

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stickWidth / 2);
            y = pos_y - (stickHeight / 2);

            // vecPC : position - center vec
            float vecPC_x = center_x - pos_x;
            float vecPC_y = center_y - pos_y;
            s_x = pos_x - (shadowWidth / 2) + vecPC_x / 3;
            s_y = pos_y - (shadowHeight / 2) + vecPC_y / 3;
        }
    }
}
