package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultCredential
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.ActiveSheet
import com.example.viewmodel.BrowserViewModel

/**
 * Floating Ubuntu Yaru autofill banner displayed above the bottom toolbar when a login form
 * is detected in the active web page and matching credentials exist in the encrypted vault.
 */
@Composable
fun AutofillPromptBanner(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val detectedForm by viewModel.detectedLoginForm.collectAsState()
    val form = detectedForm ?: return

    val credentials = form.matchedCredentials
    if (credentials.isEmpty()) return

    var selectedCredential by remember(form) { mutableStateOf(credentials.first()) }
    var showAccountMenu by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
            border = BorderStroke(1.5.dp, UbuntuOrange),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("autofill_prompt_banner")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                UbuntuDeepAubergine.copy(alpha = 0.85f),
                                UbuntuDarkSurface
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Key badge icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, UbuntuOrange, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Password Vault",
                            tint = UbuntuOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Autofill for ${form.domain}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = UbuntuOrange.copy(alpha = 0.25f),
                                border = BorderStroke(0.5.dp, UbuntuOrange)
                            ) {
                                Text(
                                    text = "AES-256",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = UbuntuOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Account selector or single account label
                        if (credentials.size > 1) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showAccountMenu = true }
                                    .padding(vertical = 2.dp)
                                    .testTag("autofill_account_selector")
                            ) {
                                Text(
                                    text = selectedCredential.username,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch account",
                                    tint = UbuntuWarmGrey,
                                    modifier = Modifier.size(18.dp)
                                )

                                DropdownMenu(
                                    expanded = showAccountMenu,
                                    onDismissRequest = { showAccountMenu = false },
                                    modifier = Modifier.background(UbuntuCardDark)
                                ) {
                                    credentials.forEach { cred ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = cred.username,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (cred.title.isNotBlank()) {
                                                        Text(
                                                            text = cred.title,
                                                            color = UbuntuWarmGrey,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedCredential = cred
                                                showAccountMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = selectedCredential.username,
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Open Full Vault Icon Button
                    IconButton(
                        onClick = { viewModel.openSheet(ActiveSheet.PASSWORD_VAULT) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("autofill_open_vault_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Open Password Vault",
                            tint = UbuntuWarmGrey,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Dismiss Button
                    IconButton(
                        onClick = { viewModel.dismissAutofillPrompt() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("autofill_dismiss_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Autofill",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // One-tap Autofill Action Button
                Button(
                    onClick = {
                        viewModel.autofillCredential(selectedCredential)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UbuntuOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("autofill_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fill as ${selectedCredential.username}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
