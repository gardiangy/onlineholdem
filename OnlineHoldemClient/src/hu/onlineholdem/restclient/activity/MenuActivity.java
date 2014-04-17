package hu.onlineholdem.restclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.enums.StartType;

public class MenuActivity extends Activity {

    private static final String TAG = "MenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.menu_layout);
    }

    public void startNewGame(View view) {
        Intent intent = new Intent(this, SinglePlayerSettingsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", StartType.NEW.name());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void loadGame(View view) {
        Intent intent = new Intent(this, SinglePlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("type", StartType.LOAD.name());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    public void showSinglePlayerOptions(View view){
        LinearLayout options = (LinearLayout) findViewById(R.id.singlePlayerOptions);
        if(options.getVisibility() == View.INVISIBLE){
            options.setVisibility(View.VISIBLE);
            LinearLayout loadGameImageView = (LinearLayout) findViewById(R.id.loadGame);
            LinearLayout newGameImageView = (LinearLayout) findViewById(R.id.newGame);
            Animation pulse = AnimationUtils.loadAnimation(this, R.animator.pulse);
            loadGameImageView.startAnimation(pulse);
            newGameImageView.startAnimation(pulse);
        } else {
            options.setVisibility(View.INVISIBLE);
        }

    }

    public void startMultiPlayerGame(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
