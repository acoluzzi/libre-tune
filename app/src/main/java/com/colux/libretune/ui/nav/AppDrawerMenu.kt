package com.colux.libretune.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.colux.libretune.R


@Composable
fun AppDrawerMenu(navController: NavController, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- 1. Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Replace with your actual logo
                Image(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("LibreTune", style = MaterialTheme.typography.titleLarge)
            }

            // --- 2. Separator ---
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Top Menu Items ---
            NavigationDrawerItem(
                label = { Text("History") },
                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                selected = false,
                onClick = { navController.navigate("history"); closeDrawer() }
            )
            NavigationDrawerItem(
                label = { Text("Statistics") },
                icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
                selected = false,
                onClick = { navController.navigate("statistics"); closeDrawer() }
            )

            // --- 3. Bottom Menu Items ---
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                selected = false,
                onClick = { /* TODO: navController.navigate("settings") */; closeDrawer() }
            )

            // Add padding at the very bottom for the system navigation bar
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}