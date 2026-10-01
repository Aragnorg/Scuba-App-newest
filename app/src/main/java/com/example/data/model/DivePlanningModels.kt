package com.example.data.model

data class EmergencyInfo(
    val nearestChamberName: String,
    val chamberLocation: String,
    val chamberDistanceKm: Double,
    val chamberPhone: String,
    val coastGuardVHF: String, // e.g. "VHF Ch 16 / Ch 68"
    val localEmergencyNumber: String, // e.g. "123" or "112" or "911"
    val danHotline: String, // e.g. "+1-919-684-9111" or "+39-06-695-4000"
    val localHospitalName: String,
    val localHospitalPhone: String,
    val emergencyNotes: String = "100% O2 kit available on dive boat. Immediate evacuation protocol via Coast Guard Marine Radio.",
    val oxygenOnSite: Boolean = true
)

data class DiveSpotMapAnnotation(
    val title: String,
    val description: String,
    val xPercent: Float, // 0f to 100f
    val yPercent: Float, // 0f to 100f
    val type: String // "ENTRY", "EXIT", "MOORING", "WALL", "WRECK", "CAVE", "SAFETY_STOP", "HAZARD"
)

data class DiveSpot(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val minDepthMeters: Int,
    val maxDepthMeters: Int,
    val difficulty: String, // "BEGINNER", "INTERMEDIATE", "ADVANCED", "TECHNICAL"
    val spotType: String, // "REEF", "WRECK", "WALL", "CAVE", "DRIFT", "PINNACLE"
    val entryType: String, // "BOAT", "SHORE", "ZODIAC"
    val waterType: String = "SALT", // "SALT", "FRESH"
    val avgVisibilityMeters: Int,
    val currentStrength: String, // "NONE", "MILD", "MODERATE", "STRONG"
    val highlights: List<String>,
    val description: String,
    val routeAdvice: String,
    val bestSeason: String,
    val emergencyInfo: EmergencyInfo,
    val mapAnnotations: List<DiveSpotMapAnnotation> = emptyList(),
    val imageUrl: String? = null
)

data class GasPlanResult(
    val o2Percentage: Int,
    val ppo2Limit: Double,
    val maxOperatingDepthMeters: Double,
    val contingencyDepthMeters: Double,
    val startPressureBar: Int,
    val reservePressureBar: Int,
    val turnPressureBar: Int,
    val plannedDepthMeters: Double,
    val plannedBottomTimeMin: Int,
    val estimatedGasConsumedBar: Int,
    val isPlanSafe: Boolean,
    val warningMessage: String? = null
)
