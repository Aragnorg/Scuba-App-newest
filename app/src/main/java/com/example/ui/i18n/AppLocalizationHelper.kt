package com.example.ui.i18n

import com.example.data.model.QuizQuestion
import com.example.data.model.SlideItem
import com.example.data.model.VideoChapter

object AppLocalizer {

    fun localizeCourseTitle(title: String, isGerman: Boolean): String {
        if (!isGerman) return title
        return when {
            title.contains("Open Water Diver", ignoreCase = true) -> "Open Water Diver (OWD)"
            title.contains("Nitrox", ignoreCase = true) -> "Enriched Air Nitrox (EANx)"
            title.contains("Deep", ignoreCase = true) -> "Tieftauchen (Deep Diver)"
            title.contains("Wreck", ignoreCase = true) -> "Wracktauchen (Wreck Diver)"
            title.contains("Night", ignoreCase = true) -> "Nachttauchen (Night Diver)"
            else -> title
        }
    }

    fun localizeModuleTitle(title: String, isGerman: Boolean): String {
        if (!isGerman) return title
        return when (title.trim()) {
            "Physics & Pressure" -> "Physik & Druck"
            "Scuba Equipment Essentials" -> "Tauch-Ausrüstungsgrundlagen"
            "Diving Physiology & Safety" -> "Tauch-Physiologie & Sicherheit"
            "Underwater Problem Solving" -> "Problemlösung unter Wasser"
            "Dive Planning & Exam Review" -> "Tauchgangsplanung & Prüfungsvorbereitung"
            "Oxygen Analysis & Gas Mixing" -> "Sauerstoffanalyse & Gasmischung"
            "Deep Diving Limits & Narcosis Safety" -> "Tieftauchgrenzen & Tiefenrausch"
            "Wreck Surveying & Line Laying" -> "Wrackvermessung & Führungsleinen"
            "Dark Navigation & Light Communication" -> "Nachtnavigation & Lampenkommunikation"
            else -> title
        }
    }

    fun localizeModuleSubtitle(subtitle: String, isGerman: Boolean): String {
        if (!isGerman) return subtitle
        return when (subtitle.trim()) {
            "Boyle's Law, Depth & Air Consumption" -> "Boyles Gesetz, Tiefe & Luftverbrauch"
            "Regulators, BCD, Cylinders & BWRAF" -> "Atemregler, Tarierweste, Tauchflaschen & BWRAF"
            "Nitrogen, Narcosis, DCS & Ascent Rates" -> "Stickstoff, Tiefenrausch, DCS & Aufstiegsgeschwindigkeiten"
            "Mask Clearing, Out-of-Air & Cramps" -> "Maske ausblasen, Luftnot & Krämpfe"
            "Dive Tables, NDL & Final Certification Exam" -> "Tauchtabelle, Nullzeit & Abschlussprüfung"
            "Enriched Air Nitrox Specialty" -> "Enriched Air Nitrox Spezialkurs"
            "Deep Diver Specialty" -> "Deep Diver Spezialkurs"
            "Wreck Diver Specialty" -> "Wreck Diver Spezialkurs"
            "Night Diver Specialty" -> "Night Diver Spezialkurs"
            else -> subtitle
        }
    }

    fun localizeModuleDescription(description: String, isGerman: Boolean): String {
        if (!isGerman) return description
        return when (description.trim()) {
            "Master how water pressure changes with depth, how air volume compresses, and the golden rules of scuba diving." ->
                "Verstehe, wie sich der Wasserdruck mit der Tiefe ändert, wie Luft komprimiert wird und die goldenen Regeln des Tauchens."
            "Understand how life-support equipment functions and execute flawless pre-dive buddy safety checks." ->
                "Verstehe die Funktionsweise der lebenserhaltenden Ausrüstung und führe den perfekten Buddy-Check durch."
            "Understand how breathing compressed gas affects the body, how to prevent decompression sickness, and ascent protocols." ->
                "Erfahre, wie komprimierte Atemgase auf den Körper wirken, wie man Dekompressionskrankheit vermeidet und Aufstiegsregeln einhält."
            "Build self-reliance and buddy teamwork: regulator recovery, mask clearing, out-of-air ascents, and calm problem resolution." ->
                "Erlange Selbstvertrauen und Buddy-Teamwork: Regler wiedererlangen, Maske ausblasen, Wechselatmung und ruhige Problemlösung."
            "Learn how to calculate No-Decompression Limits, execute surface intervals, and pass your Open Water certification exam." ->
                "Lerne Nullzeiten zu berechnen, Oberflächenpausen zu planen und bestehe deine Open-Water-Abschlussprüfung."
            "Theory and safety procedures for diving with oxygen concentrations up to 40% Enriched Air Nitrox (EANx)." ->
                "Theorie und Sicherheitsverfahren für das Tauchen mit Sauerstoffkonzentrationen bis 40% Enriched Air Nitrox (EANx)."
            "Master safe descent procedures, redundant gas planning, and narcosis management for deep diving." ->
                "Sichere Abstiegsverfahren, redundante Gasplanung und Tiefenrausch-Management beim Tieftauchen bis 40m."
            "Master safe descent procedures, redundant gas planning, and narcosis management for deep diving to 40m/130ft." ->
                "Sichere Abstiegsverfahren, redundante Gasplanung und Tiefenrausch-Management beim Tieftauchen bis 40m."
            "Procedures and protocols for mapping artificial reefs, deploying penetration guidelines, and avoiding silt-outs." ->
                "Verfahren zur Kartierung von Wracks, Verlegen von Penetrationsleinen und Vermeidung von Sedimentaufwirbelung."
            "Master night dive navigation, specialized buddy communication signals, and explore nocturnal reef environments safely." ->
                "Nachttauchnavigation, Buddy-Signale mit der Lampe und sicheres Erkunden nachtaktiver Rifflebewesen."
            else -> description
        }
    }

    fun localizePresentationTitle(title: String, isGerman: Boolean): String {
        if (!isGerman) return title
        return when (title.trim()) {
            "Scuba Physics, Boyle's Law & Equalization" -> "Tauchphysik, Boyles Gesetz & Druckausgleich"
            "Scuba Equipment System & BWRAF Check" -> "Tauch-Ausrüstungssystem & BWRAF-Check"
            "Nitrogen Off-gassing, DCS & Ascent Rules" -> "Stickstoff-Entsättigung, DCS & Aufstiegsregeln"
            "Nitrox Gas Analysis & Max Depth (MOD) calculations" -> "Nitrox-Gasanalyse & Maximaltiefen-Berechnung (MOD)"
            "Decompression Safety & Gas Planning" -> "Dekompressionssicherheit & Gasplanung"
            "Decompression Safety & Redundant Gas Systems" -> "Dekompressionssicherheit & redundante Gassysteme"
            "Wreck Hazards, Navigation & Penetration Lines" -> "Wrackgefahren, Navigation & Penetrationsleinen"
            "Nocturnal Wildlife & Dive Torch Management" -> "Nachtaktive Meeresbewohner & Lampen-Management"
            "Instructor Presentation" -> "Tauchlehrer-Präsentation"
            else -> title
        }
    }

    fun localizeVideoTitle(title: String, isGerman: Boolean): String {
        if (!isGerman) return title
        return when (title.trim()) {
            "Equalization Techniques & Pressure Awareness" -> "Druckausgleichstechniken & Druckbewusstsein"
            "Mastering the BWRAF Pre-Dive Buddy Check" -> "Perfekter BWRAF-Buddy-Check vor dem Tauchgang"
            "Mask Clearing & Regulator Recovery In-Depth" -> "Maske ausblasen & Regler wiedererlangen im Detail"
            "Buoyancy Control & Trim Mastery (Google Drive Stream)" -> "Tarierungskontrolle & Trim-Perfektion (Google Drive)"
            "Instructor Training Video" -> "Tauchlehrer-Lehrvideo"
            else -> title
        }
    }

    fun localizeQuizTitle(title: String, isGerman: Boolean): String {
        if (!isGerman) return title
        return when (title.trim()) {
            "Physics & Pressure Mastery Quiz" -> "Physik & Druck Meisterschaftsquiz"
            "Gear & Pre-Dive Safety Quiz" -> "Ausrüstung & Vor-Tauchgangs-Sicherheitsquiz"
            "Open Water Final Certification Mock Exam" -> "Open Water Abschluss-Probeprüfung"
            "Nitrox Specialty Quiz" -> "Nitrox Spezialkurs-Quiz"
            "Deep Diving Specialty Quiz" -> "Tieftauchen Spezialkurs-Quiz"
            "Enriched Air Safety & Oxygen Toxicity Exams" -> "Enriched Air Sicherheit & Sauerstofftoxizität"
            "Narcosis Symptoms & Safety Protocol Quiz" -> "Tiefenrausch-Symptome & Sicherheitsprotokoll"
            "Custom Dive Quiz" -> "Tauchquiz"
            else -> title
        }
    }

    fun localizeSlide(slide: SlideItem, isGerman: Boolean): SlideItem {
        if (!isGerman) return slide
        val newTitle = when (slide.title.trim()) {
            "Pressure Underwater & Atmospheres (ATA)" -> "Wasserdruck & Atmosphärendruck (ATA)"
            "Boyle's Law: Gas Compression" -> "Boyles Gesetz: Gaskompression"
            "Equalization Techniques" -> "Druckausgleichstechniken"
            "The Golden Rule of Scuba" -> "Die goldene Regel des Tauchens"
            "Regulator 1st & 2nd Stages" -> "Atemregler: 1. & 2. Stufe"
            "Buoyancy Control Device (BCD)" -> "Tarierweste (BCD / Jacket)"
            "Pre-Dive BWRAF Buddy Check" -> "BWRAF Vor-Tauchgangs-Check"
            "Inert Gas Absorption & Narcosis" -> "Stickstoff-Aufnahme & Tiefenrausch"
            "Decompression Sickness (DCS) Prevention" -> "DCS-Prävention (Dekompressionskrankheit)"
            "Flying After Diving Regulations" -> "Fliegen nach dem Tauchen"
            "Tauchplatz Seeordnung & Briefing" -> "Tauchplatz Seeordnung & Briefing"
            else -> slide.title
        }

        val newSubtitle = when (slide.subtitle.trim()) {
            "Water is 800x denser than air, creating rapid pressure increases." -> "Wasser ist 800-mal dichter als Luft und erzeugt raschen Druckanstieg."
            "Volume decreases as pressure increases: P1 * V1 = P2 * V2" -> "Das Volumen nimmt mit steigendem Druck ab: P1 * V1 = P2 * V2"
            "Clear ears early and often to prevent middle-ear barotrauma." -> "Druckausgleich früh und oft durchführen, um Barotraumata zu vermeiden."
            "NEVER hold your breath while scuba diving." -> "NIEMALS beim Tauchen mit Gerät die Luft anhalten."
            "Delivering air at ambient pressure smoothly in any attitude." -> "Liefert Atemgas zuverlässig bei jedem Umgebungsdruck."
            "Mastering neutral buoyancy at safety stops and depths." -> "Perfektionierung der neutralen Tarierung auf Tiefe und Sicherheitsstopps."
            "Begin With Review And Friend: Standard 5-step safety check." -> "Standardisierter 5-Schritte-Sicherheitscheck vor dem Sprung ins Wasser."
            "Gas dissolution under Dalton's and Henry's laws." -> "Gaslösung nach den Gesetzen von Dalton und Henry."
            "Microbubbles, silent bubbles, and the critical 5m / 3min safety stop." -> "Mikroblasen, stille Bläschen und der 5m / 3min Sicherheitsstopp."
            "Wait intervals to avoid trapped microbubbles expanding at cabin altitude." -> "Wartezeiten einhalten, um Gasblasenbildung im Flugzeug zu verhindern."
            "Google Drive / Slides Präsentationsdeck" -> "Google Drive / Slides Präsentationsdeck"
            else -> slide.subtitle
        }

        val newKeyTakeaway = when (slide.keyTakeaway.trim()) {
            "Every 10 meters of depth adds 1.0 bar (ATA) of pressure." -> "Alle 10 Meter Wassertiefe kommt 1,0 bar (ATA) hydrostatischer Druck hinzu."
            "Air density increases at depth, consuming gas faster." -> "Die Gasdichte steigt mit der Tiefe – der Luftverbrauch ist höher."
            "Never descend through pain. Abort or ascend if equalization fails." -> "Niemals gegen Schmerz abtauchen! Bei Problemen aufsteigen oder abbrechen."
            "Continuous breathing prevents lung overexpansion injury." -> "Stetiges Ein- und Ausatmen verhindert Lungenüberdehnungsverletzungen."
            "Always inspect SPG pressure and take 3 test breaths before entry." -> "Vor dem Einstieg Finimeter prüfen und 3 Test-Atemzüge nehmen."
            "Check both inflator buttons and manual dump valves before water entry." -> "Vor dem Einstieg Inflator und alle Schnellablässe testen."
            "Never enter the water without completing a BWRAF buddy check." -> "Niemals ohne vollständigen BWRAF-Buddy-Check ins Wasser springen."
            "Nitrogen narcosis impairs thinking; ascend immediately to resolve." -> "Tiefenrausch trübt das Urteilsvermögen; sofortige Abhilfe schafft Aufsteigen."
            "Ascend no faster than 9-18m/min and never skip the 3-minute 5m stop." -> "Aufstiegsgeschwindigkeit max. 9-18m/min und Sicherheitsstopp nie auslassen."
            "Wait at least 18-24 hours after repetitive diving before boarding an aircraft." -> "Nach mehreren Tauchgängen mindestens 18-24 Stunden vor dem Flug warten."
            "Sicherheit und Umweltschutz haben oberste Priorität." -> "Sicherheit und Umweltschutz haben oberste Priorität."
            else -> slide.keyTakeaway
        }

        return slide.copy(
            title = newTitle,
            subtitle = newSubtitle,
            keyTakeaway = newKeyTakeaway
        )
    }

    fun localizeQuizQuestion(q: QuizQuestion, isGerman: Boolean): QuizQuestion {
        if (!isGerman) return q
        return when (q.id) {
            "qp_1" -> q.copy(
                question = "Wie hoch ist der absolute Umgebungsdruck in 20 Metern Wassertiefe im Meer?",
                options = listOf("1.0 ATA", "2.0 ATA", "3.0 ATA", "4.0 ATA"),
                explanation = "Der Oberflächendruck beträgt 1,0 ATA. Alle 10 Meter Salzwasser kommt 1,0 ATA hinzu. Bei 20 Metern: 1,0 (Oberfläche) + 2,0 (Wasser) = 3,0 ATA.",
                safetyRule = "Druckberechnung: Umgebungsdruck (ATA) = (Tiefe in Metern / 10) + 1.",
                depthOrContext = "Tiefe: 20m"
            )
            "qp_2" -> q.copy(
                question = "Was geschieht nach dem Boyle-Mariotte-Gesetz mit einem flexiblen Behälter mit 6 Litern Luft an der Oberfläche, wenn er auf 10 Meter Tiefe gebracht wird?",
                options = listOf("6 Liter", "3 Liter", "2 Liter", "12 Liter"),
                explanation = "Bei 10 Metern verdoppelt sich der Druck von 1 auf 2 ATA. Druck und Volumen verhalten sich umgekehrt proportional, daher halbiert sich das Volumen auf 3 Liter: 6 L / 2 = 3 Liter.",
                safetyRule = "Boyles Gesetz besagt: P1 * V1 = P2 * V2 bei gleichbleibender Temperatur.",
                depthOrContext = "Tiefe: 10m"
            )
            "qp_3" -> q.copy(
                question = "Was ist die wichtigste Grundregel beim gesamten Gerätetauchen?",
                options = listOf(
                    "Niemals tiefer als 18 Meter ohne Buddy tauchen",
                    "Kontinuierlich atmen und niemals die Luft anhalten",
                    "Stets zwei Tauchmesser mitführen",
                    "Vor dem Tauchen niemals Kaffee trinken"
                ),
                explanation = "Wer beim Aufstieg die Luft anhält, riskiert durch die Ausdehnung der Luft in den Lungen ein Lungenbarotrauma oder eine arterielle Gasembolie.",
                safetyRule = "Regel #1: Kontinuierlich atmen. Wenn der Lungenautomat aus dem Mund ist, kleine Bläschen ausblasen.",
                depthOrContext = "Fundamentale Sicherheitsregel"
            )
            "qp_4" -> q.copy(
                question = "Was solltest du sofort tun, wenn du beim Abstieg Ohrenschmerzen verspürst und der Druckausgleich nicht gelingt?",
                options = listOf(
                    "So fest wie möglich durch die Nase pressen",
                    "Abstieg stoppen, leicht aufsteigen bis der Schmerz nachlässt, und sanft erneut ausgleichen",
                    "Schneller absteigen, um neutral zu werden",
                    "Maske abnehmen und Ohren spülen"
                ),
                explanation = "Druckausgleich niemals mit Gewalt erzwingen. Ein leichter Aufstieg verringert die Druckdifferenz am Trommelfell, sodass sich die Eustachische Röhre öffnen kann.",
                safetyRule = "Niemals gegen den Schmerz tauchen. Gelingt der Druckausgleich nicht, Tauchgang sicher abbrechen.",
                depthOrContext = "Abstiegsphase"
            )
            "qp_5" -> q.copy(
                question = "Wie wirkt sich das Atmen von Pressluft in 30 Metern Tiefe (4 ATA) im Vergleich zur Oberfläche auf deinen Luftverbrauch aus?",
                options = listOf(
                    "Man verbraucht Luft gleich schnell",
                    "Man verbraucht Luft doppelt so schnell",
                    "Man verbraucht Luft viermal so schnell",
                    "Man verbraucht Luft nur halb so schnell"
                ),
                explanation = "In 30 Metern Tiefe (4 ATA) ist die Luft viermal so dicht. Mit jedem Atemzug nimmt man viermal so viele Gasmoleküle auf – die Flasche leert sich 4-mal schneller.",
                safetyRule = "Je tiefer der Tauchgang, desto aufmerksamer muss das Finimeter kontrolliert werden.",
                depthOrContext = "Tiefe: 30m"
            )
            "qe_1" -> q.copy(
                question = "Wofür steht der Buchstabe 'W' beim BWRAF-Vor-Tauchgangs-Buddycheck?",
                options = listOf(
                    "Wasserbedingungen (Water conditions)",
                    "Bleigewichte (Weights – gesichert und frei für Schnellabwurf)",
                    "Tauchcomputer am Handgelenk (Wrist computer)",
                    "Neoprenanzug-Dicke (Wetsuit)"
                ),
                explanation = "'W' steht für Weights (Blei). Beide Buddys überprüfen gegenseitig, dass die Bleitaschen/Gurte fest sitzen und im Notfall mit einem Handgriff abgeworfen werden können.",
                safetyRule = "Blei muss immer für den sofortigen Einhand-Schnellabwurf frei zugänglich sein.",
                depthOrContext = "Check vor dem Tauchgang"
            )
            "qe_2" -> q.copy(
                question = "Was zeigt das Finimeter (SPG) an, wenn das Flaschenventil nur minimal angetestet und nicht voll geöffnet wurde?",
                options = listOf(
                    "Die Nadel steht auf Null",
                    "Die Nadel zeigt vollen Druck an, fällt aber bei jedem Atemzug stark ab",
                    "Das Finimeter explodiert",
                    "Das Finimeter zeigt 50 bar mehr an als tatsächlich vorhanden"
                ),
                explanation = "Bei kaum geöffnetem Flaschenventil baut sich statischer Druck auf (zeigt voll). Unter Atemlast strömt jedoch nicht genug Gas nach, sodass der Zeiger stark schwankt oder einbricht.",
                safetyRule = "Vor dem Sprung ins Wasser immer 3 kräftige Testatemzüge nehmen und dabei die Finimeternadel beobachten.",
                depthOrContext = "Ausrüstungssicherheit"
            )
            "qe_3" -> q.copy(
                question = "Wo muss die alternative Luftversorgung (Oktopus) während des gesamten Tauchgangs befestigt sein?",
                options = listOf(
                    "Verschlossen in der Jackettasche mit Reißverschluss",
                    "Lose hinter der Tauchflasche baumelnd",
                    "Im Dreieck zwischen Kinn und den unteren Rippenbögen (Brustbereich)",
                    "Um den Hals des Tauchers gewickelt"
                ),
                explanation = "Das goldene Brustdreieck gewährleistet, dass der Oktopus für einen luftlosen Buddy sofort sichtbar und ohne langes Suchen erreichbar ist.",
                safetyRule = "Oktopus stets mit Schnellabwurf-Clip im vorderen Brustdreieck sichern.",
                depthOrContext = "Buddy-Sicherheit"
            )
            "qm_1" -> q.copy(
                question = "Was ist die empfohlene maximale Aufstiegsgeschwindigkeit bei jedem Sporttauchgang?",
                options = listOf(
                    "18 Meter pro Minute",
                    "9 Meter pro Minute",
                    "3 Meter pro Minute",
                    "So schnell wie man mit den Flossen schlagen kann"
                ),
                explanation = "Der weltweite Standard beträgt maximal 9 Meter pro Minute (langsamer als kleine Bläschen). Moderne Tauchcomputer warnen optisch und akustisch bei Überschreitung.",
                safetyRule = "Niemals schneller als 9m/min aufsteigen. Ein guter Anhaltspunkt: Langsamer als die kleinsten Ausatembläschen steigen.",
                depthOrContext = "Aufstiegsphase"
            )
            "qm_2" -> q.copy(
                question = "Was ist der Hauptzweck des 3-minütigen Sicherheitsstopps auf 5 Metern Wassertiefe?",
                options = listOf(
                    "Auf das Tauchboot zu warten",
                    "Gelösten Stickstoff kontrolliert abzuatmen und Mikroblasenbildung zu minimieren",
                    "Die Maske im sauberen Oberflächenwasser auszuspülen",
                    "Restluft aus der Flasche zu verbrauchen"
                ),
                explanation = "Der Sicherheitsstopp gibt dem Gewebe entscheidende Zeit zum Entsättigen, während der Umgebungsdruck (1,5 ATA) noch hoch genug ist, um Blasenbildung zu unterdrücken.",
                safetyRule = "Ein 3-Minuten-Stopp auf 5m wird bei jedem Tauchgang tiefer als 10m dringend empfohlen.",
                depthOrContext = "Sicherheitsprotokoll"
            )
            "qm_3" -> q.copy(
                question = "Wie reagierst du sofort, wenn dir dein Tauchpartner unter Wasser das Handzeichen 'Hals durchschneiden' gibt?",
                options = listOf(
                    "Ihn fragen, ob er ein Foto machen möchte",
                    "Sofort den Oktopus reichen und festen Körperkontakt herstellen",
                    "Sofort alleine so schnell wie möglich an die Oberfläche schwimmen",
                    "Mit dem 'OK'-Zeichen antworten"
                ),
                explanation = "Die horizontale Handbewegung über den Hals bedeutet 'Keine Luft mehr!'. Sofort die alternative zweite Stufe reichen und den Partner festhalten, um Panik zu verhindern.",
                safetyRule = "Unverzügliche Luftspende und Körperkontakt verhindern unkontrollierte Panikaufstiege.",
                depthOrContext = "Notfallreaktion"
            )
            "qm_4" -> q.copy(
                question = "Wie lange sollte ein Taucher nach zwei Tauchgängen an einem Tag mindestens warten, bevor er in ein Verkehrsflugzeug steigt?",
                options = listOf(
                    "2 Stunden",
                    "6 Stunden",
                    "Mindestens 18 Stunden (empfohlen 24 Stunden)",
                    "Immer 48 Stunden"
                ),
                explanation = "Nach Wiederholungstauchgängen oder Mehrtagestauchen sind mindestens 18 Stunden (ideal 24 Stunden) Wartezeit vorgeschrieben, um Dekompressionskrankheit durch den verringerten Kabinendruck zu vermeiden.",
                safetyRule = "18-24 Stunden Pause vor Flügen oder Fahrten in die Berge über 300 Höhenmeter einhalten.",
                depthOrContext = "Sicherheit nach dem Tauchen"
            )
            "qm_5" -> q.copy(
                question = "Welcher Zustand ist zu vermuten, wenn ein Taucher innerhalb von 6 Stunden nach dem Tauchgang Gelenkschmerzen, Taubheitsgefühle und extreme Müdigkeit verspürt?",
                options = listOf(
                    "Tiefenrausch (Stickstoffnarkose)",
                    "Dekompressionskrankheit (DCS)",
                    "Lediglich Dehydrierung",
                    "Sonnenstich"
                ),
                explanation = "Gelenkschmerzen ('Bends'), neurologische Ausfälle und Erschöpfung sind Leitsymptome von Stickstoffblasen in Gewebe und Blutbahn. Sofortmaßnahme: 100% reiner Sauerstoff und Druckkammerkontakt.",
                safetyRule = "100% medizinischen Sauerstoff verabreichen und Notruf für Druckkammerbehandlung absetzen.",
                depthOrContext = "Tauchmedizinischer Notfall"
            )
            "qm_6" -> q.copy(
                question = "Was ist der Tiefenrausch (Stickstoffnarkose) und ab welcher Tiefe tritt er beim Atmen von Druckluft typischerweise auf?",
                options = listOf(
                    "Ein Barotrauma des Ohres bei 3 Metern",
                    "Eine betäubende Wirkung mit Urteilsverlust, die typischerweise ab etwa 30 Metern spürbar wird",
                    "Ein Wadenkrampf durch Flossenschlag bei 10 Metern",
                    "Dauerhafte Gehirnschädigung durch Salzwasser"
                ),
                explanation = "Stickstoff wirkt unter hohem Partialdruck wie ein Narkosemittel. Symptome wie Euphorie und verlangsamtes Reaktionsvermögen beginnen ab ca. 30 Metern. Einzige Abhilfe: Sofortiges Aufsteigen in geringere Tiefe.",
                safetyRule = "Bei ersten Anzeichen von Tiefenrausch dem Buddy signalisieren und gemeinsam kontrolliert aufsteigen.",
                depthOrContext = "Tieftauch-Physiologie"
            )
            "nq_1" -> q.copy(
                question = "Was ist der Hauptvorteil beim Tauchen mit Enriched Air Nitrox (EANx)?",
                options = listOf(
                    "Man kann tiefer tauchen",
                    "Geringere Stickstoffaufnahme verlängert die Nullzeit",
                    "Verhindert Tiefenrausch vollständig",
                    "Erwärmt die Körpertemperatur"
                ),
                explanation = "Durch den höheren Sauerstoff- und geringeren Stickstoffanteil sättigt das Gewebe langsamer mit Stickstoff auf, wodurch die Nullzeit deutlich verlängert wird.",
                safetyRule = "Nitrox verlängert die Grundzeit, erfordert aber strikte Überwachung der Maximaltiefe (MOD).",
                depthOrContext = "Nitrox-Physik"
            )
            "dq_1" -> q.copy(
                question = "Was ist das empfohlene Tiefenlimit für das Sporttauchen?",
                options = listOf(
                    "18 Meter",
                    "30 Meter",
                    "40 Meter",
                    "50 Meter"
                ),
                explanation = "40 Meter ist die absolute Grenze für das Sporttauchen mit Pressluft.",
                safetyRule = "Niemals ohne spezielles technisches Dekompressionstraining tiefer als 40m tauchen.",
                depthOrContext = "Tieftauchgrenzen"
            )
            else -> q
        }
    }
}
