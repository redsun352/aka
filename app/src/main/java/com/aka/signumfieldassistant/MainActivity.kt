package com.aka.signumfieldassistant

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private data class Profile(val name:String,val mode:String,val st:Int,val rt:Int,val gain:Int,val filterT:Int,val filterA:Int,val gs:String,val reason:String)

private val profiles = listOf(
    Profile("P1 • Economy / Genel","Economy",10,7,7,15,2,"20","Genel arama için dengeli başlangıç."),
    Profile("P2 • Economy / Mineral","Economy",7,5,4,15,2,"Off","Zor zemin ve yoğun demir için daha sakin."),
    Profile("P3 • Normal / Genel","Normal",11,8,7,15,2,"Off","Standart saha taraması için güçlü başlangıç."),
    Profile("P4 • Normal / Derin","Normal",12,9,7,15,2,"Off","Daha derin hedefler için daha güçlü profil.")
)

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); requestPermissionsIfNeeded(); setContent { App() } }
    private fun requestPermissionsIfNeeded() {
        val p = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) p += Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) p += Manifest.permission.RECORD_AUDIO
        if (p.isNotEmpty()) permissionLauncher.launch(p.toTypedArray())
    }
}

@Composable private fun App() {
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = darkColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF00BFA5))) {
        Scaffold(bottomBar = { NavigationBar { listOf("Saha","Ayar","Ses","Notlar").forEachIndexed { i,n -> NavigationBarItem(selected=tab==i,onClick={tab=i},icon={},label={Text(n)}) } } }) { pad ->
            LazyColumn(modifier=Modifier.padding(pad).padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
                item { Text("AKA SIGNUM FIELD AI", style=MaterialTheme.typography.headlineSmall) ; Text("MFD 7272M • Firmware 2.06", color=MaterialTheme.colorScheme.primary) }
                when(tab) {
                    0 -> { item { Section("1 • Zemin fotoğrafı") { Text("Arazi / toprak / kaya fotoğrafını çek veya seç. Fotoğraf tek başına kesin hedef teşhisi değildir.") ; Button(onClick={}) { Text("KAMERA AÇ") } } }; item { Section("2 • Ground Balance") { Text("Otomatik GB değerini ekrandan oku veya elle gir.") ; OutlinedButton(onClick={}) { Text("GB DEĞERİ GİR") } } }; item { Section("3 • Önerilen başlangıç") { ProfileCard(profiles[0]) } }; item { Section("Güvenlik") { Text("VDI ve ses, hedef kimliğini tek başına kanıtlamaz. Tekrar tarama + yön değişimi + GB kontrolü gerekir.") } } }
                    1 -> { item { Text("PROGRAMLAR", style=MaterialTheme.typography.titleLarge) }; items(profiles) { ProfileCard(it) }; item { Section("Manuel ayarlar") { Text("ST = Static Threshold • RT = Recovery Threshold • Gain = hassasiyet • Filter T/A = sinyal filtreleri • GS = Ground Signal filter") } } }
                    2 -> { item { Section("SOUND ANALYSIS") { Text("RT / RT-M / RT-M99 / RTL-ST / RT-ST / ST-P. Ses kaydı yalnızca yardımcı kanıt olarak değerlendirilir."); Button(onClick={}) { Text("SES KAYDINI BAŞLAT") } } }; item { Section("Analiz") { Text("Perde, süre, tekrar edilebilirlik ve sweep yönü karşılaştırılacak. Metal türü kesin ilan edilmez.") } } }
                    3 -> { item { Section("SAHA NOTU") { OutlinedTextField(value="",onValueChange={},label={Text("Not")},modifier=Modifier.fillMaxWidth(),minLines=4); Button(onClick={}) { Text("KAYDET") } } }; item { Section("Hızlı kontrol") { Text("☐ Zemin temizliği\n☐ Ground Balance\n☐ EMI kontrolü\n☐ Sweep hızı\n☐ İki yönlü tekrar\n☐ Hedef merkezleme") } } }
                }
            }
        }
    }
}

@Composable private fun Section(title:String, content:@Composable ColumnScope.()->Unit) { Card { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content={Text(title,style=MaterialTheme.typography.titleMedium);content()}) } }
@Composable private fun ProfileCard(p:Profile) { Card { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) { Text(p.name,style=MaterialTheme.typography.titleMedium); Text("Mode: ${p.mode} • ST ${p.st} • RT ${p.rt} • Gain ${p.gain}"); Text("Filter T ${p.filterT} • Filter A ${p.filterA} • GS ${p.gs}"); Text(p.reason); Button(onClick={}) { Text("BU PROFİLİ SEÇ") } } } }
