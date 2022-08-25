package com.oracle.parallelcsrt.runnables;

import java.io.IOException;

import com.oracle.parallelcsrt.factorys.FccmCsrtFactory;
import com.oracle.parallelcsrt.factorys.FccmTableToJsonFactory;
import com.oracle.parallelcsrt.models.HttpResponseModel;
import com.oracle.parallelcsrt.models.GatewayInput.GatewayInputModel;
import com.oracle.parallelcsrt.utils.ConfigUtil;
import com.oracle.parallelcsrt.utils.HttpRequestUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScreeningRunable implements Runnable {
    private static final Logger logger = LogManager.getLogger(ScreeningRunable.class);

    
    GatewayInputModel model;
    public ScreeningRunable(GatewayInputModel model) {
        this.model = model;
    }

    public void run() {
        for (int i = 0; i < ConfigUtil.RETRY_MAX_NUMBER; i++) {
            try {
                if (!sendScreen(model)) {
                    logger.error("Got error response wait to retry, req ID: " + model.getSourceID()
                            + ", cust seq id:" + model.getCustomer().getCustomerUniqueId());
                    logger.error("Sleep " + ConfigUtil.RETRY_SLEEP_INTERVAL + " ms and retry");
                    Thread.sleep(ConfigUtil.RETRY_SLEEP_INTERVAL);
                } else {
                    return;
                }
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.error(e.getStackTrace());                
            }
        }        
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

        if (HttpRequestUtil.checkResponseOk("CS_INPUT_OB", response)) {
            response = FccmCsrtFactory.sendRequest(gatewayInputModel.getCustomer().getCustomerType(),
                    response.getResponseString());
        } else {
            return false;
        }

        if (HttpRequestUtil.checkResponseOk("CS Screen", response)) {
            String csResult = response.getResponseString();
            response = HttpRequestUtil
                    .post(ConfigUtil.JSON_TO_TABLE_URL + "?mappingID=CS_WLS_RESPONSE&requestId="
                            + gatewayInputModel.getSourceID() + "&customerId="
                            + gatewayInputModel.getCustomer().getCustomerUniqueId(), csResult);
        } else {
            return false;
        }

        if (HttpRequestUtil.checkResponseOk("WLS Response result", response)) {
            return true;
        } else {
            return false;
        }
    }

}
