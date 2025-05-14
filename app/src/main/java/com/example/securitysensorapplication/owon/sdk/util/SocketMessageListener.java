package com.example.securitysensorapplication.owon.sdk.util;

public interface SocketMessageListener {

    void getMessage(int commandID, Object bean);
}