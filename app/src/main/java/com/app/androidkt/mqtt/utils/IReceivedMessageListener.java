package com.app.androidkt.mqtt.utils;

public interface IReceivedMessageListener {

    void onMessageReceived(ReceivedMessage message);
}