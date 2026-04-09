#!/usr/bin/env bash
set -euo pipefail

BASE_PKG="com.neo4j.workout"

if [ $# -eq 0 ]; then
  echo "Usage: ./run.sh <class>"
  echo ""
  echo "Examples:"
  echo "  ./run.sh Neo4jConnection"
  echo "  ./run.sh sessions.Session01Cypher"
  echo "  ./run.sh sessions.Session02Modeling"
  exit 1
fi

mvn -q compile exec:java -Dexec.mainClass="${BASE_PKG}.$1"
