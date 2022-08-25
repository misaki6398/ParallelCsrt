package com.oracle.parallelcsrt.Factorys;

import java.io.IOException;

import com.oracle.parallelcsrt.models.HttpResponseModel;
import com.oracle.parallelcsrt.utils.ConfigUtil;
import com.oracle.parallelcsrt.utils.HttpRequestUtil;

public class FccmTableToJsonFactory {
    public static HttpResponseModel sendRequest(String customerType, String requestId, String customerId) throws IOException {
        switch (customerType) {
            case "ORG":
                return HttpRequestUtil
                        .post(ConfigUtil.TABLE_TO_JSON_URL + "?mappingId=" +  ConfigUtil.CS_INPUT_MAP_NAME_NONIND +"&requestId=" + requestId
                                + "&customerId=" + customerId);
            case "FIN":
                return HttpRequestUtil
                        .post(ConfigUtil.TABLE_TO_JSON_URL + "?mappingId=" +  ConfigUtil.CS_INPUT_MAP_NAME_NONIND +"&requestId=" + requestId
                                + "&customerId=" + customerId);
            case "IND":
                return HttpRequestUtil
                        .post(ConfigUtil.TABLE_TO_JSON_URL + "?mappingId=" +  ConfigUtil.CS_INPUT_MAP_NAME_IND +"&requestId=" + requestId
                                + "&customerId=" + customerId);
            default:
                throw new UnsupportedOperationException(customerType + " not support");
        }
    }
}
