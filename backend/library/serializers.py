from rest_framework import serializers

from .models import (
    LikedSong,
    Playlist,
    PlaylistSong,
    SavedAlbum,
    SavedArtist,
    Song,
)


class SongSerializer(serializers.ModelSerializer):
    class Meta:
        model = Song
        fields = [
            "remote_id",
            "title",
            "artist_name",
            "album_name",
            "duration_ms",
            "thumbnail_url",
        ]


class LikedSongSerializer(serializers.ModelSerializer):
    song = SongSerializer()

    class Meta:
        model = LikedSong
        fields = ["song", "position"]


class PlaylistSongSerializer(serializers.ModelSerializer):
    song = SongSerializer()

    class Meta:
        model = PlaylistSong
        fields = ["song", "position"]


class PlaylistSerializer(serializers.ModelSerializer):
    songs = PlaylistSongSerializer(source="entries", many=True)

    class Meta:
        model = Playlist
        fields = [
            "remote_id",
            "name",
            "description",
            "thumbnail_url",
            "songs",
        ]


class SavedAlbumSerializer(serializers.ModelSerializer):
    class Meta:
        model = SavedAlbum
        fields = [
            "remote_id",
            "name",
            "artist_name",
            "thumbnail_url",
            "position",
        ]


class SavedArtistSerializer(serializers.ModelSerializer):
    class Meta:
        model = SavedArtist
        fields = [
            "remote_id",
            "name",
            "thumbnail_url",
            "position",
        ]
