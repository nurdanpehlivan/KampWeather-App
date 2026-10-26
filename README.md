# Hava — Şehir Arama ve Hava Tahmini

**Geliştiren:** Nurdan Pehlivan · Bilişim Kampı (Kamp+) Android eğitimi

Kampta herkese verilen başlangıç projesi üzerine kendi tasarımımla geliştirdiğim sürüm.
Open-Meteo servisinden şehir arar; seçilen şehrin anlık durumunu, saatlik ve 7 günlük
tahminini gösterir. Yükleniyor / veri / boş / hata durumlarının hepsi ele alınır.

## Bu sürümde neler var

| Alan | Ne yaptım |
|---|---|
| Tasarım | Havaya göre tema: ekranın zemini o anki gökyüzü (açıkta mavi, gece lacivert, yağmurda gri-mavi, fırtınada mor). Kartlar yarı saydam cam, sıcaklıklar ince ve büyük, yeni uygulama simgesi |
| Ana ekran: Şehirlerim | Konum şehri ve favoriler, her biri kendi gökyüzü renginde kart: anlık sıcaklık, durum, en yüksek/en düşük, maskot. Sola kaydırınca kaldır, aşağı çekince yenile |
| Arama | Bir şey yazınca açılır: kaydırınca küçülen başlık, cam arama kutusu, sonuç sayısı, durumlar arasında yumuşak geçiş |
| Şehir kartı | Türkiye'deki şehirler için renkli **plaka rozeti** (06, 34…), diğer ülkeler için bayrak, koordinat |
| Arama | Yazdıkça arama (450 ms bekleme ile tek istek), klavyede "Ara" tuşu, temizle (×) düğmesi |
| Seçili şehir | Dokunulan şehir vurgulanır; Logcat'e `Tıklandı: Ankara` yazılır (filtre: `CityCard`) |
| Tahmin ekranı | Havaya ve saate göre renklenen gökyüzü bloğu, ölçüm kutuları, sıcaklık çubuklu saatlik sütunlar, haftalık sıcaklık aralığı çubukları |
| Yükleniyor | Dalga gibi "nefes alan" iskelet kartlar |
| Ağ | OkHttp günlükçüsü: debug derlemesinde istekler Logcat'te (`okhttp.OkHttpClient`) |
| Maskot | Havaya göre kılık değiştiren bulut: güneşte gözlük, yağmurda şemsiye, karda atkı, fırtınada endişeli; göz kırpar ve süzülür. Tamamen Canvas ile çizildi |
| Favoriler | Arama sonucunda ya da tahmin ekranının başlığındaki kalple eklenir; DataStore'da saklanır, ana ekranda hava kartı olur |
| Konumum | İzin isteyip bulunduğun şehri bulur, ana ekranın ilk kartı yapar (Play Services gerektirmez) |
| Yağmur uyarısı | Önümüzdeki 3 saatte yağış olasılığı %50'yi geçerse "X saat içinde yağmur bekleniyor" bandı |
| Gün doğumu yayı | Güneşin doğuştan batışa ilerleyişi, gün ışığı süresi |
| Widget | Telefonun ana ekranında konum şehrinin (yoksa ilk favorinin) anlık havası, o havanın renginde; 30 dakikada bir tazelenir |
| Testler | Yazdıkça arama, favoriler, konum, plaka, yağmur uyarısı, gün yolu ve maskot için birim testleri (toplam 56) |

## Kamp kitapçığı kontrol listesi

- **Uygulama 2:** Uygulama adı, `CityCard` + `@Preview`, `LazyColumn`, Logcat'e tıklama kaydı. Bonuslar: karanlık tema, plaka rozetine renk, TopAppBar ile başlık, kendi simgem.
- **Uygulama 3:** `OutlinedTextField` ile arama, yazdıkça süzme, ekran döndürülünce kaybolmayan state (ViewModel), seçili şehir vurgusu, "Şehir bulunamadı". Bonuslar: state `CityViewModel`'de, temizle (×) düğmesi, sonuç sayısı.
- **Uygulama 4:** INTERNET izni, Retrofit + Gson, `City` / `CitySearchResponse`, `CityApi` + `ApiClient`, `CityViewModel` ile Loading → Success / Empty / Error, "Tekrar dene". Bonuslar: OkHttp log, `ImeAction.Search`, `CityRepository`.



Mimari ayrıntıları için [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

**Teknoloji:** Kotlin · Jetpack Compose (Material 3) · ViewModel + StateFlow · Coroutines · Retrofit + Gson · Navigation Compose

## Mimari — üç katman

*UI görünümü çizer, ViewModel state'i yönetir, Repository veriye erişir.*

```
UI (Composable)  →  ViewModel (StateFlow)  →  Repository (suspend)  →  Retrofit → Open-Meteo
```

```
com.nurdan.hava
├── MainActivity.kt            Uygulamanın kapısı
├── model/                     Veri neye benzer?
│   ├── City.kt                City + CitySearchResponse
│   ├── Forecast.kt            Forecast + Current/Hourly/Daily
│   └── WeatherCondition.kt    WMO kodu → emoji + Türkçe etiket
├── data/                      Veri nereden gelir?
│   ├── ApiClient.kt           object + Retrofit kurulumu
│   ├── CityApi.kt             @GET v1/search
│   ├── ForecastApi.kt         @GET v1/forecast
│   ├── ForecastResponse.kt    DTO + sütun→satır çevrimi
│   ├── CityRepository.kt      suspend search()
│   └── WeatherRepository.kt   suspend getForecast()
└── ui/                        Ne çizilir, ne zaman değişir?
    ├── LoadState.kt           Loading / Success / Empty / Error
    ├── HavaNavHost.kt         iki ekran arası geçiş
    ├── city/                  CityScreen · CityViewModel · CityCard
    ├── forecast/              ForecastScreen · ForecastViewModel
    ├── component/             SkeletonList · EmptyView · ErrorView
    └── theme/                 Color · Theme · Type
```

## API — Open-Meteo (anahtar gerekmez)

| Amaç | Çağrı |
|---|---|
| Şehir arama | `https://geocoding-api.open-meteo.com/v1/search?name=Ankara&count=10&language=tr` |
| Tahmin | `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=..&hourly=..&daily=..&forecast_days=7&timezone=auto` |

- Sonuç yoksa arama yanıtında `results` alanı **hiç gelmez** → Boş (Empty) durumu.
- Tahmin yanıtı **sütun** biçimindedir: `hourly.time[i]` ile `hourly.temperature_2m[i]` aynı saate aittir.
- `weather_code` WMO standardındadır (0 açık, 61 yağmur, 95 fırtına…); `WeatherCondition.fromWeatherCode` ile görünüme çevrilir.
- Ücretsiz kullanım ticari olmayan projeler içindir (~10.000 istek/gün).

## Çalıştırma

```bash
./gradlew assembleDebug        # debug APK
./gradlew testDebugUnitTest    # unit testler
./gradlew spotlessApply        # ktlint formatı (denetim için spotlessCheck)
./gradlew assembleRelease      # release APK (R8 açık)
```

Android SDK yolunu `local.properties` içinde `sdk.dir=...` ile veya `ANDROID_HOME` ortam değişkeniyle verin.

## Checkpoint'ler

Her ders bloğunun sonunda projenin olması gereken hâli ayrı bir branch'tedir. `cpN` = **Uygulama N+1**.

| Branch | Karşılığı | İçerik |
|---|---|---|
| `cp1-baslangic` | — | Boş Empty Activity projesi, "Merhaba Kamp!" |
| `cp1-bitis` / `cp2-baslangic` | Uygulama 2 | Sabit şehir listesi, `CityCard` + plaka rozeti, `LazyColumn`, tıklamada `Log.d` |
| `cp2-bitis` / `cp3-baslangic` | Uygulama 3 | Arama kutusu, `rememberSaveable`, filtre, boş durum, `CityViewModel`, `model/data/ui` paketleri |
| `cp3-bitis` / `cp4-baslangic` | Uygulama 4 | Retrofit + Gson, `ApiClient`, `CityRepository`, dört durum, Shimmer, `try/catch` |
| `cp4-bitis` (= `main`) | Uygulama 5 | Tahmin ekranı + navigasyon, cila, testler |

Bir bloğu yetiştiremediyseniz kodunuzu `katilimci/<ad>/cpN` branch'ine kaydedip sonraki bloğun başlangıç branch'ine geçin.

`archive/v1-clean-arch` branch'i, projenin önceki Clean Architecture + Hilt + Room sürümünü saklar.

## Testler

| Test | Neyi korur |
|---|---|
| `WeatherConditionTest` | WMO kod → görünüm eşlemesi |
| `CityApiTest` | Gson `@SerializedName` eşlemesi (sessizce bozulur) |
| `ForecastApiTest` | Sütun → satır çevrimi (index kayması hata vermez) |
| `CityViewModelTest` | Dört durum + "Tekrar dene" |
| `ForecastViewModelTest` | Rota parametreleri + durumlar |

## Release imzası

`keystore.properties.example` dosyasını `keystore.properties` olarak kopyalayıp doldurun (repoya girmez). Dosya yoksa release APK debug anahtarıyla imzalanır.
