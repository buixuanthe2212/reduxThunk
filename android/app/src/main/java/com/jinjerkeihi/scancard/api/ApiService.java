package com.jinjerkeihi.scancard.api;

import com.jinjerkeihi.scancard.api.response.ListICCardResponse;
import com.jinjerkeihi.scancard.api.response.SendCardResponse;
import com.jinjerkeihi.scancard.dto.TripTotal;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * A interface uses to request API.
 *
 * @author LongHV3
 */
public interface ApiService {

    String SEND_DATA_IC_CARD = "/v1/ic_card/save";
    String GET_LIST_IC_CARD = "/v1/ic_card?data_start_station=abc&data_end_station=demo&use_date_from=2018-02-02";

    @FormUrlEncoded
    @POST(SEND_DATA_IC_CARD)
    Call<SendCardResponse> sendDataICCard(@Field(ApiParameter.USER_ID) int userId, @Field(ApiParameter.TRANSPORTATION_EXPENSE_ID) int transportation_expense_id, @Field(ApiParameter.TRIPS) String trips);

    @GET(GET_LIST_IC_CARD)
    Call<ListICCardResponse> getListICCard();

}
