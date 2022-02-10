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

public class Downloads {
     Map<Integer, String> getStops () throws MalformedURLException {
        Map<Integer, String> stopIDToName = new HashMap<> ();

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
                return stopIDToName;
            } catch (IOException | ParseException e) {
                e.printStackTrace ();
            }
        }
        throw new RuntimeException ();
    }


    JSONArray getBussLines () throws IOException {
        final URL URL = new URL ( "https://api.sl.se/api2/linedata.json?key=5da196d47f8f4e5facdb68d2e25b9eae&model=jour&DefaultTransportModeCode=BUS" );
        try (InputStream is = URL.openStream ()) {
            BufferedReader rd = new BufferedReader ( new InputStreamReader ( is,StandardCharsets.UTF_8 ) );
            String jsonText = readAll ( rd );
            JSONParser jsonParser = new JSONParser ();
            is.close ();

            JSONObject jsonObject = (JSONObject) jsonParser.parse ( jsonText );
            JSONObject responseDatadata = (JSONObject) jsonObject.get ( "ResponseData" );
            return (JSONArray) responseDatadata.get ( "Result" );

        } catch (ParseException e) {
            e.printStackTrace ();
        }
        throw new RuntimeException ();
    }

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
