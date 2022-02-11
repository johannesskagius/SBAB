package com.company;

import java.io.IOException;
import java.util.Map;

public class Main {
    private final ApiConnection apiCon = new ApiConnection ();

    public static void main (String[] args) {
        Main main = new Main ();
        long start = System.currentTimeMillis ();
        main.run ();
        long end = System.currentTimeMillis ();
        long efficiency = (end-start);
        System.out.println (efficiency);
    }

    /**
     * runner method that calls the methods.
     */
    void run () {
        //Download stops
        try {
            Map<Integer, String> stopIDToName = apiCon.getStops ();
            BussLines bussLines = new BussLines ( stopIDToName,apiCon );
            bussLines.getBussLines ();
            bussLines.printTopScorers();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}

