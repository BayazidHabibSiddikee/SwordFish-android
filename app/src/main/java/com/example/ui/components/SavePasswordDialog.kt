package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuWarmGrey
import com.example.viewmodel.BrowserViewModel

/**
 * Ubuntu Yaru confirmation dialog prompting the user to securely save credentials
 * in the local EncryptedSharedPreferences vault after submitting a login form.
 */
@Composable
fun SavePasswordDialog(
    viewModel: BrowserViewModel
) {
    val pending by viewModel.pendingSaveCredential.collectAsState()
    val cred = pending ?: return

    var isPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.dismissSavePasswordPrompt() },
        containerColor = UbuntuDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(UbuntuOrange.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, UbuntuOrange, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Save Password",
                        tint = UbuntuOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Save Password?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Encrypted with AES-256 local keystore",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Would you like Swordfish to securely save your login for ${cred.domain}? It will be auto-filled whenever you visit this site.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = UbuntuCardDark),
                    border = BorderStroke(1.dp, Color(0xFF3F3F3F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Site:",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuWarmGrey,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = cred.domain,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "User:",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuWarmGrey,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = cred.username,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Password:",
                                style = MaterialTheme.typography.labelSmall,
                                color = UbuntuWarmGrey,
                                modifier = Modifier.width(60.dp)
                            )
                            Text(
                                text = if (isPasswordVisible) cred.password else "•".repeat(cred.password.length.coerceAtMost(12)),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = UbuntuOrange,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = UbuntuWarmGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.savePendingCredential(neverForThisSite = false) },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_password_confirm_button")
            ) {
                Text("Save to Vault", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { viewModel.savePendingCredential(neverForThisSite = true) },
                    modifier = Modifier.testTag("save_password_never_button")
                ) {
                    Text("Never", color = UbuntuWarmGrey, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = { viewModel.dismissSavePasswordPrompt() },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF4A4A4A)),
                    modifier = Modifier.testTag("save_password_not_now_button")
                ) {
                    Text("Not Now", color = Color.White)
                }
            }
        }
    )
}
