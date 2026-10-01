package com.example.data.repository

import com.example.data.model.DiveSpot
import com.example.data.model.DiveSpotMapAnnotation
import com.example.data.model.EmergencyInfo
import com.example.data.model.GasPlanResult

object DiveSpotsData {

    val allDiveSpots: List<DiveSpot> = listOf(
        DiveSpot(
            id = "spot_shark_yolanda",
            name = "Shark & Yolanda Reef",
            region = "Ras Mohammed National Park, Sharm El Sheikh",
            country = "Egypt",
            latitude = 27.7314,
            longitude = 34.2547,
            minDepthMeters = 5,
            maxDepthMeters = 40,
            difficulty = "INTERMEDIATE",
            spotType = "WALL",
            entryType = "BOAT",
            avgVisibilityMeters = 30,
            currentStrength = "STRONG",
            highlights = listOf("Yolanda Wreck toilets & cargo", "Pelagic sharks", "Lush coral wall", "Giant barracuda schools"),
            description = "One of the top 10 dive sites globally. Two coral pinnacles rising from 800m abyss connected by a saddle. Strong currents bring abundant pelagic life.",
            routeAdvice = "Negative entry from dive boat at Anemone City, drift south along Shark Reef wall, cross saddle to Yolanda Reef and inspect cargo before safety stop in blue.",
            bestSeason = "May to October (Peak fish schooling & warm waters)",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Sharm El Sheikh Hyperbaric Medical Center",
                chamberLocation = "Ras Um Sid, Sharm El Sheikh, South Sinai",
                chamberDistanceKm = 18.5,
                chamberPhone = "+20-69-3660-922",
                coastGuardVHF = "VHF Ch 16 / Ras Mohammed Marine Patrol",
                localEmergencyNumber = "123",
                danHotline = "+39-06-695-4000 (DAN Europe) / +1-919-684-9111",
                localHospitalName = "Sharm International Hospital",
                localHospitalPhone = "+20-69-3660-893",
                emergencyNotes = "Ras Mohammed Coast Guard patrol boat on station. 100% O2 rescue kits standard on all registered safari vessels.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("Boat Drop Point", "Negative entry into blue", 20f, 15f, "ENTRY"),
                DiveSpotMapAnnotation("Shark Reef Wall", "Vertical wall drops to 800m", 35f, 40f, "WALL"),
                DiveSpotMapAnnotation("Connecting Saddle", "Look out for schooling batfish", 50f, 50f, "REEF"),
                DiveSpotMapAnnotation("Yolanda Cargo", "Sanitary ware cargo & BMW car", 70f, 65f, "WRECK"),
                DiveSpotMapAnnotation("Safety Stop Zone", "Deploy SMB in shallow plateau", 85f, 80f, "SAFETY_STOP")
            )
        ),
        DiveSpot(
            id = "spot_thistlegorm",
            name = "SS Thistlegorm Wreck",
            region = "Sha'ab Ali, Gulf of Suez",
            country = "Egypt",
            latitude = 27.8143,
            longitude = 33.9189,
            minDepthMeters = 16,
            maxDepthMeters = 32,
            difficulty = "ADVANCED",
            spotType = "WRECK",
            entryType = "BOAT",
            avgVisibilityMeters = 20,
            currentStrength = "STRONG",
            highlights = listOf("WWII Bedford trucks & Norton motorbikes", "Train locomotives", "Anti-aircraft guns", "Giant groupers"),
            description = "World-famous British merchant navy ship sunk by Heinkel He 111 bombers in 1941. An intact underwater museum laden with wartime vehicles, ammunition, and boots.",
            routeAdvice = "Descent and ascent strictly via shot line due to ripping surface currents. Hold 1: Orient outside hull; Hold 2: Enter cargo holds via bomb blast opening.",
            bestSeason = "Year-round (Early mornings offer calmest currents)",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "El Gouna Hyperbaric Medical Center",
                chamberLocation = "El Gouna Hospital, Red Sea Governorate",
                chamberDistanceKm = 42.0,
                chamberPhone = "+20-65-3580-011",
                coastGuardVHF = "VHF Ch 16 / Suez Marine Rescue",
                localEmergencyNumber = "123",
                danHotline = "+39-06-695-4000 (DAN Europe)",
                localHospitalName = "Hurghada General Hospital",
                localHospitalPhone = "+20-65-3544-850",
                emergencyNotes = "Remote offshore reef. Strict reliance on vessel oxygen kits and maritime VHF emergency relay.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("Mooring / Shot Line", "Descent down forward line", 15f, 20f, "MOORING"),
                DiveSpotMapAnnotation("Bow & Windlass", "Anchor chains at 16m depth", 30f, 25f, "REEF"),
                DiveSpotMapAnnotation("Hold 1 & 2 Vehicles", "BSA Motorcycles inside", 45f, 40f, "WRECK"),
                DiveSpotMapAnnotation("Bomb Blast Crater", "Torn hull at 30m depth", 65f, 60f, "HAZARD"),
                DiveSpotMapAnnotation("Stern Gun", "Anti-aircraft 4.7 inch gun at 32m", 80f, 75f, "WRECK"),
                DiveSpotMapAnnotation("Safety Stop Shotline", "Hold line at 5m for 3+ min", 85f, 20f, "SAFETY_STOP")
            )
        ),
        DiveSpot(
            id = "spot_blue_hole_dahab",
            name = "The Blue Hole & The Canyon",
            region = "Dahab, South Sinai",
            country = "Egypt",
            latitude = 28.5724,
            longitude = 34.5372,
            minDepthMeters = 2,
            maxDepthMeters = 30, // Recreational limit (Arch is tech 56m)
            difficulty = "BEGINNER",
            spotType = "REEF",
            entryType = "SHORE",
            avgVisibilityMeters = 35,
            currentStrength = "NONE",
            highlights = listOf("The Bells vertical chimney entry", "Crystal clear blue lagoon", "Dramatic coral wall", "Camel transfer"),
            description = "World-renowned submarine sinkhole. The recreational dive starts at The Bells with an exciting drop through a vertical chimney, followed by a drift over pristine coral into the Blue Hole.",
            routeAdvice = "Enter at The Bells, drop to 26m, keep reef on right shoulder drifting south, enter the Blue Hole saddle at 7m, off-gas in the calm inner lagoon.",
            bestSeason = "March to November",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Dahab Hyperbaric Medical Center",
                chamberLocation = "Lighthouse Bay, Dahab",
                chamberDistanceKm = 8.5,
                chamberPhone = "+20-69-3640-530",
                coastGuardVHF = "VHF Ch 16 / Dahab Marine Control",
                localEmergencyNumber = "123",
                danHotline = "+39-06-695-4000",
                localHospitalName = "Dahab Central Hospital",
                localHospitalPhone = "+20-69-3640-208",
                emergencyNotes = "Chamber 15 mins by ambulance. Strict depth limit enforcement (30m recreational). Do NOT attempt Arch without Trimix.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("The Bells Entry", "Steps & vertical reef chute", 10f, 15f, "ENTRY"),
                DiveSpotMapAnnotation("Bells Drop Off", "Exit chimney at 26m depth", 25f, 30f, "WALL"),
                DiveSpotMapAnnotation("Reef Wall Drift", "Vibrant soft corals & anemones", 45f, 45f, "REEF"),
                DiveSpotMapAnnotation("The Saddle (7m)", "Crossing into the lagoon", 70f, 60f, "SAFETY_STOP"),
                DiveSpotMapAnnotation("Inner Blue Hole Exit", "Calm lagoon exit via wooden steps", 85f, 75f, "EXIT")
            )
        ),
        DiveSpot(
            id = "spot_palancar_cozumel",
            name = "Palancar Caves & Bricks",
            region = "Arrecifes de Cozumel National Park, Quintana Roo",
            country = "Mexico",
            latitude = 20.3421,
            longitude = -87.0289,
            minDepthMeters = 10,
            maxDepthMeters = 28,
            difficulty = "INTERMEDIATE",
            spotType = "CAVE",
            entryType = "BOAT",
            avgVisibilityMeters = 40,
            currentStrength = "MODERATE",
            highlights = listOf("Enormous coral buttresses & swim-throughs", "Hawksbill turtles", "Splendid toadfish", "Eagle rays"),
            description = "Spectacular coral formations rising like cathedrals from the deep. Dozens of safe wide swim-throughs with sun rays piercing through crevices.",
            routeAdvice = "Follow divemaster along drift current, weave through illuminated caves and canyons at 22m, ascend to outer ridge for turtle sightings.",
            bestSeason = "November to June",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Cozumel Hyperbaric Center (SSS Chamber)",
                chamberLocation = "Calle 5 Sur #21B, Centro, Cozumel",
                chamberDistanceKm = 14.2,
                chamberPhone = "+52-987-872-1430",
                coastGuardVHF = "VHF Ch 16 / Capitania de Puerto Cozumel",
                localEmergencyNumber = "911",
                danHotline = "+1-919-684-9111 (DAN America)",
                localHospitalName = "Costamed San Miguel Medical Clinic",
                localHospitalPhone = "+52-987-872-9400",
                emergencyNotes = "Active dive park patrol boats. SSS Chamber network is fully DAN-certified and 24h operational.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("Boat Drop Zone", "Drift descent over sand flat", 20f, 20f, "ENTRY"),
                DiveSpotMapAnnotation("Palancar Cathedral", "Enormous 25m coral archway", 40f, 45f, "CAVE"),
                DiveSpotMapAnnotation("Turtle Cleaning Station", "Hawksbill sea turtles resting", 60f, 55f, "REEF"),
                DiveSpotMapAnnotation("Sand Chute Drop-off", "Wall dropping to 400m", 80f, 70f, "WALL")
            )
        ),
        DiveSpot(
            id = "spot_silfra_iceland",
            name = "Silfra Fissure",
            region = "Thingvellir National Park",
            country = "Iceland",
            latitude = 64.2559,
            longitude = -21.1197,
            minDepthMeters = 2,
            maxDepthMeters = 18,
            difficulty = "ADVANCED",
            spotType = "WALL",
            entryType = "SHORE",
            waterType = "FRESH",
            avgVisibilityMeters = 100,
            currentStrength = "MILD",
            highlights = listOf("Dive between North American & Eurasian plates", "100m+ crystal visibility", "Silfra Cathedral & Lagoon", "Drinkable water"),
            description = "Glacial meltwater filtered for 30 years through volcanic lava rocks. World's highest underwater clarity with crisp 2°C to 4°C water temperature requiring drysuit certification.",
            routeAdvice = "Enter via steel steps at Big Crack, glide through Silfra Hall, navigate shallow section into Silfra Cathedral (18m), finish with scenic swim in Silfra Lagoon.",
            bestSeason = "June to September (Midnight sun diving)",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Landspitali University Hyperbaric Chamber",
                chamberLocation = "Fossvogur, 108 Reykjavik",
                chamberDistanceKm = 48.0,
                chamberPhone = "+354-543-1000",
                coastGuardVHF = "VHF Ch 16 / Iceland Coast Guard (LHG)",
                localEmergencyNumber = "112",
                danHotline = "+39-06-695-4000 (DAN Europe)",
                localHospitalName = "Landspitali University Hospital",
                localHospitalPhone = "+354-543-1000",
                emergencyNotes = "Park Rangers equipped with O2 resuscitation kits. Drysuit thermal emergency protocol active.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("Steel Platform Entry", "Entry stairs into Big Crack", 15f, 20f, "ENTRY"),
                DiveSpotMapAnnotation("Continental Drift Section", "Touch two continents simultaneously", 35f, 35f, "WALL"),
                DiveSpotMapAnnotation("Silfra Hall", "Wide cavern chamber (12m depth)", 55f, 50f, "REEF"),
                DiveSpotMapAnnotation("Silfra Cathedral", "Deepest section with golden sand (18m)", 75f, 65f, "WALL"),
                DiveSpotMapAnnotation("Silfra Lagoon Exit", "Shallow exit platform (2m depth)", 90f, 85f, "EXIT")
            )
        ),
        DiveSpot(
            id = "spot_cenote_dos_ojos",
            name = "Cenote Dos Ojos (Two Eyes)",
            region = "Tulum, Riviera Maya",
            country = "Mexico",
            latitude = 20.3248,
            longitude = -87.3912,
            minDepthMeters = 3,
            maxDepthMeters = 10,
            difficulty = "INTERMEDIATE",
            spotType = "CAVE",
            entryType = "SHORE",
            waterType = "FRESH",
            avgVisibilityMeters = 80,
            currentStrength = "NONE",
            highlights = listOf("The Bat Cave", "Sunbeam light laser shows", "Stalactites & stalagmites", "Halocline layer"),
            description = "One of the most expansive underwater cavern systems in the world. Freshwater crystal caverns lit by magical azure shafts of sunlight from jungle sinkholes.",
            routeAdvice = "Follow the permanent guideline (Barbie Line or Bat Cave Line). Maintain neutral buoyancy to avoid touching ancient limestone formations.",
            bestSeason = "Year-round (Midday offers the most breathtaking sunbeams)",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Playa del Carmen Hyperbaric Clinic",
                chamberLocation = "Av. 10 Norte entre 28 y 30, Playa del Carmen",
                chamberDistanceKm = 40.5,
                chamberPhone = "+52-984-873-1365",
                coastGuardVHF = "VHF Ch 16 / Quintana Roo Civil Protection",
                localEmergencyNumber = "911",
                danHotline = "+1-919-684-9111 (DAN America)",
                localHospitalName = "Costamed Tulum Emergency Clinic",
                localHospitalPhone = "+52-984-124-0830",
                emergencyNotes = "Cavern rule: Max 4 divers per guide with redundant primary and backup torches. 100% O2 kit on surface platform.",
                oxygenOnSite = true
            ),
            mapAnnotations = listOf(
                DiveSpotMapAnnotation("Wooden Deck Entry", "First Eye sinkhole entrance", 15f, 25f, "ENTRY"),
                DiveSpotMapAnnotation("Barbie Line Guideline", "Follow permanent orange guide line", 35f, 40f, "REEF"),
                DiveSpotMapAnnotation("Sunlight Cathedral", "Piercing laser sun rays through jungle", 55f, 45f, "CAVE"),
                DiveSpotMapAnnotation("The Bat Cave", "Air dome chamber with fruit bats", 75f, 70f, "CAVE"),
                DiveSpotMapAnnotation("Second Eye Platform", "Exit staircase and deco rest area", 85f, 85f, "EXIT")
            )
        )
    )

    fun calculateGasPlan(
        o2Percent: Int,
        ppo2Limit: Double,
        plannedDepthMeters: Double,
        plannedBottomTimeMin: Int,
        tankStartPressureBar: Int = 200,
        tankVolumeLiters: Int = 12,
        sacRateLitersPerMin: Double = 18.0
    ): GasPlanResult {
        // MOD formula: MOD = ((PPO2 / (FO2 / 100)) - 1) * 10
        val fo2 = o2Percent.toDouble() / 100.0
        val maxOperatingDepthMeters = ((ppo2Limit / fo2) - 1.0) * 10.0
        val contingencyDepthMeters = ((1.6 / fo2) - 1.0) * 10.0

        // Pressure at depth = (Depth / 10) + 1
        val ambientPressureBar = (plannedDepthMeters / 10.0) + 1.0

        // Total Gas Consumed in Liters = BottomTime * SAC * AmbientPressure
        val totalLitersConsumed = plannedBottomTimeMin * sacRateLitersPerMin * ambientPressureBar

        // Add 5 min safety stop at 5m (1.5 bar)
        val safetyStopLiters = 5 * sacRateLitersPerMin * 1.5
        val totalGasLiters = totalLitersConsumed + safetyStopLiters

        // In Bars = Liters / TankVolume
        val estimatedGasConsumedBar = (totalGasLiters / tankVolumeLiters).toInt()

        // Reserve calculation: Rule of Thirds (1/3 for return, 1/3 for out, 1/3 reserve) OR Rock Bottom (50 bar)
        val reservePressureBar = maxOf(50, tankStartPressureBar / 3)
        val turnPressureBar = tankStartPressureBar - ((tankStartPressureBar - reservePressureBar) / 2)

        val isModSafe = plannedDepthMeters <= maxOperatingDepthMeters
        val isGasSafe = (tankStartPressureBar - estimatedGasConsumedBar) >= reservePressureBar
        val isPlanSafe = isModSafe && isGasSafe

        val warning = when {
            !isModSafe -> "DANGER: Planned depth (${plannedDepthMeters.toInt()}m) exceeds Nitrox MOD (${maxOperatingDepthMeters.toInt()}m) at ${ppo2Limit} bar PPO2!"
            !isGasSafe -> "WARNING: High Gas Consumption! Ending pressure will drop below reserve (${reservePressureBar} bar)."
            else -> null
        }

        return GasPlanResult(
            o2Percentage = o2Percent,
            ppo2Limit = ppo2Limit,
            maxOperatingDepthMeters = maxOperatingDepthMeters,
            contingencyDepthMeters = contingencyDepthMeters,
            startPressureBar = tankStartPressureBar,
            reservePressureBar = reservePressureBar,
            turnPressureBar = turnPressureBar,
            plannedDepthMeters = plannedDepthMeters,
            plannedBottomTimeMin = plannedBottomTimeMin,
            estimatedGasConsumedBar = estimatedGasConsumedBar,
            isPlanSafe = isPlanSafe,
            warningMessage = warning
        )
    }
}
