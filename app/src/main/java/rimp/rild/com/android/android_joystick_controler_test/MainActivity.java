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

        mJoyStick.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick direction) {
                // TODO Auto-generated method stub
                mTextViewAngle.setText(" " + String.valueOf(angle) + "°");
                mTextViewDistance.setText(" " + String.valueOf(power) + "%");
                switch (direction) {
                    case UP:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.front_lab) + " C:" + testCount);
                        break;
                    case UPRIGHT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.front_right_lab) + " C:" + testCount);
                        break;
                    case RIGHT:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.right_lab) + " C:" + testCount);
                        break;
                    case DOWNRIGHT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.right_bottom_lab) + " C:" + testCount);
                        break;
                    case DOWN:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.bottom_lab) + " C:" + testCount);
                        break;
                    case DOWNLEFT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.bottom_left_lab) + " C:" + testCount);
                        break;
                    case LEFT:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.left_lab) + " C:" + testCount);
                        break;
                    case UPLEFT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.left_front_lab) + " C:" + testCount);
                        break;
                    default:
                        testCount = 0;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.center_lab) + " C:" + testCount);
                }
            }
        }, JoyStickSurfaceView.LOOP_INTERVAL_SLOW, JoyStickSurfaceView.LOOP_INTERVAL_FAST);

        mJoyStick.setOnLongPushListener(new JoyStickSurfaceView.OnLongPushListener() {
            @Override
            public void onLongPush() {
                Log.d("MainEvent", "long pushed");
                mTextViewDirection.setText("Joy Stick Long Pushed!");
            }
        });

        mJoyStick.setOnChangeStateListener(new JoyStickSurfaceView.OnChangeStateListener() {
            @Override
            public void onChangeState(JoyStickSurfaceView.JoyStick next, JoyStickSurfaceView.JoyStick previous) {
                if (testCount > 1) testCount = 0;
            }
        });
    }
}
