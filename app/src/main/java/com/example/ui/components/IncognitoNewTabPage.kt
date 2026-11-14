package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuDarkAubergine
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuIncognitoSurface
import com.example.ui.theme.UbuntuLavender
import com.example.ui.theme.UbuntuLightAubergine
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.viewmodel.BrowserViewModel

data class IncognitoQuickLink(
    val name: String,
    val url: String,
    val iconLetter: String,
    val accentColor: Color
)

@Composable
fun IncognitoNewTabPage(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var queryText by remember { mutableStateOf("") }
    val selectedSearchEngine by viewModel.selectedSearchEngine.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()

    val privateQuickLinks = remember {
        listOf(
            IncognitoQuickLink("DuckDuckGo", "https://duckduckgo.com", "D", Color(0xFFDE5833)),
            IncognitoQuickLink("Wikipedia", "https://en.wikipedia.org", "W", Color(0xFF6B7280)),
            IncognitoQuickLink("EFF Privacy", "https://www.eff.org", "E", UbuntuLavender),
            IncognitoQuickLink("Tor Project", "https://www.torproject.org", "T", Color(0xFF7D4698)),
            IncognitoQuickLink("Ubuntu Security", "https://ubuntu.com/security", "U", UbuntuOrange)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        UbuntuDeepAubergine,
                        UbuntuDarkAubergine,
                        UbuntuDarkBackground
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Ubuntu Incognito Hero Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(UbuntuAubergine, UbuntuDarkAubergine)
                    ),
                    CircleShape
                )
                .border(2.dp, UbuntuLavender.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Incognito Mode",
                tint = UbuntuLavender,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title and Ubuntu Aubergine Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ubuntu Incognito",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = UbuntuAubergine,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Private",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = UbuntuLavender,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Swordfish keeps your browsing private. Nothing stays behind.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Private Search Field
        OutlinedTextField(
            value = queryText,
            onValueChange = { queryText = it },
            placeholder = {
                Text(
                    text = "Search privately with ${selectedSearchEngine.displayName}...",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = UbuntuLavender
                )
            },
            trailingIcon = {
                if (queryText.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clickable { queryText = "" }
                            .padding(8.dp)
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = UbuntuDeepAubergine,
                unfocusedContainerColor = UbuntuDarkAubergine,
                focusedBorderColor = UbuntuLavender,
                unfocusedBorderColor = UbuntuAubergine.copy(alpha = 0.6f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = UbuntuLavender
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                if (queryText.isNotBlank()) {
                    focusManager.clearFocus()
                    viewModel.loadUrl(queryText)
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("incognito_search_input")
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Privacy Guarantee Card (Ubuntu Terminal styled)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UbuntuIncognitoSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuAubergine),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = UbuntuLavender,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Session Protections Active",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrivacyFeatureRow(
                    icon = Icons.Default.History,
                    title = "No History Tracking",
                    description = "Pages you visit will not be saved to your history database or bookmarks unless explicitly added."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyFeatureRow(
                    icon = Icons.Default.Cookie,
                    title = "Session Cookies Cleared on Close",
                    description = "All cookies, web storage, and temporary session caches are destroyed immediately when you close this tab."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyFeatureRow(
                    icon = Icons.Default.Lock,
                    title = "No Passwords or Form Autofill",
                    description = "Credentials, input fields, and search queries are never cached or remembered by the browser."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyFeatureRow(
                    icon = Icons.Default.Shield,
                    title = "AdBlocker & Shields Remain Active",
                    description = "Ad scripts, telemetry beacons, and YouTube video ads continue to be blocked in private mode."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ubuntu Terminal Console Snippet
        Surface(
            color = Color(0xFF14000D),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF330022)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "user@ubuntu-swordfish:~$ incognito --status",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = UbuntuLavender
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "[OK] Isolated sandboxed session active.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = UbuntuSuccessGreen
                )
                Text(
                    text = "[OK] Session cookies will be purged on tab close.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy Quick Links
        Text(
            text = "Privacy Starting Points",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(privateQuickLinks) { item ->
                Surface(
                    onClick = { viewModel.loadUrl(item.url) },
                    shape = RoundedCornerShape(12.dp),
                    color = UbuntuDeepAubergine,
                    border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuAubergine),
                    modifier = Modifier.testTag("incognito_quick_${item.name.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .background(item.accentColor.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Text(
                                text = item.iconLetter,
                                color = item.accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons: Close Private Tab & Open Regular Tab
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.closeTab(activeTabId) },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuAubergine),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("close_incognito_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Close Private Tab", color = Color.White, fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { viewModel.openNewTab(isIncognito = false) },
                border = androidx.compose.foundation.BorderStroke(1.dp, UbuntuOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_regular_tab_shortcut")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Regular Tab", color = UbuntuOrange, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PrivacyFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .background(UbuntuAubergine.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UbuntuLavender,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 15.sp
            )
        }
    }
}
