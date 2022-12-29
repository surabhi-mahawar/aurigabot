package com.aurigabot.entity;

import com.aurigabot.utils.BotUtil;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.Transient;
import java.util.UUID;

@Builder
@Data
@Table(name = "google_tokens")
public class GoogleTokens implements Persistable {
    @Id
    private UUID id;
    private UUID employeeId;
    private String accessToken;
    private String email;
    private String name;
    private String photo;
    private String refreshToken;

    @Override
    @Transient
    public boolean isNew() {
        /** Check for superadmin user id
         * Reason - To insert superadmin user on application run with a specific id
         */
        if(id!= null && id.equals(BotUtil.USER_ADMIN_ID)) {
            return true;
        }
        return id == null;
    }
}
