package com.jinjerkeihi.scancard.api.response;

import com.google.gson.annotations.SerializedName;
import com.jinjerkeihi.scancard.dto.ListDataICCard;

/**
 * XUAN_THE on 5/21/2018.
 */

public class ListICCardResponse extends BaseResponse {
    @SerializedName("content")
    public ListDataICCard content;
}
