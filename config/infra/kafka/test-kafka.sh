#!/bin/bash
# test-kafka-working.sh

echo "=== Testing Kafka Working ==="

echo "1. Creating test topic..."
kubectl exec -n soulmate-infra kafka-0 -- \
  kafka-topics --bootstrap-server localhost:9091 \
  --create --topic test-topic \
  --partitions 1 --replication-factor 1 \
  --if-not-exists

echo ""
echo "2. Listing topics..."
kubectl exec -n soulmate-infra kafka-0 -- \
  kafka-topics --bootstrap-server localhost:9091 --list

echo ""
echo "3. Testing producer (sending message)..."
kubectl exec -n soulmate-infra kafka-0 -- \
  kafka-console-producer \
  --bootstrap-server localhost:9091 \
  --topic test-topic <<EOF
Test message 1 from Kubernetes at $(date)
Test message 2 from Kubernetes at $(date)
EOF

echo ""
echo "4. Testing consumer (reading messages)..."
kubectl exec -n soulmate-infra kafka-0 -- \
  kafka-console-consumer \
  --bootstrap-server localhost:9091 \
  --topic test-topic \
  --from-beginning \
  --max-messages 2 \
  --timeout-ms 10000

echo ""
echo "✅ Kafka is working!"