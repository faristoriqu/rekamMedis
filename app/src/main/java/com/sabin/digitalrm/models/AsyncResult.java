package com.sabin.digitalrm.models;

/**
 * Created by xkill on 26/11/18.
 */

public class AsyncResult {
    public static final int STATUS_OK = 1;
    public static final int STATUS_FAIL = 2;
    public static final int STATUS_CANCEL = 3;

    private int status;
    private String msg;

    public AsyncResult() {
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
