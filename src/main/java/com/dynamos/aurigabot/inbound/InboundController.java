package com.dynamos.aurigabot.inbound;

import com.dynamos.aurigabot.adapters.AbstractAdapter;
import com.dynamos.aurigabot.adapters.WebAdapter;
import com.dynamos.aurigabot.model.web.InboundMessage;
import com.dynamos.aurigabot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/inbound")
public class InboundController {
    @Autowired
    private UserRepository userRepository;

    /**
     * Receives inbound message from web channel and process it.
     * @param message
     */
    @PostMapping(value = "/webMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void webMessage(@RequestBody InboundMessage message) {
        AbstractAdapter adapter = new WebAdapter();
        com.dynamos.aurigabot.inbound.InboundMessage inboundMessage = com.dynamos.aurigabot.inbound.InboundMessage.builder()
                .adapter(adapter)
                .userRepository(userRepository)
                .build();

        inboundMessage.process(message);
    }
}
