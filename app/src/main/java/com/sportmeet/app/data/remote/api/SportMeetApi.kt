package com.sportmeet.app.data.remote.api

import com.sportmeet.app.data.remote.dto.ApiResponse
import com.sportmeet.app.data.remote.dto.AuthResponse
import com.sportmeet.app.data.remote.dto.CreateTournamentRequest
import com.sportmeet.app.data.remote.dto.LoginRequest
import com.sportmeet.app.data.remote.dto.RegisterRequest
import com.sportmeet.app.data.remote.dto.RegisterTeamRequest
import com.sportmeet.app.data.remote.dto.RegistrationResponse
import com.sportmeet.app.data.remote.dto.ReportResponse
import com.sportmeet.app.data.remote.dto.ReportTournamentRequest
import com.sportmeet.app.data.remote.dto.SearchTournamentsResponse
import com.sportmeet.app.data.remote.dto.TournamentResponse
import com.sportmeet.app.data.remote.dto.UpdateRegistrationStatusRequest
import com.sportmeet.app.data.remote.dto.UpdateStatusRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface SportMeetApi {

    // File Upload
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Map<String, String>>

    // Authentication
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Public / Player Tournaments
    @GET("api/tournaments")
    suspend fun getApprovedTournaments(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<SearchTournamentsResponse>

    @GET("api/tournaments/{id}")
    suspend fun getTournamentById(@Path("id") id: String): Response<TournamentResponse>

    @GET("api/tournaments/search")
    suspend fun searchTournaments(
        @Query("sport") sport: String? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<SearchTournamentsResponse>

    // Organizer Tournaments
    @POST("api/tournaments")
    suspend fun createTournament(@Body request: CreateTournamentRequest): Response<TournamentResponse>

    @GET("api/organizer/tournaments")
    suspend fun getOrganizerTournaments(): Response<List<TournamentResponse>>

    @GET("api/organizer/tournaments/{id}/registrations")
    suspend fun getTournamentRegistrations(@Path("id") tournamentId: String): Response<List<RegistrationResponse>>

    @PUT("api/organizer/registrations/{id}/status")
    suspend fun updateRegistrationStatus(
        @Path("id") registrationId: String,
        @Body request: UpdateRegistrationStatusRequest
    ): Response<RegistrationResponse>

    // Player Registrations
    @POST("api/tournaments/{id}/register")
    suspend fun registerForTournament(
        @Path("id") tournamentId: String,
        @Body request: RegisterTeamRequest
    ): Response<RegistrationResponse>

    @GET("api/player/registrations")
    suspend fun getMyRegistrations(): Response<List<RegistrationResponse>>

    // Favorites
    @POST("api/tournaments/{id}/favorite")
    suspend fun addFavorite(@Path("id") tournamentId: String): Response<ApiResponse<Boolean>>

    @DELETE("api/tournaments/{id}/favorite")
    suspend fun removeFavorite(@Path("id") tournamentId: String): Response<ApiResponse<Boolean>>

    @GET("api/favorites")
    suspend fun getMyFavorites(): Response<List<TournamentResponse>>

    // Reports
    @POST("api/tournaments/{id}/report")
    suspend fun reportTournament(
        @Path("id") tournamentId: String,
        @Body request: ReportTournamentRequest
    ): Response<ReportResponse>

    // Admin Endpoints
    @GET("api/admin/tournaments/pending")
    suspend fun getPendingTournaments(): Response<List<TournamentResponse>>

    @PUT("api/admin/tournaments/{id}/approve")
    suspend fun approveTournament(@Path("id") tournamentId: String): Response<TournamentResponse>

    @PUT("api/admin/tournaments/{id}/reject")
    suspend fun rejectTournament(
        @Path("id") tournamentId: String,
        @Body request: UpdateStatusRequest
    ): Response<TournamentResponse>

    @GET("api/admin/reports")
    suspend fun getReports(): Response<List<ReportResponse>>

    @GET("api/admin/stats")
    suspend fun getAdminStats(): Response<Map<String, Long>>
}
