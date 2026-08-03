package com.panitia.soalsandi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
// BARIS 10 SUDAH DIHAPUS DI SINI
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panitia.soalsandi.model.SoalItem
import com.panitia.soalsandi.model.SoalPackage

/**
 * On-screen preview of the soal grid — deliberately plain: just numbered boxes
 * with the sandi code inside. No titles, no extra decoration, matching the
 * reference sheet's layout.
 *
 * Uses a plain Column/Row (not LazyVerticalGrid) on purpose: this sits inside
 * a scrollable parent in GeneratorScreen, and a lazy grid inside another
 * scrollable container crashes at runtime ("vertically scrollable component
 * measured with infinite height constraints"). 30 items is small enough that
 * a non-lazy layout is perfectly fine here.
 */
@Composable
fun SoalGrid(pkg: SoalPackage, modifier: Modifier = Modifier, columns: Int = 5) {
    Column(modifier = modifier.fillMaxWidth()) {
        pkg.items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowItems.forEach { item ->
                    // Kode ini otomatis mengenali weight karena berada di dalam Row scope
                    SoalBox(item = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SoalBox(item: SoalItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.aspectRatio(1.15f),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.number.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = item.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Plain "1. A" style answer list, laid out directly below the grid. */
@Composable
fun AnswerList(pkg: SoalPackage, modifier: Modifier = Modifier, columns: Int = 5) {
    Column(modifier = modifier.fillMaxWidth()) {
        pkg.items.chunked(columns).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    // Kode ini juga otomatis mengenali weight karena berada di dalam Row scope
                    Text(
                        text = "${item.number}. ${item.letter}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}
