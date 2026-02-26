#!/bin/bash

HOST="localhost"
PORTS=(5434 8080 5173)
ALL_ALIVE=true

for port in "${PORTS[@]}"; do
    if ! nc -z $HOST $port; then
        echo -e "\e[31mFAIL: Port $port is offline.\e[0m"
        ALL_ALIVE=false
    fi
done

if [ "$ALL_ALIVE" = true ]; then
    echo -e "\e[32mSUCCESS: All foundations (${PORTS[*]}) are ready on $HOST.\e[0m"
    exit 0
else
    echo -e "\e[33mPlease start the missing services before proceeding.\e[0m"
    exit 1
fi
