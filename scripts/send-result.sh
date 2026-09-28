#!/usr/bin/env bash
# Plays the CPO: publishes an OCPI CommandResult to cpo.command-results on the docker-compose Kafka.
# Usage: ./scripts/send-result.sh <command_id (= remote start id)> <RESULT, e.g. ACCEPTED | EVSE_OCCUPIED | FAILED>
set -euo pipefail

COMMAND_ID="${1:?command_id (remote start id) required}"
RESULT="${2:?result required, e.g. ACCEPTED}"

docker compose exec -T kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic cpo.command-results \
  --property parse.key=true \
  --property key.separator='|' <<EOF
${COMMAND_ID}|{"command_id":"${COMMAND_ID}","result":"${RESULT}","message":"sent by send-result.sh"}
EOF

echo "Sent ${RESULT} for command ${COMMAND_ID}"
