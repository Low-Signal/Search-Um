package com.mobile.searchum;

import java.util.Comparator;

public class ScoreObject implements Comparable<ScoreObject> {
    String usernameVal;
    int scoreVal;

    public ScoreObject(String usernameVal, String scoreVal){
        this.usernameVal = usernameVal;
        this.scoreVal = Integer.parseInt(scoreVal);
    }

    public String getUsername(){
        return usernameVal;
    }

    public int getScore(){
        return scoreVal;
    }

    public int compareTo(ScoreObject compareScore) {
        int compareQuantity = ((ScoreObject) compareScore).getScore();

        return compareQuantity - this.scoreVal;
    }

}
