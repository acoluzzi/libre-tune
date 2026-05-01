from django.urls import path

from .views import (
    LikedSongsView,
    PlaylistsView,
    SavedAlbumsView,
    SavedArtistsView,
)

urlpatterns = [
    path("liked-songs/", LikedSongsView.as_view(), name="liked-songs"),
    path("playlists/", PlaylistsView.as_view(), name="playlists"),
    path("saved-albums/", SavedAlbumsView.as_view(), name="saved-albums"),
    path("saved-artists/", SavedArtistsView.as_view(), name="saved-artists"),
]
