package hu.onlineholdem.restclient.entity;

import android.widget.TextView;

import java.io.Serializable;

public class Player implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long playerId;
	private Integer stackSize;
    private boolean isUser;
    private int order;
    private TextView textView;

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
}