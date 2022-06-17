package com.dynamos.aurigabot.entity;

import com.dynamos.aurigabot.enums.RoleType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String interfaceUserId;
    @Enumerated(EnumType.STRING)
    private RoleType role;
    private Date dob;
    private Date joinedAt;
    private String email;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "user")
    private AMessage lastMessage;
}
