package com.example.pjj50.xinjitai;

public interface HttpCallBackListener {
    void onSuccess(String respose);
    void onError(Exception e);
}