#!/bin/sh

# Wait until the Kafka Connect REST API is available
until $(curl --output /dev/null --head http://debezium:8083/connectors); do
    printf 'Waiting for Kafka Connect to be ready...\n'
    sleep 5
done

printf 'Kafka Connect is up! Attempting to register the connector...\n'

# Send the connector configuration to the Kafka Connect REST API
curl -X POST -H "Content-Type: application/json" --data @/etc/debezium/connector-config.json http://debezium:8083/connectors

printf 'Connector registration complete.\n'

# Keep the container running
tail -f /dev/null
