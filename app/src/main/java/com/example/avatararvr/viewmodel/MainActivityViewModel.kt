package com.example.avatararvr.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.avatararvr.model.*
import com.example.avatararvr.repository.MainActivityRepository


class MainActivityViewModel : ViewModel() {

    var servicesLiveData: MutableLiveData<ServicesSetterGetter>? = null
    var createAvatarLiveData: MutableLiveData<CreateAvatarSetterGetter>? = null
    var checkAvatarSttus: MutableLiveData<AvatarStatusSetterGetter>? = null
    var postZipSetterGetter: MutableLiveData<PostZipSetterGetter>? = null
    var postTokenSetterGetter: MutableLiveData<PostTokenSetterGetter>? = null


    fun getWebFormData(authorization: String): LiveData<ServicesSetterGetter>? {

        servicesLiveData = MainActivityRepository.getServicesApiCall(authorization)
        return servicesLiveData
    }


    fun createAvatar(authorization: String,selfie: String): LiveData<CreateAvatarSetterGetter>? {
        createAvatarLiveData = MainActivityRepository.postCreateAvatar(authorization,selfie)
        return createAvatarLiveData
    }

    fun getAvatarStatus(authorization: String,fileId: String): LiveData<AvatarStatusSetterGetter>? {
        checkAvatarSttus = MainActivityRepository.getAvatarStatus(authorization,fileId)
        return checkAvatarSttus
    }

    fun postZipFile(url: String,input:PostZipSetterGetterInput): LiveData<PostZipSetterGetter>? {
        postZipSetterGetter = MainActivityRepository.postZipFile(url,input)
        return postZipSetterGetter
    }

    fun postToken(authorization: String,grant_type: String): LiveData<PostTokenSetterGetter>? {
        postTokenSetterGetter = MainActivityRepository.postToken(authorization,grant_type)
        return postTokenSetterGetter
    }


}