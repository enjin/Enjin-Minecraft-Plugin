package com.enjin.rpc.mappings.mappings.tickets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Module {
    @Getter
    private Question[] questions;
}
