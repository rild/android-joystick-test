# About

This project is a test app for Joy Stick like controller of android apps.

[JoyStickSurfaceView.java]( ./app/src/main/java/rimp/rild/com/android/android_joystick_controler_test/JoyStickSurfaceView.java) has all of that.
 
 You can get these events from JoyStickSurfaceView with event listeners
 
 - OnLongPushListener
    - onLongPush()
 - OnChangeStateListener
    - onChangeState(JoyStick next, JoyStick previous)
 - OnJoystickMoveListener
    - onValueChanged(float angle, float power, JoyStick state)
   
 JoyStick is enum for value of JoyStick Sate
 
 ```
 enum JoyStick {
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
 ```
 
押す感じが楽しい

# Memos

### 動作速度改善 20170411

[<img width=350 src="https://gyazo.com/ab7786d2d2cd278c62f532ce6a1e3769.png"/>](https://youtu.be/45rPExCzwIQ)

[position バグ修正前](https://youtu.be/hu9dCQL4hhA)

### コピー 20170410

[<img width=350 src="https://gyazo.com/e1be98a0ab3aca90676163674fd27bfb.png">](https://youtu.be/FuGfqBKRkes)

# 開発ログ

`isInEditMode()`

http://tech.admax.ninja/2014/10/06/how-to-avoid-the-exception-of-preview-when-customview/


## メモリ

ヒープ領域使っちゃう？
http://qiita.com/kazuqqfp/items/caeea59df51802479253
44.86 MB/ 57.74 MB
コントローラーだけでこれはまずいでしょ...

<img src="https://gyazo.com/2fa06d4fb15ab37699c7e4e50a5f9080.png" />

リサイズ後のアドレスをリリースした (signal on)




