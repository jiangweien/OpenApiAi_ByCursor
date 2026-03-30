package com.dsg.aiSdk;

public final class CursorApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public CursorApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody == null ? "" : responseBody;
    }

    public int statusCode() {
        return statusCode;
    }

    public String responseBody() {
        return responseBody;
    }
}
