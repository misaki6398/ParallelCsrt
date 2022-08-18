package com.oracle.parallelcsrt;

import com.oracle.parallelcsrt.Factorys.FccmCsrtFactory;
import com.oracle.parallelcsrt.Factorys.FccmTableToJsonFactory;
import com.oracle.parallelcsrt.models.HttpResponseModel;
import com.oracle.parallelcsrt.models.GatewayInput.GatewayInputModel;
import com.oracle.parallelcsrt.utils.ConfigUtil;
import com.oracle.parallelcsrt.utils.HttpRequestUtil;
import com.oracle.parallelcsrt.utils.JsonUtil;
import com.oracle.parallelcsrt.utils.SslUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * ars[0] = requestId
     * args[1] = customerCount
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            final String requestId = args[0];
            final int maxCustomerCount = Integer.parseInt(args[1]);
            final String tableToJsonUrl = ConfigUtil.FCCM_URL + "/TabletoJSONService/TableToJson/createtabletojson";

            SslUtil.ignoreSsl();
            for (int custCount = 1; custCount <= maxCustomerCount; custCount++) {

                GatewayInputModel gatewayInputModel;
                HttpResponseModel response = HttpRequestUtil
                        .post(tableToJsonUrl + "?mappingId=CMMN_GATEWAY_INPUT&requestId="
                                + requestId + "&customerCounter=" + custCount);

                if (loggerResponseJson("CMMN_GATEWAY_INPUT", response)) {
                    gatewayInputModel = JsonUtil.deserialize(response.getResponseString(), GatewayInputModel.class);
                } else {
                    break;
                }

                response = FccmTableToJsonFactory.sendRequest(gatewayInputModel.getCustomer().getCustomerType(),
                        requestId, gatewayInputModel.getCustomer().getCustomerUniqueId());

                if (loggerResponseJson("CS_INPUT_OB", response)) {
                    response = FccmCsrtFactory.sendRequest(gatewayInputModel.getCustomer().getCustomerType(),
                            response.getResponseString());
                } else {
                    break;
                }

                loggerResponseJson("CS Screen", response);

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e, e);
        }

    }

    private static boolean loggerResponseJson(String functionName, HttpResponseModel response) {
        if (JsonUtil.isJsonValid(response.getResponseString()) && response.getStatusCode() == 200) {
            logger.debug(String.format("%s response is Valid", functionName));
            logger.debug(String.format("StatusCode: %s, response: %s", response.getStatusCode(),
                    response.getResponseString()));
            return true;
        } else {
            logger.error("%s response Json was not valid please check", functionName);
            logger.error(String.format("StatusCode: %s, response: %s", response.getStatusCode(),
                    response.getResponseString()));
            return false;
        }
    }
}
