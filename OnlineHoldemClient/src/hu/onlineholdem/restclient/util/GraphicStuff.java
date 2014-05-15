package hu.onlineholdem.restclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import hu.onlineholdem.restclient.entity.Card;
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
    private Activity activity;
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


    }

    public void dealFlop(Card flopOne, Card flopTwo, Card flopThree) {

        final int resId = resources.getIdentifier(flopOne.toString(), "drawable", packageName);
        final int res2Id = resources.getIdentifier(flopTwo.toString(), "drawable", packageName);
        final int res3Id = resources.getIdentifier(flopThree.toString(), "drawable", packageName);

        Animation flop1Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 6, 0, screenHeight / 3, true);
        flop1 = new ImageView(context);
        board.addView(flop1);
        flop1.setAnimation(flop1Anim);
        flop1.setImageResource(resId);
        flop1.setVisibility(View.VISIBLE);
        flop1.startAnimation(flop1Anim);

        Animation flop2Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 10, 0, screenHeight / 3, true);
        flop2 = new ImageView(context);
        board.addView(flop2);
        flop2.setAnimation(flop2Anim);
        flop2.setImageResource(res2Id);
        flop2.setVisibility(View.VISIBLE);
        flop2.startAnimation(flop2Anim);

        Animation flop3Anim = createAnimation(screenWidth / 2, screenWidth / 2 - screenWidth / 32, 0, screenHeight / 3, true);
        flop3 = new ImageView(context);
        board.addView(flop3);
        flop3.setAnimation(flop3Anim);
        flop3.setImageResource(res3Id);
        flop3.setVisibility(View.VISIBLE);
        flop3.startAnimation(flop3Anim);

    }

    public void dealTurn(Card turnCard) {

        int resId = resources.getIdentifier(turnCard.toString(), "drawable", packageName);

        Animation turnAnim = createAnimation(screenWidth / 2, screenWidth / 2 + screenWidth / 26, 0, screenHeight / 3, true);
        turn = new ImageView(context);
        board.addView(turn);
        turn.setAnimation(turnAnim);
        turn.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        turn.startAnimation(turnAnim);

    }

    public void dealRiver(Card riverCard) {

        int resId = resources.getIdentifier(riverCard.toString(), "drawable", packageName);

        Animation riverAnim = createAnimation(screenWidth / 2, screenWidth / 2 + screenWidth / 9, 0, screenHeight / 3, true);
        river = new ImageView(context);
        board.addView(river);
        river.setAnimation(riverAnim);
        river.setImageResource(resId);
        turn.setVisibility(View.VISIBLE);
        river.startAnimation(riverAnim);

    }

    public void deal(Player player) {

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

    public void showCards(List<Player> players) {
        for (Player player : players) {
            if (!player.isUser()) {
                final int resId = resources.getIdentifier(null == player.getCardOneLastRound() ? player.getCardOne().toString()
                        : player.getCardOneLastRound().toString(), "drawable", packageName);
                final int res2Id = resources.getIdentifier(null == player.getCardTwoLastRound() ? player.getCardTwo().toString()
                        : player.getCardTwoLastRound().toString(), "drawable", packageName);
                player.getCard2View().setImageResource(res2Id);
                player.getCard1View().setImageResource(resId);

            }
        }
        board.invalidate();
    }



    public void assignChips(RelativeLayout chip, Player winner) {
       chip.animate().setDuration(500).x(winner.getTextView().getLeft()).y(winner.getTextView().getTop());
    }


    public Animation createAnimation(int xFrom, int xTo, int yFrom, int yTo, boolean fillAfter) {
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.ABSOLUTE, xFrom, Animation.ABSOLUTE, xTo, Animation.ABSOLUTE, yFrom, Animation.ABSOLUTE, yTo);

        translateAnimation.setRepeatMode(0);
        translateAnimation.setDuration(500);
        translateAnimation.setFillAfter(fillAfter);

        return translateAnimation;
    }

    public void showCurrentPlayer(Player currentPlayer, List<Player> players) {
        currentPlayer.getTextView().setBackgroundResource(R.drawable.seatactive);
        for (Player player : players) {
            if (!player.equals(currentPlayer)) {
                player.getTextView().setBackgroundResource(R.drawable.seatnotactive);
            }
        }
    }

    public void moveBet(Player player) {

        if (null != player.getChipLayout()) {
            TextView existingChipsTextViw = (TextView) player.getChipLayout().getChildAt(0);
            existingChipsTextViw.setText(player.getBetAmount() + "");
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.chips, null);
            TextView chipsTextView = (TextView) relativeLayout.getChildAt(0);
            chipsTextView.setText(player.getBetAmount().toString());


            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 12, screenHeight / 8);
            Position position = getChipsPosition(player);
            layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);
            relativeLayout.setLayoutParams(layoutParams);

            board.addView(relativeLayout);
            player.setChipLayout(relativeLayout);
        }
        if (null != player.getPlayerName()) {
            player.getTextView().setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
        } else {
            player.getTextView().setText(player.getStackSize().toString());
        }


    }

    public void moveFold(Player player) {

        Animation card1Anim = createAnimation(player.getTextView().getRight() - 150,
                screenWidth / 2, player.getTextView().getTop() - 40, 0, false);
        player.getCard1View().setAnimation(card1Anim);

        Animation card2Anim = createAnimation(player.getTextView().getRight() - 110,
                screenWidth / 2, player.getTextView().getTop() - 40, 0, false);
        player.getCard2View().setAnimation(card2Anim);
        player.getCard1View().startAnimation(player.getCard1View().getAnimation());
        board.removeView(player.getCard1View());

        player.getCard2View().startAnimation(player.getCard2View().getAnimation());
        board.removeView(player.getCard2View());

    }

    public void collectChips(Player player) {

        if (null != player.getChipLayout()) {
            int chipsShiftX = new Random().nextInt(100) - 50;
            int chipsShiftY = new Random().nextInt(20) - 10;
            TextView chipsText = (TextView) player.getChipLayout().getChildAt(0);
            chipsText.setText("");
            player.getChipLayout().animate().x(screenWidth / 2 + chipsShiftX).y(screenHeight / 7 + chipsShiftY);
        }
    }

    public Position getChipsPosition(Player player) {
        switch (player.getPosition()) {
            case 1:
                return new Position(player.getTextView().getLeft() - getPixels(20), player.getTextView().getTop() + getPixels(40));
            case 2:
                return new Position(player.getTextView().getLeft() - getPixels(40), player.getTextView().getTop() + getPixels(20));
            case 3:
                return new Position(player.getTextView().getLeft() - getPixels(40), player.getTextView().getTop() - getPixels(20));
            case 4:
                return new Position(player.getTextView().getLeft() + getPixels(40), player.getTextView().getTop() - getPixels(60));
            case 5:
                return new Position(player.getTextView().getLeft() + getPixels(80), player.getTextView().getTop() - getPixels(60));
            case 6:
                return new Position(player.getTextView().getLeft() + getPixels(80), player.getTextView().getTop() - getPixels(60));
            case 7:
                return new Position(player.getTextView().getLeft() + getPixels(150), player.getTextView().getTop() - getPixels(30));
            case 8:
                return new Position(player.getTextView().getLeft() + getPixels(150), player.getTextView().getTop() + getPixels(20));
            case 9:
                return new Position(player.getTextView().getLeft() + getPixels(140), player.getTextView().getTop() + getPixels(40));
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

    public int getPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                (float) dp, context.getResources().getDisplayMetrics());
    }


    public void addDealer(Player dealer) {
        dealerLayout = new RelativeLayout(context);
        ImageView dealerBtn = new ImageView(context);
        dealerBtn.setImageResource(R.drawable.dealer);
        dealerBtn.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 20, screenHeight / 20);
        Position position = getDealerBtnPosition(dealer);
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

    public void moveDealer(Player dealer) {
        Position dealerPos = getDealerBtnPosition(dealer);
        dealerLayout.animate().setDuration(500).x(dealerPos.getLeft()).y(dealerPos.getTop());
    }

    public void removePlayerCards(Player player){
        board.removeView(player.getCard1View());
        board.removeView(player.getCard2View());
    }

    public void removeChips(RelativeLayout chip){
        board.removeView(chip);
    }

    public void removeBoard(){
        board.removeView(flop1);
        board.removeView(flop2);
        board.removeView(flop3);
        board.removeView(turn);
        board.removeView(river);
    }

    public void updatePotSize(Integer size) {
        potSize.setText(size.toString());
    }

    public void removeSeat(Player player) {
        seats.removeView(player.getTextView());
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

    public TextView createPlayerView(Player player){
        TextView textView = new TextView(context);
        textView.setBackgroundResource(R.drawable.seatnotactive);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(screenWidth / 5, screenHeight / 6);
        Position position = getPlayerPosition(player);
        layoutParams.setMargins(position.getLeft(), position.getTop(), 0, 0);

        textView.setTop(position.getTop());
        textView.setLeft(position.getLeft());
        textView.setLayoutParams(layoutParams);
        if(null != player.getPlayerName()){
            textView.setText(player.getPlayerName() + "\n" + player.getStackSize().toString());
        } else {
            textView.setText(player.getStackSize().toString());
        }

        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(0xFF000000);
        textView.setTextSize(15);

        seats.addView(textView);
        return  textView;
    }

    public List<List<RelativeLayout>> splitChips(List<RelativeLayout> list, int numberOfLists) {

        List<List<RelativeLayout>> subLists = new ArrayList<>();

        for (int i = 0; i < numberOfLists; i++) {
            subLists.add(new ArrayList<RelativeLayout>());
        }

        int index = 0;

        for (RelativeLayout layout : list) {
            subLists.get(index).add(layout);
            index = (index + 1) % numberOfLists;
        }
        return subLists;
    }

    public Position getPlayerPosition(Player player) {
        switch (player.getPosition()) {
            case 1:
                return new Position(screenWidth / 5 * 3, screenHeight / 15);
            case 2:
                return new Position(screenWidth / 10 * 8, screenHeight / 5);
            case 3:
                return new Position(screenWidth / 10 * 8, screenHeight / 7 * 3);
            case 4:
                return new Position(screenWidth / 25 * 16, screenHeight / 5 * 3);
            case 5:
                return new Position(screenWidth / 5 * 2, screenHeight / 5 * 3);
            case 6:
                return new Position(screenWidth / 6, screenHeight / 5 * 3);
            case 7:
                return new Position(screenWidth / 70, screenHeight / 7 * 3);
            case 8:
                return new Position(screenWidth / 70, screenHeight / 5);
            case 9:
                return new Position(screenWidth / 5, screenHeight / 15);
        }
        return null;
    }
}
