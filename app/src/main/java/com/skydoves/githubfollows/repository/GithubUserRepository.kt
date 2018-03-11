package com.skydoves.githubfollows.repository

import android.arch.lifecycle.LiveData
import com.skydoves.githubfollows.api.ApiResponse
import com.skydoves.githubfollows.api.GithubService
import com.skydoves.githubfollows.models.Envelope
import com.skydoves.githubfollows.models.GithubUser
import com.skydoves.githubfollows.models.Resource
import com.skydoves.githubfollows.preference.PreferenceComponent_PrefAppComponent
import com.skydoves.githubfollows.preference.Preference_UserProfile
import com.skydoves.githubfollows.room.GithubUserDao
import com.skydoves.preferenceroom.InjectPreference
import org.jetbrains.anko.doAsync
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**s
 * Created by skydoves on 2018. 3. 6.
 * Copyright (c) 2018 battleent All rights reserved.
 */

@Singleton
class GithubUserRepository @Inject
constructor(val githubUserDao: GithubUserDao, val service: GithubService) {

    @InjectPreference lateinit var profile: Preference_UserProfile

    init {
        Timber.d("Injection GithubUserRepository")
        PreferenceComponent_PrefAppComponent.getInstance().inject(this)
    }

    fun refreshUser(user: String) {
        profile.putName(user)
        doAsync { githubUserDao.truncateGithubUser() }
    }

    fun loadUser(user: String): LiveData<Resource<GithubUser>> {
        return object: NetworkBoundRepository<GithubUser, GithubUser>() {
            override fun saveFetchData(item: GithubUser) {
                doAsync { githubUserDao.insertGithubUser(item) }
            }

            override fun shouldFetch(data: GithubUser?): Boolean {
                return data == null
            }

            override fun loadFromDb(): LiveData<GithubUser> {
                return githubUserDao.getGithubUser(user)
            }

            override fun fetchService(): LiveData<ApiResponse<GithubUser>> {
                return service.fetchGithubUser(user)
            }

            override fun onFetchFailed(envelope: Envelope?) {
                Timber.d("onFetchFailed : $envelope")
            }
        }.asLiveData()
    }

    fun getUserKeyName(): String {
        return profile.nameKeyName()
    }

    fun getPreferenceMenuPosition(): Int {
        return profile.menuPosition
    }

    fun putPreferenceMenuPosition(position: Int) {
        profile.putMenuPosition(position)
    }

    fun getUserName(): String {
        return profile.name
    }
}
