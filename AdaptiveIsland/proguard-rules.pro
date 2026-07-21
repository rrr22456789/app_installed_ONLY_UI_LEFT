# Project-specific ProGuard rules for Adaptive Island.

# By default, R8 safely shrinks and obfuscates the code.
# The Android Gradle Plugin and AAPT2 automatically generate necessary keep rules for:
# - Application components declared in AndroidManifest.xml (Activities, Services, Receivers).
# - Custom Views referenced in XML layout files.

# Modern libraries used in this project bundle their own consumer-proguard-rules.pro:
# - Kotlin Coroutines
# - AndroidX Room
# - AndroidX DataStore
# - AndroidX Lifecycle & Navigation
# - Google Material Components
# R8 automatically merges these packaged rules during the release build.

# No custom keep or dontwarn rules are required for the current architecture.