package com.enjin.rpc.mappings.mappings.tickets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Condition {
    @Getter
    private int question;
    @Getter
    private Status status;
    @Getter
    private int answer;

    public enum Status {
        is,
        is_not
    }
}