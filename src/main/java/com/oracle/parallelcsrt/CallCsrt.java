package com.oracle.parallelcsrt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

public class CallCsrt {
    private static final Logger logger = LogManager.getLogger(CallCsrt.class);

    public String sendCsrtMain(String requestId, Integer maxCustomerCount) {
        try {
            SslUtil.ignoreSsl();
            List<GatewayInputModel> gatewayInputModels = getAllCustomer(requestId, maxCustomerCount);

            if (gatewayInputModels.size() != maxCustomerCount) {
                String errorMessage = String.format("RequestId %s customer count not match number &d, please check it",
                        requestId, maxCustomerCount);
                logger.error(errorMessage);
                return errorMessage;
            }
            int totalCount = 0;

            List<Callable<Boolean>> tasks = new ArrayList<>();
            int callerCount = 0;
            long start = System.currentTimeMillis();

            for (final GatewayInputModel model : gatewayInputModels) {
                Callable<Boolean> c = new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        for (int i = 0; i < ConfigUtil.RETRY_MAX_NUMBER; i++) {
                            if (sendScreen(model)) {
                                return true;
                            } else {
                                logger.error("Got error response wait to retry, req ID: " + model.getSourceID()
                                        + ", cust seq id:" + model.getCustomer().getCustomerUniqueId());
                                logger.error("Sleep " + ConfigUtil.RETRY_SLEEP_INTERVAL + " ms and retry");
                                Thread.sleep(ConfigUtil.RETRY_SLEEP_INTERVAL);
                            }
                        }
                        return false;
                    }
                };
                tasks.add(c);
                callerCount++;
                totalCount++;
                if (totalCount == gatewayInputModels.size() || callerCount == ConfigUtil.MAX_THREAD_NUM) {
                    ExecutorService exec = Executors.newCachedThreadPool();
                    try {
                        exec.invokeAll(tasks);
                        logger.debug("Invoke SUCCESS " + totalCount + " threads invoked. Reqid:" + requestId
                                + ", CustCount:" + maxCustomerCount);
                    } catch (Exception e) {
                        logger.error(e, e);
                        throw e;
                    } finally {
                        tasks = new ArrayList<>();
                        callerCount = 0;
                        exec.shutdown();
                    }
                }
            }
            long end = System.currentTimeMillis() - start;
            logger.debug(String.format("Elasped time %d ms", end));

            return "SUCCESS";

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e, e);
            return "Invoke FAIL: Reqid:" + requestId + ", CustCount:" + maxCustomerCount + ", Detail:"
                    + e.getStackTrace();
        }
    }

    private List<GatewayInputModel> getAllCustomer(String requestId, int maxCustomerCount) throws IOException {
        List<GatewayInputModel> gatewayInputModels = new ArrayList<>();
        int retryCount = 0;
        for (int custCount = 1; custCount <= maxCustomerCount; custCount++) {
            HttpResponseModel response = HttpRequestUtil
                    .post(ConfigUtil.TABLE_TO_JSON_URL + "?mappingId=CMMN_GATEWAY_INPUT&requestId="
                            + requestId + "&customerCounter=" + custCount);
            if (checkResponseOk("CMMN_GATEWAY_INPUT", response)) {
                GatewayInputModel gatewayInputModel = JsonUtil.deserialize(response.getResponseString(),
                        GatewayInputModel.class);
                gatewayInputModels.add(gatewayInputModel);
                retryCount = 0;
            } else {
                if (retryCount < ConfigUtil.RETRY_MAX_NUMBER) {
                    custCount--;
                    retryCount++;
                }
            }
        }
        return gatewayInputModels;
    }

    /**
     * 1. Got customer screen input
     * 2. Send screen
     * 3. Got screen response and send to WLS_RESPONSE API
     * 
     * @param gatewayInputModel
     * @return
     * @throws IOException
     */
    private Boolean sendScreen(GatewayInputModel gatewayInputModel) throws IOException {

        if (gatewayInputModel.getCustomer().getCustomerUniqueId() == null
                || gatewayInputModel.getCustomer().getCustomerType() == null) {
            return false;
        }

        HttpResponseModel response = FccmTableToJsonFactory.sendRequest(
                gatewayInputModel.getCustomer().getCustomerType(),
                gatewayInputModel.getSourceID(), gatewayInputModel.getCustomer().getCustomerUniqueId());

        if (checkResponseOk("CS_INPUT_OB", response)) {
            response = FccmCsrtFactory.sendRequest(gatewayInputModel.getCustomer().getCustomerType(),
                    response.getResponseString());
        } else {
            return false;
        }

        if (checkResponseOk("CS Screen", response)) {
            String csResult = response.getResponseString();
            response = HttpRequestUtil
                    .post(ConfigUtil.JSON_TO_TABLE_URL + "?mappingID=CS_WLS_RESPONSE&requestId="
                            + gatewayInputModel.getSourceID() + "&customerId="
                            + gatewayInputModel.getCustomer().getCustomerUniqueId(), csResult);
        } else {
            return false;
        }

        if (checkResponseOk("WLS Response result", response)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check web api response json is valid or not and check response code is 200 ok
     * 
     * @param functionName
     * @param response
     * @return
     */
    private boolean checkResponseOk(String functionName, HttpResponseModel response) {
        if (response.getResponseString().equals("")) {
            logger.error(String.format("%s response Json was blank, might be caused error", functionName));
            return false;
        }
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
