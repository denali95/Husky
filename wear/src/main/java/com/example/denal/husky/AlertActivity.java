package com.example.denal.husky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Created by denal on 11/3/2017.
 */

public class AlertActivity extends Activity implements View.OnClickListener{

    ImageButton happy, neutral, unhappy, miserable;
    int heart_rate;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_activity);
        happy = findViewById(R.id.happy);
        neutral = findViewById(R.id.neutral);
        unhappy = findViewById(R.id.unhappy);
        miserable = findViewById(R.id.miserable);
        happy.setOnClickListener(this);
        neutral.setOnClickListener(this);
        unhappy.setOnClickListener(this);
        miserable.setOnClickListener(this);
        Intent intent = getIntent();
        if (intent != null) {
            heart_rate = intent.getIntExtra("heart", 0);
            Toast.makeText(this, "hr" + heart_rate, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "How do you feel?", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.happy) {
            Toast.makeText(this, "Good", Toast.LENGTH_SHORT).show();
        }
        else if (view.getId() == R.id.neutral) {
            Toast.makeText(this, "Neutral", Toast.LENGTH_SHORT).show();
        }
        else if (view.getId() == R.id.unhappy) {
            Toast.makeText(this, "Bad", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HelpActivity.class);
            Bundle b = new Bundle();
            b.putString("emotion", "bad");
            b.putInt("heart", heart_rate);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        }
        else if (view.getId() == R.id.miserable) {
            Toast.makeText(this, "Miserable", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HelpActivity.class);
            Bundle b = new Bundle();
            b.putString("emotion", "miserable");
            b.putInt("heart", heart_rate);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        }
    }
}
