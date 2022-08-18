package com.oracle.parallelcsrt.models.GatewayInput;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayInputModel {
    private String sourceID;
    private String CustomerCounter;
    private Customer customer;
}



