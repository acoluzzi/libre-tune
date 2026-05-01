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

    def test_liked_songs_sync(self):
        self._auth(self._register())
        payload = {
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
            ]
        }
        put = self.client.put("/api/sync/liked-songs/", payload, format="json")
        self.assertEqual(put.status_code, 204)

        get = self.client.get("/api/sync/liked-songs/")
        self.assertEqual(get.status_code, 200)
        items = get.json()["items"]
        self.assertEqual(len(items), 1)
        self.assertEqual(items[0]["song"]["remote_id"], "yt:abc")

    def test_playlists_sync(self):
        self._auth(self._register())
        payload = {
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
            ]
        }
        put = self.client.put("/api/sync/playlists/", payload, format="json")
        self.assertEqual(put.status_code, 204)
        items = self.client.get("/api/sync/playlists/").json()["items"]
        self.assertEqual(items[0]["name"], "Vibes")
        self.assertEqual(items[0]["songs"][0]["song"]["remote_id"], "yt:1")

    def test_saved_albums_and_artists_sync(self):
        self._auth(self._register())
        for path in ("/api/sync/saved-albums/", "/api/sync/saved-artists/"):
            put = self.client.put(
                path,
                {
                    "items": [
                        {
                            "remote_id": "x",
                            "name": "Name",
                            "thumbnail_url": "",
                            "artist_name": "A",
                            "position": 0,
                        }
                    ]
                },
                format="json",
            )
            self.assertEqual(put.status_code, 204, path)
            self.assertEqual(
                self.client.get(path).json()["items"][0]["name"], "Name"
            )

    def test_anonymous_cannot_sync(self):
        response = self.client.get("/api/sync/liked-songs/")
        self.assertEqual(response.status_code, 401)
