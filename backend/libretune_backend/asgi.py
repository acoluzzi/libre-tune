"""ASGI entry point for the LibreTune backend service."""
import os

from django.core.asgi import get_asgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "libretune_backend.settings")

application = get_asgi_application()
