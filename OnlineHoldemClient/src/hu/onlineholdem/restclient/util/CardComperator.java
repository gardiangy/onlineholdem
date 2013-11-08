package hu.onlineholdem.restclient.util;

import java.util.Comparator;

import hu.onlineholdem.restclient.entity.Card;

public class CardComperator implements Comparator<Card>{
    @Override
    public int compare(Card o, Card o2) {
        return o.getValue().compareTo(o2.getValue());
    }
}
