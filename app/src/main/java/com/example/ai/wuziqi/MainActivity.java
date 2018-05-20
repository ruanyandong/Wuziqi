package com.example.ai.wuziqi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements WuziqiPanel.AnotherGame {

    public static WuziqiPanel mWuziqiPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWuziqiPanel = findViewById(R.id.Wuziqi);
        mWuziqiPanel.setAnotherGameListener(this);
    }

    /**
     * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例。 返回true则显示该menu,false 则不显示;
     * (只会在第一次初始化菜单时调用) Inflate the menu; this adds items to the action bar
     * if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    /**
     * 菜单项被点击时调用，也就是菜单项的监听方法。
     * 通过这几个方法，可以得知，对于Activity，同一时间只能显示和监听一个Menu 对象。
     * <p>
     * true表示该方法执行完毕后，点击事件不会再向下一个事件处理方法传递了。
     * false表示执行完该方法后，点击事件继续向下传递。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.another) {
            mWuziqiPanel.start();
        }
        return true;
    }

    @Override
    public void AnotherGame(String str) {

        AnotherGameDialog dialog = new AnotherGameDialog();

        Bundle bundle = new Bundle();
        bundle.putString("anotherGame", str);

        dialog.setArguments(bundle);

        dialog.show(getFragmentManager(), "dialog");
    }
}
