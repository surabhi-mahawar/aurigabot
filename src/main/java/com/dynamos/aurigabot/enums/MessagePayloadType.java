package com.dynamos.aurigabot.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MessagePayloadType {
    TEXT("text"),
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    FILE("file");

    private final String messagePayloadType;
}
