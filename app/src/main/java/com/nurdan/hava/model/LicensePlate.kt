package com.nurdan.hava.model

import java.util.Locale

/**
 * Türkiye il plaka kodları.
 *
 * Geocoding servisi plaka göndermez; ama Türkiye'deki her sonucun `admin1` alanı
 * bağlı olduğu ili verir. İl adını plaka numarasına çeviriyoruz ki kartta "TR"
 * yerine tanıdık "06", "34" rozetleri görünsün.
 */
object LicensePlate {

    /** Plaka sırasına göre 81 il; listedeki konum + 1 = plaka numarası. */
    private val PROVINCES = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın",
        "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı",
        "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir",
        "Gaziantep", "Giresun", "Gümüşhane", "Hakkâri", "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir",
        "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir", "Kocaeli", "Konya", "Kütahya", "Malatya",
        "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya",
        "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak",
        "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman", "Şırnak",
        "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    private val TURKISH = Locale.forLanguageTag("tr")

    /** Eski ya da farklı yazımlar. */
    private val ALIASES = mapOf("içel" to 33, "hakkari" to 30, "afyon" to 3, "maraş" to 46, "urfa" to 63)

    private val byName: Map<String, Int> =
        PROVINCES.mapIndexed { index, name -> normalize(name) to index + 1 }.toMap() + ALIASES

    /**
     * Bir şehrin plaka kodunu döner ("06"). Türkiye dışındaki ya da ili
     * tanınmayan şehirler için `null`.
     */
    fun of(city: City): String? {
        if (!city.countryCode.equals("TR", ignoreCase = true)) return null
        val number = city.admin1?.let { byName[normalize(it)] } ?: byName[normalize(city.name)] ?: return null
        return number.toString().padStart(2, '0')
    }

    /** "İstanbul İli" / "Istanbul Province" → "istanbul". */
    private fun normalize(name: String): String = name
        .lowercase(TURKISH)
        .removeSuffix(" ili")
        .removeSuffix(" province")
        .trim()
}
