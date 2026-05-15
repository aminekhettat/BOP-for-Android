package org.blindsystems.bop.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blindsystems.bop.core.Segment
import org.blindsystems.bop.infra.I18n

/**
 * Segment list panel — jump, delete, move up/down, category badge.
 * Mirrors ui/segment_list_widget.py.
 */
@Composable
fun SegmentListWidget(
    segments: List<Segment>,
    onJump: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = I18n["segments"],
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (segments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(I18n["no_segments"], color = colors.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 260.dp)
            ) {
                itemsIndexed(segments) { index, seg ->
                    SegmentRow(
                        segment = seg,
                        index = index,
                        total = segments.size,
                        onJump = onJump,
                        onDelete = onDelete,
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentRow(
    segment: Segment,
    index: Int,
    total: Int,
    onJump: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val segColor = try {
        Color(android.graphics.Color.parseColor(segment.color))
    } catch (_: Exception) {
        colors.primary
    }

    val startMin = segment.start / 60000
    val startSec = (segment.start % 60000) / 1000
    val endMin   = segment.end / 60000
    val endSec   = (segment.end   % 60000) / 1000
    val timeLabel = "%02d:%02d – %02d:%02d".format(startMin, startSec, endMin, endSec)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, segColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable { onJump(segment.id) }
            .semantics {
                contentDescription = "${segment.name}, $timeLabel. ${I18n["jump_to_segment"]}"
                role = Role.Button
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Colour badge
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(50))
                .background(segColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(segment.name, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            Text(timeLabel, fontSize = 12.sp, color = colors.onSurface.copy(alpha = 0.7f))
            if (segment.category.isNotBlank()) {
                Text(
                    segment.category,
                    fontSize = 11.sp,
                    color = segColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Move up
        IconButton(
            onClick = { onMoveUp(segment.id) },
            enabled = index > 0,
            modifier = Modifier.semantics { contentDescription = "Monter ${segment.name}" }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, null, tint = colors.primary)
        }

        // Move down
        IconButton(
            onClick = { onMoveDown(segment.id) },
            enabled = index < total - 1,
            modifier = Modifier.semantics { contentDescription = "Descendre ${segment.name}" }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, null, tint = colors.primary)
        }

        // Delete
        IconButton(
            onClick = { onDelete(segment.id) },
            modifier = Modifier.semantics { contentDescription = "${I18n["delete_segment"]} ${segment.name}" }
        ) {
            Icon(Icons.Default.Delete, null, tint = colors.error)
        }
    }
}
