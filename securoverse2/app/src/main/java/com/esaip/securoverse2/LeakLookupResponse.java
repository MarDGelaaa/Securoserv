package com.esaip.securoverse2;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class LeakLookupResponse {
    @SerializedName("error")
    private boolean error;

    @SerializedName("message")
    private Map<String, Object> message;

    public boolean isError() {
        return error;
    }

    public Map<String, Object> getMessage() {
        return message;
    }
}