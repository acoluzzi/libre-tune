from django.test import TestCase
from rest_framework.test import APIClient


class AuthAndSyncTests(TestCase):
    def setUp(self):
        self.client = APIClient()

    def _register(self, username="alice", password="StrongPass!9"):
        response = self.client.post(
            "/api/auth/register/",
            {"username": username, "password": password, "email": f"{username}@x.io"},
            format="json",
        )
        self.assertEqual(response.status_code, 201, response.content)
        return response.json()["token"]

    def _auth(self, token):
        self.client.credentials(HTTP_AUTHORIZATION=f"Token {token}")

    def test_register_login_logout_flow(self):
        token = self._register()
        self._auth(token)
        me = self.client.get("/api/auth/me/")
        self.assertEqual(me.status_code, 200)
        self.assertEqual(me.json()["username"], "alice")

        logout = self.client.post("/api/auth/logout/")
        self.assertEqual(logout.status_code, 204)

        self.client.credentials()
        login = self.client.post(
            "/api/auth/login/",
            {"username": "alice", "password": "StrongPass!9"},
            format="json",
        )
        self.assertEqual(login.status_code, 200)
        self.assertIn("token", login.json())

    def test_liked_songs_sync_round_trip_with_timestamp(self):
        self._auth(self._register())
        timestamp = 1_700_000_000_000
        payload = {
            "last_updated_ms": timestamp,
            "items": [
                {
                    "song": {
                        "remote_id": "yt:abc",
                        "title": "Song A",
                        "artist_name": "Artist",
                        "album_name": "Album",
                        "duration_ms": 180000,
                        "thumbnail_url": "",
                    },
                    "position": 0,
                }
            ],
        }
        put = self.client.put("/api/sync/liked-songs/", payload, format="json")
        self.assertEqual(put.status_code, 200, put.content)
        self.assertEqual(put.json()["last_updated_ms"], timestamp)

        get = self.client.get("/api/sync/liked-songs/")
        self.assertEqual(get.status_code, 200)
        body = get.json()
        self.assertEqual(body["last_updated_ms"], timestamp)
        self.assertEqual(len(body["items"]), 1)
        self.assertEqual(body["items"][0]["song"]["remote_id"], "yt:abc")

    def test_put_rejects_missing_timestamp(self):
        self._auth(self._register())
        response = self.client.put(
            "/api/sync/liked-songs/", {"items": []}, format="json"
        )
        self.assertEqual(response.status_code, 400)

    def test_state_endpoint_returns_per_collection_timestamps(self):
        self._auth(self._register())

        before = self.client.get("/api/sync/state/").json()
        self.assertEqual(
            before,
            {
                "liked_songs": None,
                "playlists": None,
                "saved_albums": None,
                "saved_artists": None,
            },
        )

        ts_liked = 100
        self.client.put(
            "/api/sync/liked-songs/",
            {"last_updated_ms": ts_liked, "items": []},
            format="json",
        )
        ts_artists = 200
        self.client.put(
            "/api/sync/saved-artists/",
            {"last_updated_ms": ts_artists, "items": []},
            format="json",
        )

        after = self.client.get("/api/sync/state/").json()
        self.assertEqual(after["liked_songs"], ts_liked)
        self.assertEqual(after["saved_artists"], ts_artists)
        self.assertIsNone(after["playlists"])
        self.assertIsNone(after["saved_albums"])

    def test_playlists_and_saved_collections_sync(self):
        self._auth(self._register())
        ts = 42
        playlist_payload = {
            "last_updated_ms": ts,
            "items": [
                {
                    "remote_id": "p1",
                    "name": "Vibes",
                    "description": "",
                    "thumbnail_url": "",
                    "songs": [
                        {
                            "song": {
                                "remote_id": "yt:1",
                                "title": "Track",
                                "artist_name": "A",
                                "album_name": "Al",
                                "duration_ms": 1,
                                "thumbnail_url": "",
                            },
                            "position": 0,
                        }
                    ],
                }
            ],
        }
        self.assertEqual(
            self.client.put("/api/sync/playlists/", playlist_payload, format="json").status_code,
            200,
        )
        playlists_body = self.client.get("/api/sync/playlists/").json()
        self.assertEqual(playlists_body["last_updated_ms"], ts)
        self.assertEqual(playlists_body["items"][0]["name"], "Vibes")
        self.assertEqual(
            playlists_body["items"][0]["songs"][0]["song"]["remote_id"], "yt:1"
        )

        for path in ("/api/sync/saved-albums/", "/api/sync/saved-artists/"):
            put = self.client.put(
                path,
                {
                    "last_updated_ms": ts,
                    "items": [
                        {
                            "remote_id": "x",
                            "name": "Name",
                            "thumbnail_url": "",
                            "artist_name": "A",
                            "position": 0,
                        }
                    ],
                },
                format="json",
            )
            self.assertEqual(put.status_code, 200, path)
            body = self.client.get(path).json()
            self.assertEqual(body["last_updated_ms"], ts, path)
            self.assertEqual(body["items"][0]["name"], "Name", path)

    def test_anonymous_cannot_sync(self):
        response = self.client.get("/api/sync/liked-songs/")
        self.assertEqual(response.status_code, 401)
