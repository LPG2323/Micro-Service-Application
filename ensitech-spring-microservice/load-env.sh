#!/bin/bash

# Vérifie si le fichier .env existe
if [ -f .env ]; then
    echo "Loading environment variables from .env..."
    # Lit chaque ligne du fichier, ignore les commentaires et les lignes vides, puis exporte les variables
    export $(grep -v '^#' .env | xargs)
    echo "Environment variables loaded."
else
    echo "Warning: .env file not found!"
fi