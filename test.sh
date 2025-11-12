#!/bin/sh

# Set path to .env
ENV_FILE='./.env'

# Check .env existence
if [ ! -f "$ENV_FILE" ]; then
  echo "Error: .env file not found: $ENV_FILE" >&2
  exit 1
fi

# Set environment variables in the current session
set -a
source "$ENV_FILE"
set +a

# Run Spring Boot app
./gradlew test