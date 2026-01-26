#!/bin/bash

#echo "Deploying debezium connect..."
#kubectl apply -f "debezium/debezium-connect-deployment.yaml"

echo "Deploying debezium ui..."
kubectl apply -f "debezium/debezium-ui-deployment.yaml"