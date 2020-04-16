package com.example.androble;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        TextView textView = findViewById(R.id.textView);
        textView.setText(getIntent().getStringExtra("param"));


        Toolbar mToolbar = (Toolbar) findViewById(R.id.tbDetailMbl);
        mToolbar.setTitle(getString(R.string.motor_detail));
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}