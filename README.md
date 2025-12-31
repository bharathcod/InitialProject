# collab-messaging-secure

This project builds on the collab-messaging example and adds **Security & Authentication**:

- JWT-based authentication
- Role-based authorization (VIEWER, EDITOR, OWNER)
- WebSocket handshake validation using JWT (token passed as `access_token` query param or Authorization header)
- Presigned S3 URL generation (requires AWS credentials) protected by roles

## Quick start

1. Start Redis & Postgres:

```bash
docker-compose up -d
```

2. Build:

```bash
mvn clean package
```

3. Run:

```bash
java -jar target/collab-messaging-secure-0.0.1-SNAPSHOT.jar


$env:AWS_REGION = 'us-east-1'
$env:TZ = 'Asia/Kolkata'; java "-Duser.timezone=Asia/Kolkata" -jar target\collab-messaging-secure-0.0.1-SNAPSHOT.jar

```

4. Login to get JWT:

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"username":"alice","password":"alicepass"}'
```

5. Use the token:

- Paste into `test-client.html` box and Connect to open authenticated WebSocket.
- Call protected REST endpoints with header `Authorization: Bearer <token>`.

## Helper script

There's a small PowerShell helper that logs in, extracts the JWT, copies it to the clipboard, and calls a protected endpoint:

```powershell
# default (alice)
.\scripts\login-and-call.ps1

# custom username / endpoint
.\scripts\login-and-call.ps1 -Username bob -Password bobpass -ProtectedEndpoint 'http://localhost:8080/api/protected'
```

This is convenient for testing. The script does not store secrets in the repo; do not commit any files containing real AWS keys or secrets.

## Notes

- Change the JWT secret in `application.yml` or set env var `SECURITY_JWT_SECRET`.
- For S3 presigned URLs, configure AWS credentials or use MinIO (ask me if you want a MinIO configuration).

---

## API Reference & Testing 🧭

### 🔐 Auth (REST)

- **POST** `/auth/login` — body: `{ "username": "alice", "password": "alicepass" }` → returns `{ "token": "..." }`.
  - Powershell example (get token into $token variable):
    ```powershell
    $body = @{ username = 'alice'; password = 'alicepass' } | ConvertTo-Json
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/auth/login' -Method Post -ContentType 'application/json' -Body $body
    $token = $resp.token
    Set-Clipboard $token
    ```
- **POST** `/auth/refresh` — not implemented (placeholder for refresh-token flow).

### 📁 Projects & Models (REST)

- **POST** `/projects` — create project. Body: `{ "name": "My P", "description": "...", "modelId": "optional" }` (requires authentication).
- **POST** `/models` — create a model. Body supports an optional `features` array so you can create a model and multiple features in a single request:

  ```json
  {
    "name": "My Model",
    "description": "Model description",
    "modelId": "M-1",
    "features": [
      {
        "featureId": "F-1",
        "name": "Feature 1",
        "description": "First feature",
        "metadata": "{}"
      },
      {
        "featureId": "F-2",
        "name": "Feature 2",
        "description": "Second feature",
        "metadata": "{}"
      }
    ]
  }
  ```

  Response: `201 Created` with body `ModelWithFeatures`:

  ```json
  {
    "modelId": "M-1",
    "name": "My Model",
    "description": "Model description",
    "owner": "bob",
    "createdAt": "2025-12-31T...",
    "features": [ { "featureId":"F-1", "name":"Feature 1", "description":"First feature", "metadata":"{}", "createdAt":"..." }, ... ]
  }
  ```

- **GET** `/projects/{id}` — get project details.
- **GET** `/projects/{id}/model` — get model metadata (operation count, last modified).
- **GET** `/projects/{id}/operations` — list operations for the project's model (returns array of persisted operations).

Example (create project):

```powershell
$req = @{ name = 'P1'; description = 'desc' } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/projects' -Method Post -ContentType 'application/json' -Body $req -Headers @{ Authorization = "Bearer $token" }
```

Example (create model with features):

```powershell
$payload = @{
  name = 'Team Model'
  description = 'Contains initial features'
  modelId = 'M-1'
  features = @(
    @{ featureId = 'F-1'; name = 'F-1'; description = 'Description 1'; metadata = '{}' },
    @{ featureId = 'F-2'; name = 'F-2'; description = 'Description 2'; metadata = '{}' }
  )
} | ConvertTo-Json -Depth 5
Invoke-RestMethod -Uri 'http://localhost:8080/models' -Method Post -ContentType 'application/json' -Body $payload -Headers @{ Authorization = "Bearer $token" }
```

### 👥 Sharing / Permissions (REST)

- **POST** `/projects/{id}/share` — body: `{ "username": "bob", "role": "EDITOR" }` (only project owner can call).
- **GET** `/projects/{id}/permissions` — list permissions (owner-only).
- **PUT** `/projects/{id}/permissions/{username}` — update role (owner-only).
- **DELETE** `/projects/{id}/permissions/{username}` — remove permission (owner-only).

Example (share):

```powershell
$share = @{ username = 'bob'; role = 'EDITOR' } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/projects/1/share' -Method Post -ContentType 'application/json' -Body $share -Headers @{ Authorization = "Bearer $token" }
```

> When a permission is created/updated/removed the service publishes a permission notification that is broadcast to `/topic/permissions` (STOMP) so clients can react in real time.

### 📦 Files (REST)

- **POST** `/files/presigned-upload` — body: `{ "fileName": "name.txt", "minutesValid": 15 }` → returns `{ "url": "..." }`. Requires role **EDITOR** or **OWNER**. Uses S3 if `aws.s3.enabled=true`.
- **GET** `/api/s3/presigned-upload?fileName=...` — convenience GET equivalent (returns 501 if S3 disabled).

Example (PowerShell):

```powershell
$body = @{ fileName = 'test.txt'; minutesValid = 10 } | ConvertTo-Json
Invoke-RestMethod -Uri 'http://localhost:8080/files/presigned-upload' -Method Post -ContentType 'application/json' -Body $body -Headers @{ Authorization = "Bearer $token" }
```

### ⚡ Real-time (WebSocket / STOMP)

- Connect endpoint: **`/ws`** (SockJS + STOMP).
- Handshake: pass JWT as `access_token` query param or use the `Authorization: Bearer <token>` header on the WebSocket handshake.

Client → Server (send to `/app/...`):

- `/app/update-model` — submit `ChangeEvent` (ADD/UPDATE operations).
- `/app/model/lock` — submit `LockEvent` with `{ modelId, featureId, level, action: LOCK|UNLOCK, user }`.
- `/app/file/add` — submit `FileAddEvent` when adding a file.
- `/app/presence` — submit `PresenceEvent` (JOIN / LEAVE).

Server → Client (subscribe to `/topic/...`):

- `/topic/model-updates` — model change events plus file-add events.
- `/topic/permissions` — permission notifications (SHARED, UPDATED, REMOVED).
- `/topic/locks` — lock/unlock events.
- `/topic/presence` — presence events.
- `/topic/files` — file-add events (also appear on `/topic/model-updates`).

### Using the bundled test client (`test-client.html`) 🧪

1. Open `test-client.html` in your browser (double-click or `Open File`).
2. Get a JWT via `/auth/login` and paste it into the **Paste JWT here** box.
3. Click **Connect (with token)**. On success you will see "Connected".
4. Create models & features (new UI):
   - Use the **Create Model** form to create a model and optionally add multiple features in the same request.
   - Add features to the buffer by filling **Feature ID**, **Feature name**, **Feature description**, and **metadata**, then click **Add Feature**.
   - After adding features, click **Create Model** to send a `POST /models` request containing the model and the buffered features.
   - The UI will auto-select the created model and you can click **Get Model** to see the `ModelWithFeatures` response (includes feature descriptions and createdAt timestamps).
5. Subscribe to topics:
   - Use **Subscribe Model** to subscribe to `/topic/model-updates/{modelId}` and `/topic/files/{modelId}` for model-specific events.
   - Click **Subscribe Permissions** to receive `/topic/permissions` events.
   - Use the **Lock/Unlock** and **Add File** UI controls to trigger `/app/model/lock` and `/app/file/add` messages.
   - Use **Share Project (sample)** to call `/projects/{id}/share` directly and observe permission notifications.
6. You can also open the browser console and manually subscribe, e.g.:

```js
stompClient.subscribe("/topic/locks", (msg) =>
  console.log(JSON.parse(msg.body))
);
```

---

$env:AWS_REGION = 'us-east-1'

Stop the running process

Check if your JAR is still running:

powershell
tasklist | findstr java
Kill it:

powershell
taskkill /F /IM java.exe
