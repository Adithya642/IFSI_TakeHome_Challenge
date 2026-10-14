# IFSI Exam Requests

Exam request form and staff request list: Spring Boot API with server-side validation, SQLite,
and a plain HTML/CSS/JavaScript front end.

- **Live demo:** https://adithya642.github.io/IFSI_TakeHome_Challenge/
  (no server there, so it runs in **demo mode**: sample data, new requests saved in your browser)
- **Repository:** https://github.com/Adithya642/IFSI_TakeHome_Challenge

## How to run
Needs Java 17+ (built with 21), `sqlite3` on PATH, and Bash (Git Bash on Windows). No Maven
install needed; the project uses the Maven wrapper.

```bash
./db/reset.sh                        # builds db/ifsi.db: 10 students, 20 requests
cd server && ./mvnw spring-boot:run  # run from server/ (relative DB path); Windows cmd: mvnw.cmd
# open http://localhost:8080         # page and API served together
```
The `courses` table is created on first start. Stop the server before re-running `reset.sh`.

| Task | Command |
|---|---|
| Task 1 queries (on a fresh DB) | `sqlite3 -header -column db/ifsi.db < db/task1_report.sql` |
| Server validation demo | `./server/demo-requests.sh` (server running) |
| Tests | `cd server && ./mvnw test` |
| Demo mode locally | `cd docs && python -m http.server 5500`, then open `localhost:5500/?demo` |

## Structure
```
db/      kit setup files (unchanged), reset.sh, task1_report.sql
docs/    index.html, styles.css, app.js, sample_requests.json (also served by GitHub Pages)
server/  Spring Boot: controller → service (ExamRequestValidator) → repository (JdbcTemplate SQL)
```

## Approach
- **Task 1:** `SUM(CASE WHEN status = 'Pending' …)` instead of `WHERE`, so courses with no pending
  requests still count. Standard SQL on the kit tables only, so it also runs on SQL Server.
- **Server:** every value goes through `?` placeholders. The validator returns all errors at once as
  `400 {"errors": {field: message}}`. `student_id` is accepted as text so `"abc"` gets a clear
  message, not a parser error. The course name comes from the database, never the client. Status is
  always Pending, accommodation is stored as 1/0, and the create flow runs in one transaction.
- **Front end:** the same error messages as the server, shown under each field. An accessible
  confirmation dialog for accommodation. Rows are rendered with `textContent` (XSS-safe). The
  filter runs as you type. Rows become cards below 640px. One `app.js` runs in **server mode**
  locally and **demo mode** on GitHub Pages; if the server is down, it shows an error instead of
  silently saving to the browser.

## Assumptions
- A Student ID box instead of a name list, so other people's names aren't exposed.
- The staff table is on the same page because the brief asks; in production it would sit behind a login.
- The courses table is created by the server (`schema.sql`), so adding a course is one `INSERT`.
  It uses SQLite syntax.
- No past exam dates ("today" is Central Time). New requests are always Pending. Repeat requests are allowed.

## What I skipped
- Staff login and authorization, status updates, and an add-course form.
- Broader tests (only 3 validator tests): API, repository and front-end tests.
- Duplicate protection on retry (an idempotency key).
- Database `CHECK` constraints. The kit schema was kept unchanged.
- Externalized config and migrations (Flyway); pagination and server-side search.

## Tools and AI use
- **Tools:** Java 21, Spring Boot, SQLite, VS Code, Git, GitHub Pages. **AI:** Claude Code, plus a
  second AI tool for an independent code review.
- **Generated vs. my work:** I planned the work, made the design and UX decisions, and reviewed every
  change; the AI wrote code to those decisions and explained anything I was less familiar with.
  I had code reworked when names, structure or comments weren't clear.
- **What a tool got wrong:** the assistant claimed start.spring.io had no SQLite driver, and the
  independent review found that a server error silently switched the page to browser-only saving.
  I fixed it so demo mode depends only on where the page is hosted. Testing also turned up layout
  bugs, which I had fixed at the root cause.
- **What I didn't let a tool do:** commit, push or deploy. I tested every change myself and kept the
  kit's setup files unchanged.
