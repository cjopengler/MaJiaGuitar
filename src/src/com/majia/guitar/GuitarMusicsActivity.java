package com.majia.guitar;



import com.baidu.android.pushservice.CustomPushNotificationBuilder;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.majia.guitar.ui.MainTitleBarFragment;
import com.majia.guitar.ui.MainTitleBarFragment.Args;
import com.majia.guitar.util.push.PushUtils;

import android.app.Notification;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class GuitarMusicsActivity extends FragmentActivity {
    private static final String TAG = "MaJiaGuitarActivity";
    public static final String NEW_MUSIC_ACTION = "com.majia.guitar.GuitarMusicsActivity.NEW_MUSIC_ACTION";
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.guitar_music_activity);
        
        if (savedInstanceState == null) {
        
	        FragmentManager fragmentManager = getSupportFragmentManager();
	        FragmentTransaction ft = fragmentManager.beginTransaction();
	        
	        Args titleBarArgs = Args.buidArgs()
	                                .setTitle(R.string.app_name)
	                                .setShowBack(false);
	        
	        MainTitleBarFragment titleBarFragment = MainTitleBarFragment.newInstance(titleBarArgs);
	        
	        ft.add(R.id.titleBarContainer, titleBarFragment);
	        ft.commit();
        }
        
        initPush();
    }
    
    private void initPush() {
     // Push: 以apikey的方式登录，一般放在主Activity的onCreate中。
        // 这里把apikey存放于manifest文件中，只是一种存放方式，
        // 您可以用自定义常量等其它方式实现，来替换参数中的Utils.getMetaValue(PushDemoActivity.this,
        // "api_key")
        // 通过share preference实现的绑定标志开关，如果已经成功绑定，就取消这次绑定
        
       /* if (!PushUtils.hasBind(getApplicationContext())) {
            PushManager.startWork(getApplicationContext(),
                    PushConstants.LOGIN_TYPE_API_KEY,
                    PushUtils.getMetaValue(GuitarMusicsActivity.this, "api_key"));
            // Push: 如果想基于地理位置推送，可以打开支持地理位置的推送的开关
            // PushManager.enableLbs(getApplicationContext());
        }
*/
        // Push: 设置自定义的通知样式，具体API介绍见用户手册，如果想使用系统默认的可以不加这段代码
        // 请在通知推送界面中，高级设置->通知栏样式->自定义样式，选中并且填写值：1，
        // 与下方代码中 PushManager.setNotificationBuilder(this, 1, cBuilder)中的第二个参数对应
        Resources resource = getResources();
        String pkgName = this.getPackageName();
        CustomPushNotificationBuilder cBuilder = new CustomPushNotificationBuilder(
                getApplicationContext(), resource.getIdentifier(
                        "notification_custom_builder", "layout", pkgName),
                resource.getIdentifier("notification_icon", "id", pkgName),
                resource.getIdentifier("notification_title", "id", pkgName),
                resource.getIdentifier("notification_text", "id", pkgName));
        cBuilder.setNotificationFlags(Notification.FLAG_AUTO_CANCEL);
        cBuilder.setNotificationDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE);
        cBuilder.setStatusbarIcon(this.getApplicationInfo().icon);
        cBuilder.setLayoutDrawable(resource.getIdentifier(
                "simple_notification_icon", "drawable", pkgName));
        PushManager.setNotificationBuilder(this, 1, cBuilder);
    }

}
