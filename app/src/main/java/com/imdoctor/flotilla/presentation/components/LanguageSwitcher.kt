package com.imdoctor.flotilla.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.imdoctor.flotilla.R

/**
 * Компактный переключатель языка EN | RU
 *
 * @param currentLanguage Текущий выбранный язык ("en" или "ru")
 * @param onLanguageChange Callback при смене языка
 * @param modifier Modifier для кастомизации
 */
@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // English Button
        LanguageButton(
            text = stringResource(R.string.language_english),
            isSelected = currentLanguage == "en",
            onClick = { onLanguageChange("en") }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Vertical Divider
        Text(
            text = "|",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Russian Button
        LanguageButton(
            text = stringResource(R.string.language_russian),
            isSelected = currentLanguage == "ru",
            onClick = { onLanguageChange("ru") }
        )
    }
}

/**
 * Кнопка для выбора языка
 */
@Composable
private fun LanguageButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        modifier = Modifier.size(width = 48.dp, height = 36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
