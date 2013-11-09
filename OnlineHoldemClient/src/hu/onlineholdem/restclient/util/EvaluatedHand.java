package hu.onlineholdem.restclient.util;

import java.util.List;

import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.enums.HandStrength;

public class EvaluatedHand {

    private HandStrength handStrength;

    private List<Card> highCards;

    private Integer value;

    public HandStrength getHandStrength() {
        return handStrength;
    }

    public void setHandStrength(HandStrength handStrength) {
        this.handStrength = handStrength;
    }

    public List<Card> getHighCards() {
        return highCards;
    }

    public void setHighCards(List<Card> highCards) {
        this.highCards = highCards;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
