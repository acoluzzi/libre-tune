package com.colux.libretune.ui.sign_in

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

private const val SIGN_IN_URL =
    "https://accounts.google.com/ServiceLogin?continue=https://music.youtube.com/"
private const val MUSIC_HOST = "music.youtube.com"

// A plain desktop Chrome UA — crucially without the Android WebView "; wv"
// marker that Google uses to flag embedded browsers as insecure.
private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeMusicSignInScreen(
    navController: NavController,
    viewModel: YouTubeMusicSignInViewModel = hiltViewModel(),
) {
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val cookieManager = remember { CookieManager.getInstance() }
    var manualEntry by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignedIn) "Signed in" else "Sign in to YouTube Music") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(inner)

        when {
            isSignedIn -> SignedInPanel(
                modifier = contentModifier,
                onSignOut = viewModel::signOut
            )

            manualEntry -> ManualCookieEntry(
                modifier = contentModifier,
                onSubmit = { viewModel.captureCookies(it) },
                onUseWebView = { manualEntry = false }
            )

            else -> SignInWebView(
                modifier = contentModifier,
                cookieManager = cookieManager,
                onCapture = { cookies -> viewModel.captureCookies(cookies) },
                onUseManualEntry = { manualEntry = true }
            )
        }
    }
}

@Composable
private fun SignedInPanel(
    modifier: Modifier,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "You're signed in to YouTube Music.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            "Synced playlists you create from here will mirror to your YT Music account.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.size(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
@Composable
private fun SignInWebView(
    modifier: Modifier,
    cookieManager: CookieManager,
    onCapture: (String) -> Unit,
    onUseManualEntry: () -> Unit,
) {
    Column(modifier = modifier) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                cookieManager.setAcceptCookie(true)

                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString = DESKTOP_USER_AGENT

                    // The header that gives away an embedded WebView. Removing
                    // it for all origins is what lets Google's login succeed.
                    if (WebViewFeature.isFeatureSupported(
                            WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST
                        )
                    ) {
                        WebSettingsCompat.setRequestedWithHeaderOriginAllowList(
                            settings,
                            emptySet()
                        )
                    }

                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            if (url != null && url.contains(MUSIC_HOST)) {
                                // Cookies for music.youtube.com only appear once
                                // Google bounces us back after a successful login.
                                val cookies =
                                    cookieManager.getCookie("https://$MUSIC_HOST") ?: return
                                if (cookies.contains("SAPISID") ||
                                    cookies.contains("__Secure-3PAPISID")
                                ) {
                                    onCapture(cookies)
                                }
                            }
                        }
                    }

                    loadUrl(SIGN_IN_URL)
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onUseManualEntry) {
                Text("Sign-in blocked? Paste cookies manually")
            }
        }
    }
}

@Composable
private fun ManualCookieEntry(
    modifier: Modifier,
    onSubmit: (String) -> Unit,
    onUseWebView: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Paste your YouTube Music cookie", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            "On a desktop browser, sign in at music.youtube.com, open the developer " +
                "tools (F12) → Network tab, click any request to music.youtube.com, " +
                "and copy the full value of the \"cookie\" request header. It must " +
                "contain SAPISID.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.size(16.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Cookie header") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            maxLines = 8
        )
        Spacer(modifier = Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onUseWebView) {
                Text("Use sign-in page", overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { onSubmit(text.trim()) },
                enabled = text.contains("SAPISID")
            ) {
                Text("Save")
            }
        }
    }
}
