package com.jinjerkeihi.scancard.api.response;

import com.google.gson.annotations.SerializedName;
import com.jinjerkeihi.scancard.util.StringUtil;

/**
 * Description
 *
 * @author Pika Long.
 */
public class SendCardResponse extends BaseResponse {

    @SerializedName("content")
    public String content;

}