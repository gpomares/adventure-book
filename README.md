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

The generated API documentation is available at <http://localhost:8080/swagger-ui.html>.

## Reading an adventure book

Start a reading session for a book. The reader begins at its `BEGIN` section with health `10`.

```sh
curl -X POST http://localhost:8080/api/adventure-books/7/reading-sessions
```

The `201 Created` response contains the session state and only the options available from the current section:

```json
{
   "sessionId": 42,
   "health": 10,
   "status": "IN_PROGRESS",
   "consequence": null,
   "section": {
      "id": 1,
      "text": "You stand at the entrance...",
      "type": "BEGIN",
      "options": [
         {
            "id": 12,
            "description": "Enter the cavern"
         }
      ]
   }
}
```

Choose an option using the option ID from that response:

```sh
curl -X POST \
  http://localhost:8080/api/adventure-books/7/reading-sessions/42/options/12
```

Each choice returns the updated health and current section. A session ends with one of `COMPLETED`, `DEAD`, or `STUCK`;
terminal responses expose no further options. Selecting an option that is not available from the current section, or
choosing after the session ends, returns `409 Conflict`.

