#!/usr/bin/env python
"""Django command-line utility for administrative tasks."""
import os
import sys


def main() -> None:
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "libretune_backend.settings")
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Make sure it is installed and available "
            "on your PYTHONPATH, and that your virtual environment is active."
        ) from exc
    execute_from_command_line(sys.argv)


if __name__ == "__main__":
    main()
