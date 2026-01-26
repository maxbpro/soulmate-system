#!/bin/bash

echo "=== Deploying MinIO ==="
# Check and add MinIO repo
if ! helm repo list | grep -q "minio"; then
    echo "Adding MinIO Helm repository..."
    helm repo add minio https://charts.min.io/
fi

helm repo update

helm upgrade --install minio minio/minio \
  --namespace soulmate-infra \
  -f minio/minio-values.yaml