# LibreTune Backend

Django + Django REST Framework service that powers account features for the
LibreTune client app:

- **Auth** — registration, login, logout via token authentication.
- **Library sync** — liked songs, playlists, saved albums, saved artists.

The service is deployed at https://libretune.coluzziandrea.com.

## Quickstart

Start the local PostgreSQL instance first:

```bash
cd backend
docker compose -f docker-compose.dev.yml up -d
```

Then run the Django server:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env  # then edit values
python manage.py migrate
python manage.py runserver
```

## API

All sync endpoints require `Authorization: Token <token>` headers.

| Method      | Path                       | Description                       |
| ----------- | -------------------------- | --------------------------------- |
| `POST`      | `/api/auth/register/`      | Create an account, returns token  |
| `POST`      | `/api/auth/login/`         | Exchange credentials for a token  |
| `POST`      | `/api/auth/logout/`        | Revoke the current token          |
| `GET`       | `/api/auth/me/`            | Current user info                 |
| `GET`/`PUT` | `/api/sync/liked-songs/`   | Read/replace liked songs          |
| `GET`/`PUT` | `/api/sync/playlists/`     | Read/replace playlists with songs |
| `GET`/`PUT` | `/api/sync/saved-albums/`  | Read/replace saved albums         |
| `GET`/`PUT` | `/api/sync/saved-artists/` | Read/replace saved artists        |

`PUT` accepts the full library snapshot for that resource — the server
replaces server-side state for the authenticated user, allowing the client app
to push its local Room database state in a single call.

## Deployment

The service is intended to run behind a reverse proxy (nginx / Caddy) at
`libretune.coluzziandrea.com`. Use `gunicorn libretune_backend.wsgi` as the app
server. Configure `DJANGO_ALLOWED_HOSTS=libretune.coluzziandrea.com` and
provide a strong `DJANGO_SECRET_KEY` in the environment.
