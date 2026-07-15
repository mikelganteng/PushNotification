# Current Status - Mutasi Push Notification

## Last Update: 2026-07-16

### Recent Changes

#### Build System Setup (2026-07-16)
**Implementation**: GitHub Actions automatic APK build

**Details**:
- Added GitHub Actions workflow for automatic APK builds
- Configured debug signing for release builds
- Added Gradle caching for faster builds
- Build triggered on every push to `android-app/**` folder
- APK artifacts automatically uploaded and downloadable

**Workflow Location**: `.github/workflows/build-android.yml`

**Build Status**: Check at https://github.com/mikelganteng/PushNotification/actions

#### 1. Fixed Force Close Issue When Changing Server URL
**Problem**: App crashed when switching between server URLs

**Solution**:
- Added try-catch error handling in `setupServerSpinner()` to prevent crashes during spinner selection
- Added null checks for `selectedItem` in save button listener
- Added URL validation to ensure URL starts with `http://` or `https://`
- Added empty string check before creating `ApiClient` in `testConnection()`
- Added `printStackTrace()` for better error debugging
- Improved error messages for better user feedback

**Files Modified**:
- `android-app/app/src/main/java/com/mutasi/pushnotif/MainActivity.kt`

#### 2. Added App Selection Feature
**Feature**: User can now select specific apps to monitor from installed apps list

**Implementation**:
- Added "Pilih Aplikasi dari Daftar" button below the filter field
- Shows dialog with all installed apps (that have launch intent)
- Multi-select dialog allows choosing multiple apps at once
- Selected apps are automatically added to filter field
- Apps are sorted alphabetically by name
- Shows app name (not package name) for better UX
- Includes "Hapus Semua" button to clear all filters
- Maintains previously selected apps when reopening dialog

**Files Modified**:
- `android-app/app/src/main/java/com/mutasi/pushnotif/MainActivity.kt`
  - Added `AlertDialog` import
  - Added `showAppSelector()` method
  - Added `AppItem` data class
  - Added button listener for app selection
- `android-app/app/src/main/res/layout/activity_main.xml`
  - Added `btnSelectApps` button

### Current Features

1. **Server Configuration**
   - Dropdown with predefined server options
   - Custom URL option
   - Server health check with status display
   - API key configuration

2. **App Filtering**
   - Manual filter input (comma-separated package names)
   - Visual app selector with all installed apps
   - Multi-select capability

3. **Notification Management**
   - View captured notifications
   - Retry failed notifications
   - Retry all pending/failed notifications
   - Status tracking (pending/sent/failed)

4. **Service Control**
   - Enable/disable notification forwarding
   - Notification access permission check
   - Automatic service restart

### Technical Implementation

**Error Handling Improvements**:
- All network operations wrapped in try-catch
- URL validation before API calls
- Null safety checks throughout
- Better error messages to users

**App Selection**:
- Loads installed apps on background thread (Dispatchers.IO)
- Filters for apps with launch intents only
- Uses shared preferences to persist selections
- Seamless integration with existing filter system

### Testing Checklist

- [x] Server URL switching without crash
- [x] URL validation (http/https check)
- [x] App selector dialog shows apps
- [x] Multi-select apps functionality
- [x] Selected apps saved to preferences
- [x] Filter field updates with selected apps
- [ ] Test on physical device
- [ ] Test with various server URLs
- [ ] Test app selection with many installed apps

### Known Issues

None currently

### Next Steps

1. Wait for GitHub Actions build to complete (5-10 minutes)
2. Download APK from Actions artifacts
3. Test on physical Android device
4. Test network connectivity edge cases
5. Consider adding app icons to selection dialog
6. Consider adding search functionality for app selector

### How to Download APK

1. Go to https://github.com/mikelganteng/PushNotification/actions
2. Click on the latest successful workflow run (green checkmark)
3. Scroll down to "Artifacts" section
4. Download `app-debug` (contains app-debug.apk)
5. Extract and install on Android device
