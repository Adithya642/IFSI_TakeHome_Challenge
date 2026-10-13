#!/usr/bin/env bash
# Deletes db/ifsi.db and rebuilds it from the kit's setup_sqlite.sql.
# The courses table is not created here: the server creates it on startup
# from server/src/main/resources/schema.sql.
set -euo pipefail

# Find the db folder, regardless of where this script is run.
DB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Native Windows SQLite needs a Windows path inside its .read command.
if command -v cygpath >/dev/null 2>&1; then
    DB_DIR="$(cygpath -m "$DB_DIR")"
fi

# Use sqlite3 from PATH, or override: SQLITE=/path/to/sqlite3 ./db/reset.sh
SQLITE="${SQLITE:-sqlite3}"

# Start from an empty file so no leftover tables or rows survive.
rm -f "$DB_DIR/ifsi.db"

# Create the tables and load the sample data.
"$SQLITE" -bail "$DB_DIR/ifsi.db" ".read \"$DB_DIR/setup_sqlite.sql\""

# Expected: 10 students and 20 requests.
"$SQLITE" -header -column "$DB_DIR/ifsi.db" \
    "SELECT (SELECT COUNT(*) FROM students)      AS student_count,
            (SELECT COUNT(*) FROM exam_requests) AS request_count;"
