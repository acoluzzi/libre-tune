from django.contrib import admin

from .models import LikedSong, Playlist, PlaylistSong, SavedAlbum, SavedArtist, Song

admin.site.register(Song)
admin.site.register(LikedSong)
admin.site.register(Playlist)
admin.site.register(PlaylistSong)
admin.site.register(SavedAlbum)
admin.site.register(SavedArtist)
