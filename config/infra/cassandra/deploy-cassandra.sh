#!/bin/bash

# Delete any existing Cassandra resources
echo "Cleaning up any existing Cassandra..."
kubectl delete statefulset cassandra -n soulmate-infra --ignore-not-found
kubectl delete service cassandra -n soulmate-infra --ignore-not-found
kubectl delete pvc -n soulmate-infra -l app=cassandra --ignore-not-found
kubectl delete configmap cassandra-init-script -n soulmate-infra --ignore-not-found

# Wait for cleanup
sleep 5

# Apply the deployment
echo "Deploying Cassandra..."
kubectl apply -f "cassandra/cassandra-deployment.yaml"

echo "Waiting for Cassandra pod to be ready..."
kubectl wait --for=condition=ready pod -l app=cassandra \
  --namespace soulmate-infra \
  --timeout=300s

# Get the Cassandra pod name
CASSANDRA_POD=$(kubectl get pod -l app=cassandra -n soulmate-infra -o jsonpath="{.items[0].metadata.name}")

echo "Cassandra pod: $CASSANDRA_POD"

# Wait for Cassandra to actually be ready to accept connections
echo "Waiting for Cassandra to be fully ready..."
sleep 30

# Check if keyspace already exists
echo "Checking if keyspace 'swipe' already exists..."
KEYSPACE_EXISTS=$(kubectl exec -n soulmate-infra $CASSANDRA_POD -- \
  cqlsh -u cassandra_user -p cassandra_password -e "DESCRIBE KEYSPACES" 2>/dev/null | grep swipe)

if [ -z "$KEYSPACE_EXISTS" ]; then
    echo "Keyspace 'swipe' does not exist. Creating..."

    # Create keyspace and tables
    kubectl exec -n soulmate-infra $CASSANDRA_POD -- \
      cqlsh -u cassandra_user -p cassandra_password -e "
      CREATE KEYSPACE IF NOT EXISTS swipe WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

      USE swipe;

      CREATE TABLE IF NOT EXISTS swipe (
                                         userPair     text, -- partition key: smaller_id:larger_id
                                         userId       uuid, -- clustering key
                                         swipedUserId uuid, -- clustering key
                                         liked        boolean,
                                         createdAt    timestamp,
                                         PRIMARY KEY ((userPair), userId, swipedUserId)
      );

      CREATE TABLE IF NOT EXISTS match (
                                         userPair   text PRIMARY KEY, -- partition key
                                         user1Id    uuid,
                                         user2Id    uuid,
                                         createdAt  timestamp
      );"

    echo "Keyspace and tables created successfully."
else
    echo "Keyspace 'swipe' already exists."
fi

echo "Verifying keyspace creation..."
kubectl exec -n soulmate-infra $CASSANDRA_POD -- \
  cqlsh -u cassandra_user -p cassandra_password -e "DESCRIBE KEYSPACES; DESCRIBE TABLES;" 2>/dev/null

echo "Cassandra deployment complete!"