#!/bin/bash

echo "=== Deploying Grafana ==="

kubectl apply -f monitoring/grafana/grafana-datasources.yaml

kubectl apply -f monitoring/grafana/grafana-deployment.yaml