#!/bin/bash

echo "=== Deploying Keycloak ==="

kubectl create configmap keycloak-realm-config \
    --from-file=keycloak/realm-export.json \
    --namespace soulmate-infra \
    --dry-run=client -o yaml | kubectl apply -f -


# Create Keycloak PostgreSQL secret
kubectl create secret generic keycloak-postgres-secrets \
  --from-literal=username=postgres \
  --from-literal=password=postgres \
  --namespace=soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -

#Create the database keycloak manually
POSTGRES_PASSWORD=$(kubectl get secret keycloak-postgres-secrets -n soulmate-infra -o jsonpath='{.data.password}' | base64 -d)

kubectl exec -n soulmate-infra -it $(kubectl get pods -n soulmate-infra -l app.kubernetes.io/name=postgresql -o name) -- \
  bash -c "PGPASSWORD='${POSTGRES_PASSWORD}' psql -U postgres -c 'CREATE DATABASE keycloak;'"

kubectl apply -f keycloak/keycloak-deployment.yaml

