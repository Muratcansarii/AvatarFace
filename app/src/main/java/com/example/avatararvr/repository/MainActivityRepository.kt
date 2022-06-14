package com.example.avatararvr.repository

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.avatararvr.model.*
import com.example.avatararvr.retrofit.RetrofitClient
import com.google.android.material.internal.ContextUtils.getActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

object MainActivityRepository {

    val serviceSetterGetter = MutableLiveData<ServicesSetterGetter>()
    val createAvatarSetterGetter = MutableLiveData<CreateAvatarSetterGetter>()
    val avatarStatusSetterGetter = MutableLiveData<AvatarStatusSetterGetter>()
    val postZipSetterGetter = MutableLiveData<PostZipSetterGetter>()
    val postTokenSetterGetter = MutableLiveData<PostTokenSetterGetter>()


    fun getServicesApiCall(authorization: String): MutableLiveData<ServicesSetterGetter> {
        val call = RetrofitClient.apiInterface.getWebFormURL(authorization,"comment")
        call.enqueue(object : Callback<ServicesSetterGetter> {
            override fun onFailure(call: Call<ServicesSetterGetter>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<ServicesSetterGetter>,
                response: Response<ServicesSetterGetter>
            ) {
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()


                val code = data!!.code
                val url = data!!.url
                val expires = data!!.expires
                val upload_page_visited = data!!.upload_page_visited
                val file_url = data!!.file_url
                val upload_page_url = data!!.upload_page_url
                val upload_page_qr = data!!.upload_page_qr

                serviceSetterGetter.value = ServicesSetterGetter(
                    code,
                    url,
                    expires,
                    upload_page_visited,
                    file_url,
                    upload_page_url,
                    upload_page_qr
                )
            }
        })

        return serviceSetterGetter
    }

    fun postCreateAvatar(authorization: String, selfie: String): MutableLiveData<CreateAvatarSetterGetter> {
        val call = RetrofitClient.apiInterface.createAvatar(
            authorization,
            "test from curl sample", "head_1.2", "base/mobile",
            "{\"model_info\": {\"plus\":[\"gender\",\"age\",\"race\"]},\"additional_textures\": {\"plus\":[\"lips_mask\"]},\"blendshapes\": {\"base\":[\"mobile_51\"]}}",
            "{\"format\": \"gltf\", \"blendshapes\": {\"list\": [\"mobile_51\"]}}", selfie
        )
        call.enqueue(object : Callback<CreateAvatarSetterGetter> {
            override fun onFailure(call: Call<CreateAvatarSetterGetter>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<CreateAvatarSetterGetter>,
                response: Response<CreateAvatarSetterGetter>
            ) {
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()


                val url = data!!.url
                val code = data!!.code
                val status = data!!.status
                val progress = data!!.progress
                val created_on = data!!.created_on
                val name = data!!.name
                val description = data!!.description

                createAvatarSetterGetter.value = CreateAvatarSetterGetter(
                    url,
                    code,
                    status,
                    progress,
                    created_on,
                    name,
                    description
                )
            }
        })

        return createAvatarSetterGetter
    }

    fun getAvatarStatus(authorization: String, fileId: String): MutableLiveData<AvatarStatusSetterGetter> {
        val call = RetrofitClient.apiInterface.getAvatarStatus(
            authorization,
            fileId
        )
        call.enqueue(object : Callback<ArrayList<AvatarStatusSetterGetter>> {
            override fun onFailure(call: Call<ArrayList<AvatarStatusSetterGetter>>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<ArrayList<AvatarStatusSetterGetter>>,
                response: Response<ArrayList<AvatarStatusSetterGetter>>
            ) {
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()

                if (data == null) {
                    avatarStatusSetterGetter.value = AvatarStatusSetterGetter(
                        "code",
                        "avatar_code",
                        "pending"
                    )
                } else {
                    val code = data!![0].code
                    val avatar_code = data!![0].avatar_code
                    val status = data!![0].status

                    avatarStatusSetterGetter.value = AvatarStatusSetterGetter(
                        code,
                        avatar_code,
                        status
                    )
                }
            }
        })

        return avatarStatusSetterGetter
    }

    fun postZipFile(
        url: String,
        input: PostZipSetterGetterInput
    ): MutableLiveData<PostZipSetterGetter> {
        val call = RetrofitClient.apiInterface.postZipFile(
            url,
            input
        )
        call.enqueue(object : Callback<PostZipSetterGetter> {
            override fun onFailure(call: Call<PostZipSetterGetter>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<PostZipSetterGetter>,
                response: Response<PostZipSetterGetter>
            ) {
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()

                if (data == null) {

                }
            }
        })
        return postZipSetterGetter
    }

    fun postToken(
        authorization: String,
        grant_type: String
    ): MutableLiveData<PostTokenSetterGetter> {
        val call = RetrofitClient.apiInterface.postToken(
            authorization,
            grant_type
        )
        call.enqueue(object : Callback<PostTokenSetterGetter> {
            override fun onFailure(call: Call<PostTokenSetterGetter>, t: Throwable) {
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<PostTokenSetterGetter>,
                response: Response<PostTokenSetterGetter>
            ) {
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()

                postTokenSetterGetter.value = PostTokenSetterGetter(
                    data!!.access_token,
                    data!!.token_type,
                )

            }
        })
        return postTokenSetterGetter
    }

}