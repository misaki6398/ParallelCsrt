package com.oracle.parallelcsrt.factorys;

import java.io.IOException;

import com.oracle.parallelcsrt.models.HttpResponseModel;
import com.oracle.parallelcsrt.utils.Properties;
import com.oracle.parallelcsrt.utils.HttpRequestUtil;

public class FccmCsrtFactory {
    public static HttpResponseModel sendRequest(String customerType, String jsonString) throws IOException {
        switch (customerType) {
            case "ORG":
                return HttpRequestUtil
                        .post(Properties.ORG_CSRT_URL, jsonString);
            case "FIN":
                return HttpRequestUtil
                        .post(Properties.ORG_CSRT_URL, jsonString);
            case "IND":
                return HttpRequestUtil
                        .post(Properties.IND_CSRT_URL, jsonString);
            default:
                throw new UnsupportedOperationException(customerType + " not support");
        }
    }
}
