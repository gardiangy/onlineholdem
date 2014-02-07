package hu.onlineholdem.entity;

import hu.onlineholdem.enums.Suit;
import hu.onlineholdem.util.SuitConverter;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the card database table.
 * 
 */
@Entity
@Table(name="card")
public class Card implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="card_id", unique=true, nullable=false)
	private Long cardId;

    @Convert(converter = SuitConverter.class)
	@Column(name="card_suit", nullable=false)
	private Suit suit;

	@Column(name="card_value", nullable=false)
	private Integer value;

	public Card() {
	}

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (cardId != null ? !cardId.equals(card.cardId) : card.cardId != null) return false;
        if (suit != card.suit) return false;
        if (value != null ? !value.equals(card.value) : card.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cardId != null ? cardId.hashCode() : 0;
        result = 31 * result + (suit != null ? suit.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}