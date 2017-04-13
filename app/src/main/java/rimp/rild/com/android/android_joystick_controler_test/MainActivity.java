package rimp.rild.com.android.android_joystick_controler_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView mTextViewState;
    TextView mTextViewAngle, mTextViewDistance, mTextViewDirection;

    JoyStick2 js;
    ImageView joystickSignal;

    JoyStickSurfaceView mJoyStick;

    int testCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_surfaceview_activity);
        mTextViewState = (TextView) findViewById(R.id.text_view_state);
        mTextViewAngle = (TextView) findViewById(R.id.textView3);
        mTextViewDistance = (TextView) findViewById(R.id.textView4);
        mTextViewDirection = (TextView) findViewById(R.id.textView5);

        mJoyStick = (JoyStickSurfaceView) findViewById(R.id.main_joystick);

        mJoyStick.setOnJoyStickMoveListener(new JoyStickSurfaceView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(float angle, float power, JoyStickSurfaceView.JoyStick state) {
                // TODO Auto-generated method stub
                mTextViewAngle.setText(" " + String.valueOf(angle) + "°");
                mTextViewDistance.setText(" " + String.valueOf(power) + "%");
                switch (state) {
                    case MORE_UP:
                    case UP:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.front_lab) + " C:" + testCount);
                        break;
                    case MORE_UPRIGHT:
                    case UPRIGHT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.front_right_lab) + " C:" + testCount);
                        break;
                    case MORE_RIGHT:
                    case RIGHT:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.right_lab) + " C:" + testCount);
                        break;
                    case MORE_DOWNRIGHT:
                    case DOWNRIGHT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.right_bottom_lab) + " C:" + testCount);
                        break;
                    case MORE_DOWN:
                    case DOWN:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.bottom_lab) + " C:" + testCount);
                        break;
                    case MORE_DOWNLEFT:
                    case DOWNLEFT:
                        mTextViewDirection.setText(
                                getResources().getString(R.string.bottom_left_lab) + " C:" + testCount);
                        break;
                    case MORE_LEFT:
                    case LEFT:
                        testCount++;
                        mTextViewDirection.setText(
                                getResources().getString(R.string.left_lab) + " C:" + testCount);
                        break;
                    case MORE_UPLEFT:
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
            public void onChangeState(JoyStickSurfaceView.JoyStick next,
                                      JoyStickSurfaceView.JoyStick previous) {
                if (testCount > 1) {
                    if ((!JoyStickSurfaceView.JoyStick.
                            isMore(next, previous)))
                        if ((!JoyStickSurfaceView.JoyStick.
                                isLess(next, previous)))
                            testCount = 0;
                }
                mTextViewState.setText(String.valueOf(next));
            }
        });
    }
}
