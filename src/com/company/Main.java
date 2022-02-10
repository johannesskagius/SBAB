package com.company;


//import com.google.gson.JsonParser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    //private final Map<Long, Node> nodes = new HashMap<> ();
    //private final Run run;
    private final Map<String, ArrayList<String>> stopsWithList = new HashMap<> ();
    private final Map<String, ArrayList<String>> stopsWithListForloop = new HashMap<> ();
    private final Map<Integer, String> stopIDToName = new HashMap<> ();
    private final TopRank topRank = new TopRank ();

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
            getStops ();
            getBussLines ();
            topRank.printTopScorers ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    private void getStops () throws MalformedURLException {
        final URL url = new URL ( "https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=stop&JourneyPatternPointNumber" );
        if(checkURLConn (url )){
            //Download data
            try (InputStream is = url.openStream ()) {
                BufferedReader rd = new BufferedReader ( new InputStreamReader ( is,StandardCharsets.UTF_8 ) );
                String jsonText = readAll ( rd );
                JSONParser jsonParser = new JSONParser ();
                JSONObject allData = (JSONObject) jsonParser.parse (jsonText);
                JSONObject responseDatadata = (JSONObject) allData.get ( "ResponseData" );
                JSONArray data = (JSONArray) responseDatadata.get ( "Result" );

                for (Object stopPointData : data) {
                    JSONObject bussStop = (JSONObject) stopPointData;
                    int stopID = Integer.parseInt ( (String) bussStop.get ( "StopPointNumber" ) );
                    String stopName = (String) bussStop.get ( "StopPointName" );
                    stopIDToName.put ( stopID,stopName );
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace ();
            }
        }
    }

    private boolean checkURLConn(URL x){
        try {
            HttpURLConnection conn = (HttpURLConnection) x.openConnection ();
            conn.setRequestMethod ( "GET" );
            conn.connect ();
            int response = conn.getResponseCode ();
            if (response != 200) {
                throw new RuntimeException ( "Http response: " + response );
            } else {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
        return false;
    }

    private void getBussLines () throws IOException {
        URL url = new URL ( "https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=jour&DefaultTransportModeCode=BUS" );
        if(checkURLConn ( url )){
            readJsonFromUrl ( url );
        }
    }

    private String readAll (Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder ();
        int cp;
        while ((cp = rd.read ()) != -1) {
            sb.append ( (char) cp );
        }
        return sb.toString ();
    }
    //"LineNumber":"626","DirectionCode":"1"
    public void readJsonFromUrl (URL url) throws IOException {
        try (InputStream is = url.openStream ()) {
            BufferedReader rd = new BufferedReader ( new InputStreamReader ( is,StandardCharsets.UTF_8 ) );
            String jsonText = readAll ( rd );
            JSONParser jsonParser = new JSONParser ();
            try {
                JSONObject jsonObject = (JSONObject) jsonParser.parse ( jsonText );
                JSONObject responseDatadata = (JSONObject) jsonObject.get ( "ResponseData" );
                JSONArray data = (JSONArray) responseDatadata.get ( "Result" );

                Iterator iterator = data.listIterator ();
                ArrayList<BussLine> test = new ArrayList<> ();
                String lastLineNr = "";

                for (int i = 0; i < data.size (); i++) {
                    int count = 0;
                    ArrayList<String> busslines = new ArrayList<> ();
                    JSONObject bussLine = (JSONObject) data.get ( i );
                    String journeyPatterPointNumber = (String) bussLine.get ( "JourneyPatternPointNumber" );
                    busslines.add ( journeyPatterPointNumber );
                    String lineNr = (String) bussLine.get ( "LineNumber" );

                    for (int j = i; j < data.size (); j++) {
                        JSONObject nextBussLine2 = (JSONObject) data.get ( j );
                        String lineNr2 = (String) nextBussLine2.get ( "LineNumber" );
                        String journeyPatterPointNumber2 = (String) nextBussLine2.get ( "JourneyPatternPointNumber" );
                        if (!lineNr.equalsIgnoreCase ( lineNr2 )) {
                            break;
                        } else {
                            busslines.add ( journeyPatterPointNumber2 );
                            count++;
                        }
                    }
                    BussLine bussLine1 = new BussLine ( lineNr,busslines );
                    stopsWithListForloop.putIfAbsent ( lineNr,busslines );
                    topRank.AddToRank ( bussLine1 );
                    i += count;

                }

            } catch (ParseException e) {
                e.printStackTrace ();
            }
        }
    }
    class BussLine implements Comparable<BussLine> {
        String LineNumber;
        List<String> bussStops;

        public BussLine (String lineNumber,List<String> bussStops) {
            LineNumber = lineNumber;
            this.bussStops = bussStops;
        }
        public void addStop (ArrayList<String> bussStops) {
            this.bussStops = bussStops;
        }


        private String printStopNo(){
            StringBuilder stringBuilder = new StringBuilder ();
            stringBuilder.append ( "Stops at: " );
            for(String stopID : bussStops){
                //Get stopname from hashmap
                int stop = Integer.parseInt ( stopID );
                String stopName = stopIDToName.get ( stop );
                //append to string
                stringBuilder.append ( stopName ).append ( "," ).append ( "\t" );
            }
            return stringBuilder.toString ();
        }

        @Override
        public String toString () {
            return "LineNumber='" + LineNumber + '\'' +
                    ", bussStops=" + printStopNo () + "\n";
        }


        @Override
        public int compareTo (BussLine o) {
            if (o != null) {
                return o.bussStops.size () < this.bussStops.size () ? -1 : 1;
            }
            throw new RuntimeException ();
        }
    }
    class TopRank {
        private static final int BUSSLINES_IN_RANK = 3;
        private final List<BussLine> mostStops = new ArrayList<> ();

        void AddToRank (BussLine bussLine) {
            //Add busline to top rank,
            mostStops.add ( bussLine );
            // sort with collections sort,
            Collections.sort ( mostStops );
            // delete the last element
            if (mostStops.size () > BUSSLINES_IN_RANK) {
                mostStops.remove ( mostStops.size () - 1 );
            }
        }

        void printTopScorers () {
            for (BussLine bussLine : mostStops) {
                System.out.println (bussLine.toString ());
            }
        }
    }
}

