package com.jinjerkeihi.scancard.dto;

import com.google.gson.annotations.SerializedName;

/**
 *  XUAN_THE on 5/21/2018.
 */

public class CardInfo {

    @SerializedName("use_date")
    public String use_date;

    @SerializedName("fare")
    public String fare;

    @SerializedName("getoff_id")
    public String getoff_id;

    @SerializedName("boarding_id")
    public String boarding_id;

    @SerializedName("data_start_station")
    public String data_start_station;

    @SerializedName("data_end_station")
    public String data_end_station;

}
