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

$env:AWS_REGION = 'us-east-1'

Stop the running process

Check if your JAR is still running:

powershell
tasklist | findstr java
Kill it:

powershell
taskkill /F /IM java.exe
