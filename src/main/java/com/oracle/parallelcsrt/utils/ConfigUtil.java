package com.oracle.parallelcsrt.utils;

import java.util.ResourceBundle;

public class ConfigUtil {
    public static final String FCCM_URL = getConfigElementString("FCCM_URL");
    public static final String TABLE_TO_JSON_URL = ConfigUtil.FCCM_URL + "/TabletoJSONService/TableToJson/createtabletojson";
    public static final String ORG_CSRT_URL = ConfigUtil.FCCM_URL + "/FCCM/rest-api/RTScreening/RTScreeningRestService/service/EntityScreen";
    public static final String IND_CSRT_URL = ConfigUtil.FCCM_URL + "/FCCM/rest-api/RTScreening/RTScreeningRestService/service/IndividualScreen";
    public static String getConfigElementString(String elementName) {
        if(elementName.equals("")){
            return "";
        }
        ResourceBundle resourceBundle = ResourceBundle.getBundle("config");
        return resourceBundle.getString(elementName);
    }
}
