package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SevaGreenPrimary

@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 3.dp,
    shapeRadius: Dp = 16.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.985f else 1f, label = "scale")

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else elevation,
                shape = RoundedCornerShape(shapeRadius),
                ambientColor = Color(0x1A000000),
                spotColor = Color(0x20000000)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(),
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(shapeRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SevaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    containerColor: Color = if (isPrimary) SevaGreenPrimary else MaterialTheme.colorScheme.surface,
    contentColor: Color = if (isPrimary) Color.White else SevaGreenPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "btn_scale")

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 48.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPrimary) 3.dp else 1.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        border = if (!isPrimary) BorderStroke(1.5.dp, SevaGreenPrimary) else null,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    customColor: Color? = null
) {
    val (bg, fg) = when (status.lowercase()) {
        "active", "সক্রিয়", "ongoing", "চলমান", "অনুমোদিত" -> Color(0xFFE8F5E9) to Color(0xFF1B5E20)
        "জরুরি", "urgent" -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        "fulfilled", "completed", "সম্পন্ন" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "অপেক্ষমাণ", "pending" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "inactive", "নিষ্ক্রিয়", "বাতিল" -> Color(0xFFF1F5F9) to Color(0xFF475569)
        else -> (customColor?.copy(alpha = 0.15f) ?: Color(0xFFE2E8F0)) to (customColor ?: Color(0xFF334155))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(0.5.dp, fg.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = status,
            color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
