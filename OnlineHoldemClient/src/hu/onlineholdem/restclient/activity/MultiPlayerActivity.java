package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import hu.onlineholdem.restclient.R;

public class MultiPlayerActivity  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.multi_player_layout);
    }
}
