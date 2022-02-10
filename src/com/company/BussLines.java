package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

class BussLines {
    private final TopRankings topRank = new TopRankings ();
    Downloads downloads;
    private final Map<Integer, String> stopIDToName;

    public BussLines (Map<Integer, String> stopIDToName,Downloads downloads) {
        this.stopIDToName = stopIDToName;
        this.downloads = downloads;
    }

    /**
     * This method loops through the JSON object downloaded. Since the data structure is (by the keys)
     * ["LineNumber", "JourneyPatternPointNumber"] and JourneyPatternPointNumber is a stop id, there are multiple objects for the same bussline
     * This method gets the needed information from the current object at position i,
     * Sends the data to countStops(). -> read instructions for that method.
     * Receives updated count. Count has counted how many objects ahead in data are from the same line, there is no need to go over them again. i= counts lets the forloop to jump x nr of stops
     * ahead to lower the total number of iterations within the forloop.
     * @throws IOException
     */

    public void getBussLines () throws IOException {
        JSONArray data = downloads.getBussLines ();
        for (int i = 0; i < data.size (); i++) {
            int count = 0;
            ArrayList<String> busslines = new ArrayList<> ();
            JSONObject bussLine = (JSONObject) data.get ( i );
            String journeyPatterPointNumber = (String) bussLine.get ( "JourneyPatternPointNumber" );
            busslines.add ( journeyPatterPointNumber );
            String lineNr = (String) bussLine.get ( "LineNumber" );
            count = countStops ( data,i,count,busslines,lineNr );
            Buss bussLine1 = new Buss ( lineNr,busslines );
            topRank.AddToRank ( bussLine1 );
            i += count;
        }
    }

    /**
     *
     * @param data Array of data
     * @param i current position in the array of data
     * @param count counts how many objects ahead are from the same buss line.
     * @param busslines saves the steps ahead that are on the same bussline
     * @param lineNr
     * @returns counts to the
     */
    private int countStops (JSONArray data,int i,int count,ArrayList<String> busslines,String lineNr) {
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
        return count;
    }


    public void printTopScorers () {
        topRank.printTopScorers ();
    }


    class TopRankings {
        private static final int BUSSLINES_IN_RANK = 3;
        private final List<Buss> mostStops = new ArrayList<> ();

        void AddToRank (Buss buss) {
            //Add busline to top rank,
            mostStops.add ( buss );
            // sort with collections sort,
            Collections.sort ( mostStops );
            // delete the last element
            if (mostStops.size () > BUSSLINES_IN_RANK) {
                mostStops.remove ( mostStops.size () - 1 );
            }
        }

        void printTopScorers () {
            for (Buss buss : mostStops) {
                System.out.println ( buss.toString () );
            }
        }
    }
    class Buss implements Comparable<Buss> {
        String lineNo;
        List<String> bussStops;

        public Buss (String lineNo,List<String> bussStops) {
            this.lineNo = lineNo;
            this.bussStops = bussStops;
        }

        private String printStopNo () {
            StringBuilder stringBuilder = new StringBuilder ();
            stringBuilder.append ( "Stops at: " );
            for (String stopID : bussStops) {
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
            return "LineNumber='" + lineNo + '\'' +
                    ", bussStops=" + printStopNo () + "\n";
        }

        @Override
        public int compareTo (Buss o) {
            if (o != null) {
                return o.bussStops.size () < this.bussStops.size () ? -1 : 1;
            }
            throw new RuntimeException ();
        }
    }
}




