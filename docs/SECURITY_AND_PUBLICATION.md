# Security and Publication Notes

## Completed for this portfolio copy

- Removed the embedded administrator password from `AuthService.loginAdmin`.
- Administrator access requires nonempty `BOOKING_ADMIN_USER` and `BOOKING_ADMIN_PASSWORD` environment variables. Without them, admin login is disabled.
- Excluded all local SQLite database files, WAL/SHM journals, build outputs, and JAR binaries from the distributed ZIP.
- No personal user or reservation records were added to this repository.

## Known limitations — not fixed

1. **Plaintext user password storage and comparison** in `AuthService`. Remediate with a suitable password-hashing scheme (e.g., Argon2id/bcrypt/PBKDF2 with unique salts), along with a migration plan.
2. **Mock password reset OTP shown in the UI**. This is an educational simulation, not secure account recovery.
3. **Environment variables are a demo convenience**, not a comprehensive secret-management solution.
4. Admin/user authorization, concurrency, data lifecycle, error handling, and GUI integration need additional review.
5. Clean-install data bootstrap is incomplete without some reference records from the academic database. Do not distribute the original database just to make the demo work.

## Before making this repo Public

- [ ] Confirm permission from collaborators to publish source/assets of the team project.
- [ ] Review copyright/redistribution rights for university logo/images and supplied PDF; remove/recreate assets if uncertain.
- [ ] Confirm repository is **new or has been reset**: deleting `meeting_room_booking.db` in a later commit does **not** remove it from prior Git history.
- [ ] Confirm no passwords, secrets, personal details, database exports, or configuration files with credentials remain.
- [ ] Verify `.gitignore`, source tree, and README on GitHub after upload.
- [ ] Keep repository Private until the preceding items are addressed.

## History warning

If an older repository already has real `meeting_room_booking.db` checked in, **do not reuse its commit history for a Public release**. Prepare a new clean repository or rewrite history carefully, with collaborators informed. `.gitignore` only prevents future additions; it does not erase existing commits.
