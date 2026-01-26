#!/bin/bash

# Create namespace
kubectl create namespace soulmate-infra --dry-run=client -o yaml | kubectl apply -f -


# Deploy postgres
#chmod +x postgres/deploy-postgres.sh
#postgres/deploy-postgres.sh
#sleep 10
#
## Deploy minio
#chmod +x minio/deploy-minio.sh
#minio/deploy-minio.sh
#sleep 5

#Deploy cassandra
#chmod +x cassandra/deploy-cassandra.sh
#cassandra/deploy-cassandra.sh
#sleep 5

##Deploy kafka
#chmod +x kafka/deploy-kafka.sh
#kafka/deploy-kafka.sh

##Deploy debezium-connect
chmod +x debezium/deploy-debezium.sh
debezium/deploy-debezium.sh

echo "=== Creating secrets ==="

# Create PostgreSQL secret
kubectl create secret generic postgres-secrets \
  --from-literal=username=postgres \
  --from-literal=password=postgres \
  --namespace=soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -

# Create MinIO secret with default credentials
kubectl create secret generic minio-secrets \
  --from-literal=access-key=user \
  --from-literal=secret-key=password \
  --namespace=soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -

# Create Face API secret
kubectl create secret generic face-api-secrets \
  --from-literal=api-key=test \
  --from-literal=api-secret=secret \
  --namespace=soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -

# Elasticsearch secret
kubectl create secret generic elasticsearch-secrets \
  --from-literal=username=elastic \
  --from-literal=password=password \
  --namespace=soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -

# Cassandra secret
#kubectl create secret generic cassandra-secrets \
#  --from-literal=username=cassandra_user \
#  --from-literal=password=cassandra_password \
#  --namespace=soulmate-infra \
#  --dry-run=client -o yaml | kubectl apply -f -