package hu.onlineholdem.util;

import hu.onlineholdem.entity.Player;

import java.util.Comparator;

public class PlayerComperator implements Comparator<Player>{
    @Override
    public int compare(Player p, Player p2) {
        return p.getPlayerOrder().compareTo(p2.getPlayerOrder());
    }
}
