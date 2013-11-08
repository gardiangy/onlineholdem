package hu.onlineholdem.restclient.enums;

public enum ActionType {

    CHECK("CHECK"),
    BET("BET"),
    CALL("CALL"),
    RAISE("RAISE"),
    FOLD("FOLD");

    private String name;

    ActionType(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

}
