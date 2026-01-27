#!/bin/bash

echo "=== Deploying prometheus ==="

kubectl apply -f monitoring/prometheus/prometheus-config.yaml -n soulmate-infra

kubectl apply -f monitoring/prometheus/prometheus-deployment.yaml -n soulmate-infra