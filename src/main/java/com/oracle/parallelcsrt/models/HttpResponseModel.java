package com.oracle.parallelcsrt.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HttpResponseModel {
    private int statusCode;
    private String responseString;    
}
