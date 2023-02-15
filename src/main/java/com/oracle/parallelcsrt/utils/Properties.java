package com.oracle.parallelcsrt.utils;

import java.util.ResourceBundle;

public class Properties {
    public static final String FCCM_URL = getConfigElementString("FCCM_URL");
    public static final String TABLE_TO_JSON_URL = Properties.FCCM_URL + "/TabletoJSONService/TableToJson/createtabletojson";
    public static final String JSON_TO_TABLE_URL = Properties.FCCM_URL + "/JSONToTablePersistenceUtility/jsonToTable/persistJSON";
    public static final String ORG_CSRT_URL = Properties.FCCM_URL + "/FCCM/rest-api/RTScreening/RTScreeningRestService/service/EntityScreen";
    public static final String IND_CSRT_URL = Properties.FCCM_URL + "/FCCM/rest-api/RTScreening/RTScreeningRestService/service/IndividualScreen";
    public static final String FCCM_AUTH = getConfigElementString("FCCM_AUTH");
    public static final String CS_INPUT_MAP_NAME_IND = getConfigElementString("CS_INPUT_MAP_NAME_IND");
    public static final String CS_INPUT_MAP_NAME_NONIND = getConfigElementString("CS_INPUT_MAP_NAME_NONIND");
    public static final int RETRY_MAX_NUMBER = Integer.parseInt(getConfigElementString("RETRY_MAX_NUMBER"));
    public static final int RETRY_SLEEP_INTERVAL = Integer.parseInt(getConfigElementString("RETRY_SLEEP_INTERVAL"));
    public static final int MAX_THREAD_NUM = Integer.parseInt(getConfigElementString("MAX_THREAD_NUM"));
    public static String getConfigElementString(String elementName) {
        if(elementName.equals("")){
            return "";
        }
        ResourceBundle resourceBundle = ResourceBundle.getBundle("config");
        return resourceBundle.getString(elementName);
    }
}
