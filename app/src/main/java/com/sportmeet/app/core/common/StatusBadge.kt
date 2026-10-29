package com.sportmeet.app.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmeet.app.core.theme.SportGold
import com.sportmeet.app.core.theme.SportGreen
import com.sportmeet.app.core.theme.SportRed
import com.sportmeet.app.domain.model.TournamentStatus

@Composable
fun StatusBadge(
    status: TournamentStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, bgColor, borderColor, textColor) = when (status) {
        TournamentStatus.APPROVED,
        TournamentStatus.REGISTRATION_OPEN -> Quadruple(
            SportGreen,
            SportGreen.copy(alpha = 0.12f),
            SportGreen.copy(alpha = 0.35f),
            SportGreen
        )
        TournamentStatus.PENDING_APPROVAL -> Quadruple(
            SportGold,
            SportGold.copy(alpha = 0.14f),
            SportGold.copy(alpha = 0.38f),
            Color(0xFFD97706)
        )
        TournamentStatus.DRAFT -> Quadruple(
            Color(0xFF94A3B8),
            Color(0xFF94A3B8).copy(alpha = 0.12f),
            Color(0xFF94A3B8).copy(alpha = 0.25f),
            Color(0xFF475569)
        )
        TournamentStatus.REGISTRATION_CLOSED,
        TournamentStatus.COMPLETED -> Quadruple(
            Color(0xFF64748B),
            Color(0xFF64748B).copy(alpha = 0.12f),
            Color(0xFF64748B).copy(alpha = 0.25f),
            Color(0xFF475569)
        )
        TournamentStatus.REJECTED,
        TournamentStatus.CANCELLED -> Quadruple(
            SportRed,
            SportRed.copy(alpha = 0.12f),
            SportRed.copy(alpha = 0.35f),
            SportRed
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing status indicator dot
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status.displayName.uppercase(),
            color = textColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
