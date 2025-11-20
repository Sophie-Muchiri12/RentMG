package com.example.rentmg.api

import com.example.rentmg.data.model.*
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit API Service Interface
 * Defines all REST API endpoints for communication with the backend
 * Base URL is configured in ApiClient (http://10.0.2.2:5000/ for Android emulator)
 */
interface ApiService {

    // ============================================
    // AUTHENTICATION ENDPOINTS
    // ============================================

    /**
     * Login endpoint
     * Authenticates user and returns JWT access token
     *
     * @param body LoginRequest containing email and password
     * @return LoginResponse with access token and user info
     * @throws 401 if credentials are invalid
     */
    @POST("/api/auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    /**
     * Register endpoint
     * Creates a new user account
     *
     * @param body RegisterRequest with email, password, role, and full name
     * @return RegisterResponse with new user info
     * @throws 400 if email already exists or validation fails
     */
    @POST("/api/auth/register")
    fun register(@Body body: RegisterRequest): Call<RegisterResponse>

    /**
     * Get current user info
     * Retrieves authenticated user's profile information
     * Requires JWT token in Authorization header (handled by AuthInterceptor)
     *
     * @return User object with current user's data
     * @throws 401 if token is invalid or expired
     */
    @GET("/api/auth/me")
    fun getCurrentUser(): Call<User>

    // ============================================
    // PROPERTY ENDPOINTS
    // ============================================

    /**
     * List properties
     * Landlords see only their properties
     * Requires authentication
     *
     * @return List of Property objects
     */
    @GET("/api/properties/")
    fun listProperties(): Call<List<Property>>

    /**
     * Create property
     * Only landlords can create properties
     * Requires authentication
     *
     * @param body PropertyCreateRequest with name and address
     * @return Created Property object
     * @throws 403 if user is not a landlord
     */
    @POST("/api/properties/")
    fun createProperty(@Body body: PropertyCreateRequest): Call<Property>

    /**
     * Get single property by ID
     * Requires authentication
     *
     * @param id Property ID
     * @return Property object
     * @throws 404 if property not found
     */
    @GET("/api/properties/{id}")
    fun getProperty(@Path("id") id: Int): Call<Property>

    // ============================================
    // UNIT ENDPOINTS
    // ============================================

    /**
     * List units for a property
     * Returns all units belonging to a specific property
     * Requires authentication
     *
     * @param propertyId Property ID
     * @return List of Unit objects
     */
    @GET("/api/units/by-property/{property_id}")
    fun listUnitsByProperty(@Path("property_id") propertyId: Int): Call<List<Unit>>

    /**
     * Create unit
     * Only landlords can create units
     * Requires authentication
     *
     * @param body UnitCreateRequest with code, rent amount, and property ID
     * @return Created Unit object
     * @throws 403 if user is not a landlord
     */
    @POST("/api/units/")
    fun createUnit(@Body body: UnitCreateRequest): Call<Unit>

    // ============================================
    // LEASE ENDPOINTS
    // ============================================

    /**
     * List leases
     * Tenants see only their own leases
     * Landlords see leases for their properties
     * Requires authentication
     *
     * @return List of Lease objects
     */
    @GET("/api/leases/")
    fun listLeases(): Call<List<Lease>>

    /**
     * Create lease
     * Assigns a tenant to a unit
     * Only landlords can create leases
     * Requires authentication
     *
     * @param body LeaseCreateRequest with unit ID, tenant ID, dates, and status
     * @return Created Lease object
     * @throws 403 if user is not a landlord
     */
    @POST("/api/leases/")
    fun createLease(@Body body: LeaseCreateRequest): Call<Lease>

    // ============================================
    // PAYMENT ENDPOINTS
    // ============================================

    /**
     * Initiate M-Pesa payment
     * Triggers M-Pesa STK push to tenant's phone
     * Creates payment record in pending status
     * Requires authentication
     *
     * @param body PaymentInitRequest with lease ID, amount, and phone number
     * @return PaymentInitResponse with payment ID and status message
     * @throws 400 if validation fails
     * @throws 500 if M-Pesa API fails
     */
    @POST("/api/payments/mpesa/initiate")
    fun initiatePayment(@Body body: PaymentInitRequest): Call<PaymentInitResponse>

    /**
     * Get payment by ID
     * Retrieves details of a specific payment
     * Used for polling payment status after initiation
     * Requires authentication
     *
     * @param id Payment ID
     * @return Payment object
     * @throws 404 if payment not found
     */
    @GET("/api/payments/{id}")
    fun getPayment(@Path("id") id: Int): Call<Payment>

    /**
     * Get payment history
     * Retrieves all payments, optionally filtered by lease
     * Tenants see only their payments
     * Landlords see payments for their properties
     * Requires authentication
     *
     * @param leaseId Optional lease ID to filter by specific lease
     * @return List of Payment objects
     */
    @GET("/api/payments/history")
    fun getPaymentHistory(@Query("lease_id") leaseId: Int? = null): Call<List<Payment>>

    // ============================================
    // ISSUE ENDPOINTS
    // ============================================

    /**
     * List issues
     * Tenants see only issues they reported
     * Landlords see all issues for their properties
     * Requires authentication
     *
     * @return List of Issue objects
     */
    @GET("/api/issues/")
    fun listIssues(): Call<List<Issue>>

    /**
     * Create issue
     * Tenants can report maintenance issues
     * Requires authentication
     *
     * @param body IssueCreateRequest with title, description, property/unit IDs, priority
     * @return Created Issue object
     */
    @POST("/api/issues/")
    fun createIssue(@Body body: IssueCreateRequest): Call<Issue>

    /**
     * Update issue
     * Landlords can update status, assign to someone, or change priority
     * Requires authentication
     *
     * @param id Issue ID
     * @param body IssueUpdateRequest with fields to update
     * @return Updated Issue object
     * @throws 404 if issue not found
     * @throws 403 if user doesn't have permission
     */
    @PATCH("/api/issues/{id}")
    fun updateIssue(@Path("id") id: Int, @Body body: IssueUpdateRequest): Call<Issue>
}

/**
 * Login request model
 * @property email User's email address
 * @property password User's password
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Login response model
 * @property access_token JWT token for authenticated requests
 * @property user User object containing user details
 */
data class LoginResponse(
    val access_token: String?,
    val user: User?
)
