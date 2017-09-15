package com.zd.wilddogdoctordemo.net;

import com.zd.wilddogdoctordemo.beans.Result;
import com.zd.wilddogdoctordemo.beans.User;


import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by dongjijin on 2017/8/10 0010.
 */

public interface NetService {

    @GET("doctor/login")
    Observable<Result<User>> login(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("mobile") String mobile, @Query("password") String password);


    @GET("doctor/register")
    Observable<Result<User>> register(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("mobile") String mobile, @Query("password") String password, @Query("ref") String ref);

    @Multipart
    @POST("doctor/uploadHead")
    Observable<Result<String>> uploadAvatar(
            @Part("ts") RequestBody ts, @Part("apiKey") RequestBody apiKey, @Part("sign") RequestBody sign,
            @Part("userId") RequestBody userId, @Part MultipartBody.Part upfile);

    @Multipart
    @POST("doctor/uploadAD")
    Observable<Result<String>> uploadDoctorAd(
            @Part("ts") RequestBody ts, @Part("apiKey") RequestBody apiKey, @Part("sign") RequestBody sign,
            @Part("userId") RequestBody userId, @Part MultipartBody.Part upfile);


    @GET("doctor/addVideoCall")
    Observable<Result<Object>> uploadVideoConversationRecord(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sig,
            @Query("userId") String userId, @Query("callId") String docId, @Query("start") long start,
            @Query("duration") long duration );

    @GET("doctor/getAmount")
    Observable<Result<Double>> getAmount(
            @Query("ts") String ts, @Query("apiKey") String apiKey, @Query("sign") String sign,
            @Query("userId") String userId
    );

}
