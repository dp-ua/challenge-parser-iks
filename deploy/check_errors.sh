#!/bin/bash

LOG_FILE="$1"
EMAIL="$2"
ERROR_COUNT_LIMIT="$3"

if [[ -z "$LOG_FILE" || -z "$EMAIL" || -z "$ERROR_COUNT_LIMIT" ]]; then
    echo "Usage: $0 <path_to_log> <email> <error_count_limit>"
    exit 1
fi

if [[ ! -f "$LOG_FILE" ]]; then
    echo "Error: File $LOG_FILE not found!"
    exit 2
fi

ERROR_COUNT=$(grep -c 'ERROR' "$LOG_FILE")

if [[ "$ERROR_COUNT" -gt "$ERROR_COUNT_LIMIT" ]]; then
    ERROR_REPORT="error_report.txt"

    echo "Detected $ERROR_COUNT errors in the file $LOG_FILE" > "$ERROR_REPORT"
    grep 'ERROR' "$LOG_FILE" >> "$ERROR_REPORT"

    mail -s "Jenkins: Found $ERROR_COUNT errors in logs" "$EMAIL" < "$ERROR_REPORT"
fi
