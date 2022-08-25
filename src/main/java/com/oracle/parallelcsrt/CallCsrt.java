package com.oracle.parallelcsrt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.oracle.parallelcsrt.factorys.FccmCsrtFactory;
import com.oracle.parallelcsrt.factorys.FccmTableToJsonFactory;
import com.oracle.parallelcsrt.models.HttpResponseModel;
import com.oracle.parallelcsrt.models.GatewayInput.GatewayInputModel;
import com.oracle.parallelcsrt.runnables.ScreeningRunable;
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

            long start = System.currentTimeMillis();
            int threadCount = 0;
            ExecutorService exec = Executors.newFixedThreadPool(5);

            for (final GatewayInputModel model : gatewayInputModels) {
                try {
                    threadCount++;
                    exec.execute(new ScreeningRunable(model));
                    logger.debug("Invoke SUCCESS " + threadCount + " threads invoked. Reqid:" + requestId
                            + ", CustCount:" + maxCustomerCount);
                } catch (Exception e) {
                    logger.error(e, e);
                    throw e;
                }
            }
            exec.shutdown();

            // try {
            //     exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            // } catch (Exception e) {
            //     e.printStackTrace();
            // }
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
            if (HttpRequestUtil.checkResponseOk("CMMN_GATEWAY_INPUT", response)) {
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

}
