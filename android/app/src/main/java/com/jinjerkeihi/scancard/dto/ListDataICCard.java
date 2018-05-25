package com.jinjerkeihi.scancard.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * XUAN_THE on 5/21/2018.
 */

public class ListDataICCard {
    @SerializedName("data")
    public ArrayList<CardInfo> content;

}
