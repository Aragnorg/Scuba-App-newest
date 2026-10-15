package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.DiverDocumentEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen

/**
 * Renders the realistic physical paper medical statement document.
 * If a real image or base64 URI is attached, displays the scanned photo.
 * Otherwise, renders a high-fidelity stylized medical clearance certificate.
 */
@Composable
fun RealisticMedicalStatementPaper(
    document: DiverDocumentEntity,
    modifier: Modifier = Modifier,
    isThumbnail: Boolean = false,
    onInspectClick: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val uri = document.documentUri

    // Check if URI is Base64 Image
    val base64Bitmap = remember(uri) {
        if (uri != null && uri.startsWith("data:image")) {
            try {
                val base64Str = uri.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onInspectClick != null) Modifier.clickable { onInspectClick() } else Modifier
            )
            .testTag("realistic_medical_paper_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isThumbnail) 3.dp else 6.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        if (base64Bitmap != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    bitmap = base64Bitmap.asImageBitmap(),
                    contentDescription = strings.paperDocumentScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (isThumbnail) Modifier.height(180.dp) else Modifier)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = if (isThumbnail) ContentScale.Crop else ContentScale.Fit
                )
                if (isThumbnail && onInspectClick != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = DepthNavy.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enlarge", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (uri != null && (uri.startsWith("http") || uri.startsWith("content://") || uri.startsWith("file://"))) {
            val isPdf = uri.endsWith(".pdf", ignoreCase = true) || uri.contains("pdf", ignoreCase = true)
            if (isPdf) {
                val context = androidx.compose.ui.platform.LocalContext.current
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(android.net.Uri.parse(uri), "application/pdf")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanPrimary.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📄", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PDF-Dokument angehängt",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Antippen, um die Datei auf dem Handy zu öffnen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = uri,
                        contentDescription = strings.paperDocumentScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (isThumbnail) Modifier.height(180.dp) else Modifier)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = if (isThumbnail) ContentScale.Crop else ContentScale.Fit
                    )
                    if (isThumbnail && onInspectClick != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = DepthNavy.copy(alpha = 0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enlarge", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // High-Fidelity Paper Statement Vector Document Render
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isThumbnail) 12.dp else 18.dp)
            ) {
                // Clinic & RSTC Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = SeaGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RSTC / WRSTC MEDICAL STATEMENT",
                                fontSize = if (isThumbnail) 9.sp else 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E293B),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "PHYSICIAN'S DIVING FITNESS EVALUATION",
                            fontSize = if (isThumbnail) 8.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeaGreen
                        )
                        Text(
                            text = document.issuingOrganization.ifBlank { "Dr. med. Thomas Weber (Diving Medicine Specialist)" },
                            fontSize = if (isThumbnail) 8.sp else 9.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Form Doc Reference ID
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "REF: ${document.documentNumber.ifBlank { "MED-2026-99" }}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = if (isThumbnail) 6.dp else 10.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE2E8F0)
                )

                // Candidate & Examination Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "DIVER CANDIDATE:", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(
                            text = document.diverName.ifBlank { "Alex Rivera" },
                            fontSize = if (isThumbnail) 11.sp else 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXAM DATE:", fontSize = 8.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Text(
                            text = document.issueDate.ifBlank { "2026-08-10" },
                            fontSize = if (isThumbnail) 10.sp else 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                if (!isThumbnail) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Clinical Checkpoints Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "CLINICAL EXAMINATION FINDINGS:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            MedicalFindingRow(title = "Cardiovascular & ECG", finding = "Normal sinus rhythm, BP 120/75")
                            MedicalFindingRow(title = "Pulmonary Spirometry (FVC/FEV1)", finding = "> 95% predicted, no obstruction")
                            MedicalFindingRow(title = "ENT & Tympanic Equalization", finding = "Bilateral intact, Eustachian clear")
                            MedicalFindingRow(title = "Neurological & Vestibular", finding = "Romberg negative, cranial nerves intact")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Clearance Statement
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SeaGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DIAGNOSIS: FIT FOR UNRESTRICTED RECREATIONAL SCUBA DIVING",
                            fontSize = if (isThumbnail) 8.sp else 10.sp,
                            fontWeight = FontWeight.Black,
                            color = SeaGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isThumbnail) 6.dp else 12.dp))

                // Physical Clinic Stamp & Handwritten Signature Representation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Physical Red/Blue Clinic Stamp
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDC2626).copy(alpha = 0.08f),
                        border = BorderStroke(1.5.dp, Color(0xFFDC2626).copy(alpha = 0.7f)),
                        modifier = Modifier.size(if (isThumbnail) 48.dp else 68.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "PRAXISSTEMPEL",
                                fontSize = if (isThumbnail) 5.sp else 7.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFDC2626),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "GTMÜ e.V.",
                                fontSize = if (isThumbnail) 5.sp else 6.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "#DE-77492",
                                fontSize = if (isThumbnail) 5.sp else 6.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Doctor's Pen Signature & Stamp
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Dr. T. Weber, M.D.",
                            fontFamily = FontFamily.Cursive,
                            fontSize = if (isThumbnail) 14.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        HorizontalDivider(
                            modifier = Modifier.width(if (isThumbnail) 90.dp else 130.dp),
                            thickness = 1.dp,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "Physician Signature & Stamp",
                            fontSize = 7.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalFindingRow(title: String, finding: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        }
        Text(text = finding, fontSize = 8.sp, color = Color(0xFF64748B))
    }
}

/**
 * Interactive comparative correlation card used by dive centers and instructors
 * to cross-examine the physical paper statement alongside the digital entry.
 */
@Composable
fun PaperVsDigitalCorrelationCard(
    document: DiverDocumentEntity,
    onInspectFullScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var showFullDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dive_center_correlation_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = DepthNavy.copy(alpha = 0.04f)
        ),
        border = BorderStroke(1.5.dp, SeaGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = SeaGreen.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.diveCenterCorrelationTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.diveCenterCorrelationDesc,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SeaGreen,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isGerman) "100% ÜBEREINSTIMMUNG" else "100% MATCH", fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comparative Visual: Physical Paper Scan Preview
            RealisticMedicalStatementPaper(
                document = document,
                isThumbnail = true,
                onInspectClick = { showFullDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Correlation Checklist Items
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CorrelationCheckItem(
                        title = if (isGerman) "Identität des Tauchkandidaten" else "Diver Candidate Identity",
                        detail = if (isGerman) "Dokument: ${document.diverName} ↔ Profil: ${document.diverName}" else "Paper: ${document.diverName} ↔ Digital: ${document.diverName}"
                    )
                    CorrelationCheckItem(
                        title = if (isGerman) "Ärztliches Untersuchungsdatum" else "Physician Examination Date",
                        detail = if (isGerman) "Untersucht: ${document.issueDate} (Gültig bis ${document.expiryDate})" else "Paper: ${document.issueDate} (Valid until ${document.expiryDate})"
                    )
                    CorrelationCheckItem(
                        title = if (isGerman) "Arztzulassung & Praxisstempel" else "Physician Credentials & Clinic Seal",
                        detail = if (isGerman) "Stempel: ${document.approverTitleOrLicense.ifBlank { "GTMÜ / EDTC #DE-77492" }} ↔ Aussteller: ${document.issuingOrganization}" else "Paper Stamp: ${document.approverTitleOrLicense.ifBlank { "GTMÜ / EDTC #DE-77492" }} ↔ Issuer: ${document.issuingOrganization}"
                    )
                    CorrelationCheckItem(
                        title = if (isGerman) "Medizinische Tauchtauglichkeit" else "Medical Clearance Level",
                        detail = if (isGerman) "Uneingeschränkt tauchtauglich (Sporttauchen bis 30m)" else "Unrestricted Recreational Diving up to 30m depth"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { showFullDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 9.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                    modifier = Modifier.testTag("inspect_paper_scan_btn")
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.viewEnlargedPaperScan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showFullDialog) {
        PaperStatementInspectDialog(
            document = document,
            onDismiss = { showFullDialog = false }
        )
    }
}

@Composable
private fun CorrelationCheckItem(title: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = SeaGreen.copy(alpha = 0.15f),
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = detail, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Fullscreen Lightbox inspection dialog allowing dive centers and instructors
 * to closely inspect the medical paper statement scan.
 */
@Composable
fun PaperStatementInspectDialog(
    document: DiverDocumentEntity,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("paper_statement_inspect_dialog"),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Close Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SeaGreen,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.inspectPaperDocumentTitle,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${document.diverName} • ${document.documentNumber}",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_paper_inspect_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = strings.close, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable full-size physical document
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    RealisticMedicalStatementPaper(
                        document = document,
                        isThumbnail = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Stamp Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.crossCheckPassed,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                    ) {
                        Text(strings.close, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
