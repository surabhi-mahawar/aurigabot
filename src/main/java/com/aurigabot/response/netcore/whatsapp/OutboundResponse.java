package com.aurigabot.response.netcore.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutboundResponse {

    @Getter
    @Setter
    public class Data {

        @JsonAlias({"id"})
        private String identifier;
    }

    private String status;
    private String message;

    private Data data;

    @Getter
    @Setter
    public class Error {

        @JsonAlias({"code"})
        private String code;

        @JsonAlias({"message"})
        private String message;
    }

    private Error error;
}
