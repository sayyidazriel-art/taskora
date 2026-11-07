# Room entities and DAO implementations are generated/used through annotations.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
# Keep Kotlin metadata required by reflection-based Android/Kotlin components.
-keep class kotlin.Metadata { *; }
