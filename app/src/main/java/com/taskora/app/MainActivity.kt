package com.taskora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.verticalScroll
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private object TaskoraAppearance {
    var mode by mutableStateOf("dark")
    var isDark by mutableStateOf(true)
    var language by mutableStateOf("en")
    var accentHex by mutableStateOf("#9B83E8")
    var showChallenges by mutableStateOf(true)
    var showUpcoming by mutableStateOf(true)
    var showFocusTip by mutableStateOf(true)
}

private val idUi = mapOf(
    "Home" to "Beranda", "Tasks" to "Tugas", "Calendar" to "Kalender", "Stats" to "Statistik", "Profile" to "Profil", "Recurring" to "Berulang",
    "Make every day count" to "Jadikan setiap hari berarti", "My Tasks" to "Tugas Saya", "Add" to "Tambah", "Delete" to "Hapus", "Edit" to "Ubah", "Save" to "Simpan", "Cancel" to "Batal", "Close" to "Tutup", "Confirm" to "Konfirmasi",
    "Appearance" to "Tampilan", "Choose your preferred theme" to "Pilih tema yang kamu sukai", "Light" to "Terang", "Dark" to "Gelap", "System" to "Sistem", "Date format" to "Format tanggal", "Time format" to "Format waktu",
    "Task reminders" to "Pengingat tugas", "Configure" to "Atur", "Local data backup" to "Cadangan data lokal", "Export" to "Ekspor", "Import" to "Impor", "About Taskora" to "Tentang Taskora",
    "Profile & Settings" to "Profil & Pengaturan", "Your progress, preferences, and local data." to "Progres, preferensi, dan data lokal kamu.", "Change avatar" to "Ubah avatar", "Edit display name" to "Ubah nama tampilan", "Display name" to "Nama tampilan", "Your name" to "Nama kamu",
    "Achievements" to "Pencapaian", "Recent XP history" to "Riwayat XP terbaru", "Settings" to "Pengaturan", "Privacy & storage" to "Privasi & penyimpanan", "Choose an avatar" to "Pilih avatar", "Choose photo from gallery" to "Pilih foto dari galeri", "Use initial instead" to "Gunakan inisial saja",
    "Adjust profile photo" to "Atur foto profil", "Save photo" to "Simpan foto", "Horizontal position" to "Posisi horizontal", "Vertical position" to "Posisi vertikal", "Zoom" to "Perbesar", "Weekly routines" to "Rutinitas mingguan", "No recurring templates yet." to "Belum ada template berulang.",
    "Generate next week" to "Buat untuk minggu depan", "Generate next week?" to "Buat untuk minggu depan?", "Repeat on" to "Ulangi pada", "Task name" to "Nama tugas", "Description" to "Deskripsi", "Deadline" to "Tenggat", "Start date & time" to "Tanggal & waktu mulai", "Search tasks…" to "Cari tugas…", "Sort by" to "Urutkan",
    "No matching tasks" to "Tidak ada tugas yang cocok", "No tasks due on this date." to "Tidak ada tugas pada tanggal ini.", "No upcoming tasks yet." to "Belum ada tugas mendatang.", "Your Analytics" to "Analitik Kamu", "Productivity trend" to "Tren produktivitas", "Period comparison" to "Perbandingan periode", "Completed tasks over time" to "Tugas selesai dari waktu ke waktu",
    "Your day is clear" to "Hari ini masih kosong", "Your journey starts here" to "Perjalananmu dimulai di sini", "Keep your momentum!" to "Pertahankan momentum!", "tasks completed" to "tugas selesai", "Claim" to "Ambil hadiah", "Start" to "Mulai", "Save task" to "Simpan tugas", "Save template" to "Simpan template",
    "Delete task?" to "Hapus tugas?", "Complete this task?" to "Selesaikan tugas ini?", "Confirm completion" to "Konfirmasi penyelesaian", "Not yet" to "Belum", "Continue" to "Lanjutkan", "Completed • Permanently locked" to "Selesai • Terkunci permanen", "Past deadline • Read only" to "Tenggat lewat • Hanya baca", "Overdue today • still completable" to "Terlambat hari ini • masih bisa diselesaikan",
    "100% OFFLINE • LOCAL DATA" to "100% OFFLINE • DATA LOKAL", "YOUR DAILY PROGRESS" to "PROGRES HARIANMU", "✨ HIDDEN QUEST" to "✨ MISI TERSEMBUNYI",
    "Storage management" to "Pengelolaan penyimpanan", "Optimize local storage" to "Optimalkan penyimpanan lokal", "Priority" to "Prioritas", "Difficulty" to "Kesulitan", "Pause" to "Jeda", "Enable" to "Aktifkan",
    "Add" to "Tambah", "Add a task scheduled for today to get started." to "Tambahkan tugas yang dijadwalkan hari ini untuk memulai.", "Add notes, details, or expected result…" to "Tambahkan catatan, detail, atau hasil yang diharapkan…",
    "Adjust profile photo" to "Atur foto profil", "At start + 10 minutes before deadline" to "Saat mulai + 10 menit sebelum tenggat", "Cannot preview this image" to "Foto ini tidak dapat ditampilkan",
    "Complete this task?" to "Selesaikan tugas ini?", "Complete your first task to unlock XP, levels, and streaks." to "Selesaikan tugas pertamamu untuk membuka XP, level, dan streak.", "Complete your first task to unlock your level, XP, and streak." to "Selesaikan tugas pertamamu untuk membuka level, XP, dan streak.",
    "Completed • Permanently locked" to "Selesai • Terkunci permanen", "Confirm" to "Konfirmasi", "Confirm completion" to "Konfirmasi penyelesaian", "DEADLINE" to "TENGGAT", "DIFFICULTY" to "KESULITAN", "Deadlines from your saved tasks." to "Tenggat dari tugas yang tersimpan.",
    "Export or restore a JSON file on your device" to "Ekspor atau pulihkan file JSON di perangkat", "Import replaces the current task list. Export a backup first." to "Impor mengganti daftar tugas saat ini. Ekspor cadangan terlebih dahulu.",
    "Importing a backup replaces every task currently stored on this device. Export your current data first if you may need it." to "Impor cadangan akan mengganti semua tugas di perangkat ini. Ekspor data saat ini terlebih dahulu jika masih diperlukan.",
    "Horizontal position" to "Posisi horizontal", "IN PROGRESS" to "SEDANG BERJALAN",
    "No tasks due on this date." to "Tidak ada tugas pada tanggal ini.", "Not yet" to "Belum", "OFFLINE PRODUCTIVITY" to "PRODUKTIVITAS OFFLINE", "Once confirmed, completion is locked in Tasks and cannot be unchecked. Keep your progress honest." to "Setelah dikonfirmasi, tugas terkunci dan tidak dapat dibatalkan. Jaga progres tetap jujur.",
    "Past deadline • Read only" to "Tenggat lewat • Hanya baca", "Plan it clearly, then make it happen." to "Rencanakan dengan jelas, lalu wujudkan.", "Replace current tasks?" to "Ganti tugas saat ini?", "SCHEDULE" to "JADWAL", "START" to "MULAI", "STATUS" to "STATUS",
    "Save photo" to "Simpan foto", "Save task" to "Simpan tugas", "Save template" to "Simpan template", "Start date & time" to "Tanggal & waktu mulai", "Stay focused, one task at a time." to "Tetap fokus, satu tugas setiap waktu.",
    "Task name" to "Nama tugas", "Task reminders" to "Pengingat tugas", "Task title" to "Judul tugas", "Templates are only turned into tasks after confirmation." to "Template hanya dibuat menjadi tugas setelah dikonfirmasi.",
    "Try another keyword or filter." to "Coba kata kunci atau filter lain.", "Use initial instead" to "Gunakan inisial saja", "Uses your device's regional calendar defaults." to "Mengikuti pengaturan kalender regional perangkat.", "Vertical position" to "Posisi vertikal", "XP history will appear here after a task is completed." to "Riwayat XP akan muncul setelah tugas diselesaikan.",
    "Your name" to "Nama kamu", "Your progress, preferences, and local data." to "Progres, preferensi, dan data lokal kamu.", "e.g. Finish project proposal" to "mis. Selesaikan proposal proyek", "Your Analytics" to "Analitik Kamu", "Your journey starts here" to "Perjalananmu dimulai di sini",
    "YOUR DAILY PROGRESS" to "PROGRES HARIANMU", "Keep your momentum!" to "Pertahankan momentum!", "CURRENT STREAK" to "STREAK SAAT INI", "TOTAL XP" to "TOTAL XP", "Earned from completions" to "Diperoleh dari penyelesaian", "Keep it going!" to "Pertahankan!", "Complete a task today" to "Selesaikan tugas hari ini",
    "Focus tip" to "Tips fokus", "Coming up" to "Mendatang", "Next 7 days" to "7 hari ke depan", "Today's tasks" to "Tugas hari ini", "Focus" to "Fokus", "Your Analytics" to "Analitik Kamu", "A clear picture of your productivity." to "Gambaran produktivitasmu.",
    "COMPLETED" to "SELESAI", "COMPLETION" to "PENYELESAIAN", "All time" to "Sepanjang waktu", "Overall rate" to "Persentase keseluruhan", "Earned XP ledger" to "Catatan XP diperoleh", "REMAINING" to "TERSISA", "Open tasks" to "Tugas terbuka", "XP LOST" to "XP HILANG", "Missed deadlines" to "Tenggat terlewat", "AVG DURATION" to "DURASI RATA-RATA", "Tracked completions" to "Penyelesaian terlacak", "ON TIME" to "TEPAT WAKTU", "LATE" to "TERLAMBAT", "Completed" to "Selesai", "Completed late" to "Selesai terlambat",
    "Task distribution" to "Distribusi tugas", "Difficulty distribution" to "Distribusi kesulitan", "Category performance" to "Performa kategori", "CURRENT" to "SAAT INI", "PREVIOUS" to "SEBELUMNYA", "CHANGE" to "PERUBAHAN", "Weekly routines" to "Rutinitas mingguan", "Templates are only turned into tasks after confirmation." to "Template menjadi tugas setelah dikonfirmasi.",
    "Task title" to "Judul tugas", "Start HH:mm" to "Mulai HH:mm", "Deadline HH:mm" to "Tenggat HH:mm", "New weekly template" to "Template mingguan baru", "Edit weekly template" to "Ubah template mingguan", "Generate next week" to "Buat minggu depan", "This task is overdue" to "Tugas ini melewati tenggat",
    "Use your device's regional calendar defaults." to "Gunakan pengaturan kalender regional perangkat.", "At start + 10 minutes before deadline" to "Saat mulai + 10 menit sebelum tenggat", "Export or restore a JSON file on your device" to "Ekspor atau pulihkan file JSON di perangkat", "Backup exported successfully" to "Cadangan berhasil diekspor", "Import" to "Impor", "Export" to "Ekspor",
    "Focus Mode" to "Mode Fokus", "Work in focused sessions, then take a break." to "Bekerja dalam sesi fokus, lalu istirahat.", "FOCUS IN PROGRESS" to "SEDANG FOKUS", "READY TO FOCUS" to "SIAP FOKUS", "Pomodoro session" to "Sesi Pomodoro", "General focus session" to "Sesi fokus umum", "Start focus" to "Mulai fokus", "Resume" to "Lanjutkan", "Finish" to "Selesai", "Pause" to "Jeda", "Focus history" to "Riwayat fokus", "Your focus sessions will appear here." to "Sesi fokus akan muncul di sini.", "Completed focus sessions" to "Sesi fokus selesai", "Recorded locally" to "Tersimpan secara lokal", "XP flow" to "Alur XP", "Earned versus lost" to "XP diperoleh dibandingkan hilang", "Earned" to "Diperoleh", "Lost" to "Hilang", "First Step" to "Langkah Pertama", "Getting Started" to "Mulai Bergerak", "Task Master" to "Master Tugas", "Half Century" to "Setengah Abad", "Century" to "Seratus Tugas", "Expert Finisher" to "Penyelesai Expert", "Hard Worker" to "Pekerja Keras", "XP Collector" to "Pengumpul XP", "XP Veteran" to "Veteran XP", "XP Legend" to "Legenda XP", "Focus Begins" to "Awal Fokus", "Deep Worker" to "Pekerja Fokus", "Focus sessions" to "Sesi fokus", "stored locally" to "tersimpan lokal", "Pomodoro • 25 min" to "Pomodoro • 25 menit", "Challenges" to "Tantangan", "Small goals to build consistency." to "Target kecil untuk membangun konsistensi.", "Daily challenge" to "Tantangan harian", "Weekly challenge" to "Tantangan mingguan", "Monthly challenge" to "Tantangan bulanan", "Mon" to "Sen", "Tue" to "Sel", "Wed" to "Rab", "Thu" to "Kam", "Fri" to "Jum", "Sat" to "Sab", "Sun" to "Min", "Running" to "Berjalan", "Recorded locally" to "Tersimpan lokal", "Completed focus sessions" to "Sesi fokus selesai", "Session tracking remains active while you use other screens." to "Pencatatan sesi tetap berjalan saat kamu membuka halaman lain.", "PAUSED" to "DIJEDA", "Profile photo" to "Foto profil", "Generated this week" to "Sudah dibuat minggu ini", "Already generated for the upcoming week." to "Tugas untuk minggu depan sudah dibuat.", "Personal" to "Pribadi", "Work" to "Pekerjaan", "Learn" to "Belajar", "Low" to "Rendah", "Medium" to "Sedang", "High" to "Tinggi", "Easy" to "Mudah", "Hard" to "Sulit", "Expert" to "Ahli", "Priority" to "Prioritas", "Difficulty" to "Kesulitan", "Version 3.1.0 • Built for focus" to "Versi 3.1.0 • Dirancang untuk fokus", "Week" to "Minggu", "Month" to "Bulan", "Year" to "Tahun", "All data remains on this device. Optimize checkpoints and compacts the local database without deleting tasks." to "Semua data tersimpan di perangkat ini. Optimalkan checkpoint dan padatkan database lokal tanpa menghapus tugas.", "Optimize local storage" to "Optimalkan penyimpanan lokal", "Choose the interface language" to "Pilih bahasa antarmuka", "App language" to "Bahasa aplikasi", "Best" to "Terbaik", "days" to "hari", "Independent local ledger" to "Catatan lokal terpisah", "Good morning" to "Selamat pagi", "Good afternoon" to "Selamat siang", "Good evening" to "Selamat sore", "Good night" to "Selamat malam", "Accent color" to "Warna aksen", "Choose a color for buttons and highlights." to "Pilih warna untuk tombol dan sorotan.", "Violet" to "Ungu", "Blue" to "Biru", "Teal" to "Toska", "Rose" to "Merah muda", "Amber" to "Jingga", "Home screen" to "Tampilan beranda", "Choose which sections appear on your dashboard." to "Pilih bagian yang ditampilkan di beranda.", "Show challenges" to "Tampilkan tantangan", "Show upcoming tasks" to "Tampilkan tugas mendatang", "Show focus tip" to "Tampilkan tips fokus", "Monthly overview" to "Ringkasan bulanan", "Start date" to "Tanggal mulai", "Tasks scheduled for this day" to "Tugas yang dijadwalkan pada hari ini", "Suggest free time" to "Sarankan waktu kosong", "Reward claimed" to "Hadiah sudah diambil", "Reward available" to "Hadiah tersedia", "Claim XP" to "Ambil XP", "Reminder timing" to "Waktu pengingat", "Choose how early Taskora reminds you before a deadline." to "Atur seberapa awal Taskora mengingatkan sebelum tenggat.", "Quiet hours" to "Jam tenang", "Delay reminders between 10:00 PM and 7:00 AM." to "Tunda pengingat selama jam tenang yang kamu atur.", "Quiet period" to "Periode tenang", "End" to "Selesai", "Calendar preferences" to "Preferensi kalender", "First day of the week" to "Hari pertama dalam minggu", "Monday" to "Senin", "Sunday" to "Minggu", "Your tasks and progress are stored privately on this device." to "Tugas dan progres kamu tersimpan secara pribadi di perangkat ini.", "Belum ada tugas aktif untuk hari ini." to "Belum ada tugas aktif untuk hari ini.", "No active tasks for today." to "Belum ada tugas aktif untuk hari ini.", "All status" to "Semua status", "Open" to "Belum selesai", "Overdue" to "Terlambat", "Deadline" to "Tenggat", "Newest" to "Terbaru", "Start date" to "Tanggal mulai", "Taskora will create the scheduled tasks for next week. This template will not generate twice for the same week." to "Taskora akan membuat tugas sesuai jadwal untuk minggu depan. Template ini tidak dapat dibuat dua kali pada minggu yang sama.", "New weekly template" to "Template mingguan baru", "Edit weekly template" to "Ubah template mingguan", "Saved" to "Tersimpan", "Empty slot" to "Slot kosong", "Restore" to "Pulihkan", "Three private backup slots protected by your password." to "Tiga slot cadangan pribadi yang dilindungi kata sandi.", "Use at least 8 characters. Losing this password means the encrypted backup cannot be recovered." to "Gunakan minimal 8 karakter. Jika kata sandi hilang, cadangan terenkripsi tidak dapat dipulihkan.", "At least 8 characters. Keep this password safe." to "Minimal 8 karakter. Simpan kata sandi ini dengan aman.", "Save encrypted backup" to "Simpan cadangan terenkripsi", "Restore encrypted backup" to "Pulihkan cadangan terenkripsi", "Decrypt and restore" to "Dekripsi dan pulihkan", "Encrypt and save" to "Enkripsi dan simpan", "Backup password" to "Kata sandi cadangan", "Backup slot" to "Slot cadangan", "Claim" to "Ambil", "Good morning" to "Selamat pagi", "Good afternoon" to "Selamat siang", "Good evening" to "Selamat sore", "Good night" to "Selamat malam", "min" to "menit", "Create a task" to "Buat tugas", "Edit task" to "Ubah tugas", "All status" to "Semua status", "Open" to "Belum selesai", "Overdue" to "Terlambat", "Newest" to "Terbaru", "Reward: " to "Hadiah: ", "Double reward" to "Hadiah ganda", "Move your body" to "Gerakkan tubuhmu", "Quick strength" to "Latihan kekuatan singkat", "Read a little" to "Membaca sebentar", "Learn something" to "Pelajari sesuatu", "Take a walk" to "Jalan santai", "Reset your space" to "Rapikan ruangmu", "Complete your first task" to "Selesaikan tugas pertamamu", "Complete 5 tasks" to "Selesaikan 5 tugas", "Complete 25 tasks" to "Selesaikan 25 tugas", "Complete 50 tasks" to "Selesaikan 50 tugas", "Complete 100 tasks" to "Selesaikan 100 tugas", "Complete an Expert task" to "Selesaikan tugas tingkat Ahli", "Complete a Hard task" to "Selesaikan tugas tingkat Sulit", "Earn 100 XP" to "Dapatkan 100 XP", "Earn 500 XP" to "Dapatkan 500 XP", "Earn 1,000 XP" to "Dapatkan 1.000 XP", "Complete a focus session" to "Selesaikan satu sesi fokus", "Complete 5 focus sessions" to "Selesaikan 5 sesi fokus", "Complete tasks on 7 consecutive days" to "Selesaikan tugas selama 7 hari berturut-turut", "Complete tasks on 30 consecutive days" to "Selesaikan tugas selama 30 hari berturut-turut", "Reach level 5" to "Capai level 5", "Reach level 10" to "Capai level 10", "Complete a Hidden Quest" to "Selesaikan Misi Tersembunyi", "Track 10 hours of focus" to "Catat 10 jam fokus", "Complete 25 focus sessions" to "Selesaikan 25 sesi fokus", "Complete 25 Hard or Expert tasks" to "Selesaikan 25 tugas Sulit atau Ahli", "Earn 2,000 XP" to "Dapatkan 2.000 XP", "Complete tasks on 50 consecutive days" to "Selesaikan tugas selama 50 hari berturut-turut", "Complete 10 Hidden Quests" to "Selesaikan 10 Misi Tersembunyi", "Productivity intelligence" to "Kecerdasan produktivitas", "Local scores based on completion, consistency, and focus time." to "Skor lokal berdasarkan penyelesaian, konsistensi, dan waktu fokus.", "Smart next actions" to "Saran tindakan berikutnya", "Suggested locally by deadline and priority. No schedule is changed automatically." to "Disarankan secara lokal berdasarkan tenggat dan prioritas. Jadwal tidak akan diubah otomatis.", "No open tasks to schedule." to "Tidak ada tugas terbuka untuk dijadwalkan.", "None" to "Tidak ada", "Start time cannot be in the past." to "Waktu mulai tidak boleh berada di masa lalu.", "Deadline must be at least one hour after task creation." to "Tenggat harus minimal satu jam setelah tugas dibuat.", "Deadline must be later than start time." to "Tenggat harus setelah waktu mulai.", "Consistent Week" to "Pekan Konsisten", "Unstoppable" to "Tak Terhentikan", "Rising Star" to "Bintang Baru", "Taskora Legend" to "Legenda Taskora", "Quest Seeker" to "Pemburu Misi", "Focus Marathon" to "Maraton Fokus", "Focus Habit" to "Kebiasaan Fokus", "Challenge Accepted" to "Tantangan Diterima", "XP Champion" to "Juara XP", "Daily Discipline" to "Disiplin Harian", "Quest Champion" to "Juara Misi", "Quiet start and end must be different." to "Waktu mulai dan selesai jam tenang harus berbeda.", "Lakukan 15 menit aktivitas fisik ringan." to "Lakukan 15 menit aktivitas fisik ringan.", "Selesaikan 10 push-up atau alternatif gerakan ringan." to "Selesaikan 10 push-up atau alternatif gerakan ringan.", "Baca buku atau artikel selama 15 menit." to "Baca buku atau artikel selama 15 menit.", "Belajar bahasa atau keterampilan baru selama 15 menit." to "Belajar bahasa atau keterampilan baru selama 15 menit.", "Berjalan santai selama 10–15 menit." to "Berjalan santai selama 10–15 menit.", "Rapikan meja atau area kerja selama 10 menit." to "Rapikan meja atau area kerja selama 10 menit."
)
private fun uiText(value: String): String {
    if (TaskoraAppearance.language != "id") return value
    idUi[value]?.let { return it }
    return when {
        value.startsWith("Pomodoro • ") -> value.replace(" min", " menit")
        value.startsWith("Deadline: ") -> "Tenggat: " + value.removePrefix("Deadline: ")
        value.contains(" • Starts ") -> value.replace(" • Starts ", " • Mulai ")
        value.endsWith(" priority") -> value.removeSuffix(" priority") + " prioritas"
        value.contains(" of ") && value.endsWith(" completed") -> value.replace(" of ", " dari ").removeSuffix(" completed") + " selesai"
        value.startsWith("Offline • ") -> "Offline • " + value.removePrefix("Offline • ")
        value.startsWith("Database + WAL + SHM") -> "Database + WAL + SHM • " + value.substringAfter("• ") + " byte"
        value.startsWith("Tracked: ") -> "Durasi: " + value.removePrefix("Tracked: ")
        value.startsWith("Timezone: ") -> "Zona waktu: " + value.removePrefix("Timezone: ")
        value.startsWith("Reward: ") -> "Hadiah: " + value.removePrefix("Reward: ").replace(" • Double reward", " • Hadiah ganda")
        value.startsWith("This ") && value.contains(" vs previous ") -> "Periode ini dibanding periode sebelumnya"
        value.endsWith(" tasks remaining") -> value.removeSuffix(" tasks remaining") + " tugas tersisa"
        value.endsWith(" tasks completed") -> value.removeSuffix(" tasks completed") + " tugas selesai"
        value.startsWith("Level ") -> "Level " + value.removePrefix("Level ")
        value.startsWith("Reward: ") -> "Hadiah: " + value.removePrefix("Reward: ")
        value.endsWith(" days") -> value.removeSuffix(" days") + " hari"
        value.endsWith(" completed • stored locally") -> value.replace(" completed • stored locally", " selesai • tersimpan lokal")
        value.endsWith(" unlocked • stored locally") -> value.replace(" unlocked • stored locally", " terbuka • tersimpan lokal")
        value.contains(" XP to level ") -> value.replace(" XP to level ", " XP lagi menuju level ")
        value.startsWith("Next milestone: ") -> value.replace("Next milestone:", "Target berikutnya:").replace(" completed tasks", " tugas selesai").replace(" to go", " lagi")
        else -> value
    }
}
private fun uiLocale(): Locale = if (TaskoraAppearance.language == "id") Locale("id", "ID") else Locale.getDefault()
private val Bg get() = if (TaskoraAppearance.isDark) Color(0xFF111522) else Color(0xFFF3F4F6)
private val Surface get() = if (TaskoraAppearance.isDark) Color(0xFF1A2030) else Color(0xFFFAFAF8)
private val Surface2 get() = if (TaskoraAppearance.isDark) Color(0xFF242C3D) else Color(0xFFE8E9EC)
private val Purple get() = runCatching { Color(android.graphics.Color.parseColor(TaskoraAppearance.accentHex)) }.getOrDefault(Color(0xFF9B83E8))
private val Lavender get() = if (TaskoraAppearance.isDark) Purple.copy(red = (Purple.red + .18f).coerceAtMost(1f), green = (Purple.green + .16f).coerceAtMost(1f), blue = (Purple.blue + .16f).coerceAtMost(1f)) else Purple
private val Cyan get() = if (TaskoraAppearance.isDark) Color(0xFF75C8D4) else Color(0xFF4F9EAA)
private val Green get() = if (TaskoraAppearance.isDark) Color(0xFF83C9AA) else Color(0xFF4E9677)
private val Yellow get() = if (TaskoraAppearance.isDark) Color(0xFFE4C27A) else Color(0xFFAA8545)
private val Red get() = if (TaskoraAppearance.isDark) Color(0xFFE39A9A) else Color(0xFFB96C6C)
private val Muted get() = if (TaskoraAppearance.isDark) Color(0xFFA7AFBF) else Color(0xFF747985)
private val White get() = if (TaskoraAppearance.isDark) Color(0xFFE9ECF3) else Color(0xFF30333A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TaskAlarmScheduler.createChannel(this)
        TaskDeadlineSweepScheduler.schedule(this)
        val preferences = getSharedPreferences("taskora_preferences", MODE_PRIVATE)
        TaskoraAppearance.mode = preferences.getString("appearance", "dark") ?: "dark"
        TaskoraAppearance.language = preferences.getString("language", "en") ?: "en"
        TaskoraAppearance.accentHex = preferences.getString("accent_hex", "#9B83E8") ?: "#9B83E8"
        TaskoraAppearance.showChallenges = preferences.getBoolean("dashboard_challenges", true)
        TaskoraAppearance.showUpcoming = preferences.getBoolean("dashboard_upcoming", true)
        TaskoraAppearance.showFocusTip = preferences.getBoolean("dashboard_focus_tip", true)
        setContent { TaskoraTheme { TaskoraApp() } }
    }

    override fun onResume() {
        super.onResume()
        TaskAlarmScheduler.rescheduleAll(this)
        TaskDeadlineSweepScheduler.schedule(this)
    }
}

@Composable
private fun TaskoraTheme(content: @Composable () -> Unit) {
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val dark = when (TaskoraAppearance.mode) { "light" -> false; "system" -> systemDark; else -> true }
    SideEffect { TaskoraAppearance.isDark = dark }
    val scheme = if (dark) darkColorScheme(primary = Purple, secondary = Cyan, background = Bg, surface = Surface, onBackground = White, onSurface = White)
        else lightColorScheme(primary = Purple, onPrimary = Color.White, secondary = Cyan, background = Bg, surface = Surface, onBackground = White, onSurface = White, surfaceVariant = Surface2, onSurfaceVariant = Muted)
    MaterialTheme(colorScheme = scheme, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskoraApp(viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE) }
    val xpHistory by viewModel.xpHistory.collectAsStateWithLifecycle()
    val totalXp by viewModel.totalXp.collectAsStateWithLifecycle()
    val hiddenQuests by viewModel.hiddenQuests.collectAsStateWithLifecycle()
    val recurringTemplates by viewModel.recurringTemplates.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val achievementUnlocks by viewModel.achievements.collectAsStateWithLifecycle()
    var profileRevision by remember { mutableIntStateOf(0) }
    val profileName = remember(profileRevision) { prefs.getString("display_name", "Taskora User") ?: "Taskora User" }
    val profileAvatar = remember(profileRevision) { prefs.getString("avatar", "") ?: "" }
    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.use {
                val settingKeys = listOf("display_name", "avatar", "appearance", "date_format", "time_format", "first_day", "language", "accent_hex", "dashboard_challenges", "dashboard_upcoming", "dashboard_focus_tip", "quiet_hours_enabled", "reminder_lead_minutes", "quiet_start_minute", "quiet_end_minute")
                val settings = settingKeys.mapNotNull { key -> prefs.all[key]?.toString()?.let { value -> key to value } }.toMap()
                val avatarFile = java.io.File(context.filesDir, "taskora_profile_avatar.jpg")
                TaskBackup.write(tasks, it, xpHistory, recurringTemplates, hiddenQuests, settings,
                    avatarFile.takeIf { file -> file.exists() && file.length() <= 5_000_000L }?.readBytes(), focusSessions, achievementUnlocks)
            } ?: error("Cannot open destination")
        }.onSuccess { Toast.makeText(context, uiText("Backup exported successfully"), Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, (if (TaskoraAppearance.language == "id") "Ekspor gagal: " else "Export failed: ") + it.localizedMessage, Toast.LENGTH_LONG).show() }
    }
    val importBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val restored = context.contentResolver.openInputStream(uri)?.use { TaskBackup.read(it) } ?: error("Cannot read backup")
            restored
        }.onSuccess { restored ->
            viewModel.restoreBackup(restored)
            applyBackupPreferences(prefs, restored.settings)
            if (restored.avatarBytes != null) runCatching {
                val avatarFile = java.io.File(context.filesDir, "taskora_profile_avatar.jpg")
                avatarFile.writeBytes(restored.avatarBytes)
                prefs.edit().putString("avatar", Uri.fromFile(avatarFile).toString()).apply()
            } else {
                val backupAvatar = restored.settings["avatar"]
                if (backupAvatar.isNullOrBlank() || backupAvatar.startsWith("file:") || backupAvatar.startsWith("content:")) prefs.edit().remove("avatar").apply()
                else prefs.edit().putString("avatar", backupAvatar).apply()
            }
            profileRevision++
            TaskoraAppearance.mode = prefs.getString("appearance", TaskoraAppearance.mode) ?: TaskoraAppearance.mode
            TaskoraAppearance.language = prefs.getString("language", TaskoraAppearance.language) ?: TaskoraAppearance.language
            TaskoraAppearance.accentHex = prefs.getString("accent_hex", TaskoraAppearance.accentHex) ?: TaskoraAppearance.accentHex
            TaskoraAppearance.showChallenges = prefs.getBoolean("dashboard_challenges", true)
            TaskoraAppearance.showUpcoming = prefs.getBoolean("dashboard_upcoming", true)
            TaskoraAppearance.showFocusTip = prefs.getBoolean("dashboard_focus_tip", true)
            Toast.makeText(context, if (TaskoraAppearance.language == "id") "Berhasil mengimpor ${restored.tasks.size} tugas, progres, dan pengaturan" else "Imported ${restored.tasks.size} tasks, progression, and settings", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, (if (TaskoraAppearance.language == "id") "Impor gagal: " else "Import failed: ") + it.localizedMessage, Toast.LENGTH_LONG).show() }
    }
    val saveEncryptedSlot: (Int, String) -> Unit = { slot, password ->
        runCatching {
            val output = java.io.ByteArrayOutputStream()
            val settingKeys = listOf("display_name", "avatar", "appearance", "date_format", "time_format", "first_day", "language", "accent_hex", "dashboard_challenges", "dashboard_upcoming", "dashboard_focus_tip", "quiet_hours_enabled", "reminder_lead_minutes", "quiet_start_minute", "quiet_end_minute")
            val settings = settingKeys.mapNotNull { key -> prefs.all[key]?.toString()?.let { key to it } }.toMap()
            val avatarFile = java.io.File(context.filesDir, "taskora_profile_avatar.jpg")
            TaskBackup.write(tasks, output, xpHistory, recurringTemplates, hiddenQuests, settings,
                avatarFile.takeIf { it.exists() && it.length() <= 5_000_000L }?.readBytes(), focusSessions, achievementUnlocks)
            EncryptedBackupStore.save(context, slot, password.toCharArray(), output.toByteArray())
        }.onSuccess { Toast.makeText(context, uiText("Encrypted backup saved") + " $slot", Toast.LENGTH_SHORT).show() }
            .onFailure { Toast.makeText(context, (if (TaskoraAppearance.language == "id") "Pencadangan gagal: " else "Backup failed: ") + it.localizedMessage, Toast.LENGTH_LONG).show() }
    }
    val restoreEncryptedSlot: (Int, String) -> Unit = { slot, password ->
        runCatching {
            val plain = EncryptedBackupStore.restore(context, slot, password.toCharArray())
            TaskBackup.read(java.io.ByteArrayInputStream(plain))
        }.onSuccess { restored ->
            viewModel.restoreBackup(restored)
            applyBackupPreferences(prefs, restored.settings)
            if (restored.avatarBytes != null) runCatching {
                val avatarFile = java.io.File(context.filesDir, "taskora_profile_avatar.jpg")
                avatarFile.writeBytes(restored.avatarBytes)
                prefs.edit().putString("avatar", Uri.fromFile(avatarFile).toString()).apply()
            } else {
                val backupAvatar = restored.settings["avatar"]
                if (backupAvatar.isNullOrBlank() || backupAvatar.startsWith("file:") || backupAvatar.startsWith("content:")) prefs.edit().remove("avatar").apply()
                else prefs.edit().putString("avatar", backupAvatar).apply()
            }
            profileRevision++
            TaskoraAppearance.mode = prefs.getString("appearance", TaskoraAppearance.mode) ?: TaskoraAppearance.mode
            TaskoraAppearance.language = prefs.getString("language", TaskoraAppearance.language) ?: TaskoraAppearance.language
            TaskoraAppearance.accentHex = prefs.getString("accent_hex", TaskoraAppearance.accentHex) ?: TaskoraAppearance.accentHex
            TaskoraAppearance.showChallenges = prefs.getBoolean("dashboard_challenges", true)
            TaskoraAppearance.showUpcoming = prefs.getBoolean("dashboard_upcoming", true)
            TaskoraAppearance.showFocusTip = prefs.getBoolean("dashboard_focus_tip", true)
            Toast.makeText(context, uiText("Encrypted backup restored"), Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, (if (TaskoraAppearance.language == "id") "Pemulihan gagal. Periksa kata sandi atau file cadangan: " else "Restore failed. Check your password or backup file: ") + it.localizedMessage, Toast.LENGTH_LONG).show() }
    }
    val nav = rememberNavController()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var showIntro by remember { mutableStateOf(true) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    if (showIntro) {
        SplashScreen(onContinue = { showIntro = false })
        return
    }

    val items = listOf(
        BottomItem("home", "Home", Icons.Default.Home),
        BottomItem("tasks", "Tasks", Icons.Default.CheckCircle),
        BottomItem("calendar", "Calendar", Icons.Default.CalendarMonth),
        BottomItem("analytics", "Stats", Icons.Default.BarChart),
        BottomItem("recurring", "Recurring", Icons.Default.Repeat),
        BottomItem("focus", "Focus", Icons.Default.Timer),
        BottomItem("profile", "Profile", Icons.Default.Person)
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pageTitle = items.firstOrNull { it.route == currentRoute }?.label ?: "Taskora"
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(304.dp), drawerContainerColor = Surface) {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Purple), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, null, tint = White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column { Text(uiText("TASKORA"), color = White, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp); Text(uiText("Make every day count"), color = Muted, fontSize = 11.sp) }
                }
                HorizontalDivider(color = Surface2, modifier = Modifier.padding(vertical = 12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) }, icon = { Icon(item.icon, null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            nav.navigate(item.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp),
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = Purple.copy(alpha = .22f), selectedTextColor = Lavender, selectedIconColor = Lavender, unselectedTextColor = Muted, unselectedIconColor = Muted)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(uiText("100% OFFLINE • LOCAL DATA"), color = Muted, fontSize = 9.sp, letterSpacing = 1.sp, modifier = Modifier.padding(22.dp))
            }
        }
    ) {
    Scaffold(
        containerColor = Bg,
        topBar = {
            TopAppBar(
                title = { Column { Text(uiText("TASKORA"), fontSize = 11.sp, letterSpacing = 2.sp, color = Lavender, fontWeight = FontWeight.Bold); Text(uiText(pageTitle), color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp) } },
                navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Open navigation", tint = White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Bg)
            )
        },
        floatingActionButton = {
            if (currentRoute in listOf("home", "tasks")) {
                FloatingActionButton(onClick = { showAdd = true }, containerColor = Purple,
                    contentColor = Color.White, shape = CircleShape) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah tugas")
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = "home", modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { it / 14 } }, exitTransition = { fadeOut(tween(100)) + slideOutHorizontally(tween(100)) { -it / 20 } },
            popEnterTransition = { fadeIn(tween(150)) + slideInHorizontally(tween(150)) { -it / 14 } }, popExitTransition = { fadeOut(tween(100)) + slideOutHorizontally(tween(100)) { it / 20 } }) {
            composable("home") { DashboardScreen(tasks, profileName, profileAvatar, profileRevision, totalXp, xpHistory, hiddenQuests, TaskoraAppearance.showChallenges, TaskoraAppearance.showUpcoming, TaskoraAppearance.showFocusTip, onToggle = viewModel::toggleTask, onEdit = { editingTask = it }, onHiddenQuestComplete = viewModel::completeHiddenQuest, onClaimChallenge = viewModel::claimChallenge) }
            composable("tasks") {
                TasksScreen(tasks, onToggle = { id -> viewModel.toggleTask(id) }, onStart = { id -> viewModel.startTask(id) }, onEdit = { editingTask = it }, onDelete = { pendingDelete = it })
            }
            composable("calendar") { CalendarScreen(tasks, onEdit = { editingTask = it }, firstDayOfWeek = context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE).getString("first_day", "Monday") ?: "Monday") }
            composable("analytics") { AnalyticsScreen(tasks, totalXp, xpHistory, focusSessions) }
            composable("focus") { FocusScreen(tasks, focusSessions, onStart = viewModel::startFocusSession, onPause = viewModel::pauseFocusSession, onResume = viewModel::resumeFocusSession, onStop = viewModel::stopFocusSession) }
            composable("recurring") { RecurringScreen(
                templates = recurringTemplates,
                onAdd = viewModel::addRecurringTemplate,
                onUpdate = viewModel::updateRecurringTemplate,
                onDelete = viewModel::deleteRecurringTemplate,
                onGenerate = viewModel::generateNextWeek
            ) }
            composable("profile") { ProfileScreen(tasks, totalXp, xpHistory, achievementUnlocks, focusSessions, profileRevision, onProfileChanged = { profileRevision++ },
                onEnableReminders = { (context as? MainActivity)?.requestReminderAccess() },
                onExportBackup = { exportBackup.launch("taskora-backup.json") },
                onImportBackup = { importBackup.launch(arrayOf("application/json", "text/json", "application/octet-stream")) },
                onSaveEncryptedBackup = saveEncryptedSlot,
                onRestoreEncryptedBackup = restoreEncryptedSlot
            ) }
        }
    }

    if (showAdd) {
        TaskEditorDialog(task = null, allTasks = tasks, onDismiss = { showAdd = false }, onSave = { title, description, category, startAt, deadlineAt, priority, difficulty ->
            viewModel.addTask(title, description, category, startAt, deadlineAt, priority, difficulty)
            showAdd = false
        })
    }
    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Surface,
            title = { Text(uiText("Delete task?"), color = White) },
            text = { Text("\"${task.title}\" will be permanently deleted from this device.", color = Muted) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteTask(task); pendingDelete = null }) {
                    Text(uiText("Delete"), color = Red)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(uiText("Cancel")) } }
        )
    }
    editingTask?.let { task ->
        TaskEditorDialog(task = task, allTasks = tasks, onDismiss = { editingTask = null }, onSave = { title, description, category, startAt, deadlineAt, priority, difficulty ->
            val deadlineText = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(deadlineAt))
            viewModel.updateTask(task.copy(title = title, description = description, category = category, startAt = startAt, deadlineAt = deadlineAt, deadline = deadlineText, priority = priority, difficulty = difficulty))
            editingTask = null
        })
    }
    }
}

private fun MainActivity.requestReminderAccess() {
    if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 7301)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            runCatching { startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName"))) }
        }
    }
}

private data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        content = content
    )
}

@Composable
private fun SplashScreen(onContinue: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(350)
        onContinue()
    }
    Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(82.dp).clip(RoundedCornerShape(26.dp)).background(Purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = White, modifier = Modifier.size(42.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(uiText("TASKORA"), color = White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
            Text(uiText("Make every day count."), color = Muted, fontSize = 14.sp)
            Spacer(Modifier.height(34.dp))
            CircularProgressIndicator(color = Lavender, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        }
        Text(uiText("OFFLINE PRODUCTIVITY"), color = Muted, fontSize = 10.sp, letterSpacing = 2.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp))
    }
}

@Composable
private fun DashboardScreen(tasks: List<TaskEntity>, profileName: String, profileAvatar: String, avatarRevision: Int, totalXp: Int, xpHistory: List<XpHistory>, hiddenQuests: List<HiddenQuest>, showChallenges: Boolean, showUpcoming: Boolean, showFocusTip: Boolean, onToggle: (Long) -> Unit, onEdit: (TaskEntity) -> Unit, onHiddenQuestComplete: (Long) -> Unit, onClaimChallenge: (String, String, Int) -> Unit) {
    val completed = tasks.count { it.done }
    // "Today" is a calendar-day concept: a task scheduled for today stays here all day,
    // regardless of its start hour. Deadline expiry is also evaluated by local date.
    var today by remember { mutableStateOf(java.time.LocalDate.now(ZoneId.systemDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = java.time.ZonedDateTime.now(ZoneId.systemDefault())
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plusSeconds(1)
            kotlinx.coroutines.delay(java.time.Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1_000L))
            today = java.time.LocalDate.now(ZoneId.systemDefault())
        }
    }
    // Today's Tasks is keyed strictly to the local calendar date of the deadline.
    val todayTasks = tasks.filter { task ->
        !task.done && task.effectiveDeadlineMillis()?.toLocalDate() == today
    }.sortedBy { it.effectiveDeadlineMillis() ?: Long.MAX_VALUE }
    // A past calendar date is never shown on Home. Upcoming covers the next seven days.
    val upcomingTasks = tasks.filter { task ->
        !task.done && task.startAt.toLocalDate().let { it.isAfter(today) && !it.isAfter(today.plusDays(7)) }
    }.sortedBy { it.startAt }
    val xp = totalXp
    val streak = calculateStreakFromHistory(xpHistory)
    var questClock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(30_000L); questClock = System.currentTimeMillis() } }
    ScreenColumn {
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(java.text.SimpleDateFormat("EEEE, ${LocalContext.current.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE).getString("date_format", "d MMM yyyy") ?: "d MMM yyyy"}", Locale.getDefault()).format(Date()), color = Muted, fontSize = 10.sp, letterSpacing = 1.4.sp)
                Text("${uiText(when (java.time.LocalTime.now().hour) { in 4..10 -> "Good morning"; in 11..14 -> "Good afternoon"; in 15..18 -> "Good evening"; else -> "Good night" })}, ${profileName.ifBlank { if (TaskoraAppearance.language == "id") "teman" else "there" }} 👋", color = White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            val avatarBitmap = remember(profileAvatar, avatarRevision) {
                if (profileAvatar.startsWith("file:")) runCatching {
                    android.graphics.BitmapFactory.decodeFile(Uri.parse(profileAvatar).path)?.asImageBitmap()
                }.getOrNull() else null
            }
            Box(Modifier.size(48.dp).clip(CircleShape).background(Surface2), contentAlignment = Alignment.Center) {
                when {
                    avatarBitmap != null -> Image(bitmap = avatarBitmap, contentDescription = uiText("Profile photo"), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    profileAvatar.isNotBlank() -> Text(profileAvatar, color = Lavender, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    else -> Text(profileName.trim().firstOrNull()?.uppercase() ?: "T", color = Lavender, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(
            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF7046D8), Color(0xFF34236F)))
        ).padding(20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(uiText("YOUR DAILY PROGRESS"), color = Color.White.copy(alpha = .75f), fontSize = 10.sp, letterSpacing = 1.3.sp)
                        Text(uiText("Keep your momentum!"), color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.EmojiEvents, null, tint = Yellow, modifier = Modifier.size(34.dp))
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$completed/${tasks.size}", color = White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(uiText("tasks completed"), color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Cyan, trackColor = Color.White.copy(alpha = .2f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        if (xp > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("TOTAL XP", xp.toString(), "Earned from completions", Icons.Default.Bolt, Cyan, Modifier.weight(1f))
                StatCard("CURRENT STREAK", "$streak days", if (streak > 0) "Keep it going!" else "Complete a task today", Icons.Default.LocalFireDepartment, Yellow, Modifier.weight(1f))
            }
        } else {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, null, tint = Yellow, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp))
                Column { Text(uiText("Your journey starts here"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text(uiText("Complete your first task to unlock XP, levels, and streaks."), color = Muted, fontSize = 11.sp) }
            }
        }
        Spacer(Modifier.height(18.dp))
        val completionEvents = xpHistory.filter { it.eventType in listOf("COMPLETION", "LATE_COMPLETION") }
        val completedDates = completionEvents.map { Instant.ofEpochMilli(it.earnedAt).atZone(ZoneId.systemDefault()).toLocalDate() }
        val weekStartChallenge = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val dailyProgress = completedDates.count { it == today }
        val weeklyProgress = completedDates.count { !it.isBefore(weekStartChallenge) && !it.isAfter(today) }
        val monthlyProgress = completedDates.count { it.year == today.year && it.month == today.month }
        val dailyKey = "daily_${today}"
        val weeklyKey = "weekly_${weekStartChallenge}"
        val monthlyKey = "monthly_${today.year}_${today.monthValue}"
        val claimedIds = xpHistory.filter { it.eventType == "CHALLENGE_REWARD" }.map { it.taskId }.toSet()
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp)) {
            Text(uiText("Challenges"), color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(uiText("Small goals to build consistency."), color = Muted, fontSize = 10.sp)
            ChallengeProgressRow(uiText("Daily challenge"), dailyProgress, 3, 15, dailyKey, challengeLedgerId(dailyKey) in claimedIds, onClaimChallenge)
            ChallengeProgressRow(uiText("Weekly challenge"), weeklyProgress, 10, 50, weeklyKey, challengeLedgerId(weeklyKey) in claimedIds, onClaimChallenge)
            ChallengeProgressRow(uiText("Monthly challenge"), monthlyProgress, 30, 150, monthlyKey, challengeLedgerId(monthlyKey) in claimedIds, onClaimChallenge)
        }
        Spacer(Modifier.height(24.dp))
        SectionTitle("Today's tasks", "${todayTasks.size} active")
        Spacer(Modifier.height(10.dp))
        if (todayTasks.isEmpty()) Text(if (TaskoraAppearance.language == "id") "Belum ada tugas aktif untuk hari ini." else "No active tasks for today.", color = Muted, fontSize = 13.sp)
        todayTasks.take(5).forEach { TaskRow(it, onClick = { onEdit(it) }, onToggle = { onToggle(it.id) }) }
        if (todayTasks.isEmpty()) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventAvailable, null, tint = Green, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp))
                Column { Text(uiText("Your day is clear"), color = White, fontWeight = FontWeight.SemiBold); Text(uiText("Add a task scheduled for today to get started."), color = Muted, fontSize = 11.sp) }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (showUpcoming) {
        SectionTitle("Coming up", "Next 7 days")
        Spacer(Modifier.height(8.dp))
        if (upcomingTasks.isEmpty()) Text(uiText("No upcoming tasks yet."), color = Muted, fontSize = 12.sp)
        else Column(
            Modifier.fillMaxWidth().heightIn(max = 300.dp)
                .clip(RoundedCornerShape(16.dp)).background(Surface.copy(alpha = .55f))
                .verticalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            upcomingTasks.forEach { TaskRow(it, onClick = { if (!it.done && !it.isPastDue()) onEdit(it) }) }
        }
        }
        val activeHiddenQuest = hiddenQuests.firstOrNull { it.status == "PENDING" && it.appearsAt <= questClock && it.deadlineAt >= questClock }
        if (activeHiddenQuest != null) {
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface2).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(uiText("✨ HIDDEN QUEST"), color = Lavender, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(uiText(activeHiddenQuest.title), color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(uiText(activeHiddenQuest.description), color = Muted, fontSize = 12.sp)
                    Text(uiText("Reward: ${activeHiddenQuest.rewardXp} XP • Double reward"), color = Green, fontSize = 11.sp)
                }
                Button(onClick = { onHiddenQuestComplete(activeHiddenQuest.id) }) { Text(uiText("Claim")) }
            }
        }
        if (showFocusTip) {
            Spacer(Modifier.height(20.dp))
            SectionTitle("Focus tip", null)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp)) {
                Icon(Icons.Default.Lightbulb, null, tint = Yellow)
                Spacer(Modifier.width(12.dp))
                Text(if (TaskoraAppearance.language == "id") "Progres yang jujur, sekecil apa pun, lebih berharga daripada pencapaian besar yang tidak mencerminkan kenyataan. Catat apa yang benar-benar kamu kerjakan." else "Honest progress, however small, is more valuable than a big achievement that does not reflect reality. Track what you actually do.", color = Muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun ChallengeProgressRow(title: String, progress: Int, target: Int, reward: Int, key: String, claimed: Boolean, onClaim: (String, String, Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = White, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("${progress.coerceAtMost(target)}/$target", color = if (progress >= target) Green else Lavender, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        LinearProgressIndicator({ (progress.toFloat() / target).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = if (progress >= target) Green else Purple, trackColor = Surface2)
        if (progress >= target) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (claimed) uiText("Reward claimed") else "${uiText("Reward available")}: $reward XP", color = if (claimed) Green else Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            if (!claimed) TextButton(onClick = { onClaim(key, title, reward) }) { Text(uiText("Claim XP"), color = Lavender, fontSize = 10.sp) }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, caption: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(12.dp))
        Text(uiText(title), color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(uiText(caption), color = tint, fontSize = 10.sp)
    }
}

@Composable
private fun SectionTitle(title: String, action: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(uiText(title), color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (action != null) Text(uiText(action), color = Lavender, fontSize = 12.sp)
    }
}

@Composable
private fun TaskRow(task: TaskEntity, onClick: () -> Unit, onToggle: (() -> Unit)? = null, onStart: (() -> Unit)? = null) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    var confirmComplete by remember(task.id) { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().animateContentSize().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp))
            .background(Surface).clickable { onClick() }.padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onToggle != null && !task.done) {
            Checkbox(checked = false, onCheckedChange = { confirmComplete = true },
                colors = CheckboxDefaults.colors(checkedColor = Purple, uncheckedColor = Muted))
        } else {
            Box(Modifier.size(4.dp, 38.dp).clip(CircleShape).background(
                when (task.priority) { "High" -> Red; "Medium" -> Yellow; else -> Green }
            ))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(task.title, color = if (task.done) Muted else White, fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(uiText("${task.category}  •  Starts ${formatTaskDateTime(task.startAt, context)}"), color = Muted, fontSize = 10.sp)
            Text(uiText("Deadline: ${task.effectiveDeadlineMillis()?.let { formatTaskDateTime(it, context) } ?: uiText("None")}"), color = Muted, fontSize = 10.sp)
            if (task.isOverdueToday()) Text(uiText("Overdue today • still completable"), color = Yellow, fontSize = 10.sp)
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                Text(task.description, color = Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (task.actualStartedAt != null) {
                val durationEnd = task.completedAt ?: System.currentTimeMillis()
                val minutes = ((durationEnd - task.actualStartedAt) / 60_000L).coerceAtLeast(0)
                Text(uiText("Tracked: ${minutes / 60}h ${minutes % 60}m"), color = Cyan, fontSize = 10.sp)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(task.priority, color = when (task.priority) { "High" -> Red; "Medium" -> Yellow; else -> Green }, fontSize = 10.sp)
            if (onStart != null && task.actualStartedAt == null && !task.done) {
                TextButton(onClick = onStart, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) { Text(uiText("Start"), color = Cyan, fontSize = 10.sp) }
            }
        }
    }
    if (confirmComplete) {
        AlertDialog(onDismissRequest = { confirmComplete = false }, containerColor = Surface,
            title = { Text(uiText("Complete this task?"), color = White) },
            text = { Text(uiText("Once confirmed, completion is locked in Tasks and cannot be unchecked. Keep your progress honest."), color = Muted) },
            confirmButton = { TextButton(onClick = { confirmComplete = false; haptics.performHapticFeedback(HapticFeedbackType.LongPress); onToggle?.invoke() }) { Text(uiText("Confirm completion"), color = Green) } },
            dismissButton = { TextButton(onClick = { confirmComplete = false }) { Text(uiText("Not yet")) } })
    }
}

@Composable
private fun TasksScreen(tasks: List<TaskEntity>, onToggle: (Long) -> Unit, onStart: (Long) -> Unit, onEdit: (TaskEntity) -> Unit, onDelete: (TaskEntity) -> Unit) {
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf("Deadline") }
    var status by remember { mutableStateOf("All status") }
    ScreenColumn {
        Spacer(Modifier.height(24.dp))
        Text(uiText("My Tasks"), color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(uiText("Stay focused, one task at a time."), color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, placeholder = { Text(uiText("Search tasks…")) }, leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Purple, unfocusedBorderColor = Surface2, focusedContainerColor = Surface, unfocusedContainerColor = Surface))
        Spacer(Modifier.height(12.dp))
        Text(uiText("STATUS"), color = Muted, fontSize = 10.sp, letterSpacing = 1.1.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("All status", "Open", "Completed", "Overdue").forEach { option ->
                FilterChip(selected = status == option, onClick = { status = option }, label = { Text(uiText(option), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple.copy(alpha = .18f), selectedLabelColor = Lavender))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(uiText("Sort by"), color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            var sortExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { sortExpanded = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text(sort, fontSize = 11.sp); Icon(Icons.Default.ArrowDropDown, null) }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    listOf("Deadline", "Priority", "Newest", "Start date").forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = { sort = option; sortExpanded = false })
                    }
                }
            }
        }
        val filtered = tasks.withIndex().filter { (_, task) ->
            (query.isBlank() || task.title.contains(query, true) || task.description.contains(query, true) || task.category.contains(query, true)) &&
            when (status) { "Open" -> !task.done && !task.isPastDue(); "Completed" -> task.done; "Overdue" -> !task.done && task.isPastDue(); else -> true }
        }
        val visible = when (sort) {
            "Priority" -> filtered.sortedWith(compareBy<IndexedValue<TaskEntity>> { it.value.done }.thenBy { when (it.value.priority) { "High" -> 0; "Medium" -> 1; else -> 2 } })
            "Newest" -> filtered.sortedWith(compareBy<IndexedValue<TaskEntity>> { it.value.done }.thenByDescending { it.value.createdAt })
            "Start date" -> filtered.sortedWith(compareBy<IndexedValue<TaskEntity>> { it.value.done }.thenBy { it.value.startAt })
            else -> filtered.sortedWith(compareBy<IndexedValue<TaskEntity>> { it.value.done }.thenBy { it.value.effectiveDeadlineMillis() ?: Long.MAX_VALUE })
        }
        Text(uiText("${visible.count { !it.value.done }} tasks remaining"), color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        if (visible.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, tint = Muted, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(8.dp)); Text(uiText("No matching tasks"), color = White, fontWeight = FontWeight.SemiBold)
                Text(uiText("Try another keyword or filter."), color = Muted, fontSize = 12.sp)
            }
        }
        visible.forEach { indexed ->
            TaskRow(
                indexed.value,
                onClick = { if (!indexed.value.done && !indexed.value.isPastDue()) onEdit(indexed.value) },
                onToggle = if (indexed.value.done || indexed.value.isPastDue()) null else ({ onToggle(indexed.value.id) }),
                onStart = if (!indexed.value.done && !indexed.value.isPastDue() && indexed.value.actualStartedAt == null) ({ onStart(indexed.value.id) }) else null
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (indexed.value.done) {
                    Text(uiText("Completed • Permanently locked"), color = Green, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                } else if (indexed.value.isPastDue()) {
                    Text(uiText("Past deadline • Read only"), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                } else {
                    TextButton(onClick = { onEdit(indexed.value) }) { Text(uiText("Edit"), color = Lavender) }
                    TextButton(onClick = { onDelete(indexed.value) }) { Text(uiText("Delete"), color = Red) }
                }
            }
        }

        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun RecurringScreen(
    templates: List<RecurringTemplate>,
    onAdd: (RecurringTemplate) -> Unit,
    onUpdate: (RecurringTemplate) -> Unit,
    onDelete: (RecurringTemplate) -> Unit,
    onGenerate: (RecurringTemplate) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecurringTemplate?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal") }
    var priority by remember { mutableStateOf("Medium") }
    var difficulty by remember { mutableStateOf("Medium") }
    var startTime by remember { mutableStateOf("09:00") }
    var deadlineTime by remember { mutableStateOf("17:00") }
    var days by remember { mutableStateOf(setOf(1, 3, 5)) }
    var confirmTemplate by remember { mutableStateOf<RecurringTemplate?>(null) }
    ScreenColumn {
        Spacer(Modifier.height(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(uiText("Weekly routines"), color = White, fontSize = 26.sp, fontWeight = FontWeight.Bold); Text(uiText("Templates are only turned into tasks after confirmation."), color = Muted, fontSize = 12.sp) }
            Button(onClick = { editing = null; title = ""; description = ""; category = "Personal"; priority = "Medium"; difficulty = "Medium"; startTime = "09:00"; deadlineTime = "17:00"; days = setOf(1,3,5); showAdd = true }) { Icon(Icons.Default.Add, null); Text(uiText("Add")) }
        }
        Spacer(Modifier.height(16.dp))
        if (templates.isEmpty()) Text(uiText("No recurring templates yet."), color = Muted)
        templates.forEach { item ->
            val dayNames = item.weekdays.split(",").mapNotNull { it.toIntOrNull() }
                .map { uiText(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").getOrElse(it - 1) { "?" }) }.joinToString()
            Column(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(16.dp)).background(Surface).padding(15.dp)) {
                Text(item.title, color = White, fontWeight = FontWeight.Bold)
                Text("${uiText(item.category)} • $dayNames", color = Muted, fontSize = 11.sp)
                Text("${item.startHour.toString().padStart(2,'0')}:${item.startMinute.toString().padStart(2,'0')} – ${item.deadlineHour.toString().padStart(2,'0')}:${item.deadlineMinute.toString().padStart(2,'0')}", color = Muted, fontSize = 11.sp)
                val nextWeekKey = java.time.LocalDate.now().with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).toString()
                val alreadyGenerated = item.lastGeneratedWeek == nextWeekKey
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { editing = item; title = item.title; description = item.description; category = item.category; priority = item.priority; difficulty = item.difficulty; days = item.weekdays.split(",").mapNotNull { it.toIntOrNull() }.toSet(); startTime = "%02d:%02d".format(item.startHour, item.startMinute); deadlineTime = "%02d:%02d".format(item.deadlineHour, item.deadlineMinute); showAdd = true }) { Text(uiText("Edit"), color = Lavender) }
                    TextButton(onClick = { onDelete(item) }) { Text(uiText("Delete"), color = Red) }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { confirmTemplate = item }, enabled = !alreadyGenerated) {
                        Text(uiText(if (alreadyGenerated) "Generated this week" else "Generate next week"), maxLines = 1, fontSize = 11.sp)
                    }
                }
                if (alreadyGenerated) Text(uiText("Already generated for the upcoming week."), color = Green, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(90.dp))
    }
    if (showAdd) AlertDialog(
        onDismissRequest = { showAdd = false }, containerColor = Surface,
        title = { Text(uiText(if (editing == null) "New weekly template" else "Edit weekly template"), color = White) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(uiText("Task title")) }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text(uiText("Description")) })
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Personal", "Work", "Learn").forEach { FilterChip(category == it, { category = it }, label = { Text(uiText(it)) }) } }
                Text(uiText("Priority"), color = White, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Low", "Medium", "High").forEach { FilterChip(priority == it, { priority = it }, label = { Text(uiText(it)) }) } }
                Text(uiText("Difficulty"), color = White, fontWeight = FontWeight.SemiBold)
                listOf(listOf("Easy", "Medium"), listOf("Hard", "Expert")).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            val xpValue = difficultyXp(option)
                            FilterChip(selected = difficulty == option, onClick = { difficulty = option },
                                modifier = Modifier.weight(1f), label = {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(uiText(option), fontWeight = FontWeight.SemiBold)
                                        Text("$xpValue XP", fontSize = 9.sp, color = Muted)
                                    }
                                })
                        }
                    }
                }
                Text(uiText("Repeat on"), color = White, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf(0..3, 4..6).forEach { indexes ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            indexes.forEach { i ->
                                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                FilterChip(selected = (i + 1) in days, onClick = { days = if ((i + 1) in days) days - (i + 1) else days + (i + 1) },
                                    modifier = Modifier.weight(1f), label = { Text(uiText(labels[i]), maxLines = 1) })
                            }
                            if (indexes.last == 6) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(startTime, { startTime = it.take(5) }, modifier = Modifier.weight(1f), label = { Text(uiText("Start HH:mm")) }, singleLine = true)
                    OutlinedTextField(deadlineTime, { deadlineTime = it.take(5) }, modifier = Modifier.weight(1f), label = { Text(uiText("Deadline HH:mm")) }, singleLine = true)
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (title.isNotBlank() && days.isNotEmpty()) {
            val current = editing
            val startParts = startTime.split(":").mapNotNull { it.toIntOrNull() }
            val endParts = deadlineTime.split(":").mapNotNull { it.toIntOrNull() }
            if (startParts.size != 2 || endParts.size != 2 || startParts[0] !in 0..23 || endParts[0] !in 0..23 || startParts[1] !in 0..59 || endParts[1] !in 0..59) return@TextButton
            val startMinutes = startParts[0] * 60 + startParts[1]
            val endMinutes = endParts[0] * 60 + endParts[1]
            if (endMinutes <= startMinutes) return@TextButton
            if (current == null) onAdd(RecurringTemplate(title = title.trim(), description = description.trim(), category = category, priority = priority, difficulty = difficulty, weekdays = days.sorted().joinToString(","), startHour = startParts[0], startMinute = startParts[1], deadlineHour = endParts[0], deadlineMinute = endParts[1]))
            else onUpdate(current.copy(title = title.trim(), description = description.trim(), category = category, priority = priority, difficulty = difficulty, weekdays = days.sorted().joinToString(","), startHour = startParts[0], startMinute = startParts[1], deadlineHour = endParts[0], deadlineMinute = endParts[1]))
            title = ""; description = ""; editing = null; showAdd = false
        } }) { Text(uiText("Save template")) } },
        dismissButton = { TextButton(onClick = { showAdd = false }) { Text(uiText("Cancel")) } }
    )
    confirmTemplate?.let { item -> AlertDialog(
        onDismissRequest = { confirmTemplate = null }, containerColor = Surface,
        title = { Text(uiText("Generate next week?"), color = White) },
        text = { Text(uiText("Taskora will create the scheduled tasks for next week. This template will not generate twice for the same week."), color = Muted) },
        confirmButton = { TextButton(onClick = { onGenerate(item); confirmTemplate = null }) { Text(uiText("Confirm")) } },
        dismissButton = { TextButton(onClick = { confirmTemplate = null }) { Text(uiText("Cancel")) } }
    ) }
}

@Composable
private fun CalendarScreen(tasks: List<TaskEntity>, onEdit: (TaskEntity) -> Unit, firstDayOfWeek: String) {
    val context = LocalContext.current
    val today = remember { java.util.Calendar.getInstance() }
    var monthOffset by remember { mutableIntStateOf(0) }
    val month = remember(monthOffset) { (today.clone() as java.util.Calendar).apply { add(java.util.Calendar.MONTH, monthOffset) } }
    var selectedDay by remember(monthOffset) {
        mutableIntStateOf(if (monthOffset == 0) today.get(java.util.Calendar.DAY_OF_MONTH) else 1)
    }
    val year = month.get(java.util.Calendar.YEAR)
    val monthIndex = month.get(java.util.Calendar.MONTH)
    val first = (month.clone() as java.util.Calendar).apply { set(java.util.Calendar.DAY_OF_MONTH, 1) }
    val daysInMonth = month.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val weekStartDay = if (firstDayOfWeek.equals("Sunday", true)) java.util.Calendar.SUNDAY else java.util.Calendar.MONDAY
    val offset = (first.get(java.util.Calendar.DAY_OF_WEEK) - weekStartDay + 7) % 7
    val weekdayLabels = if (weekStartDay == java.util.Calendar.SUNDAY)
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val monthLabel = java.text.SimpleDateFormat("MMMM yyyy", uiLocale()).format(month.time)
    val selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, monthIndex + 1, selectedDay)
    val selectedLocalDate = java.time.LocalDate.parse(selectedDate)
    val dayTasks = tasks.filter { task ->
        val startDate = task.startAt.toLocalDate()
        val deadlineDate = task.effectiveDeadlineMillis()?.toLocalDate()
        if (deadlineDate == null) startDate == selectedLocalDate
        else !selectedLocalDate.isBefore(startDate) && !selectedLocalDate.isAfter(deadlineDate)
    }.distinctBy { it.id }.sortedBy { it.effectiveDeadlineMillis() ?: Long.MAX_VALUE }

    ScreenColumn {
        Spacer(Modifier.height(22.dp))
        Text(uiText("Calendar"), color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(uiText("Deadlines from your saved tasks."), color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { monthOffset-- }) { Icon(Icons.Default.ChevronLeft, null, tint = White) }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(monthLabel, color = White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(uiText("Monthly overview"), color = Muted, fontSize = 10.sp)
                }
                IconButton(onClick = { monthOffset++ }) { Icon(Icons.Default.ChevronRight, null, tint = White) }
            }
            Spacer(Modifier.height(8.dp))
            Row { weekdayLabels.forEach { Text(uiText(it), color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
            Spacer(Modifier.height(8.dp))
            val cells = List(offset) { 0 } + (1..daysInMonth).toList()
            cells.chunked(7).forEach { week ->
                Row(Modifier.padding(vertical = 2.dp)) {
                    (week + List(7 - week.size) { 0 }).forEach { day ->
                        val date = if (day == 0) "" else String.format(Locale.US, "%04d-%02d-%02d", year, monthIndex + 1, day)
                        val hasStart = day != 0 && tasks.any { it.startAt.toLocalDate().toString() == date }
                        val hasDeadline = day != 0 && tasks.any { it.effectiveDeadlineMillis()?.toLocalDate()?.toString() == date }
                        val isToday = day != 0 && year == today.get(java.util.Calendar.YEAR) && monthIndex == today.get(java.util.Calendar.MONTH) && day == today.get(java.util.Calendar.DAY_OF_MONTH)
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(when { day == selectedDay -> Purple; isToday -> Purple.copy(alpha = .18f); else -> Color.Transparent })
                            .clickable(enabled = day != 0) { selectedDay = day }, contentAlignment = Alignment.Center) {
                            if (day != 0) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$day", color = if (day == selectedDay) Color.White else if (isToday) Lavender else White, fontSize = 12.sp, fontWeight = if (isToday || day == selectedDay) FontWeight.Bold else FontWeight.Normal)
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(top = 3.dp)) {
                                    if (hasStart) Box(Modifier.size(4.dp).clip(CircleShape).background(Cyan))
                                    if (hasDeadline) Box(Modifier.size(4.dp).clip(CircleShape).background(Yellow))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(Cyan)); Spacer(Modifier.width(5.dp)); Text(uiText("Start date"), color = Muted, fontSize = 10.sp) }
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(7.dp).clip(CircleShape).background(Yellow)); Spacer(Modifier.width(5.dp)); Text(uiText("Deadline"), color = Muted, fontSize = 10.sp) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(runCatching { formatTaskDate(selectedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), context) }.getOrDefault(selectedDate), color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(uiText("Tasks scheduled for this day"), color = Muted, fontSize = 11.sp)
            }
            Surface(color = Purple.copy(alpha = .18f), shape = RoundedCornerShape(12.dp)) {
                Text("${dayTasks.size}", color = Lavender, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (dayTasks.isEmpty()) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EventAvailable, null, tint = Muted, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(7.dp))
                Text(uiText("No tasks due on this date."), color = Muted, fontSize = 12.sp)
            }
        } else dayTasks.forEach { task ->
            TaskRow(task, onClick = { if (!task.done && !task.isPastDue()) onEdit(task) })
            Row(Modifier.padding(start = 18.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.startAt.toLocalDate() == selectedLocalDate) Text(uiText("START"), color = Cyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                if (task.effectiveDeadlineMillis()?.toLocalDate() == selectedLocalDate) Text(uiText("DEADLINE"), color = Yellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                if (task.startAt.toLocalDate().isBefore(selectedLocalDate) && task.effectiveDeadlineMillis()?.toLocalDate()?.isAfter(selectedLocalDate) == true) Text(uiText("IN PROGRESS"), color = Lavender, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun AnalyticsScreen(tasks: List<TaskEntity>, totalXp: Int, xpHistory: List<XpHistory>, focusSessions: List<FocusSession>) {
    var range by remember { mutableStateOf("Week") }
    val completedTasks = tasks.filter { it.done && it.completedAt != null }
    val completionEvents = xpHistory.filter { it.eventType == "COMPLETION" || it.eventType == "LATE_COMPLETION" }
    val completed = completedTasks.size
    val rate = if (tasks.isEmpty()) 0 else (completed * 100 / tasks.size)
    val today = java.time.LocalDate.now(ZoneId.systemDefault())
    val activity = when (range) {
        "Week" -> (6 downTo 0).map { ago -> val date = today.minusDays(ago.toLong()); date to completionEvents.count { it.earnedAt.toLocalDate() == date } }
        "Month" -> (11 downTo 0).map { ago -> val date = today.minusMonths(ago.toLong()).withDayOfMonth(1); date to completionEvents.count { val d = it.earnedAt.toLocalDate(); d.year == date.year && d.month == date.month } }
        else -> (4 downTo 0).map { ago -> val date = today.minusYears(ago.toLong()).withDayOfYear(1); date to completionEvents.count { it.earnedAt.toLocalDate().year == date.year } }
    }
    val bestDay = completionEvents.groupBy { it.earnedAt.toLocalDate().dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()) }
        .maxByOrNull { it.value.size }
    val xp = totalXp
    val trackedDurations = focusSessions.filter { it.status == "COMPLETED" }.map { it.durationSeconds }
    val averageMinutes = if (trackedDurations.isEmpty()) 0L else trackedDurations.average().toLong() / 60L
    val xpLost = xpHistory.filter { it.amount < 0 }.sumOf { -it.amount }
    val onTimeCount = completedTasks.count { task -> task.effectiveDeadlineMillis()?.let { (task.completedAt ?: Long.MAX_VALUE) <= it } ?: true }
    val lateCount = completedTasks.size - onTimeCount
    ScreenColumn {
        Spacer(Modifier.height(18.dp))
        Text(uiText("Your Analytics"), color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(uiText("A clear picture of your productivity."), color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("COMPLETED", "$completed", "All time", Icons.Default.TaskAlt, Green, Modifier.weight(1f))
            StatCard("COMPLETION", "$rate%", "Overall rate", Icons.Default.DonutLarge, Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("TOTAL XP", if (xp > 0) "$xp" else "—", "Earned XP ledger", Icons.Default.Bolt, Yellow, Modifier.weight(1f))
            StatCard("REMAINING", "${tasks.count { !it.done && !it.isPastDue() }}", "Open tasks", Icons.Default.PendingActions, Lavender, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("XP LOST", xpLost.toString(), "Missed deadlines", Icons.Default.TrendingDown, Red, Modifier.weight(1f))
            StatCard("AVG DURATION", "${averageMinutes / 60}h ${averageMinutes % 60}m", "Tracked completions", Icons.Default.Timer, Cyan, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("ON TIME", onTimeCount.toString(), "Completed", Icons.Default.CheckCircle, Green, Modifier.weight(1f))
            StatCard("LATE", lateCount.toString(), "Completed late", Icons.Default.Schedule, Yellow, Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        val weekStart = today.minusDays(6)
        val activeDays = completionEvents.map { it.earnedAt.toLocalDate() }.filter { !it.isBefore(weekStart) && !it.isAfter(today) }.distinct().size
        val consistencyScore = (activeDays * 100 / 7).coerceIn(0, 100)
        val focusMinutesThisWeek = focusSessions.filter { it.status == "COMPLETED" && it.endedAt != null && !Instant.ofEpochMilli(it.endedAt).atZone(ZoneId.systemDefault()).toLocalDate().isBefore(weekStart) }.sumOf { it.durationSeconds } / 60
        val focusScore = (focusMinutesThisWeek * 100 / (7 * 60)).toInt().coerceIn(0, 100)
        val productivityScore = ((rate + consistencyScore + focusScore) / 3).coerceIn(0, 100)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp)) {
            Text(uiText("Productivity intelligence"), color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(uiText("Local scores based on completion, consistency, and focus time."), color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("PRODUCTIVITY", "$productivityScore", "Composite score", Icons.Default.AutoAwesome, Lavender, Modifier.weight(1f))
                StatCard("CONSISTENCY", "$consistencyScore%", "$activeDays of 7 active days", Icons.Default.CalendarMonth, Cyan, Modifier.weight(1f))
                StatCard("FOCUS SCORE", "$focusScore%", "Weekly focus target", Icons.Default.Timer, Green, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(12.dp))
        val suggestedTasks = tasks.filter { !it.done && !it.isPastDue() }.sortedWith(
            compareBy<TaskEntity> { it.effectiveDeadlineMillis() ?: Long.MAX_VALUE }
                .thenBy { when (it.priority) { "High" -> 0; "Medium" -> 1; else -> 2 } }
        ).take(3)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp)) {
            Text(uiText("Smart next actions"), color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(uiText("Suggested locally by deadline and priority. No schedule is changed automatically."), color = Muted, fontSize = 10.sp)
            if (suggestedTasks.isEmpty()) Text(uiText("No open tasks to schedule."), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
            suggestedTasks.forEachIndexed { index, task ->
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(25.dp).clip(CircleShape).background(Purple.copy(alpha = .2f)), contentAlignment = Alignment.Center) { Text("${index + 1}", color = Lavender, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(9.dp))
                    Column(Modifier.weight(1f)) {
                        Text(task.title, color = White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${task.priority} priority • ${task.category}", color = Muted, fontSize = 10.sp)
                    }
                    Text(task.effectiveDeadlineMillis()?.let { formatTaskDate(it, LocalContext.current) } ?: "No deadline", color = Lavender, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(uiText("Productivity trend"), color = White, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text(uiText("Completed tasks over time"), color = Muted, fontSize = 11.sp) }
                listOf("Week", "Month", "Year").forEach { label ->
                    FilterChip(selected = range == label, onClick = { range = label }, label = { Text(uiText(label), fontSize = 10.sp) })
                }
            }
            Spacer(Modifier.height(18.dp))
            val maxCount = (activity.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
            Row(Modifier.fillMaxWidth().height(130.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                activity.forEach { (date, count) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                        Box(Modifier.fillMaxWidth(.65f).fillMaxHeight((count.toFloat() / maxCount).coerceAtLeast(.025f)).clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)).background(if (count > 0) Cyan else Surface2))
                        Spacer(Modifier.height(5.dp))
                        Text(when (range) { "Week" -> date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, uiLocale()).take(1); "Month" -> date.month.getDisplayName(java.time.format.TextStyle.SHORT, uiLocale()).take(1); else -> date.year.toString().takeLast(2) }, color = Muted, fontSize = 9.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).padding(16.dp)) {
            Text(uiText("XP flow"), color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(uiText("Earned versus lost"), color = Muted, fontSize = 10.sp)
            val gained = xpHistory.filter { it.amount > 0 }.sumOf { it.amount }
            val lost = xpHistory.filter { it.amount < 0 }.sumOf { -it.amount }
            val maximum = maxOf(gained, lost, 1)
            listOf("Earned" to gained, "Lost" to lost).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(uiText(label), color = Muted, fontSize = 11.sp, modifier = Modifier.width(54.dp))
                    Box(Modifier.weight(1f).height(12.dp).clip(CircleShape).background(Surface2)) {
                        Box(Modifier.fillMaxWidth(value.toFloat() / maximum).fillMaxHeight().clip(CircleShape).background(if (label == "Earned") Green else Red))
                    }
                    Text(" $value", color = White, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("Task distribution", null)
        Spacer(Modifier.height(8.dp))
        listOf("High", "Medium", "Low").forEach { priority ->
            val count = tasks.count { it.priority == priority }
            val fraction = if (tasks.isEmpty()) 0f else count.toFloat() / tasks.size
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface).padding(14.dp)) {
                Row { Text(uiText("$priority priority"), color = White, fontSize = 12.sp, modifier = Modifier.weight(1f)); Text("$count", color = Muted, fontSize = 12.sp) }
                Spacer(Modifier.height(8.dp)); LinearProgressIndicator({ fraction }, Modifier.fillMaxWidth().height(6.dp).clip(CircleShape), color = when(priority) { "High" -> Red; "Medium" -> Yellow; else -> Green }, trackColor = Surface2)
            }
            Spacer(Modifier.height(7.dp))
        }
        Spacer(Modifier.height(12.dp))
        val currentCount: Int
        val previousCount: Int
        when (range) {
            "Week" -> { val currentStart = today.minusDays(6); val previousStart = currentStart.minusDays(7); currentCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(currentStart) && !d.isAfter(today) }; previousCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(previousStart) && d.isBefore(currentStart) } }
            "Month" -> { val currentStart = today.withDayOfMonth(1); val previousStart = currentStart.minusMonths(1); currentCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(currentStart) && !d.isAfter(today) }; previousCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(previousStart) && d.isBefore(currentStart) } }
            else -> { val currentStart = today.withDayOfYear(1); val previousStart = currentStart.minusYears(1); currentCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(currentStart) && !d.isAfter(today) }; previousCount = completionEvents.count { val d = it.earnedAt.toLocalDate(); !d.isBefore(previousStart) && d.isBefore(currentStart) } }
        }
        val delta = currentCount - previousCount
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp)) {
            Text(uiText("Period comparison"), color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(uiText("This ${range.lowercase()} vs previous ${range.lowercase()}"), color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { Text(uiText("CURRENT"), color = Muted, fontSize = 9.sp); Text("$currentCount", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) { Text(uiText("PREVIOUS"), color = Muted, fontSize = 9.sp); Text("$previousCount", color = White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f)) { Text(uiText("CHANGE"), color = Muted, fontSize = 9.sp); Text(if (delta > 0) "+$delta" else delta.toString(), color = if (delta >= 0) Green else Red, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(Modifier.height(12.dp))
        SectionTitle("Difficulty distribution", null)
        listOf("Easy", "Medium", "Hard", "Expert").forEach { difficulty ->
            val count = tasks.count { it.difficulty == difficulty }
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(difficulty, color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("$count", color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        SectionTitle("Category performance", null)
        tasks.map { it.category }.distinct().sorted().forEach { category ->
            val categoryTasks = tasks.filter { it.category == category }
            val done = categoryTasks.count { it.done }
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(category, color = White, fontSize = 13.sp); Text(uiText("$done of ${categoryTasks.size} completed"), color = Muted, fontSize = 10.sp) }
                Text("${if (categoryTasks.isEmpty()) 0 else done * 100 / categoryTasks.size}%", color = Lavender, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))
        InsightRow(Icons.Default.EmojiEvents, Yellow, "Most productive day", bestDay?.let { "${it.key} • ${it.value.size} tasks completed" } ?: "Complete a task to unlock your first insight")
        Spacer(Modifier.height(88.dp))
    }
}

@Composable
private fun InsightRow(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, title: String, desc: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ProfileScreen(tasks: List<TaskEntity>, totalXp: Int, xpHistory: List<XpHistory>, achievementUnlocks: List<AchievementUnlock>, focusSessions: List<FocusSession>, avatarRevision: Int, onProfileChanged: () -> Unit, onEnableReminders: () -> Unit, onExportBackup: () -> Unit, onImportBackup: () -> Unit, onSaveEncryptedBackup: (Int, String) -> Unit, onRestoreEncryptedBackup: (Int, String) -> Unit) {
    val context = LocalContext.current
    val storageScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE) }
    var displayName by remember(avatarRevision) { mutableStateOf(prefs.getString("display_name", "Taskora User") ?: "Taskora User") }
    var editingName by remember { mutableStateOf(false) }
    var editingAvatar by remember { mutableStateOf(false) }
    var avatar by remember(avatarRevision) { mutableStateOf(prefs.getString("avatar", "") ?: "") }
    var dateFormat by remember(avatarRevision) { mutableStateOf(prefs.getString("date_format", "d MMM yyyy") ?: "d MMM yyyy") }
    var firstDayOfWeek by remember(avatarRevision) { mutableStateOf(prefs.getString("first_day", "Monday") ?: "Monday") }
    var timeFormat by remember(avatarRevision) { mutableStateOf(prefs.getString("time_format", "24h") ?: "24h") }
    var accentHex by remember(avatarRevision) { mutableStateOf(prefs.getString("accent_hex", "#9B83E8") ?: "#9B83E8") }
    var showChallenges by remember(avatarRevision) { mutableStateOf(prefs.getBoolean("dashboard_challenges", true)) }
    var showUpcoming by remember(avatarRevision) { mutableStateOf(prefs.getBoolean("dashboard_upcoming", true)) }
    var showFocusTip by remember(avatarRevision) { mutableStateOf(prefs.getBoolean("dashboard_focus_tip", true)) }
    var quietHoursEnabled by remember(avatarRevision) { mutableStateOf(prefs.getBoolean("quiet_hours_enabled", false)) }
    var reminderLead by remember(avatarRevision) { mutableIntStateOf(prefs.getInt("reminder_lead_minutes", 10)) }
    var quietStartMinute by remember(avatarRevision) { mutableIntStateOf(prefs.getInt("quiet_start_minute", 22 * 60)) }
    var quietEndMinute by remember(avatarRevision) { mutableIntStateOf(prefs.getInt("quiet_end_minute", 7 * 60)) }
    var encryptedDialog by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    var encryptedPassword by remember { mutableStateOf("") }
    var confirmImport by remember { mutableStateOf(false) }
    var cropSource by remember { mutableStateOf<Uri?>(null) }
    var cropZoom by remember { mutableFloatStateOf(1f) }
    var cropX by remember { mutableFloatStateOf(0f) }
    var cropY by remember { mutableFloatStateOf(0f) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            cropSource = uri
            cropZoom = 1f; cropX = 0f; cropY = 0f
        }
    }
    val avatarBitmap = remember(avatar, avatarRevision) {
        when {
            avatar.startsWith("content://") -> runCatching { context.contentResolver.openInputStream(Uri.parse(avatar))?.use { android.graphics.BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull()
            avatar.startsWith("file:") -> runCatching { android.graphics.BitmapFactory.decodeFile(Uri.parse(avatar).path)?.asImageBitmap() }.getOrNull()
            else -> null
        }
    }
    var infoDialog by remember { mutableStateOf<String?>(null) }
    val completed = tasks.count { it.done }
    val databaseFile = remember { context.getDatabasePath("taskora_local.db") }
    val databaseBytes = (databaseFile.length() + java.io.File(databaseFile.path + "-wal").length() + java.io.File(databaseFile.path + "-shm").length()).coerceAtLeast(0L)
    val xp = totalXp
    val level = levelForXp(xp)
    val levelStart = xpBeforeLevel(level)
    val levelEnd = xpBeforeLevel(level + 1)
    val levelProgress = ((xp.coerceAtLeast(0) - levelStart).toFloat() / (levelEnd - levelStart).coerceAtLeast(1)).coerceIn(0f, 1f)
    val streak = calculateStreakFromHistory(xpHistory)
    val longest = calculateLongestStreakFromHistory(xpHistory)
    ScreenColumn {
        Spacer(Modifier.height(18.dp))
        Text(uiText("Profile & Settings"), color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(uiText("Your progress, preferences, and local data."), color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(18.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Surface).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(Purple.copy(alpha = .18f)).clickable { editingAvatar = true }, contentAlignment = Alignment.Center) {
                if (avatarBitmap != null) Image(bitmap = avatarBitmap, contentDescription = "Profile avatar", modifier = Modifier.fillMaxSize())
                else Text(if (avatar.isNotBlank() && !avatar.startsWith("file:") && !avatar.startsWith("content:")) avatar else displayName.trim().firstOrNull()?.uppercase() ?: "T", color = Lavender, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { editingAvatar = true }) { Text(uiText("Change avatar"), color = Lavender, fontSize = 11.sp) }
            Spacer(Modifier.height(10.dp))
            Text(displayName, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { editingName = true }) { Text(uiText("Edit display name"), color = Lavender) }
            if (xp > 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileMetric("LEVEL", level.toString().padStart(2, '0'))
                    ProfileMetric("TOTAL XP", xp.toString())
                    ProfileMetric("STREAK", "$streak 🔥")
                    ProfileMetric("BEST", "$longest days")
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(uiText("Level $level"), color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text("${xp - levelStart}/${levelEnd - levelStart} XP", color = Muted, fontSize = 11.sp) }
                Spacer(Modifier.height(7.dp)); LinearProgressIndicator({ levelProgress }, Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Purple, trackColor = Surface2)
                Text(uiText("${levelEnd - xp} XP to level ${level + 1}"), color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.End).padding(top = 5.dp))
            } else {
                Text(uiText("Complete your first task to unlock your level, XP, and streak."), color = Muted, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("Achievements", null)
        Text("${achievementUnlocks.size} unlocked • stored locally", color = Muted, fontSize = 10.sp)
        if (achievementUnlocks.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                achievementUnlocks.forEach { item ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(item.icon, fontSize = 22.sp); Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(uiText(item.title), color = White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp); Text(uiText(item.description), color = Muted, fontSize = 10.sp) }
                        Text(formatTaskDate(item.unlockedAt, context), color = Muted, fontSize = 9.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        val nextMilestone = listOf(1, 5, 10, 25, 50, 100).firstOrNull { completed < it } ?: completed + 25
        Text(uiText("Next milestone: $nextMilestone completed tasks • ${(nextMilestone - completed).coerceAtLeast(0)} to go"), color = Lavender, fontSize = 11.sp, modifier = Modifier.padding(top = 10.dp))
        Spacer(Modifier.height(18.dp))
        SectionTitle("Recent XP history", "Independent local ledger")
        if (xpHistory.isEmpty()) Text(uiText("XP history will appear here after a task is completed."), color = Muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 10.dp))
        Column(Modifier.fillMaxWidth().heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
        xpHistory.forEach { entry ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(12.dp)).background(Surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = Yellow); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(entry.taskTitle, color = White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(formatTaskDate(entry.earnedAt, context), color = Muted, fontSize = 10.sp) }
                Text("${if (entry.amount >= 0) "+" else ""}${entry.amount} XP", color = if (entry.amount >= 0) Green else Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        }
        Spacer(Modifier.height(20.dp))
        SectionTitle("Focus sessions", null)
        val focusSeconds = focusSessions.filter { it.status != "RUNNING" }.sumOf { it.durationSeconds }
        Text("${focusSessions.count { it.status == "COMPLETED" }} completed • ${focusSeconds / 3600}h ${(focusSeconds % 3600) / 60}m total", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(20.dp))
        SectionTitle("Settings", null)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Appearance"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("Choose your preferred theme"), color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("light" to "Light", "dark" to "Dark", "system" to "System").forEach { (value, label) ->
                    FilterChip(selected = TaskoraAppearance.mode == value, onClick = {
                        TaskoraAppearance.mode = value
                        prefs.edit().putString("appearance", value).apply()
                    }, label = { Text(label) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Accent color"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("Choose a color for buttons and highlights."), color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            val accents = listOf("#9B83E8" to "Violet", "#4387E8" to "Blue", "#22AFA4" to "Teal", "#D66A91" to "Rose", "#D69A36" to "Amber")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                accents.forEach { (hex, label) ->
                    val tint = Color(android.graphics.Color.parseColor(hex))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                        accentHex = hex; TaskoraAppearance.accentHex = hex; prefs.edit().putString("accent_hex", hex).apply()
                    }.padding(4.dp)) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                            if (accentHex == hex) Icon(Icons.Default.Check, null, tint = Color.White)
                        }
                        Text(uiText(label), color = if (accentHex == hex) White else Muted, fontSize = 9.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Home screen"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("Choose which sections appear on your dashboard."), color = Muted, fontSize = 10.sp)
            listOf(
                Triple("Show challenges", showChallenges, { value: Boolean -> showChallenges = value; TaskoraAppearance.showChallenges = value; prefs.edit().putBoolean("dashboard_challenges", value).apply() }),
                Triple("Show upcoming tasks", showUpcoming, { value: Boolean -> showUpcoming = value; TaskoraAppearance.showUpcoming = value; prefs.edit().putBoolean("dashboard_upcoming", value).apply() }),
                Triple("Show focus tip", showFocusTip, { value: Boolean -> showFocusTip = value; TaskoraAppearance.showFocusTip = value; prefs.edit().putBoolean("dashboard_focus_tip", value).apply() })
            ).forEach { (label, checked, change) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(uiText(label), color = White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(checked = checked, onCheckedChange = change)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("App language"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("Choose the interface language"), color = Muted, fontSize = 10.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("en" to "English", "id" to "Bahasa Indonesia").forEach { (code, label) ->
                    FilterChip(selected = TaskoraAppearance.language == code, onClick = { TaskoraAppearance.language = code; prefs.edit().putString("language", code).apply() }, label = { Text(label) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Calendar preferences"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("First day of the week"), color = Muted, fontSize = 10.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Monday", "Sunday").forEach { day ->
                    FilterChip(selected = firstDayOfWeek == day, onClick = {
                        firstDayOfWeek = day; prefs.edit().putString("first_day", day).apply()
                    }, label = { Text(uiText(day)) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Date format"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("d MMM yyyy", "MMM d, yyyy", "yyyy-MM-dd").forEach { format ->
                    FilterChip(selected = dateFormat == format, onClick = { dateFormat = format; prefs.edit().putString("date_format", format).apply() }, label = { Text(format, fontSize = 9.sp) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(uiText("Time format"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("24h", "12h").forEach { format ->
                    FilterChip(selected = timeFormat == format, onClick = { timeFormat = format; prefs.edit().putString("time_format", format).apply() }, label = { Text(format) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsActive, null, tint = Cyan); Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(uiText("Task reminders"), color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold); Text(uiText("At start + 10 minutes before deadline"), color = Muted, fontSize = 10.sp) }
            TextButton(onClick = onEnableReminders) { Text(uiText("Configure"), color = Lavender) }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Text(uiText("Reminder timing"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(uiText("Choose how early Taskora reminds you before a deadline."), color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 10, 15, 30, 60).forEach { minutes ->
                    FilterChip(selected = reminderLead == minutes, onClick = {
                        reminderLead = minutes; prefs.edit().putInt("reminder_lead_minutes", minutes).apply()
                        TaskAlarmScheduler.rescheduleAll(context)
                    }, label = { Text("${minutes}m", fontSize = 10.sp) })
                }
            }
            HorizontalDivider(color = Surface2, modifier = Modifier.padding(vertical = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(uiText("Quiet hours"), color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("${uiText("Quiet period")}: ${"%02d:%02d".format(quietStartMinute / 60, quietStartMinute % 60)} – ${"%02d:%02d".format(quietEndMinute / 60, quietEndMinute % 60)}", color = Muted, fontSize = 10.sp)
                }
                Switch(checked = quietHoursEnabled, onCheckedChange = {
                    quietHoursEnabled = it; prefs.edit().putBoolean("quiet_hours_enabled", it).apply()
                    TaskAlarmScheduler.rescheduleAll(context)
                })
            }
            if (quietHoursEnabled) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    TimePickerDialog(context, { _, hour, minute ->
                        val selected = hour * 60 + minute
                        if (selected != quietEndMinute) {
                            quietStartMinute = selected
                            prefs.edit().putInt("quiet_start_minute", quietStartMinute).apply()
                            TaskAlarmScheduler.rescheduleAll(context)
                        } else Toast.makeText(context, uiText("Quiet start and end must be different."), Toast.LENGTH_SHORT).show()
                    }, quietStartMinute / 60, quietStartMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("${uiText("Start")}: ${"%02d:%02d".format(quietStartMinute / 60, quietStartMinute % 60)}") }
                OutlinedButton(onClick = {
                    TimePickerDialog(context, { _, hour, minute ->
                        val selected = hour * 60 + minute
                        if (selected != quietStartMinute) {
                            quietEndMinute = selected
                            prefs.edit().putInt("quiet_end_minute", quietEndMinute).apply()
                            TaskAlarmScheduler.rescheduleAll(context)
                        } else Toast.makeText(context, uiText("Quiet start and end must be different."), Toast.LENGTH_SHORT).show()
                    }, quietEndMinute / 60, quietEndMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("${uiText("End")}: ${"%02d:%02d".format(quietEndMinute / 60, quietEndMinute % 60)}") }
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Storage, null, tint = Lavender); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) {
                Text(uiText("Storage management"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(uiText("Offline • ${String.format(Locale.getDefault(), "%.2f", databaseBytes / (1024.0 * 1024.0))} MB"), color = Muted, fontSize = 10.sp)
            } }
            Text(uiText("Your tasks and progress are stored privately on this device."), color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
            OutlinedButton(onClick = {
                storageScope.launch {
                    val result = runCatching { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val writable = TaskDatabase.getInstance(context).openHelper.writableDatabase
                        writable.query("PRAGMA wal_checkpoint(FULL)").use { }
                        writable.execSQL("VACUUM")
                    } }
                    Toast.makeText(context, if (result.isSuccess) uiText("Local storage optimized") else (if (TaskoraAppearance.language == "id") "Pengoptimalan gagal: " else "Optimization failed: ") + result.exceptionOrNull()?.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.AutoFixHigh, null); Spacer(Modifier.width(6.dp)); Text(uiText("Optimize local storage")) }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Backup, null, tint = Lavender); Spacer(Modifier.width(10.dp)); Column { Text(uiText("Local data backup"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text(uiText("Export or restore a JSON file on your device"), color = Muted, fontSize = 10.sp) } }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExportBackup, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileUpload, null); Spacer(Modifier.width(5.dp)); Text(uiText("Export")) }
                OutlinedButton(onClick = { confirmImport = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileDownload, null); Spacer(Modifier.width(5.dp)); Text(uiText("Import")) }
            }
            Text(uiText("Import replaces the current task list. Export a backup first."), color = Yellow, fontSize = 10.sp)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = Lavender); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(uiText("Encrypted backup slots"), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(uiText("Three private backup slots protected by your password."), color = Muted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            (1..3).forEach { slot ->
                val file = EncryptedBackupStore.file(context, slot)
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${uiText("Backup slot")} $slot", color = White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(if (file.exists()) "${uiText("Saved")} • ${(file.length() / 1024).coerceAtLeast(1)} KB" else uiText("Empty slot"), color = Muted, fontSize = 10.sp)
                    }
                    if (file.exists()) TextButton(onClick = { encryptedPassword = ""; encryptedDialog = slot to true }) { Text(uiText("Restore"), color = Lavender) }
                    TextButton(onClick = { encryptedPassword = ""; encryptedDialog = slot to false }) { Text(uiText("Save"), color = Cyan) }
                }
            }
            Text(uiText("Use at least 8 characters. Losing this password means the encrypted backup cannot be recovered."), color = Yellow, fontSize = 10.sp)
        }
        SettingsRow(Icons.Default.Info, "About Taskora", "Version 3.1.0 • Built for focus", onClick = { infoDialog = if (TaskoraAppearance.language == "id") "Tentang Taskora\n\nTaskora adalah aplikasi produktivitas offline untuk membantu merencanakan tugas, menjaga konsistensi, dan memahami progres pribadi.\nVersi 3.1.0" else "About Taskora\n\nTaskora is an offline productivity app that helps you plan tasks, build consistency, and understand your progress.\nVersion 3.1.0" })
        Spacer(Modifier.height(88.dp))
    }
    encryptedDialog?.let { (slot, restore) ->
        AlertDialog(onDismissRequest = { encryptedDialog = null }, containerColor = Surface,
            title = { Text(uiText(if (restore) "Restore encrypted backup" else "Save encrypted backup"), color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(uiText("Backup slot") + " $slot", color = Muted, fontSize = 11.sp)
                    OutlinedTextField(value = encryptedPassword, onValueChange = { encryptedPassword = it.take(128) },
                        label = { Text(uiText("Backup password")) }, singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
                    Text(uiText("At least 8 characters. Keep this password safe."), color = Muted, fontSize = 10.sp)
                }
            },
            confirmButton = { TextButton(enabled = encryptedPassword.length >= 8, onClick = {
                if (restore) onRestoreEncryptedBackup(slot, encryptedPassword) else onSaveEncryptedBackup(slot, encryptedPassword)
                encryptedDialog = null; encryptedPassword = ""
            }) { Text(uiText(if (restore) "Decrypt and restore" else "Encrypt and save")) } },
            dismissButton = { TextButton(onClick = { encryptedDialog = null; encryptedPassword = "" }) { Text(uiText("Cancel")) } }
        )
    }
    cropSource?.let { source ->
        val previewBitmap = remember(source) { runCatching { context.contentResolver.openInputStream(source)?.use { android.graphics.BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull() }
        AlertDialog(
            onDismissRequest = { cropSource = null }, containerColor = Surface,
            title = { Text(uiText("Adjust profile photo"), color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                        if (previewBitmap != null) Image(bitmap = previewBitmap, contentDescription = "Crop preview", contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape).graphicsLayer(scaleX = cropZoom, scaleY = cropZoom, translationX = cropX * 90f, translationY = cropY * 90f))
                        else Text(uiText("Cannot preview this image"), color = Muted)
                    }
                    Text(uiText("Zoom"), color = White, fontSize = 11.sp)
                    Slider(value = cropZoom, onValueChange = { cropZoom = it }, valueRange = 1f..3f)
                    Text(uiText("Horizontal position"), color = White, fontSize = 11.sp)
                    Slider(value = cropX, onValueChange = { cropX = it }, valueRange = -1f..1f)
                    Text(uiText("Vertical position"), color = White, fontSize = 11.sp)
                    Slider(value = cropY, onValueChange = { cropY = it }, valueRange = -1f..1f)
                }
            },
            confirmButton = { TextButton(onClick = {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(source, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val bitmap = context.contentResolver.openInputStream(source)?.use { android.graphics.BitmapFactory.decodeStream(it) } ?: error("Cannot decode photo")
                    val output = android.graphics.Bitmap.createBitmap(512, 512, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(output)
                    canvas.drawColor(android.graphics.Color.TRANSPARENT)
                    val scale = maxOf(512f / bitmap.width, 512f / bitmap.height) * cropZoom
                    val left = (512f - bitmap.width * scale) / 2f + cropX * 150f
                    val top = (512f - bitmap.height * scale) / 2f + cropY * 150f
                    val matrix = android.graphics.Matrix().apply { setScale(scale, scale); postTranslate(left, top) }
                    canvas.drawBitmap(bitmap, matrix, android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG))
                    val file = java.io.File(context.filesDir, "taskora_profile_avatar.jpg")
                    file.outputStream().use { output.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, it) }
                    avatar = Uri.fromFile(file).toString(); prefs.edit().putString("avatar", avatar).apply(); onProfileChanged()
                    bitmap.recycle(); output.recycle()
                    cropSource = null
                }.onFailure { Toast.makeText(context, (if (TaskoraAppearance.language == "id") "Tidak dapat memotong foto: " else "Could not crop photo: ") + it.localizedMessage, Toast.LENGTH_LONG).show() }
            }) { Text(uiText("Save photo")) } },
            dismissButton = { TextButton(onClick = { cropSource = null }) { Text(uiText("Cancel")) } }
        )
    }
    if (editingAvatar) {
        AlertDialog(onDismissRequest = { editingAvatar = false }, containerColor = Surface,
            title = { Text(uiText("Choose an avatar"), color = White) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { editingAvatar = false; avatarPicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.PhotoLibrary, null); Spacer(Modifier.width(8.dp)); Text(uiText("Choose photo from gallery")) }
                listOf("🚀", "🌙", "⚡", "🎯", "🌱", "🦊", "🐼", "🐱", "🧠", "💎", "🔥", "⭐").chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { emoji ->
                        TextButton(onClick = { avatar = emoji; prefs.edit().putString("avatar", emoji).apply(); onProfileChanged(); editingAvatar = false }) { Text(emoji, fontSize = 25.sp) }
                    } }
                }
                TextButton(onClick = { avatar = ""; prefs.edit().remove("avatar").apply(); onProfileChanged(); editingAvatar = false }) { Text(uiText("Use initial instead")) }
            } },
            confirmButton = { TextButton(onClick = { editingAvatar = false }) { Text(uiText("Close")) } })
    }
    infoDialog?.let { message ->
        AlertDialog(onDismissRequest = { infoDialog = null }, containerColor = Surface,
            title = { Text(message.substringBefore("\n"), color = White) },
            text = { Text(message.substringAfter("\n\n", ""), color = Muted) },
            confirmButton = { TextButton(onClick = { infoDialog = null }) { Text(uiText("Close")) } })
    }
    if (confirmImport) {
        AlertDialog(onDismissRequest = { confirmImport = false }, containerColor = Surface,
            title = { Text(uiText("Replace current tasks?"), color = White) },
            text = { Text(uiText("Importing a backup replaces every task currently stored on this device. Export your current data first if you may need it."), color = Muted) },
            confirmButton = { TextButton(onClick = { confirmImport = false; onImportBackup() }) { Text(uiText("Continue"), color = Red) } },
            dismissButton = { TextButton(onClick = { confirmImport = false }) { Text(uiText("Cancel")) } })
    }
    if (editingName) {
        var draft by remember { mutableStateOf(displayName) }
        AlertDialog(onDismissRequest = { editingName = false }, containerColor = Surface,
            title = { Text(uiText("Display name"), color = White) },
            text = { OutlinedTextField(value = draft, onValueChange = { draft = it.take(32) }, singleLine = true, label = { Text(uiText("Your name")) }) },
            confirmButton = { TextButton(onClick = { if (draft.isNotBlank()) { displayName = draft.trim(); prefs.edit().putString("display_name", displayName).apply(); onProfileChanged() }; editingName = false }) { Text(uiText("Save")) } },
            dismissButton = { TextButton(onClick = { editingName = false }) { Text(uiText("Cancel")) } })
    }
}

@Composable
private fun FocusScreen(
    tasks: List<TaskEntity>,
    sessions: List<FocusSession>,
    onStart: (Long?, String) -> Unit,
    onPause: () -> Unit,
    onResume: (Long?, String) -> Unit,
    onStop: () -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(25) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var timerBaseSeconds by remember { mutableIntStateOf(25 * 60) }
    var timerStartedAt by remember { mutableLongStateOf(0L) }
    var selectedTaskId by remember { mutableStateOf<Long?>(null) }
    var taskMenuExpanded by remember { mutableStateOf(false) }
    val selectedTask = tasks.firstOrNull { it.id == selectedTaskId && !it.done && !it.isPastDue() }
    val active = sessions.firstOrNull { it.status == "RUNNING" }
    val lastPaused = sessions.firstOrNull()?.takeIf { it.status == "PAUSED" }
    val completed = sessions.filter { it.status == "COMPLETED" }
    LaunchedEffect(active?.id) {
        while (active != null) {
            kotlinx.coroutines.delay(1000)
            val base = if (timerStartedAt > 0L) timerBaseSeconds else selectedMinutes * 60
            val started = if (timerStartedAt > 0L) timerStartedAt else active.startedAt
            remainingSeconds = (base - ((System.currentTimeMillis() - started) / 1000).toInt()).coerceAtLeast(0)
            if (remainingSeconds == 0) { onStop(); break }
        }
    }
    ScreenColumn {
        Spacer(Modifier.height(18.dp))
        Text(uiText("Focus Mode"), color = White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(uiText("Work in focused sessions, then take a break."), color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(20.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Surface).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val isPaused = active == null && sessions.firstOrNull()?.status == "PAUSED"
            Text(if (active != null) uiText("FOCUS IN PROGRESS") else if (isPaused) uiText("PAUSED") else uiText("READY TO FOCUS"), color = Lavender, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            val shown = if (active != null || isPaused) remainingSeconds else selectedMinutes * 60
            Text(String.format(Locale.getDefault(), "%02d:%02d", shown / 60, shown % 60), color = White, fontSize = 52.sp, fontWeight = FontWeight.ExtraBold)
            Text(uiText("Pomodoro session"), color = Muted, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            if (active == null) {
                Box {
                    OutlinedButton(onClick = { taskMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedTask?.title ?: uiText("General focus session"), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ExpandMore, null)
                    }
                    DropdownMenu(expanded = taskMenuExpanded, onDismissRequest = { taskMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text(uiText("General focus session")) }, onClick = { selectedTaskId = null; taskMenuExpanded = false })
                        tasks.filter { !it.done && !it.isPastDue() }.forEach { task ->
                            DropdownMenuItem(text = { Text(task.title, maxLines = 1) }, onClick = { selectedTaskId = task.id; taskMenuExpanded = false })
                        }
                    }
                }
            } else Text(active.taskTitle, color = Lavender, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 60).forEach { minutes ->
                    FilterChip(selected = selectedMinutes == minutes, onClick = { if (active == null) { selectedMinutes = minutes; remainingSeconds = minutes * 60 } }, enabled = active == null, label = { Text("${minutes}m") })
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (active == null) {
                    Button(onClick = { remainingSeconds = selectedMinutes * 60; timerBaseSeconds = remainingSeconds; timerStartedAt = System.currentTimeMillis(); onStart(selectedTask?.id, "Pomodoro • ${selectedMinutes} min") }) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(uiText("Start focus")) }
                    if (lastPaused != null) OutlinedButton(onClick = { timerBaseSeconds = remainingSeconds.coerceAtLeast(1); timerStartedAt = System.currentTimeMillis(); onResume(lastPaused.taskId, lastPaused.taskTitle) }) { Text(uiText("Resume")) }
                } else {
                    OutlinedButton(onClick = { timerBaseSeconds = remainingSeconds; onPause() }) { Icon(Icons.Default.Pause, null); Spacer(Modifier.width(5.dp)); Text(uiText("Pause")) }
                    Button(onClick = onStop) { Icon(Icons.Default.Stop, null); Spacer(Modifier.width(5.dp)); Text(uiText("Finish")) }
                }
            }
            if (active != null) Text(uiText("Session tracking remains active while you use other screens."), color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 12.dp))
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("SESSIONS", completed.size.toString(), "Completed focus sessions", Icons.Default.Timer, Cyan, Modifier.weight(1f))
            StatCard("FOCUS TIME", "${completed.sumOf { it.durationSeconds } / 3600}h ${(completed.sumOf { it.durationSeconds } % 3600) / 60}m", "Recorded locally", Icons.Default.Schedule, Green, Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))
        SectionTitle(uiText("Focus history"), null)
        if (sessions.isEmpty()) Text(uiText("Your focus sessions will appear here."), color = Muted, fontSize = 12.sp)
        sessions.take(30).forEach { session ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(12.dp)).background(Surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, null, tint = Cyan); Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) { Text(session.taskTitle, color = White, fontSize = 12.sp); Text(formatTaskDate(session.startedAt, LocalContext.current), color = Muted, fontSize = 10.sp) }
                Text(if (session.status == "RUNNING") uiText("Running") else "${session.durationSeconds / 60} ${uiText("min")}", color = Lavender, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

private fun applyBackupPreferences(prefs: android.content.SharedPreferences, settings: Map<String, String>) {
    val editor = prefs.edit()
    settings.forEach { (key, value) ->
        when (key) {
            "dashboard_challenges", "dashboard_upcoming", "dashboard_focus_tip", "quiet_hours_enabled" -> editor.putBoolean(key, value.toBoolean())
            "reminder_lead_minutes", "quiet_start_minute", "quiet_end_minute" -> value.toIntOrNull()?.let { editor.putInt(key, it) }
            "display_name", "avatar", "appearance", "date_format", "time_format", "first_day", "language", "accent_hex" -> editor.putString(key, value)
        }
    }
    editor.apply()
}

@Composable
private fun ProfileMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(uiText(label), color = Muted, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun Achievement(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Surface)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 23.sp)
        Spacer(Modifier.height(6.dp))
        Text(uiText(label), color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp)).background(Surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Lavender)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(uiText(title), color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(uiText(subtitle), color = Muted, fontSize = 10.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Muted)
    }
}

internal fun TaskEntity.effectiveDeadlineMillis(): Long? = deadlineAt ?: deadline?.let {
    runCatching { java.time.LocalDate.parse(it).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
}

private fun Long.toLocalDate(): java.time.LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun formatTaskDate(value: Long, context: android.content.Context): String {
    val pattern = context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE).getString("date_format", "d MMM yyyy") ?: "d MMM yyyy"
    return java.text.SimpleDateFormat(pattern, uiLocale()).format(Date(value))
}

private fun formatTaskDateTime(value: Long, context: android.content.Context): String {
    val prefs = context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE)
    val date = prefs.getString("date_format", "d MMM yyyy") ?: "d MMM yyyy"
    val time = if ((prefs.getString("time_format", "24h") ?: "24h") == "12h") "hh:mm a" else "HH:mm"
    return java.text.SimpleDateFormat("$date, $time", uiLocale()).format(Date(value))
}

// A task becomes overdue after its deadline calendar date has ended, not at the deadline hour.
private fun TaskEntity.isPastDue(): Boolean = effectiveDeadlineMillis()?.let { it.toLocalDate() < java.time.LocalDate.now(ZoneId.systemDefault()) } ?: false
private fun TaskEntity.isOverdueToday(): Boolean = effectiveDeadlineMillis()?.let {
    it.toLocalDate() == java.time.LocalDate.now(ZoneId.systemDefault()) && System.currentTimeMillis() > it
} ?: false

private fun calculateLongestStreakFromHistory(history: List<XpHistory>): Int {
    val days = history.filter { it.eventType == "COMPLETION" || it.eventType == "LATE_COMPLETION" }.map { it.earnedAt.toLocalDate() }.distinct().sorted()
    var best = 0; var run = 0; var previous: java.time.LocalDate? = null
    days.forEach { day -> run = if (previous?.plusDays(1) == day) run + 1 else 1; best = maxOf(best, run); previous = day }
    return best
}

private fun calculateStreakFromHistory(history: List<XpHistory>): Int {
    val days = history.filter { it.eventType == "COMPLETION" || it.eventType == "LATE_COMPLETION" }.map { it.earnedAt.toLocalDate() }.toSet()
    var date = java.time.LocalDate.now(ZoneId.systemDefault())
    if (date !in days) date = date.minusDays(1)
    var count = 0
    while (date in days) { count++; date = date.minusDays(1) }
    return count
}

private fun calculateLongestStreak(tasks: List<TaskEntity>): Int {
    val days = tasks.filter { it.done }.mapNotNull { it.completedAt }.map { it.toLocalDate() }.distinct().sorted()
    var best = 0; var run = 0; var previous: java.time.LocalDate? = null
    days.forEach { day -> run = if (previous?.plusDays(1) == day) run + 1 else 1; best = maxOf(best, run); previous = day }
    return best
}

private fun calculateStreak(tasks: List<TaskEntity>): Int {
    val completedDays = tasks.filter { it.done }.mapNotNull { it.completedAt }
        .map { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
    var date = java.time.LocalDate.now()
    if (date !in completedDays) date = date.minusDays(1)
    var count = 0
    while (date in completedDays) { count++; date = date.minusDays(1) }
    return count
}

@Composable
private fun TaskEditorDialog(
    task: TaskEntity?, allTasks: List<TaskEntity>, onDismiss: () -> Unit,
    onSave: (String, String, String, Long, Long, String, String) -> Unit
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    val now = System.currentTimeMillis()
    var title by remember(task?.id) { mutableStateOf(task?.title ?: "") }
    var description by remember(task?.id) { mutableStateOf(task?.description ?: "") }
    var category by remember(task?.id) { mutableStateOf(task?.category ?: "Personal") }
    var priority by remember(task?.id) { mutableStateOf(task?.priority ?: "Medium") }
    var difficulty by remember(task?.id) { mutableStateOf(task?.difficulty ?: "Medium") }
    var startAt by remember(task?.id) { mutableLongStateOf(task?.startAt?.takeIf { it > 0 } ?: now) }
    val minimumDeadline = (task?.createdAt ?: now) + 60L * 60 * 1000
    var deadlineAt by remember(task?.id) { mutableLongStateOf(task?.deadlineAt ?: task?.deadline?.let { runCatching { java.time.LocalDate.parse(it).atTime(23, 59).atZone(zone).toInstant().toEpochMilli() }.getOrNull() } ?: maxOf(now + 60L * 60 * 1000, minimumDeadline)) }
    var dateError by remember(task?.id) { mutableStateOf<String?>(null) }

    fun formatted(value: Long): String = formatTaskDateTime(value, context)

    fun suggestNextFreeSlot() {
        val nowMillis = System.currentTimeMillis()
        val baseDate = Instant.ofEpochMilli(maxOf(startAt, nowMillis)).atZone(zone).toLocalDate()
        val busy = allTasks.filter { !it.done && it.id != task?.id }
        for (dayOffset in 0..13) {
            val date = baseDate.plusDays(dayOffset.toLong())
            val firstMinute = if (date == java.time.LocalDate.now(zone)) maxOf(8 * 60, Instant.ofEpochMilli(nowMillis).atZone(zone).hour * 60 + Instant.ofEpochMilli(nowMillis).atZone(zone).minute) else 8 * 60
            for (minute in firstMinute..(19 * 60) step 15) {
                val candidateStart = date.atTime(minute / 60, minute % 60).atZone(zone).toInstant().toEpochMilli()
                val candidateEnd = candidateStart + 60L * 60_000L
                val hasConflict = busy.any { existing ->
                    val existingEnd = existing.effectiveDeadlineMillis() ?: (existing.startAt + 60L * 60_000L)
                    existing.startAt < candidateEnd && existingEnd > candidateStart
                }
                if (!hasConflict && candidateStart >= nowMillis && (task != null || candidateEnd >= minimumDeadline)) {
                    startAt = candidateStart
                    deadlineAt = candidateEnd
                    dateError = null
                    return
                }
            }
        }
        dateError = if (TaskoraAppearance.language == "id") "Tidak ada slot kosong selama 14 hari ke depan." else "No free 60-minute slot was found in the next 14 days."
    }

    fun chooseDateTime(initial: Long, minTime: Long, onPicked: (Long) -> Unit) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = initial.coerceAtLeast(minTime) }
        DatePickerDialog(context, { _, year, month, day ->
            val pickedDate = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, year); set(java.util.Calendar.MONTH, month); set(java.util.Calendar.DAY_OF_MONTH, day)
                set(java.util.Calendar.HOUR_OF_DAY, cal.get(java.util.Calendar.HOUR_OF_DAY)); set(java.util.Calendar.MINUTE, cal.get(java.util.Calendar.MINUTE)); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
            }
            TimePickerDialog(context, { _, hour, minute ->
                pickedDate.set(java.util.Calendar.HOUR_OF_DAY, hour); pickedDate.set(java.util.Calendar.MINUTE, minute)
                val result = pickedDate.timeInMillis
                if (result >= minTime) { onPicked(result); dateError = null } else { dateError = "Choose a time that is not in the past and after the start time." }
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE),
                (context.getSharedPreferences("taskora_preferences", android.content.Context.MODE_PRIVATE).getString("time_format", "24h") ?: "24h") == "24h").show()
        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = minTime
        }.show()
    }

    val startValid = (task != null && startAt == task.startAt) || startAt >= now
    val valid = title.isNotBlank() && startValid && deadlineAt > startAt && (task != null || deadlineAt >= minimumDeadline)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = { Text(uiText(if (task == null) "Create a task" else "Edit task"), color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 540.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(uiText("Plan it clearly, then make it happen."), color = Muted, fontSize = 12.sp)
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiText("Task name")) }, placeholder = { Text(uiText("e.g. Finish project proposal")) }, singleLine = true,
                    shape = RoundedCornerShape(14.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text(uiText("Description")) }, placeholder = { Text(uiText("Add notes, details, or expected result…")) },
                    minLines = 3, maxLines = 5, shape = RoundedCornerShape(14.dp))
                Text(uiText("CATEGORY"), color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Personal", "Work", "Learn").forEach { value ->
                        FilterChip(selected = category == value, onClick = { category = value }, label = { Text(uiText(value)) })
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(uiText("SCHEDULE"), color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { suggestNextFreeSlot() }) { Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text(uiText("Suggest free time"), fontSize = 10.sp) }
                }
                OutlinedCard(onClick = { chooseDateTime(startAt, System.currentTimeMillis()) { picked ->
                    startAt = picked
                    if (deadlineAt <= startAt) deadlineAt = startAt + 60L * 60 * 1000
                    dateError = null
                } }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = Surface2)) {
                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayCircle, null, tint = Cyan)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(uiText("Start date & time"), color = Muted, fontSize = 11.sp); Text(formatted(startAt), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        Icon(Icons.Default.EditCalendar, null, tint = Lavender)
                    }
                }
                OutlinedCard(onClick = { chooseDateTime(deadlineAt, maxOf(startAt + 60_000L, if (task == null) minimumDeadline else startAt + 60_000L)) { picked -> deadlineAt = picked; dateError = null } },
                    modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = Surface2)) {
                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, null, tint = Yellow)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) { Text(uiText("Deadline"), color = Muted, fontSize = 11.sp); Text(formatted(deadlineAt), color = White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        Icon(Icons.Default.EditCalendar, null, tint = Lavender)
                    }
                }
                if (dateError != null || !startValid || deadlineAt <= startAt || (task == null && deadlineAt < minimumDeadline)) {
                    Text(uiText(dateError ?: when { !startValid -> "Start time cannot be in the past."; task == null && deadlineAt < minimumDeadline -> "Deadline must be at least one hour after task creation."; else -> "Deadline must be later than start time." }), color = Red, fontSize = 11.sp)
                }
                Text(uiText("DIFFICULTY"), color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(listOf("Easy", "Medium"), listOf("Hard", "Expert")).forEach { group ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            group.forEach { value ->
                                val reward = when (value) { "Easy" -> 10; "Medium" -> 20; "Hard" -> 35; else -> 55 }
                                Surface(modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).clickable { difficulty = value },
                                    color = if (difficulty == value) Purple.copy(alpha = .22f) else Surface,
                                    border = androidx.compose.foundation.BorderStroke(if (difficulty == value) 1.5.dp else 1.dp, if (difficulty == value) Purple else Surface2), shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(horizontal = 12.dp, vertical = 11.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(uiText(value), color = White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                            if (difficulty == value) Icon(Icons.Default.CheckCircle, null, tint = Lavender, modifier = Modifier.size(16.dp))
                                        }
                                        Text("+$reward XP", color = if (difficulty == value) Lavender else Muted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Text(uiText("PRIORITY"), color = Muted, fontSize = 10.sp, letterSpacing = 1.2.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Low", "Medium", "High").forEach { value ->
                        FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(uiText(value)) })
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(title.trim(), description.trim(), category, startAt, deadlineAt, priority, difficulty) }, enabled = valid) { Text(uiText("Save task")) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(uiText("Cancel")) } }
    )
}
