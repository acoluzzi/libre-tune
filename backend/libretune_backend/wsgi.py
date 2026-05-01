"""WSGI entry point for the LibreTune backend service."""
import os

from django.core.wsgi import get_wsgi_application

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "libretune_backend.settings")

application = get_wsgi_application()
