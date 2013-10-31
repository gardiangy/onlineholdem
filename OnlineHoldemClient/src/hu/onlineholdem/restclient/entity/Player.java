package hu.onlineholdem.restclient.entity;

import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;

import hu.onlineholdem.restclient.enums.ActionType;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long playerId;
	private Integer stackSize;
    private boolean isUser;
    private int order;
    private TextView textView;
    private ImageView card1View;
    private ImageView card2View;
    private ActionType actionType;
    private Integer betAmount;

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

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
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
}