#!/bin/bash

echo "=== Deploying PostgreSQL ==="
# Add Bitnami Helm repo if not already added
if ! helm repo list | grep -q "bitnami"; then
    echo "Adding Bitnami Helm repository..."
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo update
fi

helm upgrade --install postgres bitnami/postgresql \
  --namespace soulmate-infra \
  -f postgres/postgres-values.yaml