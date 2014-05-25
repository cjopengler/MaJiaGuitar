package com.majia.guitar;


import com.majia.guitar.ui.MusicFragment;
import com.majia.guitar.ui.TitleBarFragment;
import com.majia.guitar.ui.TitleBarFragment.Args;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class MaJiaGuitarActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_ma_jia_guitar);
        
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.content_container, MusicFragment.newInstance());
        
        Args titleBarArgs = Args.buidArgs()
                                .setTitle(R.string.yoga_guitar)
                                .setShowBack(false);
        
        TitleBarFragment titleBarFragment = TitleBarFragment.newInstance(titleBarArgs);
        
        ft.add(R.id.titleBarContainer, titleBarFragment);
        ft.commit();
        
    }

}
