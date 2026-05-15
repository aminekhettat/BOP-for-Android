package org.blindsystems.bop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.blindsystems.bop.R
import org.blindsystems.bop.infra.I18n

/**
 * About Dialog showing credits and information.
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = I18n["about_title"],
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Culture Musique Logo & Info ────────────────────────────────
                Image(
                    painter = painterResource(id = R.mipmap.logo_culture_musique),
                    contentDescription = "Logo Culture Musique",
                    modifier = Modifier.size(100.dp)
                )
                
                Text(
                    text = I18n["developed_for_culture_musique"],
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                LinkText(
                    text = "www.culture-musique.org",
                    url = "https://www.culture-musique.org"
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ── Blind Systems Info ────────────────────────────────────────
                Text(
                    text = I18n["by_blind_systems"],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LinkText(
                    text = "www.blindsystems.org",
                    url = "https://www.blindsystems.org"
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ── Legal & GitHub ────────────────────────────────────────────
                Text(
                    text = I18n["license_apache"],
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = I18n["free_access"],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = I18n["contribute_invite"],
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                LinkText(
                    text = "GitHub: BOP for Android",
                    url = "https://github.com/aminekhettat/BOP-for-Android"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(I18n["confirm"])
            }
        }
    )
}

@Composable
fun LinkText(text: String, url: String) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append(text)
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        }
    )
}
