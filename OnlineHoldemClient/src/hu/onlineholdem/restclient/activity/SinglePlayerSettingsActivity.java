package hu.onlineholdem.restclient.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.NumberPicker;

import hu.onlineholdem.restclient.R;

public class SinglePlayerSettingsActivity extends Activity {

    private NumberPicker playerNumPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.single_player_settings_layout);
        playerNumPicker = (NumberPicker) findViewById(R.id.playerNumPicker);
        playerNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        playerNumPicker.setMaxValue(9);
        playerNumPicker.setMinValue(2);

        NumberPicker difficultyPicker = (NumberPicker) findViewById(R.id.difficultyPicker);
        difficultyPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        difficultyPicker.setMaxValue(3);
        difficultyPicker.setMinValue(1);

        String[] diffs = new String[3];
        diffs[0] = "Easy";
        diffs[1] = "Normal";
        diffs[2] = "Hard";

        difficultyPicker.setDisplayedValues(diffs);

    }


    public void startGame(View view) {
        Intent intent = new Intent(this, SinglePlayerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("numOfPlayers", playerNumPicker.getValue());
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
