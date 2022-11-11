package com.dynamos.aurigabot.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public enum CommandType {
	REGTELEGRAMUSER("/regTelegramUser"),
	BIRTHDAY("/birthday"),
	LEAVE("/leave"),
	DASHBOARD("/dashboard"),
	TODO("/todo");

	private String commandType;

	private CommandType(String value) {
		this.commandType = value;
	}

	public String getDisplayValue() {
		return commandType;
	}

	public static CommandType getEnumByValue(String value) {
		for(CommandType type : values()){
			if( type.getDisplayValue().equals(value)){
				return type;
			}
		}
		return null;
	}
}
