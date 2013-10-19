package hu.onlineholdem.restclient.entity;

import java.io.Serializable;

import hu.onlineholdem.restclient.enums.Suit;


public class Card implements Serializable {
	private static final long serialVersionUID = 1L;

	private Suit suit;
	private Integer value;

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return suit.getName() + value;
    }
}