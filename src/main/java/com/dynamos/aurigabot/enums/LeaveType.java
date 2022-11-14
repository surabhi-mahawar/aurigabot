package com.dynamos.aurigabot.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public enum LeaveType {
    CL("CL"),
    PL("PL");

    private String leaveType;

    private LeaveType(String value) {
        this.leaveType = value;
    }

    public String getDisplayValue() {
        return leaveType;
    }

    public static LeaveType getEnumByValue(String value) {
        for(LeaveType type : values()){
            if( type.getDisplayValue().equals(value)){
                return type;
            }
        }
        return null;
    }
}
