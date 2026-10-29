# Sports Arena — Grassroots Sports Tournament Discovery & Posting Platform

**Sports Arena** is a complete, production-grade Android and backend platform engineered to connect sports tournament organizers and grassroots athletes/teams.

---

## 🏆 System Architecture & Tech Stack

### 📱 Android Application (`:app`)
* **Language & Compiler**: Kotlin 2.0.21 with Jetpack Compose Compiler Plugin
* **UI Toolkit**: Jetpack Compose + Material 3 Design System
* **Architecture**: Clean Architecture + MVVM + Repository Pattern
* **Networking**: Retrofit 2 + OkHttp 4 + Gson + Auth Bearer Token Interceptor
* **Session Management**: Persistent `SessionManager` storing JWT, user role, and profile
* **Intents**: Native Android Dial (`ACTION_DIAL`), WhatsApp (`ACTION_VIEW` URL), Google Maps Navigation (`geo:lat,lng` & query)
* **SDK Compatibility**: Compile/Target SDK 35 (Android 15), Min SDK 24 (95%+ device reach)

### ⚙️ Backend REST API (`:backend`)
* **Framework**: Spring Boot 3.3.5
* **Language**: Kotlin 2.0.21
* **Data Access**: Spring Data JPA + Hibernate
* **Database**: PostgreSQL (Production) with embedded H2 PostgreSQL-compatible mode for instant zero-config evaluation
* **Security**: Spring Security + Stateless JWT (JJWT HMAC-SHA256) + BCrypt password hashing
* **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, etc.)
* **Error Handling**: `GlobalExceptionHandler` delivering clean JSON error responses

---

## 📂 Project Directory Structure

```text
d:/tounment project/
├── app/                                    # Android Application Module
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/sportmeet/app/
│       │   ├── MainActivity.kt
│       │   ├── core/
│       │   │   ├── common/                # Reusable UI (TournamentCard, SportChip, Top/BottomBar)
│       │   │   ├── navigation/            # NavGraph & Screen routes
│       │   │   ├── session/               # SessionManager (JWT & User credentials)
│       │   │   └── theme/                 # Athletic palette, Material 3 Typography & Theme
│       │   ├── data/
│       │   │   ├── mapper/                # DTO to Domain model mappers
│       │   │   ├── remote/api/            # Retrofit SportMeetApi & ApiClient
│       │   │   ├── remote/dto/            # Auth, Tournament, Registration, Report DTOs
│       │   │   └── repository/            # AuthRepository, TournamentRepository, RegistrationRepository
│       │   ├── domain/model/              # Tournament, Sport, UserRole, TournamentStatus
│       │   └── presentation/
│       │       ├── admin/                 # AdminDashboardScreen (Review, Approve, Reject)
│       │       ├── auth/                  # LoginScreen, RegisterScreen, AuthViewModel
│       │       ├── organizer/             # OrganizerHomeScreen (+ Post Tournament, Manage Entries)
│       │       ├── player/                # PlayerHomeScreen, Dedicated Search, Saved, Registrations
│       │       ├── profile/               # ProfileScreen (Role switch, API config, Logout)
│       │       ├── role/                  # RoleSelectionScreen
│       │       ├── splash/                # SplashScreen
│       │       └── welcome/               # WelcomeScreen
│       └── res/                           # Icons, Colors, Themes, Strings
│
├── backend/                                # Spring Boot Backend Module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── kotlin/com/sportmeet/backend/
│       │   │   ├── SportMeetApplication.kt
│       │   │   ├── config/                # SecurityConfig, DataInitializer
│       │   │   ├── controller/            # Auth, Tournament, Organizer, Player, Admin, Sports
│       │   │   ├── dto/                   # Request & Response contracts
│       │   │   ├── entity/                # User, Sport, Tournament, Registration, Favorite, Report
│       │   │   ├── exception/             # GlobalExceptionHandler
│       │   │   ├── repository/            # Spring Data JPA repositories
│       │   │   ├── security/              # JwtTokenProvider, JwtAuthenticationFilter
│       │   │   └── service/               # Core business services
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── application-postgres.properties
│       │       └── schema.sql             # Full DDL for PostgreSQL
│       └── test/                          # Spring Boot Integration Tests
│
├── build.gradle.kts                        # Root Gradle configuration
├── settings.gradle.kts                     # Multi-project build definition
└── README.md
```

---

## 🗄️ Database Tables & Relationships

| Table | Primary Key | Foreign Keys / Relations | Purpose |
| :--- | :--- | :--- | :--- |
| `users` | `id` (UUID) | - | Organizers, Players, Admins |
| `sports` | `id` (slug) | - | Cricket, Football, Volleyball, etc. |
| `tournaments` | `id` (UUID) | `sport_id` -> `sports`, `organizer_id` -> `users` | Tournament fixtures, fees, prize, status |
| `registrations` | `id` (UUID) | `tournament_id` -> `tournaments`, `user_id` -> `users` | Team entries & squad player count |
| `favorites` | `id` (UUID) | `tournament_id` -> `tournaments`, `user_id` -> `users` | Player bookmarks |
| `reports` | `id` (UUID) | `tournament_id` -> `tournaments`, `reporter_id` -> `users` | Flagged tournament reports |

---

## 🔑 Development Test Accounts

Pre-seeded into the database upon startup with 1-tap autofill buttons available directly on the Android Login screen:

| Role | Email | Password | Pre-seeded Context |
| :--- | :--- | :--- | :--- |
| **Player** | `player@sportmeet.com` | `pass123` | Arun Kumar — Registered captain for *Erode Strikers XI* |
| **Organizer** | `organizer@sportmeet.com` | `pass123` | Karthik Raja — Organizer of *Erode Cricket Cup* |
| **Admin** | `admin@sportmeet.com` | `admin123` | SportMeet Admin — Can approve/reject pending events |

---

## 🚀 How to Run

### Option A: Run Backend (Instant Mode)
The default profile uses an in-memory database with PostgreSQL dialect and automatic data seeding:
```powershell
.\gradlew.bat :backend:bootRun
```
The server will start on `http://localhost:8080`.

### Option B: Run Backend with Live PostgreSQL
1. Create database in PostgreSQL:
   ```sql
   CREATE DATABASE sportmeet_db;
   ```
2. Run backend with `postgres` profile:
   ```powershell
   .\gradlew.bat :backend:bootRun --args="--spring.profiles.active=postgres"
   ```

### Option C: Build and Run Android Application
1. Connect an Android phone (with USB debugging enabled) or launch an Android Studio emulator.
2. Build the debug APK:
   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```
   The APK is generated at:
   `app/build/outputs/apk/debug/app-debug.apk`
3. Install directly using ADB:
   ```powershell
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

*Note for Android Emulator networking*: The app is pre-configured to communicate with the host computer's backend via `http://10.0.2.2:8080/`. You can edit this endpoint dynamically at any time inside the app from **Profile -> Backend API Endpoint**.

---

## 🎬 Complete Demonstration Flow (Section 62)

1. **Launch App**: Open SportMeet -> Animated Splash screen transitions to Welcome screen.
2. **Register Organizer**: Tap "Get Started" -> Tap "Register here" -> Choose "Tournament Organizer" -> Enter Name, Email, Phone, Password -> Account is created and user enters Organizer Home.
3. **Post Tournament**:
   * Click **+ Post Tournament** (or FAB).
   * Enter:
     * Name: `Erode Cricket Cup`
     * Sport: `🏏 Cricket`
     * Location: `Erode`
     * Venue: `VOC Park Sports Complex`
     * Date: `15 October 2026`
     * Entry Fee: `2000`, Prize: `25000`
     * Phone: `+91 98765 43210`
   * Submit -> Status becomes **PENDING_APPROVAL**.
4. **Admin Approval**:
   * Switch user: Go to Profile -> Sign Out.
   * Tap "Quick Fill: Admin" -> Sign in.
   * Admin lands on the **Admin Console**.
   * Under "Tournaments Awaiting Approval", see the newly submitted tournament.
   * Tap **Approve** -> Tournament status updates to **APPROVED**.
5. **Player Discovery & Registration**:
   * Sign out -> Tap "Quick Fill: Player" -> Sign in.
   * Under Search / Home, filter by Sport `🏏 Cricket` or City `Erode`.
   * Tap on the card for `Erode Cricket Cup`.
   * Modal opens with full schedule, venue, prize, call button, WhatsApp button, map button, and team registration form.
   * Tap **📝 Register Team** -> Enter squad details -> Submit.
6. **Organizer Verification**:
   * Sign out -> Sign in as Organizer.
   * Tap **Teams** tab -> See the player's newly registered team with options to **Confirm Team** or **Reject**.
