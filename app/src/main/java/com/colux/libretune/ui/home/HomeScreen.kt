package com.colux.libretune.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.colux.libretune.data.model.HomeFeedItem
import com.colux.libretune.ui.nav.AppDrawerMenu
import com.colux.libretune.ui.player.PlayerViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playerViewModel: PlayerViewModel,
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {

    val greeting = remember { getGreeting() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Your existing AppDrawer composable
            AppDrawerMenu(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        // 2. The HomeScreen has its own Scaffold.
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(greeting) },
                    // 3. The burger icon to open the drawer.
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            HomeScreenContent(
                innerPadding = innerPadding,
                uiState = uiState,
                navController = navController
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    innerPadding: PaddingValues,
    navController: NavController
) {
    when (uiState) {
        is HomeUiState.Loading -> {
            HomeScreenSkeleton(innerPadding)
        }

        is HomeUiState.Empty -> {
            HomeScreenEmpty(innerPadding)
        }

        is HomeUiState.Success -> {
            HomeScreenSuccess(innerPadding, uiState.items, navController = navController)
        }
    }
}


@Composable
fun HomeScreenSuccess(
    innerPadding: PaddingValues,
    items: List<HomeFeedItem>,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {

        items(items) { item ->
            HomeSuggestionItemView(
                item = item,
                navController = navController
            )
        }


        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@Composable
fun HomeScreenEmpty(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        item {
            Text(
                "Start searching for you favorite music to populate the home.",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}


private fun getGreeting(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}


