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

public class JoyStickSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;

    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;
    private final int DENO_RATE_STICK_TALL_TO_SIZE = 25;
    private final int DENO_RATE_STICK_SIZE_TO_PAD = 2;
    private final int MARGIN_SHADOW = 32;
    private final int DENO_RATE_OFFSET_TO_PAD = 3;
    private final int DENO_RATE_MIN_DISTANCE_TO_PAD = 12;

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
    private JoyStickEvent stickState = JoyStickEvent.NONE;

    private On8DirectListener on8DirectListener;
    private On4DirectListener on4DirectListener;

    public void setOn4DirectListener(On4DirectListener on4DirectListener) {
        this.on4DirectListener = on4DirectListener;
    }

    public void setOn8DirectListener(On8DirectListener on8DirectListener) {
        this.on8DirectListener = on8DirectListener;
        this.on4DirectListener = on8DirectListener;
    }

    public JoyStickSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mContext = context;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        if (!isInEditMode()) setZOrderOnTop(true);

        res = context.getResources();
        registerBitmapImages(res);

        stickWidth = stick.getWidth();
        stickHeight = stick.getHeight();
        shadowWidth = shadow.getWidth();
        shadowHeight = shadow.getHeight();

        alphaSignal = new Paint();
        alphaBackground = new Paint();
        alphaStick = new Paint();
        jsEntity = new JoyStick();

        registerOnTouchEvent();
    }

    private void registerBitmapImages(Resources res) {
        background = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_base);
        stick = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_stick);
        shadow = BitmapFactory.decodeResource(res,
                R.drawable.s_joystick_shadow);

        signalUp = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_up);
        signalRight = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_right);
        signalDown = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_down);
        signalLeft = BitmapFactory.decodeResource(res,
                R.drawable.s_signal_left);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    private void registerScreenSize() {
        params = new ViewGroup.LayoutParams(getWidth(), getHeight());
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
        setOffset(params.width / DENO_RATE_OFFSET_TO_PAD);
        setMinimumDistance(params.width / DENO_RATE_MIN_DISTANCE_TO_PAD);

        resizeImages();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        params = getLayoutParams();
        Log.d("created", "surfaceCreated");
        init();
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawBackground(canvas);
        drawStick(canvas);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void registerOnTouchEvent() {
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Event", "touch event");
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawBackground(canvas);
                drawStick(canvas, event);
                surfaceHolder.unlockCanvasAndPost(canvas);

                if (on4DirectListener == null) return true;

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    on4DirectListener.onDirect(getPosX(), getPosY(), getAngle(), getDistance());

                    if (distance > minDistance && isTouched) {
                        if (angle >= 247.5 && angle < 292.5) {
                            // STICK_UP;
                            on4DirectListener.onUp();
                            stickState = JoyStickEvent.UP;
                        } else if (angle >= 292.5 && angle < 337.5) {
                            // STICK_UPRIGHT;
                            if (on8DirectListener != null) on8DirectListener.onUpRight();
                        } else if (angle >= 337.5 || angle < 22.5) {
                            // STICK_RIGHT;
                            on4DirectListener.onRight();
                            stickState = JoyStickEvent.RIGHT;
                        } else if (angle >= 22.5 && angle < 67.5) {
                            // STICK_DOWNRIGHT;
                            if (on8DirectListener != null) on8DirectListener.onDownRight();
                        } else if (angle >= 67.5 && angle < 112.5) {
                            // STICK_DOWN;
                            on4DirectListener.onDown();
                            stickState = JoyStickEvent.DOWN;
                        } else if (angle >= 112.5 && angle < 157.5) {
                            // STICK_DOWNLEFT;
                            if (on8DirectListener != null) on8DirectListener.onDownLeft();
                        } else if (angle >= 157.5 && angle < 202.5) {
                            // STICK_LEFT;
                            on4DirectListener.onLeft();
                            stickState = JoyStickEvent.LEFT;
                        } else if (angle >= 202.5 && angle < 247.5) {
                            // STICK_UPLEFT;
                            if (on8DirectListener != null) on8DirectListener.onUpLeft();
                        }
                    } else if (distance <= minDistance && isTouched) {
                        // STICK_NONE;
                        on4DirectListener.onNone();
                    }

                } else {
                    on4DirectListener.onFinish();
                }
                return true;
            }
        });
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

    public void drawBackground(Canvas canvas) {
        // now lets draw using alphaPaint instance
        canvas.drawBitmap(background, 0, 0, alphaBackground);
    }

    public void drawStick(Canvas canvas, MotionEvent event) {
        positionX = (int) (event.getX() - (params.width / 2));
        positionY = (int) (event.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(positionX, 2) + Math.pow(positionY, 2));
        angle = (float) cal_angle(positionX, positionY);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (params.width / 2) - OFFSET) {
                jsEntity.position(event.getX(), event.getY());
                isTouched = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && isTouched) {
            if (distance <= (params.width / 2) - OFFSET) {
                jsEntity.position(event.getX(), event.getY());
            } else if (distance > (params.width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(positionX, positionY))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(positionX, positionY))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                jsEntity.position(x, y);
                drawSignal(canvas);
            } else {
                // reset stick
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawBackground(canvas);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // reset stick
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawBackground(canvas);
            isTouched = false;
        }
        // reset stick
        drawStick(canvas);
        if (isTouched) drawStick(canvas);
    }

    private void drawSignal(Canvas canvas) {
        switch (stickState) {
            case UP:
                Log.d("Draw", "signal up");
                canvas.drawBitmap(signalUp, 0, 0, alphaSignal);
                break;
            case RIGHT:
                Log.d("Draw", "signal right");
                canvas.drawBitmap(signalRight, 0, 0, alphaSignal);
                break;
            case DOWN:
                Log.d("Draw", "signal down");
                canvas.drawBitmap(signalDown, 0, 0, alphaSignal);
                break;
            case LEFT:
                Log.d("Draw", "signal left");
                canvas.drawBitmap(signalLeft, 0, 0, alphaSignal);
                break;
        }
    }

    private void drawStick(Canvas canvas) {
        if (isTouched) {
            canvas.drawBitmap(shadow, jsEntity.s_x, jsEntity.s_y, alphaStick);
            canvas.drawBitmap(stick, jsEntity.x, jsEntity.y, alphaStick);
        } else {
            canvas.drawBitmap(shadow, jsEntity.center_x - (shadowWidth / 2), jsEntity.center_y - (shadowHeight / 2) + stickTall, alphaStick);
            canvas.drawBitmap(stick, jsEntity.center_x - (stickWidth / 2), jsEntity.center_y - (stickHeight / 2) - stickTall, alphaStick);
        }
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

    private void resizeImages() {
        if (shadow != null)
            shadow = Bitmap.createScaledBitmap(shadow, shadowWidth, shadowHeight, false);
        stick = Bitmap.createScaledBitmap(stick, stickWidth, stickHeight, false);
        background = Bitmap.createScaledBitmap(background, params.width, params.height, false);
        signalUp = Bitmap.createScaledBitmap(signalUp, params.width, params.height, false);
        signalRight = Bitmap.createScaledBitmap(signalRight, params.width, params.height, false);
        signalDown = Bitmap.createScaledBitmap(signalDown, params.width, params.height, false);
        signalLeft = Bitmap.createScaledBitmap(signalLeft, params.width, params.height, false);
    }

    public void setStickSize(int width, int height) {
        stickWidth = width;
        stickHeight = height;
    }

    public void registerLayoutCenter(int width, int height) {
        // addition
        jsEntity.center_x = width / 2;
        jsEntity.center_y = height / 2;
        // end
    }

    public void setMinimumDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public int getMinimumDistance() {
        return minDistance;
    }

    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public int getOffset() {
        return OFFSET;
    }

    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        alphaBackground.setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        alphaStick.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setShadowSize(int width, int height) {
        this.shadowWidth = width;
        this.shadowHeight = height;
    }

    public void setStickState(JoyStickEvent stickState) {
        this.stickState = stickState;
    }

    public JoyStickEvent getStickState() {
        return stickState;
    }

    private double cal_angle(float x, float y) {
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

    enum JoyStickEvent {
        NONE,
        UP,
        UPRIGHT,
        RIGHT,
        DOWNRIGHT,
        DOWN,
        DOWNLEFT,
        LEFT,
        UPLEFT
    }

    class JoyStick {
        float x, y;
        float s_x, s_y;
        float center_x, center_y;

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stickWidth / 2);
            y = pos_y - (stickHeight / 2);

            // addition
            float vecPC_x = center_x - pos_x;
            float vecPC_y = center_y - pos_y;
            s_x = pos_x - (shadowWidth / 2) + vecPC_x / 3;
            s_y = pos_y - (shadowHeight / 2) + vecPC_y / 3;
            // end
        }
    }
}
