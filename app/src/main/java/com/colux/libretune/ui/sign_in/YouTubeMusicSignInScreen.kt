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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

private const val SIGN_IN_URL =
    "https://accounts.google.com/ServiceLogin?continue=https://music.youtube.com/"
private const val MUSIC_HOST = "music.youtube.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouTubeMusicSignInScreen(
    navController: NavController,
    viewModel: YouTubeMusicSignInViewModel = hiltViewModel(),
) {
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val cookieManager = remember { CookieManager.getInstance() }

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
        if (isSignedIn) {
            SignedInPanel(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                onSignOut = viewModel::signOut
            )
        } else {
            SignInWebView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                cookieManager = cookieManager,
                onCapture = { cookies -> viewModel.captureCookies(cookies) }
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
            androidx.compose.material3.TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SignInWebView(
    modifier: Modifier,
    cookieManager: CookieManager,
    onCapture: (String) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            cookieManager.setAcceptCookie(true)

            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (url != null && url.contains(MUSIC_HOST)) {
                            // Drain cookies for music.youtube.com — they're
                            // only populated once Google bounces us back here.
                            val cookies = cookieManager.getCookie("https://$MUSIC_HOST") ?: return
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
}

