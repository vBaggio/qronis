#!/bin/bash
# Script to stop Qronis frontend and backend services

echo "Stopping Frontend (Port 5173)..."
FRONTEND_PID=$(lsof -t -i:5173)
if [ -n "$FRONTEND_PID" ]; then
    kill -9 $FRONTEND_PID
    echo "\e[32mFrontend stopped.\e[0m"
else
    echo "\e[33mFrontend is not running.\e[0m"
fi

echo "Stopping Backend (Port 8080)..."
BACKEND_PID=$(lsof -t -i:8080)
if [ -n "$BACKEND_PID" ]; then
    kill -9 $BACKEND_PID
    echo "\e[32mBackend stopped.\e[0m"
else
    echo "\e[33mBackend is not running.\e[0m"
fi
