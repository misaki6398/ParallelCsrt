package com.oracle.parallelcsrt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * ars[0] = requestId
     * args[1] = customerCount
     * 
     * @param args
     */
    public static void main(String[] args) {
        final int maxCustomerCount = Integer.parseInt(args[1]);

        // CallCsrt csrt = new CallCsrt();
        // csrt.sendCsrtMain(args[0], maxCustomerCount);
    }

}
