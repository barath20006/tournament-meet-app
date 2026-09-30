package com.sportmeet.app.core.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val (bgColor, textColor) = when (status) {
        TournamentStatus.APPROVED,
        TournamentStatus.REGISTRATION_OPEN -> Pair(
            SportGreen.copy(alpha = 0.15f),
            SportGreen
        )
        TournamentStatus.PENDING_APPROVAL -> Pair(
            SportGold.copy(alpha = 0.18f),
            Color(0xFFB45309)
        )
        TournamentStatus.DRAFT -> Pair(
            Color.LightGray.copy(alpha = 0.35f),
            Color.DarkGray
        )
        TournamentStatus.REGISTRATION_CLOSED,
        TournamentStatus.COMPLETED -> Pair(
            Color(0xFF64748B).copy(alpha = 0.2f),
            Color(0xFF475569)
        )
        TournamentStatus.REJECTED,
        TournamentStatus.CANCELLED -> Pair(
            SportRed.copy(alpha = 0.15f),
            SportRed
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
