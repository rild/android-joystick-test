package rimp.rild.com.android.android_joystick_controler_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView mTextViewPosX, mTextViewPosY, mTextViewAngle, mTextViewDistance, mTextViewDirection;

    JoyStick2 js;
    ImageView joystickSignal;

    JoyStickSurfaceView mJoyStick;

    int testCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_surfaceview_activity);
        Log.d("Main", "set content");

        mTextViewPosX = (TextView)findViewById(R.id.textView1);
        mTextViewPosY = (TextView)findViewById(R.id.textView2);
        mTextViewAngle = (TextView)findViewById(R.id.textView3);
        mTextViewDistance = (TextView)findViewById(R.id.textView4);
        mTextViewDirection = (TextView)findViewById(R.id.textView5);

        mTextViewPosX.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        mJoyStick = (JoyStickSurfaceView) findViewById(R.id.main_joystick);
//        mJoyStick.setOn8DirectListener(new JoyStickSurfaceView.On8DirectListener() {
//            @Override
//            public void onUpRight() {
//                mTextViewDirection.setText("Direction : Up Right");
//            }
//
//            @Override
//            public void onDownRight() {
//                mTextViewDirection.setText("Direction : Down Right");
//            }
//
//            @Override
//            public void onDownLeft() {
//                mTextViewDirection.setText("Direction : Down Left");
//            }
//
//            @Override
//            public void onUpLeft() {
//                mTextViewDirection.setText("Direction : Up Left");
//            }
//
//            @Override
//            public void onDirect(int posX, int posY, float angle, float distance) {
//                mTextViewPosX.setText("X : " + String.valueOf(posX));
//                mTextViewPosY.setText("Y : " + String.valueOf(posY));
//                mTextViewAngle.setText("Angle : " + String.valueOf(angle));
//                mTextViewDistance.setText("Distance : " + String.valueOf(distance));
//            }
//
//            @Override
//            public void onNone() {
//                mTextViewDirection.setText("Direction : Center");
//            }
//
//            @Override
//            public void onUp() {
//                mTextViewDirection.setText("Direction : Up");
//                Log.d("Action", "up");
//            }
//
//            @Override
//            public void onRight() {
//                mTextViewDirection.setText("Direction : Right");
//                Log.d("Action", "right");
//            }
//
//            @Override
//            public void onDown() {
//                mTextViewDirection.setText("Direction : Down");
//                Log.d("Action", "down");
//            }
//
//            @Override
//            public void onLeft() {
//                mTextViewDirection.setText("Direction : Left");
//                Log.d("Action", "left");
//            }
//
//            @Override
//            public void onFinish() {
//                mTextViewPosX.setText("X :");
//                mTextViewPosY.setText("Y :");
//                mTextViewAngle.setText("Angle :");
//                mTextViewDistance.setText("Distance :");
//                mTextViewDirection.setText("Direction :");
//            }
//        });

        mJoyStick.setOnJoystickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStickState direction) {
                // TODO Auto-generated method stub
                mTextViewAngle.setText(" " + String.valueOf(angle) + "°");
                mTextViewDistance.setText(" " + String.valueOf(power) + "%");
                switch (direction) {
                    case UP:
                        testCount++;
                        Log.d("MainEvent", "up");
                        mTextViewDirection.setText(R.string.front_lab + " C:" + testCount);
                        break;
                    case UPRIGHT:
                        mTextViewDirection.setText(R.string.front_right_lab);
                        break;
                    case RIGHT:
                        mTextViewDirection.setText(R.string.right_lab);
                        break;
                    case DOWNRIGHT:
                        mTextViewDirection.setText(R.string.right_bottom_lab);
                        break;
                    case DOWN:
                        mTextViewDirection.setText(R.string.bottom_lab);
                        break;
                    case DOWNLEFT:
                        mTextViewDirection.setText(R.string.bottom_left_lab);
                        break;
                    case LEFT:
                        mTextViewDirection.setText(R.string.left_lab);
                        break;
                    case UPLEFT:
                        mTextViewDirection.setText(R.string.left_front_lab);
                        break;
                    default:
                        testCount = 0;
                        mTextViewDirection.setText(R.string.center_lab);
                }
            }
        }, JoyStickSurfaceView.DEFAULT_LOOP_INTERVAL);
    }
}
