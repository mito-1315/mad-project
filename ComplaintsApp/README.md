# Complaints App — Kotlin Android

A full-featured Android app (Jetpack Compose + Kotlin) that mirrors the React frontend of the
Complaints Management System, connecting to the existing Node.js / Express / MongoDB backend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Navigation | Jetpack Navigation Compose |
| HTTP | Retrofit 2 + OkHttp |
| JSON | Gson |
| Async | Kotlin Coroutines |
| Auth | JWT (stored in EncryptedSharedPreferences) |
| Architecture | MVVM — ViewModel + StateFlow |

---

## Project Structure

```
ComplaintsApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── java/com/complaints/app/
│       ├── ComplaintsApp.kt          ← Application class
│       ├── MainActivity.kt           ← Single Activity entry point
│       ├── data/
│       │   ├── api/                  ← Retrofit interfaces (AuthApi, ComplaintsApi, ApiClient)
│       │   ├── model/                ← Data classes matching backend JSON
│       │   └── repository/           ← Repository pattern wrapping API calls
│       ├── ui/
│       │   ├── auth/                 ← Login + Signup screens + ViewModels
│       │   ├── student/              ← Dashboard + Submit screens + ViewModels
│       │   ├── admin/                ← Admin panel screen + ViewModel
│       │   ├── components/           ← Reusable composables (badges, topbar)
│       │   ├── navigation/           ← NavGraph + Screen routes
│       │   └── theme/                ← Material 3 colors, typography, theme
│       └── util/
│           ├── TokenManager.kt       ← Encrypted JWT storage
│           └── SessionManager.kt     ← Global auth state (StateFlow)
```

---

## Setup & Running in Android Studio

### Step 1 — Open project
1. Open **Android Studio** (Hedgehog 2023.1.1 or later recommended)
2. Click **"Open"** → select `MADProject/ComplaintsApp/`
3. Let Gradle sync complete (first sync downloads dependencies, ~2 min)

### Step 2 — Start the backend
```bash
cd complaints_system-main/backend
npm install
npm start          # Runs on http://localhost:5000
```

### Step 3 — Configure API URL

**For Android Emulator (default):**
The app uses `http://10.0.2.2:5000` by default — this is the emulator's alias for your Mac's `localhost`.
No changes needed.

**For a Real Physical Device:**
Open `app/src/main/java/com/complaints/app/data/api/ApiClient.kt` and change:
```kotlin
var BASE_URL = "http://10.0.2.2:5000/"
// Change to your Mac's local IP, e.g.:
var BASE_URL = "http://192.168.1.100:5000/"
```
Find your Mac's IP with: `ifconfig | grep "inet " | grep -v 127`

### Step 4 — Create / select emulator
1. In Android Studio → **Device Manager** → Create Device
2. Choose Pixel 6 (or similar) with API 26+
3. Start the emulator

### Step 5 — Run the app
- Click the **▶ Run** button (or `Ctrl+R`)
- The app will install and launch on the emulator

---

## App Features

| Feature | Details |
|---|---|
| Login / Signup | Email + password with client-side validation |
| Role-based routing | Students → Dashboard; Admins → Admin Panel |
| Session persistence | JWT stored securely in EncryptedSharedPreferences |
| Student Dashboard | Stats cards, status filter chips, complaint list |
| Submit Complaint | Form with validation, AI severity shown after submit |
| Admin Panel | All complaints with stats, severity bar chart, filters |
| Inline Edit | Admin can update status and notes per complaint |
| Delete | Admin can delete complaints with confirmation dialog |
| Pull-to-refresh | Swipe down to reload any list |
| Error handling | Network errors shown as inline banners + snackbars |

---

## API Endpoints Used

| Action | Endpoint | Auth |
|---|---|---|
| Login | `POST /api/auth/login` | None |
| Signup | `POST /api/auth/signup` | None |
| Get complaints | `GET /api/complaints` | Bearer JWT |
| Submit complaint | `POST /api/complaints` | Bearer JWT |
| Get stats | `GET /api/complaints/stats` | Admin JWT |
| Update complaint | `PUT /api/complaints/:id` | Admin JWT |
| Delete complaint | `DELETE /api/complaints/:id` | Admin JWT |

---

## Troubleshooting

| Issue | Fix |
|---|---|
| `CLEARTEXT communication not permitted` | Already handled by `usesCleartextTraffic="true"` in Manifest |
| `Connection refused` on emulator | Make sure backend is running on port 5000 |
| `Connection refused` on real device | Change `BASE_URL` to your Mac's LAN IP in `ApiClient.kt` |
| Gradle sync fails | File → Invalidate Caches → Restart |
| `401 Unauthorized` | Token expired — logout and login again |
