#!/bin/bash

echo "=== Deploying Tempo ==="

kubectl apply -f monitoring/tempo/tempo-config.yaml

kubectl apply -f monitoring/tempo/tempo-deployment.yaml