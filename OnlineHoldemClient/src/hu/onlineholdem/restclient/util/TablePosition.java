package hu.onlineholdem.restclient.util;

public class TablePosition {

    private int left;
    private int top;

    public TablePosition(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }
}
