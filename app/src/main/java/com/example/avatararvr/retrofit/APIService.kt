package com.example.avatararvr.retrofit

import com.example.avatararvr.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface APIService {


    @POST("/selfies/")
    @FormUrlEncoded
    fun getWebFormURL(
        @Header("Authorization") authorization: String,
        @Field("comment") comment: String
    ): Call<ServicesSetterGetter>

    @POST("/avatars/")
    @FormUrlEncoded
    fun createAvatar(
        @Header("Authorization") authorization: String,
        @Field("name") name: String,
        @Field("pipeline") pipeline: String,
        @Field("pipeline_subtype") pipeline_subtype: String,
        @Field("parameters") parameters: String,
        @Field("export_parameters") export_parameters: String,
        @Field("selfie") comment: String
    ): Call<CreateAvatarSetterGetter>

    @GET("avatars/{fileId}/exports/")
    fun getAvatarStatus(
        @Header("Authorization") authorization: String,
        @Path("fileId") fileId: String
    ): Call<ArrayList<AvatarStatusSetterGetter>>

    @GET("avatars/{avatarCode}/exports/{code}/files/avatar/file/")
    @Streaming
    fun downloadZipFile(
        @Header("Authorization") authorization: String,
        @Path("code") code: String,
        @Path("avatarCode") avatar_code: String
    ): Call<ResponseBody>

    @POST
    fun postZipFile(
        @Url url: String,
        @Body input: PostZipSetterGetterInput
    ): Call<PostZipSetterGetter>

    @POST("/o/token/")
    @FormUrlEncoded
    fun postToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grant_type: String,
    ): Call<PostTokenSetterGetter>

}