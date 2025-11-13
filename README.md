# JWT-Secured URL Shortener Web Service
A secure, production-ready API for shortening HTTP URLs using JWT-based authentication.

## Rationale
  - **Production-ready**: Instrumented for observability with OpenTelemetry and Zipkin.
  - **Maintainable**: Organized using the Vertical Slice Architecture with (incomplete) unit tests.
  - **Extensible**: Designed for granular access control and easy feature expansion.
  - **Performant**: Minimizes operations and network overhead.
  - **Pragmatic**: Leverages framework mechanisms over custom code.
  - **Security-focused**: Follows security practices in both application logic and secret management.

## Setup

### Running Locally (Non-Containerized)

**Requirements**
- Java 25 JDK & JRE  
- Gradle 9.1.0+  
- [Make](https://en.wikipedia.org/wiki/Make_(software))  

The application uses an **H2 in-memory database** (for `test` and `local` profiles) configured with a **PostgreSQL dialect**.

**Steps**
1. Create a `.env` file in the project root (same directory as the `Makefile`).
2. Copy all `key:value` pairs from `url-shortener.environment` inside `docker-compose.yml` into `.env`, converting them to `key=value` format.
3. Set `APP_ENVIRONMENT=local`. You may adjust other values as needed.  
   *Note: Changing `VAULT_URI`, `OTLP_METRICS_ENDPOINT`, `ZIPKIN_TRACES_ENDPOINT`, or `URL_SHORTENER_VAULT_TOKEN` has no effect in the local profile.*
4. Copy the public/private key files from `src/test/resources/` into the paths specified by  
   `URL_SHORTENER_PRIVATE_KEY_PATH` and `URL_SHORTENER_PUBLIC_KEY_PATH`.  
   You can also generate your own local RSA key pair (≥2048 bits).
5. From the project root, run:
   - `make run` – start the application  
   - `make test` – run unit and context load tests  

### Running Locally (Containerized)

1. Use the `docker-compose.yml` example to define the required environment variables.  
2. Start the application with:
   ```bash
   docker compose up -d
  
### Notes

- For full system integration, use profiles other than `test` or `local` (e.g., `APP_ENVIRONMENT=sit`).  
- Ensure **PostgreSQL**, **Zipkin**, and the **OpenTelemetry Collector** are reachable.  
- **HashiCorp Vault** must contain the required key-value secrets (see `application.yml` and  
  `src/main/java/dev/hireben/url_shortener/common/configuration/DataSourceConfig.java`) for the application to start successfully.

