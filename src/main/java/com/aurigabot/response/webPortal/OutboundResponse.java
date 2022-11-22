package com.aurigabot.response.webPortal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutboundResponse {
    private String id;
    private String status;
    private String message;
}