package com.diago.ship;

public class SplitException extends Exception {
    private String expMsg = "General Split Exception";

    public SplitException() {
    }

    public SplitException(String expMsg) {
        this.expMsg = expMsg;
    }

    public String getExpMsg() {
        return expMsg;
    }
}
