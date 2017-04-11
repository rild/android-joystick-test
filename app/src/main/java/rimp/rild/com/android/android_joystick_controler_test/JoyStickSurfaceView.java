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
import android.widget.LinearLayout;

/**
 * Created by rild on 2017/04/10.
 */

public class JoyStickSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder surfaceHolder;


    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;
    private int OFFSET_SHADOW = 0; // addition by rild

    private Context mContext;
    //    private ViewGroup.LayoutParams params;
    private int width, height;
    private int stick_width, stick_height;
    private int shadow_width, shadow_height; // addition by rild
    ViewGroup.LayoutParams params;
    private int position_x = 0, position_y = 0, min_distance = 0;
    private float distance = 0, angle = 0;

    private JoyStick draw;
    private Paint paint;
    Paint alphaBackground;
    Paint alphaStick;

    Resources res;
    private Bitmap background;
    private Bitmap stick;
    private Bitmap shadow; // addition by rild
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
        mContext = context;
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        res = mContext.getResources();
        registerBitmapImages(res);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
        shadow_width = shadow.getWidth();
        shadow_height = shadow.getHeight();

        paint = new Paint();
        alphaBackground = new Paint();
        alphaStick = new Paint();
        draw = new JoyStick();
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

    public void init() {
//        params = getLayoutParams();
//        params.width = 500;
//        params.height = 500;
//
//        setLayoutParams(params);
        setLayoutSize(500, 500);
        setStickSize(220, 220);
        setLayoutAlpha(150);
        setStickAlpha(180);
        setOffset(180);
        setMinimumDistance(50);

        setShadowSize(250, 250);
        resizeImages();

//        // show stick
//        draw();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        params = getLayoutParams();
        init();
//        if (canvas == null) canvas = surfaceHolder.lockCanvas();
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
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawBackground(canvas);
                drawStick(canvas, event);
                surfaceHolder.unlockCanvasAndPost(canvas);
//                Log.d("Event", "on touch 2");

                if (on4DirectListener == null) return true;

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    on4DirectListener.onDirect(getPosX(), getPosY(), getAngle(), getDistance());

                    if (distance > min_distance && isTouched) {
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
                    } else if (distance <= min_distance && isTouched) {
                        // STICK_NONE;
                        on4DirectListener.onNone();
                    }

                } else {
                    Log.d("Event", "on move finished");
                    on4DirectListener.onFinish();
                }
                return true;
            }
        });
    }

    public int getPosX() {
        if (distance > min_distance && isTouched) {
            return position_x;
        }
        return 0;
    }

    public int getPosY() {
        if (distance > min_distance && isTouched) {
            return position_y;
        }
        return 0;
    }

    public void drawBackground(Canvas canvas) {
        // now lets draw using alphaPaint instance
        canvas.drawBitmap(background, 0, 0, alphaBackground);
    }

    public void drawStick(Canvas canvas, MotionEvent event) {
        position_x = (int) (event.getX() - (width / 2));
        position_y = (int) (event.getY() - (height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (width / 2) - OFFSET) {
                draw.position(event.getX(), event.getY());
//                drawStick(canvas);
                isTouched = true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE && isTouched) {
            if (distance <= (width / 2) - OFFSET) {
                draw.position(event.getX(), event.getY());
//                drawStick(canvas);
            } else if (distance > (width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((height / 2) - OFFSET));
                x += (width / 2);
                y += (height / 2);
                draw.position(x, y);
                drawSignal(canvas);
//                drawStick(canvas);
            } else {
//                mLayout.removeView(draw);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawBackground(canvas);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
//            mLayout.removeView(draw);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            drawBackground(canvas);
            isTouched = false;
        }
        // reset stick
//        drawSignal(canvas);
//        Log.d("Draw", "stick");
        drawStick(canvas);
    }

    private void drawSignal(Canvas canvas) {
//        Log.d("method", "signal");
        switch (stickState) {
            case UP:
                Log.d("Draw", "signal up");
                canvas.drawBitmap(signalUp, 0, 0, paint);
                break;
            case RIGHT:
                Log.d("Draw", "signal right");
                canvas.drawBitmap(signalRight, 0, 0, paint);
                break;
            case DOWN:
                Log.d("Draw", "signal down");
                canvas.drawBitmap(signalDown, 0, 0, paint);
                break;
            case LEFT:
                Log.d("Draw", "signal left");
                canvas.drawBitmap(signalLeft, 0, 0, paint);
                break;
        }
    }

    private void drawStick(Canvas canvas) {
//        canvas.drawBitmap(stick, draw.x, draw.y, paint);
        if (isTouched) {
            canvas.drawBitmap(shadow, draw.s_x, draw.s_y, paint);
            canvas.drawBitmap(stick, draw.x, draw.y, paint);
        } else {
            int stick_tall = 15;// make user feel sticky
            canvas.drawBitmap(shadow, draw.center_x - (shadow_width / 2), draw.center_y - (shadow_height / 2) + stick_tall, paint);
            canvas.drawBitmap(stick, draw.center_x - (stick_width / 2), draw.center_y - (stick_height / 2) - stick_tall, paint);
        }
    }

    public float getAngle() {
        if (distance > min_distance && isTouched) {
            return angle;
        }
        return 0;
    }

    public float getDistance() {
        if (distance > min_distance && isTouched) {
            return distance;
        }
        return 0;
    }

    private void resizeImages() {
        if (shadow != null)
            shadow = Bitmap.createScaledBitmap(shadow, shadow_width, shadow_height, false);
        stick = Bitmap.createScaledBitmap(stick, stick_width, stick_height, false);
        background = Bitmap.createScaledBitmap(background, width, height, false);
        signalUp = Bitmap.createScaledBitmap(signalUp, width, height, false);
        signalRight = Bitmap.createScaledBitmap(signalRight, width, height, false);
        signalDown = Bitmap.createScaledBitmap(signalDown, width, height, false);
        signalLeft = Bitmap.createScaledBitmap(signalLeft, width, height, false);
    }

    public void setStickSize(int width, int height) {
//        stick_width = stick.getWidth();
//        stick_height = stick.getHeight();
        stick_width = width;
        stick_height = height;
    }

    public void setLayoutSize(int width, int height) {
        this.width = width;
        this.height = height;

        // addition
        draw.center_x = width / 2;
        draw.center_y = height / 2;
        // end
    }

    public void setMinimumDistance(int minDistance) {
        min_distance = minDistance;
    }

    public int getMinimumDistance() {
        return min_distance;
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
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setShadowSize(int width, int height) {
        this.shadow_width = width;
        this.shadow_height = height;
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

        public void onDraw(Canvas canvas) {
            // check center
            if (isTouched) {
                canvas.drawBitmap(shadow, s_x, s_y, paint);
                canvas.drawBitmap(stick, x, y, paint);
            } else {
                int stick_tall = 15;// make user feel sticky
                canvas.drawBitmap(shadow, center_x - (shadow_width / 2), center_y - (shadow_height / 2) + stick_tall, paint);
                canvas.drawBitmap(stick, center_x - (stick_width / 2), center_y - (stick_height / 2) - stick_tall, paint);
            }
        }

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);

            // addition
            float vecPC_x = center_x - pos_x;
            float vecPC_y = center_y - pos_y;
            s_x = pos_x - (shadow_width / 2) + vecPC_x / 3;
            s_y = pos_y - (shadow_height / 2) + vecPC_y / 3;
            // end
        }
    }
}
