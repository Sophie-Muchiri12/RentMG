# RentMG - Rental Management System

A comprehensive rental property management Android application built with Kotlin and Flask backend, featuring M-Pesa payment integration for seamless rent collection.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Architecture](#project-architecture)
- [Setup Instructions](#setup-instructions)
- [API Documentation](#api-documentation)
- [User Guide](#user-guide)
- [Code Structure](#code-structure)
- [Troubleshooting](#troubleshooting)

---

## Overview

RentMG is a complete rental management solution designed for the Kenyan market, providing separate interfaces for landlords/property managers and tenants. The system handles property management, lease tracking, rent collection via M-Pesa, payment history, and issue reporting.

### Key Highlights

- **Role-Based Access**: Separate dashboards for tenants, landlords, and property managers
- **M-Pesa Integration**: Real-time STK push payments with status tracking
- **JWT Authentication**: Secure token-based authentication system
- **Real-Time Updates**: Payment status polling and live data synchronization
- **Clean Architecture**: Well-commented, organized codebase following Android best practices

---

## Features

### Tenant Features ✅ (100% Complete)

- **Authentication**
  - Sign in with email/password
  - Sign up with role selection (Tenant/Landlord/Property Manager)
  - JWT token management with auto-login
  - Secure logout with session clearing

- **Dashboard**
  - View current rent status (Paid/Unpaid)
  - Property and unit details display
  - Monthly rent amount and due dates
  - Quick access to payment

- **Payment System**
  - M-Pesa STK push integration
  - Real-time payment status tracking
  - Phone number validation and formatting
  - Transaction reference tracking
  - Success/failure notifications

- **Payment History**
  - Complete transaction history
  - Status-based color coding (Completed/Pending/Failed)
  - Date and amount formatting
  - Transaction reference display

- **Profile & Settings**
  - User information display
  - Account statistics
  - Join date tracking
  - Logout functionality

### Landlord Features 🚧 (40% Complete)

- **Dashboard Structure** ✅
  - Bottom navigation setup
  - Fragment container architecture
  - Role-based routing

- **Home Dashboard** ⏳ (Pending)
  - Property statistics
  - Revenue tracking
  - Recent activities

- **Property Management** ⏳ (Pending)
  - Add/edit/delete properties
  - Property list view
  - Unit management per property

- **Tenant Management** ⏳ (Pending)
  - View tenant list
  - Assign tenants to units
  - Lease creation and management

- **Issue Reporting** ⏳ (Pending)
  - View tenant-reported issues
  - Update issue status
  - Priority management

---

## Tech Stack

### Frontend (Android)

- **Language**: Kotlin 1.9.0
- **Build System**: Gradle 8.7.0
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)

### Libraries

- **Networking**: Retrofit 2.9.0 + OkHttp 4.11.0
- **JSON Serialization**: Gson 2.10.1
- **UI Components**: Material Design 1.11.0
- **HTTP Logging**: OkHttp Logging Interceptor 4.11.0

### Backend

- **Framework**: Flask (Python)
- **Database**: SQLAlchemy ORM
- **Authentication**: JWT (JSON Web Tokens)
- **Payment**: M-Pesa Daraja API (Sandbox)

---

## Project Architecture

### Architecture Pattern

The app follows **MVC (Model-View-Controller)** pattern with repository-like structure, ready for migration to MVVM:

```
┌─────────────────┐
│     View        │  Activities & Fragments (UI)
│  (Activities)   │
└────────┬────────┘
         │
┌────────▼────────┐
│   Controller    │  Business Logic & Event Handling
│  (Activities)   │
└────────┬────────┘
         │
┌────────▼────────┐
│  Data Layer     │  API Service, Models, AppManager
│  (Repository)   │
└─────────────────┘
```

### Package Structure

```
com/example/rentmg/
├── MainActivity.kt                    # App entry point
├── api/                               # API layer
│   ├── ApiClient.kt                   # Retrofit client builder
│   ├── ApiService.kt                  # API endpoint definitions
│   ├── AuthInterceptor.kt             # JWT token injection
│   └── TokenStore.kt                  # Token persistence
├── data/model/                        # Data models
│   ├── User.kt                        # User model
│   ├── Property.kt                    # Property model
│   ├── Unit.kt                        # Unit model
│   ├── Lease.kt                       # Lease model
│   ├── Payment.kt                     # Payment model
│   └── Issue.kt                       # Issue model
├── pages/                             # UI screens
│   ├── auth/                          # Authentication
│   │   ├── SignInActivity.kt          # Login screen
│   │   └── SignUpActivity.kt          # Registration screen
│   ├── dashboard/                     # Dashboard screens
│   │   ├── DashboardActivity.kt       # Landlord dashboard
│   │   ├── TenantDashboardActivity.kt # Tenant dashboard
│   │   ├── home/                      # Home fragments
│   │   │   ├── HomeFragment.kt        # Landlord home
│   │   │   └── TenantDashboardFragment.kt
│   │   ├── tenant/                    # Tenant fragments
│   │   │   ├── HistoryFragment.kt     # Payment history
│   │   │   └── ProfileFragment.kt     # Profile & settings
│   │   └── settings/
│   │       └── SettingsFragment.kt
│   └── payment/
│       └── CheckoutActivity.kt        # M-Pesa payment
└── util/
    └── AppManager.kt                  # Global state manager
```

### Data Flow

1. **Authentication Flow**:
   ```
   SignInActivity → ApiService.login() → Backend
   Backend → JWT Token → TokenStore → AppManager
   AppManager → Role-based Navigation → Dashboard
   ```

2. **Payment Flow**:
   ```
   TenantDashboard → CheckoutActivity → M-Pesa STK Push
   Backend → M-Pesa API → User's Phone
   User confirms → Backend updates Payment status
   CheckoutActivity polls → Payment status → Success/Failure UI
   ```

3. **Data Fetching Flow**:
   ```
   Fragment → showLoading() → AppManager.getApiService()
   ApiService → Retrofit Call → Backend API
   Response → Update UI → showContent() / showError()
   ```

---

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Python 3.8+ (for backend)
- Android device/emulator with API 26+

### Backend Setup

1. **Navigate to backend directory**:
   ```bash
   cd rentmg_backend
   ```

2. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment variables**:
   Create `.env` file:
   ```env
   FLASK_APP=app.py
   FLASK_ENV=development
   SECRET_KEY=your-secret-key-here
   DATABASE_URL=sqlite:///rentmg.db

   # M-Pesa Daraja API Credentials (Sandbox)
   MPESA_CONSUMER_KEY=your-consumer-key
   MPESA_CONSUMER_SECRET=your-consumer-secret
   MPESA_SHORTCODE=174379
   MPESA_PASSKEY=your-passkey
   MPESA_CALLBACK_URL=https://your-domain.com/api/payments/mpesa/callback
   ```

5. **Initialize database**:
   ```bash
   flask db upgrade
   ```

6. **Run backend server**:
   ```bash
   flask run
   ```
   Server will start on `http://localhost:5000`

### Android App Setup

1. **Open project in Android Studio**:
   ```bash
   cd /path/to/RentMG
   # Open in Android Studio
   ```

2. **Sync Gradle**:
   - Android Studio will prompt to sync
   - Click "Sync Now"

3. **Configure Base URL**:
   - Open `app/src/main/java/com/example/rentmg/util/AppManager.kt`
   - Verify base URL:
     ```kotlin
     private const val BASE_URL = "http://10.0.2.2:5000/"  // For emulator
     // Use "http://YOUR_LOCAL_IP:5000/" for physical device
     ```

4. **Build and Run**:
   - Select emulator or physical device
   - Click Run (green triangle) or `Shift + F10`

### Test Credentials

**Tenant Account**:
- Email: `tenant@test.com`
- Password: `password123`

**Landlord Account**:
- Email: `landlord@test.com`
- Password: `password123`

**M-Pesa Test Numbers** (Sandbox):
- Phone: `254708374149` (Success)
- Phone: `254700000000` (Failure)

---

## API Documentation

### Base URL

```
http://localhost:5000/api
```

### Authentication Endpoints

#### POST /auth/login
**Description**: Authenticate user and receive JWT token

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (200 OK):
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "tenant",
    "full_name": "John Doe",
    "created_at": "2025-01-15T10:30:00",
    "updated_at": "2025-01-15T10:30:00"
  }
}
```

#### POST /auth/register
**Description**: Create new user account

**Request Body**:
```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "role": "tenant",
  "full_name": "Jane Smith"
}
```

**Response** (201 Created):
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 2,
    "email": "newuser@example.com",
    "role": "tenant",
    "full_name": "Jane Smith",
    "created_at": "2025-01-15T11:00:00",
    "updated_at": "2025-01-15T11:00:00"
  }
}
```

### Lease Endpoints

#### GET /leases/
**Description**: Get all leases for authenticated user

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "unit_id": 5,
    "tenant_id": 2,
    "start_date": "2025-01-01",
    "end_date": "2025-12-31",
    "status": "active",
    "created_at": "2025-01-01T09:00:00",
    "updated_at": "2025-01-01T09:00:00"
  }
]
```

### Payment Endpoints

#### POST /payments/mpesa/initiate
**Description**: Initiate M-Pesa STK push payment

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**:
```json
{
  "lease_id": 1,
  "amount": 25000,
  "phone": "254712345678"
}
```

**Response** (200 OK):
```json
{
  "message": "Payment initiated successfully",
  "payment_id": 15,
  "checkout_request_id": "ws_CO_15012025120000000712345678",
  "merchant_request_id": "29115-34620561-1"
}
```

#### GET /payments/{id}
**Description**: Get payment details and status

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response** (200 OK):
```json
{
  "id": 15,
  "lease_id": 1,
  "amount": 25000.0,
  "status": "completed",
  "method": "mpesa",
  "reference": "QA12BC34DE",
  "mpesa_receipt_number": "QA12BC34DE",
  "created_at": "2025-01-15T12:00:00",
  "updated_at": "2025-01-15T12:00:45"
}
```

#### GET /payments/history
**Description**: Get payment history for user's leases

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters**:
- `lease_id` (optional): Filter by specific lease

**Response** (200 OK):
```json
[
  {
    "id": 15,
    "lease_id": 1,
    "amount": 25000.0,
    "status": "completed",
    "method": "mpesa",
    "reference": "QA12BC34DE",
    "created_at": "2025-01-15T12:00:00",
    "updated_at": "2025-01-15T12:00:45"
  },
  {
    "id": 14,
    "lease_id": 1,
    "amount": 25000.0,
    "status": "completed",
    "method": "mpesa",
    "reference": "QA11AB23CD",
    "created_at": "2024-12-15T10:30:00",
    "updated_at": "2024-12-15T10:31:20"
  }
]
```

### Property Endpoints (Landlord Only)

#### GET /properties/
**Description**: Get all properties for landlord

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Sunrise Apartments",
    "address": "Kilimani, Nairobi",
    "landlord_id": 1,
    "created_at": "2024-06-01T08:00:00",
    "updated_at": "2024-06-01T08:00:00"
  }
]
```

#### POST /properties/
**Description**: Create new property

**Headers**:
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**:
```json
{
  "name": "Ocean View Apartments",
  "address": "Nyali, Mombasa"
}
```

**Response** (201 Created):
```json
{
  "message": "Property created successfully",
  "property": {
    "id": 2,
    "name": "Ocean View Apartments",
    "address": "Nyali, Mombasa",
    "landlord_id": 1,
    "created_at": "2025-01-15T14:00:00",
    "updated_at": "2025-01-15T14:00:00"
  }
}
```

---

## User Guide

### For Tenants

#### Signing In

1. Open the app
2. Enter your email and password
3. Click "Sign In"
4. You'll be directed to the Tenant Dashboard

#### Paying Rent

1. From the Dashboard, view your rent status
2. If rent is due, click "Proceed to Payment"
3. Enter your M-Pesa phone number (format: 254712345678)
4. Click "Pay with M-Pesa"
5. Enter M-Pesa PIN on your phone when prompted
6. Wait for confirmation (up to 30 seconds)
7. Payment success will show transaction reference

#### Viewing Payment History

1. Tap "History" in bottom navigation
2. View all past payments
3. Each payment shows:
   - Amount paid
   - Date and time
   - Payment method
   - Status (Completed/Pending/Failed)
   - Transaction reference

#### Updating Profile

1. Tap "Profile" in bottom navigation
2. View your account information
3. Click "Edit Profile" (coming soon)
4. Click "Logout" to sign out

### For Landlords

#### Managing Properties

1. Sign in with landlord account
2. Navigate to "Properties" tab
3. View all your properties
4. Click "+" to add new property
5. Fill in property details (name, address)
6. Save to create property

#### Managing Units

1. Select a property
2. View units within property
3. Add new units with rent amounts
4. Edit or delete existing units

#### Viewing Tenants

1. Navigate to tenant management
2. View all tenants across properties
3. Assign tenants to units
4. Create new leases

---

## Code Structure

### Key Classes

#### AppManager.kt
**Purpose**: Global singleton for API service and session management

**Key Methods**:
```kotlin
// Initialize with saved token
fun initialize(context: Context)

// Get API service instance
fun getApiService(): ApiService

// Save JWT token
fun setToken(context: Context, token: String)

// Get current user
fun getCurrentUser(): User?

// Clear session data
fun logout(context: Context)
```

#### SignInActivity.kt
**Purpose**: User authentication screen

**Key Features**:
- Email/password validation
- JWT token retrieval and storage
- Role-based dashboard routing
- Error handling with user-friendly messages

#### CheckoutActivity.kt
**Purpose**: M-Pesa payment processing

**Key Features**:
- Phone number validation (Kenyan format)
- STK push initiation
- Payment status polling (3-second intervals)
- Success/failure UI states
- Transaction reference display

#### TenantDashboardFragment.kt
**Purpose**: Main tenant dashboard

**Key Features**:
- Fetches lease data from API
- Displays property and unit details
- Shows rent status (paid/unpaid)
- Navigates to checkout for payment

#### HistoryFragment.kt
**Purpose**: Payment history display

**Key Features**:
- RecyclerView with custom adapter
- Fetches payment history from API
- Status-based color coding
- Date and currency formatting

---

## Troubleshooting

### Common Issues

#### 1. Cannot Connect to Backend

**Problem**: App shows "Network error" or "Failed to connect"

**Solutions**:
- Verify backend is running: `flask run`
- Check backend URL in `AppManager.kt`
  - Emulator: Use `10.0.2.2:5000`
  - Physical device: Use your computer's local IP (e.g., `192.168.1.100:5000`)
- Disable Android Studio's "Instant Run" if having issues
- Check firewall settings allowing port 5000

#### 2. M-Pesa Payment Not Working

**Problem**: STK push not received or payment fails

**Solutions**:
- Verify you're using sandbox test phone numbers
- Check M-Pesa credentials in backend `.env` file
- Ensure phone number format is correct: `254712345678` (no + or spaces)
- Check backend logs for M-Pesa API errors
- Verify callback URL is accessible (use ngrok for local testing)

#### 3. Login Returns 401 Unauthorized

**Problem**: Correct credentials but login fails

**Solutions**:
- Verify user exists in database
- Check password is correct (case-sensitive)
- Ensure backend JWT secret is configured
- Clear app data and try again
- Check backend logs for authentication errors

#### 4. Blank Screen After Login

**Problem**: Dashboard doesn't load or shows blank screen

**Solutions**:
- Check Logcat for errors
- Verify user role is correctly returned from API
- Ensure fragment layouts exist
- Check if API endpoints are accessible
- Clear app cache and rebuild

#### 5. Payment History Not Loading

**Problem**: History fragment shows "No payments" despite having payments

**Solutions**:
- Verify lease_id is correctly passed
- Check API response in Logcat
- Ensure user has an active lease
- Verify payment records exist in database
- Check API authentication token is valid

### Debug Mode

Enable detailed logging by adding to `ApiClient.kt`:

```kotlin
val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY  // Change to BODY for full logs
}
```

### Getting Help

For issues not covered here:

1. Check Logcat for error messages
2. Review backend logs
3. Verify API responses using Postman
4. Check database for data consistency
5. Create an issue in the project repository

---

## Contributing

### Code Style Guidelines

1. **Comments**: Every class, method, and complex logic should have KDoc comments
2. **Naming**: Use descriptive names (e.g., `loadPaymentHistory()` not `loadData()`)
3. **Organization**: Group related code with section comments
4. **Error Handling**: Always implement onFailure for Retrofit calls
5. **UI States**: Use showLoading(), showContent(), showError() pattern

### Adding New Features

1. Create data model in `data/model/`
2. Add API endpoint to `ApiService.kt`
3. Create UI in `pages/` with Activity/Fragment
4. Implement loading/error/content states
5. Add comprehensive comments
6. Test with backend API
7. Update README with feature documentation

---

## License

This project is licensed under the MIT License - see LICENSE file for details.

---

## Contact

For questions or support, please contact the development team.

**Project Status**: 85% Complete (Tenant features 100%, Landlord features 40%)

**Last Updated**: January 2025
