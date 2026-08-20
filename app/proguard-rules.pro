-keep class com.shockwave.** { *; }
-keep class com.github.barteksc.** { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-dontwarn com.shockwave.**
-dontwarn com.github.barteksc.**
