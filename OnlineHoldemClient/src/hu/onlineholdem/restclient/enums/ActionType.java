package hu.onlineholdem.restclient.enums;

public enum ActionType {

    CHECK("CHECK"),
    BET("BET"),
    FOLD("FOLD");

    private String name;

    ActionType(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public static class Message {

        private Long id;
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
