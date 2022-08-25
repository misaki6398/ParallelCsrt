package com.oracle.parallelcsrt;

public class App {
  

    /**
     * ars[0] = requestId
     * args[1] = customerCount
     * 
     * @param args
     */
    public static void main(String[] args) {
        final int maxCustomerCount = Integer.parseInt(args[1]);

        CallCsrt csrt = new CallCsrt();
        csrt.sendCsrtMain(args[0], maxCustomerCount);
    }


}
