package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import hu.onlineholdem.restclient.R;

public class MenuActivity extends Activity {

    private static final String TAG = "MenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_layout);
    }

    public void startSinglePlayerGame(View view) {
        Intent intent = new Intent(this, SinglePlayerSettingsActivity.class);
        startActivity(intent);
    }

    public void startMultiPlayerGame(View view) {
        Intent intent = new Intent(this, MultiPlayerActivity.class);
        startActivity(intent);
    }
}
