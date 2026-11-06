# University Meeting Room & Equipment Booking System

> **Archived academic team project · Java Swing + SQLite · Not production-ready**

A desktop reservation-management prototype built as a university Object-Oriented Programming and Data Structures team assignment. The application models meeting-room and equipment booking for students and administrators.

## What the application demonstrates

- **User workflows:** registration, login, room/equipment booking requests, booking calendar, history, and status tracking.
- **Administrative workflows:** booking approval, equipment and user management, alerts, statistics, and calendars.
- **Programming concepts:** object-oriented modeling, separation of UI/model/service layers, Swing events, JDBC queries, and relational data persistence.

### Technology

| Layer | Technology |
|---|---|
| Application | Java 17+ / Java Swing and AWT |
| Persistence | SQLite via JDBC |
| Build | Maven descriptor added for portfolio packaging |
| Original IDE | Eclipse |

## Repository layout

```text
src/main/             # Java source (37 .java files)
  MainApp.java        # Application entry point
  MainUI.java         # Login window
  model/              # Booking, user, and notification models
  service/            # Authentication and SQLite logic
  ui/admin/           # Admin screens
  ui/user/            # User screens
images/               # Assets from the academic prototype
pom.xml               # Maven dependencies and run configuration
docs/                 # Project summary and security / publication notes
```

## Run locally (experimental)

Prerequisites: Java JDK 17+, Maven, desktop GUI (X11/Wayland), and internet access for Maven's first dependency download.

From the repository root:

```bash
mvn compile
mvn exec:java
```

The app initializes a **new local SQLite file** (`meeting_room_booking.db`) at startup, excluded from Git using `.gitignore`. For user workflows, create a demo account with invented details only. Some screens may expect room/equipment seed data from the original class project database; that database is intentionally **not included** to prevent exposing personal records. Therefore clean-install end-to-end booking is **not guaranteed**.

### Optional administrator demo login

The original student project included a fixed admin password. **This portfolio copy removes that hard-coded login**. For local demonstration only, configure credentials *outside source control* before launching:

```bash
export BOOKING_ADMIN_USER=localadmin
read -r -s -p 'Local demo admin password: ' BOOKING_ADMIN_PASSWORD; echo
export BOOKING_ADMIN_PASSWORD
mvn exec:java
```

When these environment variables are absent, administrator login is disabled. This is only a minimal portfolio redaction, **not** production-grade authentication.

## Security / limitations

This is an **educational prototype**, not an application to deploy in a real university or production environment.

- The original database (which may contain accounts/phone numbers/booking records) is not distributed.
- The original hard-coded admin login is replaced with opt-in environment variables in this copy.
- **User passwords are still stored and compared in plaintext** in the archived application. They require a proper password hashing scheme and migration before any real deployment.
- The original "forgot password" flow uses a simulated on-screen OTP, not a real verified reset protocol.
- Application authorization, input validation, database connection lifecycle, and full GUI/workflow tests have not been security-audited.
- Brand/logo images were retained from the academic prototype; check redistribution permissions. The original room-rules PDF was omitted pending permission, so its corresponding UI menu will show a missing-file message.

See [`docs/SECURITY_AND_PUBLICATION.md`](docs/SECURITY_AND_PUBLICATION.md) for the disclosure checklist and security improvements.

## Attribution

This is an **academic team project**, not solely an individual-developed system. Contributions of other team members are acknowledged collectively; specific feature ownership has not been independently verified. The project is presented as a portfolio artifact, not as an official university service.

## Verification

- `javac` source compilation: checked for this repackaging (JDK 21).
- Maven dependency resolution, GUI execution, login, and full workflows: **not verified here**.

See also [`docs/PROJECT_SUMMARY.md`](docs/PROJECT_SUMMARY.md).
