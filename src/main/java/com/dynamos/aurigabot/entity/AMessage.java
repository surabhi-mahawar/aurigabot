package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.State;
import com.dynamos.aurigabot.enums.Status;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "message")
public class AMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private String options;
    private Date receivedAt;
    private Date sentAt;
    private State state;
    private String interfaceUserId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    private String command;
    private String botName;
}
