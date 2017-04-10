package rimp.rild.com.android.android_joystick_controler_test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class JoyStick2 {
    public static final int STICK_NONE = 0;
    public static final int STICK_UP = 1;
    public static final int STICK_UPRIGHT = 2;
    public static final int STICK_RIGHT = 3;
    public static final int STICK_DOWNRIGHT = 4;
    public static final int STICK_DOWN = 5;
    public static final int STICK_DOWNLEFT = 6;
    public static final int STICK_LEFT = 7;
    public static final int STICK_UPLEFT = 8;

    private int STICK_ALPHA = 200;
    private int LAYOUT_ALPHA = 200;
    private int OFFSET = 0;
    private int OFFSET_SHADOW = 0; // addition by rild

    private Context mContext;
    private ViewGroup mLayout;
    private LayoutParams params;
    private int stick_width, stick_height;
    private int shadow_width, shadow_height; // addition by rild

    private int position_x = 0, position_y = 0, min_distance = 0;
    private float distance = 0, angle = 0;

    private DrawCanvas draw;
    private Paint paint;
    private Bitmap stick;
    private Bitmap stickShadow; // addition by rild

    private boolean isTouched = false;
    private JoyStickEvent stickState = JoyStickEvent.STICK_NONE;

    private On8DirectListener on8DirectListener;
    private On4DirectListener on4DirectListener;


    public void init() {
        setLayoutSize(500, 500);
        setStickSize(220, 220);
        setLayoutAlpha(150);
        setStickAlpha(180);
        setOffset(180);
        setMinimumDistance(50);

        setStickShadow();
        setShadowSize(250, 250);

        // show stick
        draw();
    }

    public void setStickShadow() {
        stickShadow = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.joystick_shadow);
    }

    public void setShadowSize(int width, int height) {
        if (stickShadow == null) return;
        stickShadow = Bitmap.createScaledBitmap(stickShadow, width, height, false);
        this.shadow_width = width;
        this.shadow_height = height;
    }

    public void setShadowOffset(int offset) {
        OFFSET_SHADOW = offset;
    }

    public int getShadowOffset() {
        return OFFSET_SHADOW;
    }

    // end

    public JoyStick2(Context context, ViewGroup layout, int stick_res_id) {
        mContext = context;

        stick = BitmapFactory.decodeResource(mContext.getResources(),
                stick_res_id);

        stick_width = stick.getWidth();
        stick_height = stick.getHeight();

        paint = new Paint();
        mLayout = layout;
        params = mLayout.getLayoutParams();
        draw = new DrawCanvas(mContext);

        registerOnTouchEvent();
    }

    private void registerOnTouchEvent() {
        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                drawStick(event);
                if (on4DirectListener == null) return true;

                if (event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_MOVE) {
                    on4DirectListener.onDirect();

                    if (distance > min_distance && isTouched) {
                        if (angle >= 247.5 && angle < 292.5) {
                            // STICK_UP;
                            on4DirectListener.onUp();
                        } else if (angle >= 292.5 && angle < 337.5) {
                            // STICK_UPRIGHT;
                            if (on8DirectListener != null) on8DirectListener.onUpRight();
                        } else if (angle >= 337.5 || angle < 22.5) {
                            // STICK_RIGHT;
                            on4DirectListener.onRight();
                        } else if (angle >= 22.5 && angle < 67.5) {
                            // STICK_DOWNRIGHT;
                            if (on8DirectListener != null) on8DirectListener.onDownRight();
                        } else if (angle >= 67.5 && angle < 112.5) {
                            // STICK_DOWN;
                            on4DirectListener.onDown();
                        } else if (angle >= 112.5 && angle < 157.5) {
                            // STICK_DOWNLEFT;
                            if (on8DirectListener != null) on8DirectListener.onDownLeft();
                        } else if (angle >= 157.5 && angle < 202.5) {
                            // STICK_LEFT;
                            on4DirectListener.onLeft();
                        } else if (angle >= 202.5 && angle < 247.5) {
                            // STICK_UPLEFT;
                            if (on8DirectListener != null) on8DirectListener.onUpLeft();
                        }
                    } else if (distance <= min_distance && isTouched) {
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

    public void drawStick(MotionEvent arg1) {
        position_x = (int) (arg1.getX() - (params.width / 2));
        position_y = (int) (arg1.getY() - (params.height / 2));
        distance = (float) Math.sqrt(Math.pow(position_x, 2) + Math.pow(position_y, 2));
        angle = (float) cal_angle(position_x, position_y);

        if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
            if (distance <= (params.width / 2) - OFFSET) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
                isTouched = true;
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_MOVE && isTouched) {
            if (distance <= (params.width / 2) - OFFSET) {
                draw.position(arg1.getX(), arg1.getY());
                draw();
            } else if (distance > (params.width / 2) - OFFSET) {
                float x = (float) (Math.cos(Math.toRadians(cal_angle(position_x, position_y))) * ((params.width / 2) - OFFSET));
                float y = (float) (Math.sin(Math.toRadians(cal_angle(position_x, position_y))) * ((params.height / 2) - OFFSET));
                x += (params.width / 2);
                y += (params.height / 2);
                draw.position(x, y);
                draw();
            } else {
                mLayout.removeView(draw);
            }
        } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
            mLayout.removeView(draw);
            isTouched = false;
        }
        // reset stick
        draw();
    }

    public int[] getPosition() {
        if (distance > min_distance && isTouched) {
            return new int[]{position_x, position_y};
        }
        return new int[]{0, 0};
    }

    public int getX() {
        if (distance > min_distance && isTouched) {
            return position_x;
        }
        return 0;
    }

    public int getY() {
        if (distance > min_distance && isTouched) {
            return position_y;
        }
        return 0;
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

    public void setMinimumDistance(int minDistance) {
        min_distance = minDistance;
    }

    public int getMinimumDistance() {
        return min_distance;
    }

    public int get8Direction() {
        if (distance > min_distance && isTouched) {
            if (angle >= 247.5 && angle < 292.5) {
                return STICK_UP;
            } else if (angle >= 292.5 && angle < 337.5) {
                return STICK_UPRIGHT;
            } else if (angle >= 337.5 || angle < 22.5) {
                return STICK_RIGHT;
            } else if (angle >= 22.5 && angle < 67.5) {
                return STICK_DOWNRIGHT;
            } else if (angle >= 67.5 && angle < 112.5) {
                return STICK_DOWN;
            } else if (angle >= 112.5 && angle < 157.5) {
                return STICK_DOWNLEFT;
            } else if (angle >= 157.5 && angle < 202.5) {
                return STICK_LEFT;
            } else if (angle >= 202.5 && angle < 247.5) {
                return STICK_UPLEFT;
            }
        } else if (distance <= min_distance && isTouched) {
            return STICK_NONE;
        }
        return 0;
    }

    public int get4Direction() {
        if (distance > min_distance && isTouched) {
            if (angle >= 225 && angle < 315) {
                return STICK_UP;
            } else if (angle >= 315 || angle < 45) {
                return STICK_RIGHT;
            } else if (angle >= 45 && angle < 135) {
                return STICK_DOWN;
            } else if (angle >= 135 && angle < 225) {
                return STICK_LEFT;
            }
        } else if (distance <= min_distance && isTouched) {
            return STICK_NONE;
        }
        return 0;
    }

    public void setOffset(int offset) {
        OFFSET = offset;
    }

    public int getOffset() {
        return OFFSET;
    }

    public void setStickAlpha(int alpha) {
        STICK_ALPHA = alpha;
        paint.setAlpha(alpha);
    }

    public int getStickAlpha() {
        return STICK_ALPHA;
    }

    public void setLayoutAlpha(int alpha) {
        LAYOUT_ALPHA = alpha;
        mLayout.getBackground().setAlpha(alpha);
    }

    public int getLayoutAlpha() {
        return LAYOUT_ALPHA;
    }

    public void setStickSize(int width, int height) {
        stick = Bitmap.createScaledBitmap(stick, width, height, false);
        stick_width = stick.getWidth();
        stick_height = stick.getHeight();
    }

    public void setStickWidth(int width) {
        stick = Bitmap.createScaledBitmap(stick, width, stick_height, false);
        stick_width = stick.getWidth();
    }

    public void setStickHeight(int height) {
        stick = Bitmap.createScaledBitmap(stick, stick_width, height, false);
        stick_height = stick.getHeight();
    }

    public int getStickWidth() {
        return stick_width;
    }

    public int getStickHeight() {
        return stick_height;
    }

    public void setLayoutSize(int width, int height) {
        params.width = width;
        params.height = height;

        // addition
        draw.center_x = params.width / 2;
        draw.center_y = params.height / 2;
        // end
    }

    public int getLayoutWidth() {
        return params.width;
    }

    public int getLayoutHeight() {
        return params.height;
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

    public void setOn4DirectListener(On4DirectListener on4DirectListener) {
        this.on4DirectListener = on4DirectListener;
    }

    public void setOn8DirectListener(On8DirectListener on8DirectListener) {
        this.on8DirectListener = on8DirectListener;
        this.on4DirectListener = on8DirectListener;
    }

    private void draw() {
        try {
            mLayout.removeView(draw);
        } catch (Exception e) {
        }
        mLayout.addView(draw);
    }

    private class DrawCanvas extends View {
        float x, y;
        float s_x, s_y;
        float center_x, center_y;

        private DrawCanvas(Context mContext) {
            super(mContext);
        }

        @Override
        public void onDraw(Canvas canvas) {
            // check center
            if (isTouched) {
                canvas.drawBitmap(stickShadow, s_x, s_y, paint);
                canvas.drawBitmap(stick, x, y, paint);
            } else {
                int stick_tall = 15;// make user feel sticky
                canvas.drawBitmap(stickShadow, center_x - (shadow_width / 2), center_y - (shadow_height / 2) + stick_tall, paint);
                canvas.drawBitmap(stick, center_x - (stick_width / 2), center_y - (stick_height / 2) - stick_tall, paint);
            }
        }

        private void position(float pos_x, float pos_y) {
            x = pos_x - (stick_width / 2);
            y = pos_y - (stick_height / 2);

            // addition
            float vecPC_x = center_x - pos_x;
            float vecPC_y = center_y - pos_y;
            Log.d("Vec", "(" + vecPC_x + "," + vecPC_y + ")");
            s_x = pos_x - (shadow_width / 2) + vecPC_x / 3;
            s_y = pos_y - (shadow_height / 2) + vecPC_y / 3;
            // end
        }
    }

    interface On8DirectListener extends On4DirectListener{
        void onUpRight();

        void onDownRight();

        void onDownLeft();

        void onUpLeft();
    }

    interface On4DirectListener {
        void onDirect();

        void onNone();

        void onUp();

        void onRight();

        void onDown();

        void onLeft();

        void onFinish();
    }

    enum JoyStickEvent {
        STICK_NONE,
        STICK_UP,
        STICK_UPRIGHT,
        STICK_RIGHT,
        STICK_DOWNRIGHT,
        STICK_DOWN,
        STICK_DOWNLEFT,
        STICK_LEFT,
        STICK_UPLEFT
    }
}
