package hu.onlineholdem.response;

import java.util.ArrayList;
import java.util.List;

public class DatatableAjaxResponse {

    private final List<List<?>> aaData = new ArrayList<>();
    private long iTotalRecords;
    private long iTotalDisplayRecords;
    private String sEcho;

    public List<List<?>> getAaData() {
        return aaData;
    }

    public long getiTotalRecords() {
        return iTotalRecords;
    }

    public void setiTotalRecords(long iTotalRecords) {
        this.iTotalRecords = iTotalRecords;
    }

    public long getiTotalDisplayRecords() {
        return iTotalDisplayRecords;
    }

    public void setiTotalDisplayRecords(long iTotalDisplayRecords) {
        this.iTotalDisplayRecords = iTotalDisplayRecords;
    }

    public String getsEcho() {
        return sEcho;
    }

    public void setsEcho(String sEcho) {
        this.sEcho = sEcho;
    }
}
