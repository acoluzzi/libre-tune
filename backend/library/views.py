from django.db import transaction
from rest_framework import status
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import (
    CollectionMeta,
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


def _read_meta(user, collection):
    meta = CollectionMeta.objects.filter(user=user, collection=collection).first()
    return meta.last_updated_ms if meta else None


def _write_meta(user, collection, value):
    if value is None:
        return
    CollectionMeta.objects.update_or_create(
        user=user,
        collection=collection,
        defaults={"last_updated_ms": int(value)},
    )


def _required_timestamp(request):
    raw = request.data.get("last_updated_ms")
    if raw is None:
        raise ValueError("last_updated_ms is required.")
    try:
        return int(raw)
    except (TypeError, ValueError) as exc:
        raise ValueError("last_updated_ms must be an integer.") from exc


class _SyncView(APIView):
    permission_classes = [IsAuthenticated]
    collection: str = ""

    def _envelope(self, items_data):
        return {
            "last_updated_ms": _read_meta(self.request.user, self.collection),
            "items": items_data,
        }


class LikedSongsView(_SyncView):
    collection = CollectionMeta.LIKED_SONGS

    def get(self, request):
        liked = LikedSong.objects.filter(user=request.user).select_related("song")
        return Response(self._envelope(LikedSongSerializer(liked, many=True).data))

    @transaction.atomic
    def put(self, request):
        try:
            timestamp = _required_timestamp(request)
        except ValueError as exc:
            return Response({"detail": str(exc)}, status=status.HTTP_400_BAD_REQUEST)
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
        _write_meta(request.user, self.collection, timestamp)
        return Response({"last_updated_ms": timestamp})


class PlaylistsView(_SyncView):
    collection = CollectionMeta.PLAYLISTS

    def get(self, request):
        playlists = (
            Playlist.objects.filter(user=request.user)
            .prefetch_related("entries__song")
            .order_by("id")
        )
        return Response(self._envelope(PlaylistSerializer(playlists, many=True).data))

    @transaction.atomic
    def put(self, request):
        try:
            timestamp = _required_timestamp(request)
        except ValueError as exc:
            return Response({"detail": str(exc)}, status=status.HTTP_400_BAD_REQUEST)
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
        _write_meta(request.user, self.collection, timestamp)
        return Response({"last_updated_ms": timestamp})


class _SavedCollectionView(_SyncView):
    model = None
    serializer_class = None

    def get(self, request):
        items = self.model.objects.filter(user=request.user)
        return Response(self._envelope(self.serializer_class(items, many=True).data))

    @transaction.atomic
    def put(self, request):
        try:
            timestamp = _required_timestamp(request)
        except ValueError as exc:
            return Response({"detail": str(exc)}, status=status.HTTP_400_BAD_REQUEST)
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
        _write_meta(request.user, self.collection, timestamp)
        return Response({"last_updated_ms": timestamp})

    def extra_fields(self, item):
        return {}


class SavedAlbumsView(_SavedCollectionView):
    collection = CollectionMeta.SAVED_ALBUMS
    model = SavedAlbum
    serializer_class = SavedAlbumSerializer

    def extra_fields(self, item):
        return {"artist_name": item.get("artist_name", "")}


class SavedArtistsView(_SavedCollectionView):
    collection = CollectionMeta.SAVED_ARTISTS
    model = SavedArtist
    serializer_class = SavedArtistSerializer


class SyncStateView(APIView):
    """Cheap timestamp-only summary the client app polls before deciding
    which collections to push or pull."""

    permission_classes = [IsAuthenticated]

    def get(self, request):
        timestamps = {
            collection: None
            for collection, _ in CollectionMeta.COLLECTION_CHOICES
        }
        for meta in CollectionMeta.objects.filter(user=request.user):
            timestamps[meta.collection] = meta.last_updated_ms
        return Response(timestamps)
