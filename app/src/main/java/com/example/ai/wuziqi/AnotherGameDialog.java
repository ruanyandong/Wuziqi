package com.example.ai.wuziqi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;


public class AnotherGameDialog extends DialogFragment {

    private String mAnotherGame;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle=getArguments();
        mAnotherGame=bundle.getString("anotherGame");

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setMessage(mAnotherGame+",是否再来一局？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MainActivity.mWuziqiPanel.start();
            }
        });
        builder.setNegativeButton("取消",null);

        return builder.create();//将构建的dialog对象返回


    }
}
