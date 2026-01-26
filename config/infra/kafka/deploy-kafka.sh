#!/bin/bash

echo "=== Deploying Kafka (Simple Deployment) ==="
echo "Pulling Kafka image..."
docker pull confluentinc/cp-kafka:7.8.0

# Clean up any existing Kafka
echo "Cleaning up any existing Kafka..."
kubectl delete statefulset kafka -n soulmate-infra --ignore-not-found 2>/dev/null
kubectl delete service kafka kafka-external -n soulmate-infra --ignore-not-found 2>/dev/null
kubectl delete pvc -n soulmate-infra -l app=kafka --ignore-not-found 2>/dev/null

# Wait for cleanup
echo "Waiting for cleanup..."
sleep 5

# Deploy Kafka
echo "Deploying Kafka..."
kubectl apply -f "kafka/kafka-deployment.yaml"

echo "Waiting for Kafka to start (this can take 1-2 minutes)..."
MAX_RETRIES=30
for i in $(seq 1 $MAX_RETRIES); do
  # Check if pod is running
  POD_STATUS=$(kubectl get pod -n soulmate-infra kafka-0 -o jsonpath='{.status.phase}' 2>/dev/null || echo "Not Found")

  if [ "$POD_STATUS" = "Running" ]; then
    # Check if Kafka is actually ready inside the container
    if kubectl exec -n soulmate-infra kafka-0 -- \
      sh -c "kafka-broker-api-versions --bootstrap-server=localhost:9091 > /dev/null 2>&1" 2>/dev/null; then
      echo "✅ Kafka is running and ready!"
      break
    fi
  fi

  echo "Attempt $i/$MAX_RETRIES: Pod status: $POD_STATUS"
  sleep 5
done

# Additional wait for Kafka to be fully operational
sleep 10

echo "✅ Kafka deployed successfully!"
echo ""
echo "=== Connection Details ==="
echo "Internal (within cluster):"
echo "  kafka:9091"
echo ""
echo "External (via port-forward):"
echo "  localhost:29092"
echo ""
echo "=== Useful Commands ==="
echo "1. Create a test topic:"
echo "   kubectl exec -n soulmate-infra kafka-0 -- \\"
echo "     kafka-topics --bootstrap-server localhost:9091 \\"
echo "     --create --topic test \\"
echo "     --partitions 1 --replication-factor 1"
echo ""
echo "2. Port-forward for local access:"
echo "   kubectl port-forward -n soulmate-infra svc/kafka-external 29092:29092"
echo ""
echo "3. Check logs:"
echo "   kubectl logs -n soulmate-infra kafka-0 -f"
echo ""

echo "=== Testing Kafka ==="
chmod +x kafka/test-kafka.sh
"kafka/test-kafka.sh"