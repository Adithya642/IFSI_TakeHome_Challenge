#!/usr/bin/env bash
# Sends one valid request and one request per validation failure to the running server.
# Usage (server must be running): ./server/demo-requests.sh
set -u
URL="${URL:-http://localhost:8080/api/requests}"

# Prints the case name, then the response body and HTTP status.
post() {
    echo "== $1"
    curl -s -w "\nHTTP %{http_code}\n\n" -X POST "$URL" \
        -H "Content-Type: application/json" -d "$2"
}

LONG_NOTES=$(printf 'x%.0s' {1..501})   # 501 characters, one over the limit

post "Valid request"            '{"student_id":"4","course_code":"FF2","request_date":"2026-12-01","needs_accommodation":true,"notes":"Demo"}'
post "Missing required fields"  '{}'
post "Non-numeric student ID"   '{"student_id":"abc","course_code":"FF2","request_date":"2026-12-01"}'
post "Unknown student"          '{"student_id":"99","course_code":"FF2","request_date":"2026-12-01"}'
post "Unknown course"           '{"student_id":"4","course_code":"XYZ","request_date":"2026-12-01"}'
post "Impossible date"          '{"student_id":"4","course_code":"FF2","request_date":"2026-02-30"}'
post "Past date"                '{"student_id":"4","course_code":"FF2","request_date":"2020-01-01"}'
post "Notes too long"           "{\"student_id\":\"4\",\"course_code\":\"FF2\",\"request_date\":\"2026-12-01\",\"notes\":\"$LONG_NOTES\"}"
post "Broken JSON"              '{"student_id": "4", '
