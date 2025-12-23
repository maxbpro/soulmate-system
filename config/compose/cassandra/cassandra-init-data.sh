#!/bin/bash
echo "Creating keyspace and tables..."
cqlsh cassandra -f /init.cql
echo "Initialization complete."