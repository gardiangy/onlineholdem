package hu.onlineholdem.restclient.util;

import java.util.Comparator;

import hu.onlineholdem.restclient.entity.Card;
import hu.onlineholdem.restclient.entity.Player;

public class PlayerComperator implements Comparator<Player>{
    @Override
    public int compare(Player p, Player p2) {
        return p.getOrder().compareTo(p2.getOrder());
    }
}
