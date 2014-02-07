package hu.onlineholdem.restclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;

public class GraphicStuff {

    private Resources resources;
    private int screenWidth;
    private RelativeLayout seats;
    private int screenHeight;
    private Context context;
    private ImageView flop1;
    private ImageView flop2;
    private ImageView flop3;
    private ImageView turn;
    private ImageView river;
    private Button btnCheck;
    private Button btnBet;
    private Button btnFold;
    private SeekBar betBar;
    private TextView potSize;
    private TextView betValue;
    private RelativeLayout board;
    private String packageName;
    private Game game;
    private Activity activity;
    private List<Player> players;
    private int betAmount;

    public GraphicStuff(Context context) {
        this.context = context;
        activity = (Activity) context;
        flop1 = (ImageView) activity.findViewById(R.id.flop1);
        flop2 = (ImageView) activity.findViewById(R.id.flop2);
        flop3 = (ImageView) activity.findViewById(R.id.flop3);
        turn = (ImageView) activity.findViewById(R.id.turn);
        river = (ImageView) activity.findViewById(R.id.river);
        board = (RelativeLayout) activity.findViewById(R.id.board);
        packageName = activity.getPackageName();
        resources = activity.getResources();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        potSize = (TextView) activity.findViewById(R.id.potSize);
        betValue = (TextView) activity.findViewById(R.id.betValue);
        seats = (RelativeLayout) activity.findViewById(R.id.seats);
        betBar = (SeekBar) activity.findViewById(R.id.betBar);
        btnCheck = (Button) activity.findViewById(R.id.btnCheck);
        btnBet = (Button) activity.findViewById(R.id.btnBet);
        btnFold = (Button) activity.findViewById(R.id.btnFold);
        final TextView betValue = (TextView) activity.findViewById(R.id.betValue);

        betBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                betValue.setText("" + i);
                betAmount = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void dealFlop() {

        int resId = resources.getIdentifier(game.getBoard().get(0).toString(), "drawable", packageName);
        int res2Id = resources.getIdentifier(game.getBoard().get(1).toString(), "drawable", packageName);
        int res3Id = resources.getIdentifier(game.getBoard().get(2).toString(), "drawable", packageName);

        Animation flop1Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 300, 0, screenHeight / 4, true);
        flop1 = new ImageView(context);
        board.addView(flop1);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.setVisibility(View.VISIBLE);
        flop1.startAnimation(flop1Anim);

        Animation flop2Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 200, 0, screenHeight / 4, true);
        flop2 = new ImageView(context);
        board.addView(flop2);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.setVisibility(View.VISIBLE);
        flop2.startAnimation(flop2Anim);

        Animation flop3Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 100, 0, screenHeight / 4, true);
        flop3 = new ImageView(context);
        board.addView(flop3);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.setVisibility(View.VISIBLE);
        flop3.startAnimation(flop3Anim);

    }

    public void deal() {

        for (Player player : players) {

            final int resId = player.isUser() ? resources.getIdentifier(player.getCardOne().toString(), "drawable", packageName)
                    : resources.getIdentifier("back", "drawable", packageName);

            TextView textView = player.getTextView();
            Animation card1Anim = createAnimation(screenWidth / 2, textView.getLeft() + screenWidth / 20, 0, textView.getTop() - screenHeight / 20, true);
            ImageView card1 = new ImageView(context);
            board.addView(card1);
            card1.setAnimation(card1Anim);
            card1.setImageResource(resId);
            card1.startAnimation(card1Anim);
            player.setCard1View(card1);

            final int res2Id = player.isUser() ? resources.getIdentifier(player.getCardTwo().toString(), "drawable", packageName)
                    : resources.getIdentifier("back", "drawable", packageName);

            Animation card2Anim = createAnimation(screenWidth / 2, textView.getLeft() + screenWidth / 13, 0, textView.getTop() - screenHeight / 20, true);
            ImageView card2 = new ImageView(context);
            board.addView(card2);
            card2.setAnimation(card2Anim);
            card2.setImageResource(res2Id);
            card2.startAnimation(card2Anim);
            player.setCard2View(card2);

        }

    }


    public Animation createAnimation(int xFrom, int xTo, int yFrom, int yTo, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, xFrom, Animation.ABSOLUTE, xTo, Animation.ABSOLUTE, yFrom, Animation.ABSOLUTE, yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(fillAfter);

        return translateAnimation;
    }


    public Position getPlayerPostion(int order) {
        switch (order) {
            case 1:
                return new Position(screenWidth / 6 * 5, screenHeight / 14);
            case 2:
                return new Position(screenWidth / 10 * 8, screenHeight / 11 * 3);
            case 3:
                return new Position(screenWidth / 3 * 2, screenHeight / 7 * 3);
            case 4:
                return new Position(screenWidth / 5 * 2, screenHeight / 7 * 3);
            case 5:
                return new Position(screenWidth / 6, screenHeight / 7 * 3);
            case 6:
                return new Position(screenWidth / 40, screenHeight / 11 * 3);
            case 7:
                return new Position(screenWidth / 18, screenHeight / 14);
        }
        return null;
    }

    public void showCurrentPlayer() {
        for (Player player : players) {
            if (player.isPlayerTurn()) {
                player.getTextView().setBackgroundResource(R.drawable.seatactive);
                if (player.isUser()) {
                    showActionButtons(true);
                } else {
                    showActionButtons(false);
                }
            } else {
                player.getTextView().setBackgroundResource(R.drawable.seatnotactive);
            }
        }
    }

    public void moveBet(int amount, long playerId) {
        Player player = null;
        for (Player pl : players) {
            if (pl.getPlayerId().equals(playerId)) {
                player = pl;
            }
        }

        if (null != player.getChipLayout()) {
            TextView existingChipsTextViw = (TextView) player.getChipLayout().getChildAt(0);
            existingChipsTextViw.setText(player.getBetAmount() + amount + "");
        } else {
            RelativeLayout relativeLayout = new RelativeLayout(context);
            TextView chipsTextView = new TextView(context);
            chipsTextView.setTextSize(15);
            chipsTextView.setText(amount + "");
            relativeLayout.addView(chipsTextView);
            ImageView chipsImageView = new ImageView(context);
            chipsImageView.setImageResource(R.drawable.chips);
            relativeLayout.addView(chipsImageView);


            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 12, screenHeight / 18);
            Position position = getChipsPosition(player);
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
            relativeLayout.setLayoutParams(layoutParams);

            board.addView(relativeLayout);
            player.setChipLayout(relativeLayout);
        }

        player.getTextView().setText(player.getPlayerName() + "\n" + player.getStackSize().toString());


    }

    public void moveFold(long playerId) {
        Player player = null;
        for (Player pl : players) {
            if (pl.getPlayerId().equals(playerId)) {
                player = pl;
            }
        }
        Animation card1Anim = createAnimation(player.getTextView().getLeft() + screenWidth / 20, screenWidth / 2, player.getTextView().getTop() - screenHeight / 20, 0, false);
        player.getCard1View().setAnimation(card1Anim);

        Animation card2Anim = createAnimation(player.getTextView().getLeft() + screenWidth / 13, screenWidth / 2, player.getTextView().getTop() - screenHeight / 20, 0, false);
        player.getCard2View().setAnimation(card2Anim);
        player.getCard1View().startAnimation(player.getCard1View().getAnimation());
        board.removeView(player.getCard1View());

        player.getCard2View().startAnimation(player.getCard2View().getAnimation());
        board.removeView(player.getCard2View());

    }

    public Position getChipsPosition(Player player) {
        switch (player.getOrder()) {
            case 1:
                return new Position(player.getTextView().getLeft() - 70, player.getTextView().getTop() + 20);
            case 2:
                return new Position(player.getTextView().getLeft() - 50, player.getTextView().getTop() - 20);
            case 3:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 4:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 5:
                return new Position(player.getTextView().getLeft() + 50, player.getTextView().getTop() - 100);
            case 6:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
            case 7:
                return new Position(player.getTextView().getLeft() + 220, player.getTextView().getTop() + 20);
        }
        return null;
    }


    public void createPlayers() {
        players = new ArrayList<>();
        players.addAll(game.getPlayers());
        for (Player player : players) {

            TextView textView = new TextView(context);
            textView.setBackgroundResource(R.drawable.seatnotactive);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 5, screenHeight / 6);
            Position position = getPlayerPostion(player.getOrder());
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);

            textView.setTop(position.getTop());
            textView.setLeft(position.getLeft());
            textView.setLayoutParams(layoutParams);
            textView.setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(0xFF000000);
            textView.setTextSize(15);

            player.setTextView(textView);

            seats.addView(textView);
        }
    }

    public void showActionButtons(final boolean show) {
        if (show) {
            btnCheck.setVisibility(View.VISIBLE);
            btnBet.setVisibility(View.VISIBLE);
            btnFold.setVisibility(View.VISIBLE);
            betBar.setVisibility(View.VISIBLE);
            betValue.setVisibility(View.VISIBLE);
        } else {
            btnCheck.setVisibility(View.INVISIBLE);
            btnBet.setVisibility(View.INVISIBLE);
            btnFold.setVisibility(View.INVISIBLE);
            betBar.setVisibility(View.INVISIBLE);
            betValue.setVisibility(View.INVISIBLE);
            betValue.setText("");
        }


    }

    public void updatePlayers(List<Player> newPlayers){
        for(Player player : players){
            for(Player newPlayer : newPlayers){
                if(player.getPlayerId().equals(newPlayer.getPlayerId())){
                    player.setPlayerTurn(newPlayer.isPlayerTurn());
                    player.setPlayerInTurn(newPlayer.getPlayerInTurn());
                    player.setStackSize(newPlayer.getStackSize());
                }
            }
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
