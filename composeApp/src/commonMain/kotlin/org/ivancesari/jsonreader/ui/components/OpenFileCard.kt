package org.ivancesari.jsonreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ivancesari.jsonreader.resources.Res
import org.ivancesari.jsonreader.resources.ic_folder
import org.ivancesari.jsonreader.resources.open_file_description
import org.ivancesari.jsonreader.resources.open_file_title
import org.ivancesari.jsonreader.theme.JsonReaderTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun OpenFileCard(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = JsonReaderTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(Res.string.open_file_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        Text(
            text = stringResource(Res.string.open_file_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(spacing.lg))

        Button(
            onClick = onBrowseClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_folder),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Browse Storage",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
