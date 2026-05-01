from django.db import transaction
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import (
    LikedSong,
    Playlist,
    PlaylistSong,
    SavedAlbum,
    SavedArtist,
    Song,
)
from .serializers import (
    LikedSongSerializer,
    PlaylistSerializer,
    SavedAlbumSerializer,
    SavedArtistSerializer,
)


def _upsert_song(user, payload):
    song, _ = Song.objects.update_or_create(
        user=user,
        remote_id=payload["remote_id"],
        defaults={
            "title": payload.get("title", ""),
            "artist_name": payload.get("artist_name", ""),
            "album_name": payload.get("album_name", ""),
            "duration_ms": payload.get("duration_ms", 0),
            "thumbnail_url": payload.get("thumbnail_url", ""),
        },
    )
    return song


class LikedSongsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        liked = LikedSong.objects.filter(user=request.user).select_related("song")
        return Response({"items": LikedSongSerializer(liked, many=True).data})

    @transaction.atomic
    def put(self, request):
        items = request.data.get("items", [])
        LikedSong.objects.filter(user=request.user).delete()
        created = []
        for index, item in enumerate(items):
            song = _upsert_song(request.user, item["song"])
            created.append(
                LikedSong(
                    user=request.user,
                    song=song,
                    position=item.get("position", index),
                )
            )
        LikedSong.objects.bulk_create(created)
        return Response(status=status.HTTP_204_NO_CONTENT)


class PlaylistsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        playlists = (
            Playlist.objects.filter(user=request.user)
            .prefetch_related("entries__song")
            .order_by("id")
        )
        return Response({"items": PlaylistSerializer(playlists, many=True).data})

    @transaction.atomic
    def put(self, request):
        items = request.data.get("items", [])
        Playlist.objects.filter(user=request.user).delete()
        for item in items:
            playlist = Playlist.objects.create(
                user=request.user,
                remote_id=item.get("remote_id", ""),
                name=item.get("name", ""),
                description=item.get("description", ""),
                thumbnail_url=item.get("thumbnail_url", ""),
            )
            entries = []
            for index, song_entry in enumerate(item.get("songs", [])):
                song = _upsert_song(request.user, song_entry["song"])
                entries.append(
                    PlaylistSong(
                        playlist=playlist,
                        song=song,
                        position=song_entry.get("position", index),
                    )
                )
            PlaylistSong.objects.bulk_create(entries)
        return Response(status=status.HTTP_204_NO_CONTENT)


class _SavedCollectionView(APIView):
    permission_classes = [IsAuthenticated]
    model = None
    serializer_class = None

    def get(self, request):
        items = self.model.objects.filter(user=request.user)
        return Response({"items": self.serializer_class(items, many=True).data})

    @transaction.atomic
    def put(self, request):
        items = request.data.get("items", [])
        self.model.objects.filter(user=request.user).delete()
        objects = []
        for index, item in enumerate(items):
            objects.append(
                self.model(
                    user=request.user,
                    remote_id=item["remote_id"],
                    name=item.get("name", ""),
                    thumbnail_url=item.get("thumbnail_url", ""),
                    position=item.get("position", index),
                    **self.extra_fields(item),
                )
            )
        self.model.objects.bulk_create(objects)
        return Response(status=status.HTTP_204_NO_CONTENT)

    def extra_fields(self, item):
        return {}


class SavedAlbumsView(_SavedCollectionView):
    model = SavedAlbum
    serializer_class = SavedAlbumSerializer

    def extra_fields(self, item):
        return {"artist_name": item.get("artist_name", "")}


class SavedArtistsView(_SavedCollectionView):
    model = SavedArtist
    serializer_class = SavedArtistSerializer
