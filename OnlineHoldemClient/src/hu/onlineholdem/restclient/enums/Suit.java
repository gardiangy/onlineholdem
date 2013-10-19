package hu.onlineholdem.restclient.enums;

public enum Suit {

    DIAMONDS("d"),
    SPADES("s"),
    CLUBS("c"),
    HEARTS("h"),;

    private String name;

    Suit(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
