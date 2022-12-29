package com.aurigabot.enums.netcore.whatsapp;

public enum MessageType {
    TEXT("text"),
    INTERACTIVE("interactive"),
    MEDIA("media");


    private String name;

    MessageType(String messageType) {
        name=messageType;
    }

    public String toString(){
        return name;
    }
}
