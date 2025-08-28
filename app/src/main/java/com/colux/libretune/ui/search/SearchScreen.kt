import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SearchViewModel

@Composable
fun SearchScreen(playerViewModel: PlayerViewModel, navController: NavHostController) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Field
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                searchViewModel.search(it)
            },
            label = { Text("Search for a song...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Results List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { result ->
                    // Use a 'when' statement to render the correct UI for each type
                    when (result) {
                        is SearchResult.SongResult -> {
                            SongItem(
                                song = result.song,
                                onClick = {
                                    playerViewModel.playSongById(result.song.id)
                                }
                            )
                        }

                        is SearchResult.ArtistResult -> {
                            ArtistSearchResultItem(
                                artist = result.artist,
                                onClick = { navController.navigate(Screen.Artist.createRoute(result.artist.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ArtistSearchResultItem(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.imageUrl,
            contentDescription = artist.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape) // Artists often have circular profile images
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = artist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
    }
}