#!/bin/bash

echo "=== Deploying Alloy ==="

kubectl apply -f monitoring/alloy/alloy-config.yaml

kubectl apply -f monitoring/alloy/alloy-deployment.yaml