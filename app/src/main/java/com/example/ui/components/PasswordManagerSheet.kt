package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PasswordStrength
import com.example.data.model.PasswordStrengthLevel
import com.example.data.model.VaultCredential
import com.example.ui.theme.UbuntuAubergine
import com.example.ui.theme.UbuntuCardDark
import com.example.ui.theme.UbuntuDarkBackground
import com.example.ui.theme.UbuntuDarkSurface
import com.example.ui.theme.UbuntuDeepAubergine
import com.example.ui.theme.UbuntuErrorRed
import com.example.ui.theme.UbuntuOrange
import com.example.ui.theme.UbuntuSuccessGreen
import com.example.ui.theme.UbuntuWarmGrey
import com.example.ui.theme.UbuntuWarningYellow
import com.example.viewmodel.BrowserViewModel

/**
 * Full Ubuntu Yaru modal bottom sheet for managing stored login credentials,
 * searching, adding/editing passwords, and generating strong encryption passwords.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val credentials by viewModel.filteredVaultCredentials.collectAsState()
    val allCredentials by viewModel.vaultCredentials.collectAsState()
    val searchQuery by viewModel.vaultSearchQuery.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingCredential by remember { mutableStateOf<VaultCredential?>(null) }
    var showGeneratorDialog by remember { mutableStateOf(false) }

    // Map to track password visibility for individual credentials in the list
    val passwordVisibilityMap = remember { mutableStateMapOf<String, Boolean>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = UbuntuDarkBackground,
        dragHandle = null,
        modifier = Modifier.fillMaxSize().testTag("password_manager_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(UbuntuOrange.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .border(1.dp, UbuntuOrange, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = "Vault",
                        tint = UbuntuOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Password Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = UbuntuSuccessGreen.copy(alpha = 0.2f),
                            border = BorderStroke(0.5.dp, UbuntuSuccessGreen)
                        ) {
                            Text(
                                text = "AES-256 SIV/GCM",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = UbuntuSuccessGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Encrypted local keystore • ${allCredentials.size} saved logins",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp).testTag("close_password_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setVaultSearchQuery(it) },
                placeholder = { Text("Search by domain, title, username...", color = UbuntuWarmGrey, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = UbuntuWarmGrey, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setVaultSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = UbuntuWarmGrey, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UbuntuOrange,
                    unfocusedBorderColor = Color(0xFF3F3F3F),
                    focusedContainerColor = UbuntuDarkSurface,
                    unfocusedContainerColor = UbuntuDarkSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_search_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar: Add Login + Generate Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        editingCredential = null
                        showAddEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("vault_add_login_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Login", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { showGeneratorDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UbuntuOrange),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("vault_open_generator_button")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generator", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Credentials List or Empty State
            if (credentials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                        border = BorderStroke(1.dp, Color(0xFF3A3A3A)),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(UbuntuAubergine.copy(alpha = 0.3f), CircleShape)
                                    .border(1.dp, UbuntuOrange.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = UbuntuOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No credentials match '$searchQuery'" else "Vault is Empty",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Try searching for another domain or username"
                                else
                                    "Logins you save while browsing or create manually will appear here, encrypted with Android Keystore.",
                                style = MaterialTheme.typography.bodySmall,
                                color = UbuntuWarmGrey,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    editingCredential = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_vault_add_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create First Login", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).testTag("vault_credentials_list")
                ) {
                    items(credentials, key = { it.id }) { cred ->
                        val isPassVisible = passwordVisibilityMap[cred.id] == true

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = UbuntuDarkSurface),
                            border = BorderStroke(1.dp, Color(0xFF383838)),
                            modifier = Modifier.fillMaxWidth().testTag("credential_item_${cred.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Domain and Title Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Domain initial avatar
                                    val domainInitial = cred.domain.firstOrNull()?.uppercase() ?: "W"
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(UbuntuOrange, UbuntuDeepAubergine)
                                                ),
                                                CircleShape
                                            )
                                    ) {
                                        Text(
                                            text = domainInitial,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cred.title.ifBlank { cred.domain },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = cred.domain,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = UbuntuWarmGrey,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Edit button
                                    IconButton(
                                        onClick = {
                                            editingCredential = cred
                                            showAddEditDialog = true
                                        },
                                        modifier = Modifier.size(36.dp).testTag("edit_cred_${cred.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Credential",
                                            tint = UbuntuWarmGrey,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete button
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteVaultCredential(cred.id)
                                            Toast.makeText(context, "Deleted login for ${cred.domain}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(36.dp).testTag("delete_cred_${cred.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Credential",
                                            tint = UbuntuErrorRed.copy(alpha = 0.85f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Username Row with Copy Action
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UbuntuCardDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "User:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UbuntuWarmGrey,
                                            modifier = Modifier.width(45.dp)
                                        )
                                        Text(
                                            text = cred.username,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(cred.username))
                                                Toast.makeText(context, "Username copied to clipboard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp).testTag("copy_user_${cred.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Username",
                                                tint = UbuntuOrange,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Password Row with Copy & Show/Hide Actions
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = UbuntuCardDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Pass:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UbuntuWarmGrey,
                                            modifier = Modifier.width(45.dp)
                                        )
                                        Text(
                                            text = if (isPassVisible) cred.password else "•".repeat(cred.password.length.coerceAtMost(16)),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = UbuntuOrange,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        // Visibility toggle
                                        IconButton(
                                            onClick = {
                                                passwordVisibilityMap[cred.id] = !isPassVisible
                                            },
                                            modifier = Modifier.size(32.dp).testTag("toggle_pass_visibility_${cred.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isPassVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Visibility",
                                                tint = UbuntuWarmGrey,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        // Copy password
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(cred.password))
                                                Toast.makeText(context, "Password copied securely!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp).testTag("copy_pass_${cred.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Password",
                                                tint = UbuntuOrange,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                if (cred.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Notes: ${cred.notes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UbuntuWarmGrey,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Credential Dialog
    if (showAddEditDialog) {
        AddEditCredentialDialog(
            initialCredential = editingCredential,
            viewModel = viewModel,
            onDismiss = { showAddEditDialog = false },
            onSaved = { cred ->
                viewModel.saveVaultCredential(cred)
                showAddEditDialog = false
                Toast.makeText(context, "Saved login for ${cred.domain}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dedicated Password Generator Dialog
    if (showGeneratorDialog) {
        PasswordGeneratorModal(
            viewModel = viewModel,
            onDismiss = { showGeneratorDialog = false },
            onUsePassword = { generatedPass ->
                showGeneratorDialog = false
                editingCredential = VaultCredential(
                    domain = "",
                    title = "",
                    username = "",
                    password = generatedPass
                )
                showAddEditDialog = true
            }
        )
    }
}

/**
 * Add / Edit Login Credential Modal with inline live password generator & strength meter.
 */
@Composable
fun AddEditCredentialDialog(
    initialCredential: VaultCredential?,
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit,
    onSaved: (VaultCredential) -> Unit
) {
    var domain by remember { mutableStateOf(initialCredential?.domain ?: "") }
    var title by remember { mutableStateOf(initialCredential?.title ?: "") }
    var username by remember { mutableStateOf(initialCredential?.username ?: "") }
    var password by remember { mutableStateOf(initialCredential?.password ?: "") }
    var notes by remember { mutableStateOf(initialCredential?.notes ?: "") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val strength = remember(password) { viewModel.calculatePasswordStrength(password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialCredential != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialCredential != null) "Edit Login" else "Add New Login",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Domain / Website
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Website or Domain (e.g. github.com)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = UbuntuOrange,
                        unfocusedLabelColor = UbuntuWarmGrey
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_domain_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Label / Title (e.g. Personal Account)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = UbuntuOrange,
                        unfocusedLabelColor = UbuntuWarmGrey
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_title_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username / Email
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = UbuntuOrange,
                        unfocusedLabelColor = UbuntuWarmGrey
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_username_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password with inline generator icon & visibility toggle
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Generator Icon Button
                            IconButton(
                                onClick = {
                                    password = viewModel.generatePassword(16, includeUppercase = true, includeNumbers = true, includeSymbols = true)
                                    isPasswordVisible = true
                                },
                                modifier = Modifier.size(36.dp).testTag("dialog_inline_generate_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Generate Strong Password",
                                    tint = UbuntuOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Visibility Toggle
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(36.dp).testTag("dialog_toggle_pass_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = UbuntuWarmGrey,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = UbuntuOrange,
                        unfocusedLabelColor = UbuntuWarmGrey
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_password_field")
                )

                // Password Strength Meter
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val barColor = when (strength.level) {
                        PasswordStrengthLevel.VERY_WEAK -> UbuntuErrorRed
                        PasswordStrengthLevel.WEAK -> UbuntuErrorRed
                        PasswordStrengthLevel.FAIR -> UbuntuWarningYellow
                        PasswordStrengthLevel.STRONG -> UbuntuOrange
                        PasswordStrengthLevel.VERY_STRONG -> UbuntuSuccessGreen
                    }
                    LinearProgressIndicator(
                        progress = { strength.percent },
                        color = barColor,
                        trackColor = Color(0xFF3A3A3A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strength.level.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                        Text(
                            text = "${(strength.percent * 100).toInt()}% Entropy",
                            style = MaterialTheme.typography.labelSmall,
                            color = UbuntuWarmGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UbuntuOrange,
                        unfocusedBorderColor = Color(0xFF3F3F3F),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = UbuntuOrange,
                        unfocusedLabelColor = UbuntuWarmGrey
                    ),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_notes_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (domain.isBlank() || username.isBlank() || password.isBlank()) return@Button
                    val finalCred = (initialCredential ?: VaultCredential(
                        domain = domain,
                        title = title.ifBlank { domain },
                        username = username,
                        password = password,
                        notes = notes
                    )).copy(
                        domain = domain,
                        title = title.ifBlank { domain },
                        username = username,
                        password = password,
                        notes = notes,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaved(finalCred)
                },
                enabled = domain.isNotBlank() && username.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_save_credential_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_cancel_button")
            ) {
                Text("Cancel", color = UbuntuWarmGrey)
            }
        }
    )
}

/**
 * Dedicated Password Generator modal with custom length, symbol/number toggles,
 * and high entropy output.
 */
@Composable
fun PasswordGeneratorModal(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var length by remember { mutableIntStateOf(16) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember {
        mutableStateOf(
            viewModel.generatePassword(16, includeUppercase = true, includeNumbers = true, includeSymbols = true)
        )
    }

    fun regenerate() {
        generatedPassword = viewModel.generatePassword(
            length = length,
            includeUppercase = includeUppercase,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols
        )
    }

    val strength = remember(generatedPassword) { viewModel.calculatePasswordStrength(generatedPassword) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UbuntuDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    tint = UbuntuOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Password Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Cryptographically secure random entropy",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Generated Password Terminal Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = UbuntuCardDark,
                    border = BorderStroke(1.dp, UbuntuOrange.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = generatedPassword,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = UbuntuOrange,
                            modifier = Modifier.weight(1f).testTag("generated_password_text")
                        )

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedPassword))
                                Toast.makeText(context, "Generated password copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp).testTag("copy_generated_pass_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { regenerate() },
                            modifier = Modifier.size(36.dp).testTag("regenerate_password_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = UbuntuOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Entropy Bar
                val barColor = when (strength.level) {
                    PasswordStrengthLevel.VERY_WEAK, PasswordStrengthLevel.WEAK -> UbuntuErrorRed
                    PasswordStrengthLevel.FAIR -> UbuntuWarningYellow
                    PasswordStrengthLevel.STRONG -> UbuntuOrange
                    PasswordStrengthLevel.VERY_STRONG -> UbuntuSuccessGreen
                }
                LinearProgressIndicator(
                    progress = { strength.percent },
                    color = barColor,
                    trackColor = Color(0xFF3F3F3F),
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${strength.level.label} (${(strength.percent * 100).toInt()}% Entropy)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Text(
                        text = "$length chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = UbuntuWarmGrey
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Length Slider
                Text(
                    text = "Password Length: $length characters",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = length.toFloat(),
                    onValueChange = {
                        length = it.toInt()
                        regenerate()
                    },
                    valueRange = 8f..32f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = UbuntuOrange,
                        activeTrackColor = UbuntuOrange,
                        inactiveTrackColor = Color(0xFF3F3F3F)
                    ),
                    modifier = Modifier.testTag("generator_length_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Character Rule Checkboxes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            includeUppercase = !includeUppercase
                            regenerate()
                        }
                ) {
                    Checkbox(
                        checked = includeUppercase,
                        onCheckedChange = {
                            includeUppercase = it
                            regenerate()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UbuntuOrange,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Uppercase Letters (A-Z)", color = Color.White, fontSize = 13.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            includeNumbers = !includeNumbers
                            regenerate()
                        }
                ) {
                    Checkbox(
                        checked = includeNumbers,
                        onCheckedChange = {
                            includeNumbers = it
                            regenerate()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UbuntuOrange,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Numbers (0-9)", color = Color.White, fontSize = 13.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            includeSymbols = !includeSymbols
                            regenerate()
                        }
                ) {
                    Checkbox(
                        checked = includeSymbols,
                        onCheckedChange = {
                            includeSymbols = it
                            regenerate()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UbuntuOrange,
                            checkmarkColor = Color.White
                        )
                    )
                    Text("Symbols (!@#$%^&*)", color = Color.White, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUsePassword(generatedPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = UbuntuOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("generator_use_password_button")
            ) {
                Text("Use This Password", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UbuntuWarmGrey)
            }
        }
    )
}
