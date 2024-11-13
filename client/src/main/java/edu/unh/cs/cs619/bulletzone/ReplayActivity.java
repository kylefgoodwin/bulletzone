package edu.unh.cs.cs619.bulletzone;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_replay)
public class ReplayActivity extends Activity {

    private static final String TAG = "ReplayActivity";

    @Bean
    ReplayController replayController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
    }

    @Click(R.id.replayTestButton)
    void testReplay() {
        Intent intent = new Intent(this, ReplayInstanceActivity_.class);
        startActivity(intent);
        finish();
    }

    @Click(R.id.backToMenuButton)
    void backToMenu() {
        Intent intent = new Intent(this, MenuActivity_.class);
        startActivity(intent);
        finish();
    }

}
