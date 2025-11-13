.PHONY: run test clean build

ENV_FILE := ./.env

define LOAD_ENV
	@if [ ! -f "$(ENV_FILE)" ]; then \
		echo "Error: .env file not found: $(ENV_FILE)" >&2; \
		exit 1; \
	fi; \
	set -a; \
	. $(ENV_FILE); \
	set +a;
endef

run:
	$(LOAD_ENV) \
	./gradlew bootRun

test:
	$(LOAD_ENV) \
	./gradlew test

clean:
	./gradlew clean

build:
	./gradlew build -x test