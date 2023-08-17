package models;

import java.util.Date;

public class Breach {
    String company;
    long records_lost;
    Date date;
    String detailedStory;
    String[] sectorAndMethod;

    Breach(String company, long records_lost, Date date, String detailedStory, String[] sectorAndMethod){
        this.company = company;
        this.records_lost = records_lost;
        this.date = date;
        this.detailedStory = detailedStory;
        this.sectorAndMethod = sectorAndMethod;
    };

    Breach(){};

}
