package com.example.ricefieldsurvey.domain.model

object SurveyOptions {
    val sexes = listOf("Male", "Female")
    val ecosystems = listOf("Irrigated", "Rainfed Lowland", "Upland")
    val establishmentMethods = listOf("Direct Seeding", "Transplanting")
    const val OTHER = "Others"
    val challenges = listOf(
        "High fertilizer cost", "Pest and disease", "Lack of irrigation",
        "Labor shortage", "Low selling price", OTHER,
    )

    // Stored on the device: province selection does not require an internet connection.
    val provinces = listOf(
        "Abra", "Agusan del Norte", "Agusan del Sur", "Aklan", "Albay", "Antique",
        "Apayao", "Aurora", "Basilan", "Bataan", "Batanes", "Batangas", "Benguet",
        "Biliran", "Bohol", "Bukidnon", "Bulacan", "Cagayan", "Camarines Norte",
        "Camarines Sur", "Camiguin", "Capiz", "Catanduanes", "Cavite", "Cebu",
        "Cotabato", "Davao de Oro", "Davao del Norte", "Davao del Sur",
        "Davao Occidental", "Davao Oriental", "Dinagat Islands", "Eastern Samar",
        "Guimaras", "Ifugao", "Ilocos Norte", "Ilocos Sur", "Iloilo", "Isabela",
        "Kalinga", "La Union", "Laguna", "Lanao del Norte", "Lanao del Sur",
        "Leyte", "Maguindanao del Norte", "Maguindanao del Sur", "Marinduque",
        "Masbate", "Metro Manila (NCR)", "Misamis Occidental", "Misamis Oriental",
        "Mountain Province", "Negros Occidental", "Negros Oriental", "Northern Samar",
        "Nueva Ecija", "Nueva Vizcaya", "Occidental Mindoro", "Oriental Mindoro",
        "Palawan", "Pampanga", "Pangasinan", "Quezon", "Quirino", "Rizal",
        "Romblon", "Samar", "Sarangani", "Siquijor", "Sorsogon", "South Cotabato",
        "Southern Leyte", "Sultan Kudarat", "Sulu", "Surigao del Norte",
        "Surigao del Sur", "Tarlac", "Tawi-Tawi", "Zambales",
        "Zamboanga del Norte", "Zamboanga del Sur", "Zamboanga Sibugay",
    )
}
