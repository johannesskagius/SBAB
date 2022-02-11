package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiConnection {
    /**
     * Download all the stops from the api, saves the position in a hashmap.
     * @return Map with buss stops id as key and buss stop name as vale
     * @throws MalformedURLException if URL connection isn't good.
     */

    // https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=SiteId
    Map<Integer, String> getStops () throws MalformedURLException {
        Map<Integer, String> stopIDToName = new HashMap<> ();
        final URL url = new URL ( "https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=stop" );
        if (checkURLConn ( url )) {
            //Download data
            try (InputStream is = url.openStream ()) {
                JSONArray data = getJsonArray ( is );

                for (Object stopPointData : data) {
                    JSONObject bussStop = (JSONObject) stopPointData;
                    int stopID = Integer.parseInt ( (String) bussStop.get ( "StopPointNumber" ) );
                    String stopName = (String) bussStop.get ( "StopPointName" );
                    stopIDToName.put ( stopID,stopName );
                }
                return stopIDToName;
            } catch (IOException | ParseException e) {
                e.printStackTrace ();
            }
        }
        throw new RuntimeException ();
    }

    JSONArray getBussLines () throws MalformedURLException {
        final URL URL = new URL ( "https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=jour&DefaultTransportModeCode=BUS" );
        try (InputStream is = URL.openStream ()) {
            return getJsonArray ( is );

        } catch (ParseException | IOException e) {
            e.printStackTrace ();
        }
        throw new RuntimeException ();
    }

    /**
     * Method takes an InputStream and converts it to a jsonArray;
     *
     * @param is takes inputstream
     * @return The JsonArray our data is in.
     * @throws IOException throws exception if readall fails.
     * @throws ParseException if the jsonText can't be parsed to a json Object.
     */
    private JSONArray getJsonArray (InputStream is) throws IOException, ParseException {
        BufferedReader rd = new BufferedReader ( new InputStreamReader ( is,StandardCharsets.UTF_8 ) );
        String jsonText = readAll ( rd );
        JSONParser jsonParser = new JSONParser ();
        is.close ();

        JSONObject jsonObject = (JSONObject) jsonParser.parse ( jsonText );
        JSONObject responseDatadata = (JSONObject) jsonObject.get ( "ResponseData" );
        return (JSONArray) responseDatadata.get ( "Result" );
    }


    /**
     * Support  methods
     * @param rd is a buffered reader with assigned values
     * @return a JSON string
     * @throws IOException if the rd fails for some reason
     */

    private String readAll (Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder ();
        int cp;
        while ((cp = rd.read ()) != -1) {
            sb.append ( (char) cp );
        }
        return sb.toString ();
    }

    private boolean checkURLConn (URL x) {
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
}
