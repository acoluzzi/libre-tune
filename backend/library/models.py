from django.conf import settings
from django.db import models


class Song(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="songs",
    )
    remote_id = models.CharField(max_length=128)
    title = models.CharField(max_length=512)
    artist_name = models.CharField(max_length=512, blank=True, default="")
    album_name = models.CharField(max_length=512, blank=True, default="")
    duration_ms = models.PositiveIntegerField(default=0)
    thumbnail_url = models.URLField(blank=True, default="", max_length=1024)

    class Meta:
        unique_together = ("user", "remote_id")


class LikedSong(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="liked_songs",
    )
    song = models.ForeignKey(Song, on_delete=models.CASCADE, related_name="+")
    liked_at = models.DateTimeField(auto_now_add=True)
    position = models.PositiveIntegerField(default=0)

    class Meta:
        ordering = ["position", "id"]
        unique_together = ("user", "song")


class Playlist(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="playlists",
    )
    remote_id = models.CharField(max_length=128, blank=True, default="")
    name = models.CharField(max_length=512)
    description = models.TextField(blank=True, default="")
    thumbnail_url = models.URLField(blank=True, default="", max_length=1024)
    updated_at = models.DateTimeField(auto_now=True)


class PlaylistSong(models.Model):
    playlist = models.ForeignKey(
        Playlist, on_delete=models.CASCADE, related_name="entries"
    )
    song = models.ForeignKey(Song, on_delete=models.CASCADE, related_name="+")
    position = models.PositiveIntegerField(default=0)

    class Meta:
        ordering = ["position", "id"]
        unique_together = ("playlist", "song")


class SavedAlbum(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="saved_albums",
    )
    remote_id = models.CharField(max_length=128)
    name = models.CharField(max_length=512)
    artist_name = models.CharField(max_length=512, blank=True, default="")
    thumbnail_url = models.URLField(blank=True, default="", max_length=1024)
    saved_at = models.DateTimeField(auto_now_add=True)
    position = models.PositiveIntegerField(default=0)

    class Meta:
        ordering = ["position", "id"]
        unique_together = ("user", "remote_id")


class SavedArtist(models.Model):
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="saved_artists",
    )
    remote_id = models.CharField(max_length=128)
    name = models.CharField(max_length=512)
    thumbnail_url = models.URLField(blank=True, default="", max_length=1024)
    saved_at = models.DateTimeField(auto_now_add=True)
    position = models.PositiveIntegerField(default=0)

    class Meta:
        ordering = ["position", "id"]
        unique_together = ("user", "remote_id")


class CollectionMeta(models.Model):
    """Tracks the most recent client-supplied modification timestamp for
    each (user, collection) pair so the mobile app can perform a
    last-writer-wins comparison on every sync run.
    """

    LIKED_SONGS = "liked_songs"
    PLAYLISTS = "playlists"
    SAVED_ALBUMS = "saved_albums"
    SAVED_ARTISTS = "saved_artists"
    COLLECTION_CHOICES = (
        (LIKED_SONGS, "Liked songs"),
        (PLAYLISTS, "Playlists"),
        (SAVED_ALBUMS, "Saved albums"),
        (SAVED_ARTISTS, "Saved artists"),
    )

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name="collection_meta",
    )
    collection = models.CharField(max_length=32, choices=COLLECTION_CHOICES)
    last_updated_ms = models.BigIntegerField()

    class Meta:
        unique_together = ("user", "collection")
