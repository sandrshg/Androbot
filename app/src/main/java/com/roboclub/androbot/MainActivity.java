package com.roboclub.androbot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RG);
        Button nxt = (Button) findViewById(R.id.button);
        nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkId = radioGroup.getCheckedRadioButtonId();
                RadioButton sel1 = (RadioButton) findViewById(checkId);
                int checkedId = radioGroup.indexOfChild(sel1);
                switch (checkedId) {
                    case 0: {
                        Intent intent = new Intent("com.roboclub.androbot.Location");
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        Intent intent = new Intent("com.roboclub.androbot.Steer");
                        startActivity(intent);
                        break;
                    }
                    case 2: {
                        Intent intent = new Intent("com.roboclub.androbot.AutoControl");
                        startActivity(intent);
                        break;
                    }
                    default:
                }
            }
        });
    }
}

