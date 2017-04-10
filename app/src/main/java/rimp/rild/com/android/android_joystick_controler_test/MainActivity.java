package rimp.rild.com.android.android_joystick_controler_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;

    JoyStick2 js;
    ImageView joystickSignal;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView5 = (TextView)findViewById(R.id.textView5);

        layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);
        joystickSignal = (ImageView) findViewById(R.id.signal_layer);

        js = new JoyStick2(getApplicationContext()
                , layout_joystick, R.drawable.joystick_stick);
        js.init();

        js.setOn8DirectListener(new JoyStick2.On8DirectListener() {
            @Override
            public void onUpRight() {
                textView5.setText("Direction : Up Right");
            }

            @Override
            public void onDownRight() {
                textView5.setText("Direction : Down Right");
            }

            @Override
            public void onDownLeft() {
                textView5.setText("Direction : Down Left");
            }

            @Override
            public void onUpLeft() {
                textView5.setText("Direction : Up Left");
            }

            @Override
            public void onDirect(int posX, int posY, float angle, float distance) {
                textView1.setText("X : " + String.valueOf(posX));
                textView2.setText("Y : " + String.valueOf(posY));
                textView3.setText("Angle : " + String.valueOf(angle));
                textView4.setText("Distance : " + String.valueOf(distance));
            }

            @Override
            public void onNone() {
                if (joystickSignal != null) joystickSignal.setBackgroundResource(0);
                textView5.setText("Direction : Center");
            }

            @Override
            public void onUp() {
                textView5.setText("Direction : Up");
                if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_up);
                Log.d("Action", "up");
            }

            @Override
            public void onRight() {
                if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_right);
                textView5.setText("Direction : Right");
            }

            @Override
            public void onDown() {
                if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_down);
                textView5.setText("Direction : Down");
            }

            @Override
            public void onLeft() {
                if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_left);
                textView5.setText("Direction : Left");
            }

            @Override
            public void onFinish() {
                if (joystickSignal != null) joystickSignal.setBackgroundResource(0);
                textView1.setText("X :");
                textView2.setText("Y :");
                textView3.setText("Angle :");
                textView4.setText("Distance :");
                textView5.setText("Direction :");
            }
        });

//        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                js.drawStick(arg1);
//                if(arg1.getAction() == MotionEvent.ACTION_DOWN
//                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
//                    textView1.setText("X : " + String.valueOf(js.getX()));
//                    textView2.setText("Y : " + String.valueOf(js.getY()));
//                    textView3.setText("Angle : " + String.valueOf(js.getAngle()));
//                    textView4.setText("Distance : " + String.valueOf(js.getDistance()));
//
//                    int direction = js.get8Direction();
//                    if(direction == JoyStick.STICK_UP) {
//                        textView5.setText("Direction : Up");
//                        if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_up);
//                        Log.d("Action", "up");
//                    } else if(direction == JoyStick.STICK_UPRIGHT) {
//                        textView5.setText("Direction : Up Right");
//                    } else if(direction == JoyStick.STICK_RIGHT) {
//                        if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_right);
//                        textView5.setText("Direction : Right");
//                    } else if(direction == JoyStick.STICK_DOWNRIGHT) {
//                        textView5.setText("Direction : Down Right");
//                    } else if(direction == JoyStick.STICK_DOWN) {
//                        if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_down);
//                        textView5.setText("Direction : Down");
//                    } else if(direction == JoyStick.STICK_DOWNLEFT) {
//                        textView5.setText("Direction : Down Left");
//                    } else if(direction == JoyStick.STICK_LEFT) {
//                        if (joystickSignal != null) joystickSignal.setBackgroundResource(R.drawable.signal_left);
//                        textView5.setText("Direction : Left");
//                    } else if(direction == JoyStick.STICK_UPLEFT) {
//                        textView5.setText("Direction : Up Left");
//                    } else if(direction == JoyStick.STICK_NONE) {
//                        if (joystickSignal != null) joystickSignal.setBackgroundResource(0);
//                        textView5.setText("Direction : Center");
//                    }
//                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
//                    if (joystickSignal != null) joystickSignal.setBackgroundResource(0);
//                    textView1.setText("X :");
//                    textView2.setText("Y :");
//                    textView3.setText("Angle :");
//                    textView4.setText("Distance :");
//                    textView5.setText("Direction :");
//                }
//                return true;
//            }
//        });
    }
}
