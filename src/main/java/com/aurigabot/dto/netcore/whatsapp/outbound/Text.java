package com.aurigabot.dto.netcore.whatsapp.outbound;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Text {
    @JsonProperty("preview_url")
    @JsonAlias({"preview_url"})
    private String previewURL;

    @JsonProperty("content")
    @JsonAlias({"content"})
    private String content;
}
