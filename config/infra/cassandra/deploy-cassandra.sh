#!/bin/bash

echo "=== Deploying Cassandra 3.11.2 ==="

# Pull the image first
echo "Pulling Cassandra image..."
docker pull cassandra:3.11.2

# Delete any existing Cassandra resources
echo "Cleaning up any existing Cassandra..."
kubectl delete statefulset cassandra -n soulmate-infra --ignore-not-found
kubectl delete service cassandra -n soulmate-infra --ignore-not-found
kubectl delete pvc -n soulmate-infra -l app=cassandra --ignore-not-found
kubectl delete job cassandra-init-verification -n soulmate-infra --ignore-not-found
kubectl delete configmap cassandra-init-script -n soulmate-infra --ignore-not-found

# Wait for cleanup
sleep 5

# Apply the deployment
echo "Deploying Cassandra..."
kubectl apply -f "cassandra-deployment.yaml"

echo "Waiting for Cassandra pod to be ready..."
kubectl wait --for=condition=ready pod -l app=cassandra \
  --namespace soulmate-infra \
  --timeout=300s
