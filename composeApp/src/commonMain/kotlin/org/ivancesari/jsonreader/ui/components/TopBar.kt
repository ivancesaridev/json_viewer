package org.ivancesari.jsonreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.app_name
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    val spacing = JsonReaderTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    // Thin blue divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    )
}
