package com.company;

import java.io.IOException;
import java.util.Map;

public class Main {
    private final Downloads downloads = new Downloads ();

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
            Map<Integer, String> stopIDToName = downloads.getStops ();
            BussLines bussLines = new BussLines ( stopIDToName, downloads );
            bussLines.getBussLines ();
            bussLines.printTopScorers();

        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}

