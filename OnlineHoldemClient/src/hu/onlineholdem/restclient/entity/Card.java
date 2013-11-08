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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (suit != card.suit) return false;
        if (value != null ? !value.equals(card.value) : card.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = suit != null ? suit.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}