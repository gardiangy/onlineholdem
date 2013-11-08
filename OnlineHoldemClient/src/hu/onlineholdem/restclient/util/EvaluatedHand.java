package hu.onlineholdem.restclient.util;

import java.util.List;

import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.enums.HandStrength;

public class EvaluatedHand {

    private HandStrength handStrength;

    private List<Card> highCard;

    public HandStrength getHandStrength() {
        return handStrength;
    }

    public void setHandStrength(HandStrength handStrength) {
        this.handStrength = handStrength;
    }

    public List<Card> getHighCard() {
        return highCard;
    }

    public void setHighCard(List<Card> highCard) {
        this.highCard = highCard;
    }
}
