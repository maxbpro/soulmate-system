#!/bin/bash

echo "=== Deploying faceplusplus-api mock ==="

kubectl create configmap wiremock-mappings \
  --from-file=stubs.json=faceplusplus-api/mappings/stubs.json \
  --namespace soulmate-infra \
  --dry-run=client -o yaml | kubectl apply -f -


kubectl apply -f "faceplusplus-api/wiremock-deployment.yaml"