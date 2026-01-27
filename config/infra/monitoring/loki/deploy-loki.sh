#!/bin/bash

echo "=== Deploying Loki ==="

kubectl apply -f monitoring/loki/loki-config.yaml

kubectl apply -f monitoring/loki/loki-deployment.yaml