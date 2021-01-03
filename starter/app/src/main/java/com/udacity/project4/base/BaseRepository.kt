package com.udacity.project4.base

import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun <T : Any> safeApiCall(
            apiCall: suspend () -> T
    ) : Result<T> {
        return withContext(ioDispatcher){
            try {
                Result.Success(apiCall.invoke())
            }catch(throwable: Throwable){
                Result.Error(throwable.message, null)
            }
        }
    }

}