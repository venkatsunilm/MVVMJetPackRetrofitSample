package com.venkat.digitalclinic.apiservice.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.venkat.digitalclinic.apiservice.utils.RetryCallback
import com.venkat.digitalclinic.apiservice.data.repository.contracts.IPatientDetailsRepository
import com.venkat.digitalclinic.apiservice.utils.mockdata.EventsMockList
import com.venkat.digitalclinic.apiservice.data.remote.IPatientDetailsService
import com.venkat.digitalclinic.apiservice.utils.ResponseError
import com.venkat.digitalclinic.apiservice.models.DigitalClinic
import com.venkat.digitalclinic.apiservice.models.PatientEvent
import com.venkat.digitalclinic.apiservice.models.PatientPrescription
import com.venkat.digitalclinic.apiservice.models.ResponseObject
import com.venkat.digitalclinic.apiservice.utils.AppPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientDetailsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val patientDetailsService: IPatientDetailsService
) : IPatientDetailsRepository {

//    private var patientDetailsService: IPatientDetailsService = RetrofitClient.getInstance()
//        .create(IPatientDetailsService::class.java)

    private var appPreference: AppPreference = AppPreference.getInstance(context)

    // TODO: Transform the rest methods similar to getEvents Design.
    override suspend fun getEvents(): List<PatientEvent> {
        try {
            val token = appPreference.getString(AppPreference.Keys.TOKEN)
            return patientDetailsService.getEvents(token)
        } catch (error: Throwable) {
            throw ResponseError("Error fetching events", error)
        }
    }

    // TODO: Transform the rest methods similar to getEvents Design.
    override fun getPrescriptions(): LiveData<ResponseObject<List<PatientPrescription>>> {
        val responseData: MutableLiveData<ResponseObject<List<PatientPrescription>>> =
            MutableLiveData()
        val token = appPreference.getString(AppPreference.Keys.TOKEN)
        patientDetailsService.getPrescriptions(token)
            .enqueue(RetryCallback(object : Callback<List<PatientPrescription>> {

                override fun onFailure(call: Call<List<PatientPrescription>>, t: Throwable) {
                    responseData.postValue(
                        ResponseObject(
                            EventsMockList.getPrescriptionMockList(),
                            t.message
                        )
                    )
                }

                override fun onResponse(
                    call: Call<List<PatientPrescription>>,
                    response: Response<List<PatientPrescription>>
                ) {
                    if (response.isSuccessful) {
                        responseData.postValue(
                            ResponseObject(
                                response.body(),
                                statusCode = response.code()
                            )
                        )
                    } else {
                        responseData.postValue(
                            ResponseObject(
                                null,
                                response.message(),
                                response.code()
                            )
                        )
                    }
                }

            }))
        return responseData
    }

    override fun getDigitalClinicInfo(): LiveData<ResponseObject<DigitalClinic>> {
        val responseData: MutableLiveData<ResponseObject<DigitalClinic>> = MutableLiveData()
        val token = appPreference.getString(AppPreference.Keys.TOKEN)
        patientDetailsService.getDigitalClinic(token)
            .enqueue(RetryCallback(object : Callback<DigitalClinic> {

                override fun onFailure(call: Call<DigitalClinic>, t: Throwable) {
                    responseData.postValue(ResponseObject(null, t.message))
                }

                override fun onResponse(
                    call: Call<DigitalClinic>,
                    response: Response<DigitalClinic>
                ) {
                    if (response.isSuccessful) {
                        responseData.postValue(
                            ResponseObject(
                                response.body(),
                                statusCode = response.code()
                            )
                        )
                    } else {
                        responseData.postValue(
                            ResponseObject(
                                null,
                                response.message(),
                                response.code()
                            )
                        )
                    }
                }

            }))
        return responseData
    }

    companion object {
        const val DATA_KEY = "data"
    }
}