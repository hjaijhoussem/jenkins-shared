#!/bin/bash

# Parameters from Jenkins
IMAGE_NAME=$1
SEVERITY=$2
EXIT_CODE=$3

# Run trivy scan
trivy image $IMAGE_NAME \
    --severity $SEVERITY \
    --exit-code $EXIT_CODE \
    --format json -o trivy-image-$SEVERITY-results.json
