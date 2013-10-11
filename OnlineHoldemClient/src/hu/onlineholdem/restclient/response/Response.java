package hu.onlineholdem.restclient.response;

import hu.onlineholdem.restclient.enums.ResponseType;

public class Response {

    private Object responseObject;
    private ResponseType responseType;
    private String errorMessage;

    public Object getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(Object responseObject) {
        this.responseObject = responseObject;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
