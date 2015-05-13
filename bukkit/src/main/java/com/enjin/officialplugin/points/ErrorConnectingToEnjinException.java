package com.enjin.officialplugin.points;

public class ErrorConnectingToEnjinException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 8988005958432507165L;
    String message;

    public ErrorConnectingToEnjinException(String string) {
        message = string;
    }

    @Override
    public String getMessage() {
        // TODO Auto-generated method stub
        return message;
    }

}
