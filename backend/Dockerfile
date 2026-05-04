FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
ARG GIT_SHA=unknown
ENV GIT_SHA=${GIT_SHA}
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]