#!/bin/bash

# Create namespace
kubectl create namespace soulmate-infra --dry-run=client -o yaml | kubectl apply -f -


# Deploy postgres
chmod +x postgres/deploy-postgres.sh
postgres/deploy-postgres.sh

# Deploy minio
chmod +x minio/deploy-minio.sh
minio/deploy-minio.sh

#Deploy cassandra
chmod +x cassandra/deploy-cassandra.sh
cassandra/deploy-cassandra.sh

#Deploy kafka
chmod +x kafka/deploy-kafka.sh
kafka/deploy-kafka.sh

#Deploy debezium-connect
chmod +x debezium/deploy-debezium.sh
debezium/deploy-debezium.sh

#Deploy keycloak
chmod +x keycloak/deploy-keycloak.sh
keycloak/deploy-keycloak.sh

# Deploy elasticsearch
chmod +x elasticsearch/deploy-elasticsearch.sh
elasticsearch/deploy-elasticsearch.sh

#Deploy prometheus
chmod +x monitoring/prometheus/deploy-prometheus.sh
monitoring/prometheus/deploy-prometheus.sh

#Deploy Tempo
chmod +x monitoring/tempo/deploy-tempo.sh
monitoring/tempo/deploy-tempo.sh

#Deploy Loki
chmod +x monitoring/loki/deploy-loki.sh
monitoring/loki/deploy-loki.sh

#Deploy Alloy
chmod +x monitoring/alloy/deploy-alloy.sh
monitoring/alloy/deploy-alloy.sh

#Deploy grafana
chmod +x monitoring/grafana/deploy-grafana.sh
monitoring/grafana/deploy-grafana.sh

#Deploy faceplusplus-api
chmod +x faceplusplus-api/deploy-faceplusplus-api.sh
faceplusplus-api/deploy-faceplusplus-api.sh

echo "=== Creating secrets ==="

# Create PostgreSQL secret
kubectl create secret generic postgres-secrets \
  --from-literal=username=postgres \
  --from-literal=password=postgres \
  --dry-run=client -o yaml | kubectl apply -f -

# Create MinIO secret with default credentials
kubectl create secret generic minio-secrets \
  --from-literal=access-key=user \
  --from-literal=secret-key=password \
  --dry-run=client -o yaml | kubectl apply -f -

# Create Face API secret
kubectl create secret generic face-api-secrets \
  --from-literal=api-key=test \
  --from-literal=api-secret=secret \
  --dry-run=client -o yaml | kubectl apply -f -

# Elasticsearch secret
kubectl create secret generic elasticsearch-secrets \
  --from-literal=username=elastic \
  --from-literal=password=password \
  --dry-run=client -o yaml | kubectl apply -f -
