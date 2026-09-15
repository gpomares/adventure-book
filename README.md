# Adventure Book

## Local development

1. Copy `.env.example` to `.env`.
2. Choose a local value for `POSTGRES_PASSWORD`.
3. Start the complete stack:

   ```sh
   docker compose up --build
   ```

4. Application at <http://localhost:8080>.
5. Stop the stack with `docker compose down`. 

`docker compose down -v` also deletes the local PostgreSQL volume and its database.