package com.aka.signumfieldassistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private data class Profile(val name:String,val mode:String,val st:Int,val rt:Int,val gain:Int,val filterT:Int,val filterA:Int,val gs:String,val reason:String)
private val profiles = listOf(
    Profile("P1 • Economy / Genel","Economy",10,7,7,15,2,"20","Genel arama için dengeli başlangıç."),
    Profile("P2 • Economy / Mineral","Economy",7,5,4,15,2,"Off","Zor zemin ve yoğun demir için daha sakin."),
    Profile("P3 • Normal / Genel","Normal",11,8,7,15,2,"Off","Standart saha taraması için güçlü başlangıç."),
    Profile("P4 • Normal / Derin","Normal",12,9,7,15,2,"Off","Daha derin hedefler için güçlü başlangıç.")
)

class MainActivity : ComponentActivity() {
    private var recorder: MediaRecorder? = null
    private var recording = false
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) (r.data?.extras?.get("data") as? Bitmap)?.let { cameraBitmap = it }
    }
    private var cameraBitmap: Bitmap? by mutableStateOf(null)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsIfNeeded()
        setContent { App() }
    }
    private fun requestPermissionsIfNeeded() {
        val p = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) p += Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) p += Manifest.permission.RECORD_AUDIO
        if (p.isNotEmpty()) permissionLauncher.launch(p.toTypedArray())
    }
    fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            cameraLauncher.launch(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
    }
    fun toggleRecording() {
        if (recording) {
            runCatching { recorder?.stop() }
            recorder?.release(); recorder = null; recording = false
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val file = java.io.File(cacheDir, "signum_${System.currentTimeMillis()}.3gp")
            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); setOutputFile(file.absolutePath); prepare(); start()
            }
            recording = true
        }
    }
}

@Composable private fun App() {
    val activity = androidx.compose.ui.platform.LocalContext.current as MainActivity
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf(profiles[0]) }
    var terrain by remember { mutableStateOf("Genel / bilinmiyor") }
    var gb by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var savedNote by remember { mutableStateOf("") }
    var recording by remember { mutableStateOf(false) }
    val bitmap = activity.cameraBitmap
    val recommendations = when (terrain) {
        "Mineralli / kırmızı toprak" -> profiles[1]
        "Yoğun demir / çöplü alan" -> profiles[1]
        "Düz / temiz zemin" -> profiles[2]
        "Derin hedef / sakin zemin" -> profiles[3]
        else -> profiles[0]
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF00BFA5))) {
        Scaffold(bottomBar = { NavigationBar { listOf("Saha","Ayar","Ses","Notlar").forEachIndexed { i,n -> NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(if(i==tab) "●" else "○")},label={Text(n)}) } } }) { pad ->
            LazyColumn(modifier=Modifier.padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
                item { Text("AKA SIGNUM FIELD AI", style=MaterialTheme.typography.headlineSmall); Text("MFD 7272M • Firmware 2.06", color=MaterialTheme.colorScheme.primary) }
                when(tab) {
                    0 -> {
                        item { Section("1 • Zemin / ekran fotoğrafı") {
                            Text("Fotoğraf yardımcı değerlendirmedir; tek başına metal teşhisi değildir.")
                            Button(onClick={activity.openCamera()}) { Text("KAMERA AÇ") }
                            bitmap?.let { Image(it.asImageBitmap(), "Arazi veya Signum ekran fotoğrafı", Modifier.fillMaxWidth().heightIn(max=260.dp)) }
                        } }
                        item { Section("2 • Zemin tipi") {
                            Text("Fotoğrafa bakarak uygulamadaki en yakın zemin kategorisini seç:")
                            listOf("Genel / bilinmiyor","Mineralli / kırmızı toprak","Yoğun demir / çöplü alan","Düz / temiz zemin","Derin hedef / sakin zemin").forEach { t ->
                                FilterChip(selected=terrain==t,onClick={terrain=t},label={Text(t)},modifier=Modifier.padding(end=6.dp,bottom=4.dp))
                            }
                        } }
                        item { Section("3 • Ground Balance") {
                            Text("Dedektörün GB değerini ekrandan okuyup gir. Otomatik GB sonrası gerekirse küçük düzeltme yap.")
                            OutlinedTextField(value=gb,onValueChange={gb=it.filter { c -> c.isDigit() || c=='-' || c=='.' }},label={Text("GB değeri")},singleLine=true)
                            Text("Uygulama notu: temiz zeminde coil'i yukarı-aşağı pompalayarak veya temiz noktada kısa yatay süpürerek GB kontrolü yapılır.",style=MaterialTheme.typography.bodySmall)
                        } }
                        item { Section("4 • Önerilen başlangıç") { ProfileCard(recommendations, recommendations==selected) { selected=recommendations } } }
                        item { Section("Aktif profil") { Text(selected.name,style=MaterialTheme.typography.titleMedium); Text("Mode ${selected.mode} • ST ${selected.st} • RT ${selected.rt} • Gain ${selected.gain}"); Text("Filter T ${selected.filterT} • Filter A ${selected.filterA} • GS ${selected.gs}") } }
                        item { Section("Saha güvenliği") { Text("VDI, hodograph ve ses yalnızca yardımcı kanıttır. Hedefi iki yönde tekrar tara, GB ve zemin davranışını kontrol et; kesin metal kimliği ilan etme.") } }
                    }
                    1 -> { item { Text("PROGRAMLAR", style=MaterialTheme.typography.titleLarge) }; items(profiles) { p -> ProfileCard(p,p==selected) { selected=p } }; item { Section("Terimler") { Text("ST = Static Threshold • RT = Recovery Threshold • Gain = hassasiyet • Filter T/A = sinyal filtreleri • GS = Ground Signal filter") } } }
                    2 -> { item { Section("SOUND ANALYSIS") {
                        Text("RT / RT-M / RT-M99 / RTL-ST / RT-ST / ST-P. Ses kaydı yardımcı kanıttır; metal türü kesin ilan edilmez.")
                        Button(onClick={ activity.toggleRecording(); recording=!recording }) { Text(if(recording) "SES KAYDINI DURDUR" else "SES KAYDINI BAŞLAT") }
                        Text(if(recording) "● Kayıt devam ediyor" else "Kayıt kapalı")
                    } }; item { Section("Analiz") { Text("Kaydı değerlendirirken perde, süre, tekrar edilebilirlik ve sweep yönünü karşılaştır. Aynı hedefi iki yönde tekrar kontrol et.") } } }
                    3 -> { item { Section("SAHA NOTU") {
                        OutlinedTextField(value=note,onValueChange={note=it},label={Text("Not")},modifier=Modifier.fillMaxWidth(),minLines=4)
                        Button(onClick={savedNote=note}) { Text("KAYDET") }
                        if(savedNote.isNotBlank()) Text("Kaydedilen not: $savedNote")
                    } }; item { Section("Hızlı kontrol") { Text("☐ Zemin temizliği\n☐ Ground Balance\n☐ EMI kontrolü\n☐ Sweep hızı\n☐ İki yönlü tekrar\n☐ Hedef merkezleme") } } }
                }
            }
        }
    }
}

@Composable private fun Section(title:String, content:@Composable ColumnScope.()->Unit) { Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content={Text(title,style=MaterialTheme.typography.titleMedium);content()}) } }
@Composable private fun ProfileCard(p:Profile,selected:Boolean,onSelect:()->Unit) { Card { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) { Text(p.name,style=MaterialTheme.typography.titleMedium); Text("Mode: ${p.mode} • ST ${p.st} • RT ${p.rt} • Gain ${p.gain}"); Text("Filter T ${p.filterT} • Filter A ${p.filterA} • GS ${p.gs}"); Text(p.reason); Button(onClick=onSelect) { Text(if(selected) "SEÇİLDİ ✓" else "BU PROFİLİ SEÇ") } } } }
