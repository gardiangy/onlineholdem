package hu.onlineholdem.restclient.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
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
import java.util.Random;

import hu.onlineholdem.restclient.R;
import hu.onlineholdem.restclient.activity.GameBrowserActivity;
import hu.onlineholdem.restclient.entity.Action;
import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Game;
import hu.onlineholdem.restclient.entity.Player;
import hu.onlineholdem.restclient.enums.ActionType;

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
    private int minBet;
    private RelativeLayout dealerLayout;

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
                betValue.setText("" + (i + minBet));
                betAmount = i + minBet;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void dealFlop() {

        int resId = resources.getIdentifier(game.getBoard().get(0).toString(), "drawable", packageName);
        int res2Id = resources.getIdentifier(game.getBoard().get(1).toString(), "drawable", packageName);
        int res3Id = resources.getIdentifier(game.getBoard().get(2).toString(), "drawable", packageName);

        Animation flop1Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 300, 0, screenHeight / 4, true);
        flop1Anim.setStartOffset(2000);
        flop1 = new ImageView(context);
        board.addView(flop1);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.setVisibility(View.VISIBLE);
        flop1.startAnimation(flop1Anim);

        Animation flop2Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 200, 0, screenHeight / 4, true);
        flop2Anim.setStartOffset(2000);
        flop2 = new ImageView(context);
        board.addView(flop2);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.setVisibility(View.VISIBLE);
        flop2.startAnimation(flop2Anim);

        Animation flop3Anim = createAnimation(screenWidth / 2, screenWidth / 2 - 100, 0, screenHeight / 4, true);
        flop3Anim.setStartOffset(2000);
        flop3 = new ImageView(context);
        board.addView(flop3);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.setVisibility(View.VISIBLE);
        flop3.startAnimation(flop3Anim);

    }

    public void dealTurn() {

        int resId = resources.getIdentifier(game.getBoard().get(3).toString(), "drawable", packageName);

        Animation turnAnim = createAnimation(screenWidth / 2, screenWidth / 2, 0, screenHeight / 4, true);
        turn = new ImageView(context);
        board.addView(turn);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        turn.startAnimation(turnAnim);

    }

    public void dealRiver() {

        int resId = resources.getIdentifier(game.getBoard().get(4).toString(), "drawable", packageName);

        Animation riverAnim = createAnimation(screenWidth / 2, screenWidth / 2 + 100, 0, screenHeight / 4, true);
        river = new ImageView(context);
        board.addView(river);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        river.startAnimation(riverAnim);

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

    public void showCards(){
        for(Player player : players){
            if(!player.isUser()){
                final int resId = resources.getIdentifier(player.getCardOne().toString(), "drawable", packageName);
                final int res2Id = resources.getIdentifier(player.getCardTwo().toString(), "drawable", packageName);
                player.getCard2View().setImageResource(res2Id);
                player.getCard1View().setImageResource(resId);

            }
        }
        board.invalidate();
    }

    public void endRound() {

        for(Player player : players){
            player.getTextView().setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
        }

        List<Player> playerList = new ArrayList<>();
        playerList.addAll(players);
        for (Player player : playerList) {
            if (player.getStackSize() == 0) {
                seats.removeView(player.getTextView());
                players.remove(player);
            }
            board.removeView(player.getCard1View());
            board.removeView(player.getCard2View());
            player.setCard1View(null);
            player.setCard2View(null);
            player.setAmountInPot(0);
        }
        for (RelativeLayout chip : game.getPotChips()) {
            board.removeView(chip);
        }
        board.removeView(flop1);
        board.removeView(flop2);
        board.removeView(flop3);
        board.removeView(turn);
        board.removeView(river);
        game.setBoard(new ArrayList<Card>());


    }

    public void assignChips(List<Player> winners){
        if (winners.size() > 1) {
//            List<List<RelativeLayout>> chipsList = splitChips(game.getPotChips(), winners.size());
//
//            int splitPotAmount = game.getPotSize() / winners.size();
//
//            List<Player> winnerList = new ArrayList<>();
//            winnerList.addAll(winners);
//
//            for (Player winner : winnerList) {
//                if(winner.getAmountToWin() <= splitPotAmount){
//                    game.setPotSize(game.getPotSize() - winner.getAmountToWin());
//                    List<List<RelativeLayout>> layoutList = new ArrayList<>();
//                    layoutList.addAll(chipsList);
//                    for (List<RelativeLayout> relativeLayouts : layoutList) {
//                        for (RelativeLayout chips : relativeLayouts) {
//                            chips.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
//                        }
//                        chipsList.remove(relativeLayouts);
//                    }
//                    winner.setStackSize(winner.getStackSize() + winner.getAmountToWin());
//                    winners.remove(winner);
//                }
//            }
//            for (Player winner : winners) {
//                List<List<RelativeLayout>> layoutList = new ArrayList<>();
//                layoutList.addAll(chipsList);
//                for (List<RelativeLayout> relativeLayouts : layoutList) {
//                    for (RelativeLayout chips : relativeLayouts) {
//                        chips.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
//                    }
//                    chipsList.remove(relativeLayouts);
//                }
//                winner.setStackSize(winner.getStackSize() + game.getPotSize() / winners.size());
//            }
        } else {
            if(winners.get(0).getAmountToWin() >= game.getPotSize()){
                for (final RelativeLayout chips : game.getPotChips()) {
                    chips.animate().setDuration(500).x(winners.get(0).getTextView().getLeft()).y(winners.get(0).getTextView().getTop());
                }
            }
            else {
                game.getPotChips().get(0).animate().setDuration(500).x(winners.get(0).getTextView().getLeft()).y(winners.get(0).getTextView().getTop());
                game.getPotChips().remove(0);
                game.setPotSize(game.getPotSize() - winners.get(0).getAmountToWin());
                List<Player> remainingPotWinners = new ArrayList<>();
                remainingPotWinners.addAll(winners);
                remainingPotWinners.remove(winners.get(0));
                if(remainingPotWinners.size() > 0){
//                    assignChips(remainingPotWinners);
                }

            }

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

    public void showCurrentPlayer(Player currentPlayer) {
        currentPlayer.getTextView().setBackgroundResource(R.drawable.seatactive);
        for (Player player : players) {
            if (!player.equals(currentPlayer)) {
                player.getTextView().setBackgroundResource(R.drawable.seatnotactive);
            }
        }
        if (currentPlayer.isUser()) {
            betBar.setMax(currentPlayer.getStackSize() - minBet);
            betBar.setProgress(0);
        }
    }

    public void moveBet(int amount, long playerId) {

        Player actionPlayer = null;
        for (Player player : players) {
            if (player.getPlayerId() == playerId) {
                actionPlayer = player;
                break;
            }
        }

        if (null != actionPlayer.getChipLayout()) {
            TextView existingChipsTextViw = (TextView) actionPlayer.getChipLayout().getChildAt(0);
            existingChipsTextViw.setText(amount + "");
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
            Position position = getChipsPosition(actionPlayer);
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
            relativeLayout.setLayoutParams(layoutParams);

            board.addView(relativeLayout);
            actionPlayer.setChipLayout(relativeLayout);
        }

        actionPlayer.getTextView().setText(actionPlayer.getPlayerName() + "\n" + actionPlayer.getStackSize().toString());


    }

    public void moveFold(long playerId) {

        Player actionPlayer = null;
        for (Player player : players) {
            if (player.getPlayerId() == playerId) {
                actionPlayer = player;
                break;
            }
        }

        Animation card1Anim = createAnimation(actionPlayer.getTextView().getLeft() + screenWidth / 20, screenWidth / 2, actionPlayer.getTextView().getTop() - screenHeight / 20, 0, false);
        actionPlayer.getCard1View().setAnimation(card1Anim);

        Animation card2Anim = createAnimation(actionPlayer.getTextView().getLeft() + screenWidth / 13, screenWidth / 2, actionPlayer.getTextView().getTop() - screenHeight / 20, 0, false);
        actionPlayer.getCard2View().setAnimation(card2Anim);
        actionPlayer.getCard1View().startAnimation(actionPlayer.getCard1View().getAnimation());
        board.removeView(actionPlayer.getCard1View());

        actionPlayer.getCard2View().startAnimation(actionPlayer.getCard2View().getAnimation());
        board.removeView(actionPlayer.getCard2View());

    }

    public void collectChips(List<Player> playersInRound) {


        for (Player player : playersInRound) {
            if (null != player.getChipLayout()) {
                int chipsShiftX = new Random().nextInt(100) - 50;
                int chipsShiftY = new Random().nextInt(20) - 10;
                TextView chipsText = (TextView) player.getChipLayout().getChildAt(0);
                chipsText.setText("");
                player.getChipLayout().animate().x(screenWidth / 2 + chipsShiftX).y(screenHeight / 7 + chipsShiftY);
                game.getPotChips().add(player.getChipLayout());
            }
            player.setChipLayout(null);
            player.setBetAmount(0);
        }
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

    public Position getDealerBtnPosition(Player player) {
        switch (player.getPosition()) {
            case 1:
                return new Position(player.getTextView().getLeft() - getPixels(10), player.getTextView().getTop() + getPixels(60));
            case 2:
                return new Position(player.getTextView().getLeft() + getPixels(10), player.getTextView().getTop() - getPixels(20));
            case 3:
                return new Position(player.getTextView().getLeft() + getPixels(10), player.getTextView().getTop() - getPixels(20));
            case 4:
                return new Position(player.getTextView().getLeft() + getPixels(10), player.getTextView().getTop() - getPixels(20));
            case 5:
                return new Position(player.getTextView().getLeft() + getPixels(10), player.getTextView().getTop() - getPixels(20));
            case 6:
                return new Position(player.getTextView().getLeft() + getPixels(10), player.getTextView().getTop() - getPixels(20));
            case 7:
                return new Position(player.getTextView().getLeft() + getPixels(120), player.getTextView().getTop() - getPixels(20));
            case 8:
                return new Position(player.getTextView().getLeft() + getPixels(120), player.getTextView().getTop() - getPixels(20));
            case 9:
                return new Position(player.getTextView().getLeft() + getPixels(100), player.getTextView().getTop() + getPixels(60));
        }
        return null;
    }

    public int getPixels(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) dp, context.getResources().getDisplayMetrics());
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

    public void addDealer(){
        dealerLayout = new RelativeLayout(context);
        ImageView dealerBtn = new ImageView(context);
        dealerBtn.setImageResource(R.drawable.dealer);
        dealerBtn.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 20, screenHeight / 20);
        Position position = getDealerBtnPosition(game.getDealer());
        layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
        dealerLayout.setLayoutParams(layoutParams);
        dealerLayout.addView(dealerBtn);
        board.addView(dealerLayout);
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

    public void moveDealer(){
        Position dealerPos = getDealerBtnPosition(game.getDealer());
        dealerLayout.animate().setDuration(500).x(dealerPos.getLeft()).y(dealerPos.getTop());
    }

    public void showAvailableActionButtons(Action lastAction, Action highestBetAction, boolean roundOver) {
        List<ActionType> availableActions = new ArrayList<>();
        if (null == lastAction || roundOver) {
            availableActions.add(ActionType.CHECK);
            availableActions.add(ActionType.BET);
            availableActions.add(ActionType.FOLD);
        } else {
            Boolean higherStackThanBetAmount = highestBetAction == null ? null : game.getUser().getStackSize() > highestBetAction.getBetValue();

            availableActions = getAvailableActions(lastAction.getActionType(), highestBetAction == null ? null : highestBetAction.getActionType(),
                    higherStackThanBetAmount);
        }


        btnCheck.setVisibility(View.VISIBLE);
        if (availableActions.contains(ActionType.CALL)) {
            btnCheck.setText("CALL");
        } else {
            btnCheck.setText("CHECK");
        }
        if (availableActions.contains(ActionType.RAISE)) {
            btnBet.setText("RAISE");
            if(game.getUser().getStackSize() > highestBetAction.getBetValue() * 2){
                minBet = highestBetAction.getBetValue() * 2;
            } else {
                minBet = game.getUser().getStackSize();
                betBar.setMax(0);
            }

            betValue.setText("" + minBet);
        } else {
            btnBet.setText("BET");
            minBet = game.getBigBlindValue();
            betValue.setText("" + minBet);
        }
        if (availableActions.contains(ActionType.ALL_IN)) {
            btnBet.setText("ALL IN");
            btnCheck.setVisibility(View.GONE);
        }

    }

    public void updateGame(Game game) {

//        this.game.setPotSize(game.getPotSize());
        potSize.setText(game.getPotSize().toString());
        if(game.getBoard().size() > this.game.getBoard().size()){
            for(Card card : game.getBoard()){
                if(game.getBoard().indexOf(card) > this.game.getBoard().size() - 1){
                    this.game.getBoard().add(card);
                }
            }
        }
        this.game.setDealer(game.getDealer());
        this.game.setSmallBlind(game.getSmallBlind());
        this.game.setSmallBlindValue(game.getSmallBlindValue());
        this.game.setBigBlind(game.getBigBlind());
        this.game.setBigBlindValue(game.getBigBlindValue());
        for (Player player : players) {

            if(game.getPlayers().contains(player)){
                for (Player newPlayer : game.getPlayers()) {
                    if (player.getPlayerId().equals(newPlayer.getPlayerId())) {
                        player.setPlayerTurn(newPlayer.isPlayerTurn());
                        player.setPlayerInTurn(newPlayer.getPlayerInTurn());
                        player.setPlayerWinner(newPlayer.isPlayerWinner());
                        player.setPlayerRaiser(newPlayer.isPlayerRaiser());
                        player.setStackSize(newPlayer.getStackSize());
                        player.setCardOne(newPlayer.getCardOne());
                        player.setCardTwo(newPlayer.getCardTwo());
                    }
                }
            } else {
                seats.removeView(player.getTextView());
                if(player.isUser()){
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            context).create();

                    alertDialog.setTitle("Game Over!");
                    alertDialog.setMessage("You have finished " + game.getPlayers().size() + 1 + ". place!");

                    final long userId = player.getUserId();
                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Back to Game Browser", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent gameBrowserActivity = new Intent(context.getApplicationContext(), GameBrowserActivity.class);
                            gameBrowserActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            Bundle bundle = new Bundle();
                            bundle.putLong("userId", userId);
                            gameBrowserActivity.putExtras(bundle);
                            context.startActivity(gameBrowserActivity);
                        }
                    });

                    alertDialog.show();
                }
            }

        }

    }


    public static List<ActionType> getAvailableActions(ActionType previousAction, ActionType highestBetAction, Boolean stackBiggerThanRaiser) {
        List<ActionType> availableActions = new ArrayList<>();
        switch (previousAction) {
            case CHECK:
                availableActions.add(ActionType.CHECK);
                availableActions.add(ActionType.BET);
                availableActions.add(ActionType.FOLD);
                break;
            case BET:
                if (stackBiggerThanRaiser) {
                    availableActions.add(ActionType.CALL);
                    availableActions.add(ActionType.RAISE);
                    availableActions.add(ActionType.FOLD);
                } else {
                    availableActions.add(ActionType.ALL_IN);
                    availableActions.add(ActionType.FOLD);
                }

                break;
            case RAISE:
                if (stackBiggerThanRaiser) {
                    availableActions.add(ActionType.CALL);
                    availableActions.add(ActionType.RAISE);
                    availableActions.add(ActionType.FOLD);
                } else {
                    availableActions.add(ActionType.ALL_IN);
                    availableActions.add(ActionType.FOLD);
                }

                break;
            case CALL:
                if (stackBiggerThanRaiser) {
                    availableActions.add(ActionType.CALL);
                    availableActions.add(ActionType.RAISE);
                    availableActions.add(ActionType.FOLD);
                } else {
                    availableActions.add(ActionType.ALL_IN);
                    availableActions.add(ActionType.FOLD);
                }
                break;
            case ALL_IN:
                if (stackBiggerThanRaiser) {
                    availableActions.add(ActionType.CALL);
                    availableActions.add(ActionType.RAISE);
                    availableActions.add(ActionType.FOLD);
                } else {
                    availableActions.add(ActionType.ALL_IN);
                    availableActions.add(ActionType.FOLD);
                }


                break;
            case FOLD:
                if (null == highestBetAction) {
                    availableActions.add(ActionType.CHECK);
                    availableActions.add(ActionType.BET);
                    availableActions.add(ActionType.FOLD);
                } else {
                    availableActions.addAll(getAvailableActions(highestBetAction, null, stackBiggerThanRaiser));
                }

                break;
        }

        return availableActions;
    }

    public static List<List<RelativeLayout>> splitChips(List<RelativeLayout> list, int numberOfLists) {

        int sizeOfSubList = list.size() / numberOfLists;
        List<List<RelativeLayout>> subLists = new ArrayList<>(numberOfLists);

        List<RelativeLayout> subList = new ArrayList<>();
        for (RelativeLayout relativeLayout : list) {
            if(subLists.size() == numberOfLists - 1){
                subList = list.subList(list.indexOf(relativeLayout) - 1, list.size()-1);
                subLists.add(subList);
                break;
            }
            if (subList.size() < sizeOfSubList) {
                subList.add(relativeLayout);
            }
            if (subList.size() == sizeOfSubList || list.indexOf(relativeLayout) == list.size() - 1) {
                subLists.add(subList);
                subList = new ArrayList<>();
            }

        }
        return subLists;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
