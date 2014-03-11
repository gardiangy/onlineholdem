package hu.onlineholdem.restclient.entity;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;

import hu.onlineholdem.restclient.enums.ActionType;
import hu.onlineholdem.restclient.util.EvaluatedHand;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long playerId;
	private Integer stackSize;
    private Boolean isUser;
    private Integer order;
    private TextView textView;
    private ImageView card1View;
    private ImageView card2View;
    private RelativeLayout chipLayout;
    private ActionType actionType;
    private Integer betAmount;
    private Card cardOne;
    private Card cardTwo;
    private EvaluatedHand evaluatedHand;
    private Integer amountInPot;
    private Integer amountToWin;
    private String playerName;
    private Long userId;
    private Boolean playerTurn;
    private Boolean playerInTurn;

	public Player() {
	}

	public Long getPlayerId() {
		return this.playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	public Integer getStackSize() {
		return this.stackSize;
	}

	public void setStackSize(Integer stackSize) {
		this.stackSize = stackSize;
	}

    public Boolean isUser() {
        return isUser;
    }

    public void setIsUser(Boolean isUser) {
        this.isUser = isUser;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(Integer betAmount) {
        this.betAmount = betAmount;
    }

    public ImageView getCard1View() {
        return card1View;
    }

    public void setCard1View(ImageView card1View) {
        this.card1View = card1View;
    }

    public ImageView getCard2View() {
        return card2View;
    }

    public void setCard2View(ImageView card2View) {
        this.card2View = card2View;
    }

    public RelativeLayout getChipLayout() {
        return chipLayout;
    }

    public void setChipLayout(RelativeLayout chipLayout) {
        this.chipLayout = chipLayout;
    }

    public Card getCardOne() {
        return cardOne;
    }

    public void setCardOne(Card cardOne) {
        this.cardOne = cardOne;
    }

    public Card getCardTwo() {
        return cardTwo;
    }

    public void setCardTwo(Card cardTwo) {
        this.cardTwo = cardTwo;
    }

    public EvaluatedHand getEvaluatedHand() {
        return evaluatedHand;
    }

    public void setEvaluatedHand(EvaluatedHand evaluatedHand) {
        this.evaluatedHand = evaluatedHand;
    }

    public Integer getAmountInPot() {
        return amountInPot;
    }

    public void setAmountInPot(Integer amountInPot) {
        this.amountInPot = amountInPot;
    }

    public Integer getAmountToWin() {
        return amountToWin;
    }

    public void setAmountToWin(Integer amountToWin) {
        this.amountToWin = amountToWin;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(Boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public Boolean getPlayerInTurn() {
        return playerInTurn;
    }

    public void setPlayerInTurn(Boolean playerInTurn) {
        this.playerInTurn = playerInTurn;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (playerId != null ? !playerId.equals(player.playerId) : player.playerId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return playerId != null ? playerId.hashCode() : 0;
    }
}