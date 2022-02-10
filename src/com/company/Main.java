package com.company;


//import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private final Downloads downloads = new Downloads ();
    private Map<Integer, String> stopIDToName = new HashMap<> ();

    public static void main (String[] args) {
        Main main = new Main ();
        long start = System.currentTimeMillis ();
        main.run ();
        long end = System.currentTimeMillis ();
        long efficiency = (end-start);
        System.out.println (efficiency);
    }

    void run () {
        //Download stops
        try {
            stopIDToName = downloads.getStops ();
            BussLines bussLines = new BussLines ( stopIDToName, downloads );
            bussLines.getBussLines ();
            bussLines.printTopScorers();

        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}

