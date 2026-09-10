#!/bin/bash

# Configuration des chemins (à ajuster si nécessaire)
DOSSIER_PROJET="$HOME/eclipse-workspace/java-backend-api"
DOSSIER_JAVA="$DOSSIER_PROJET/java-backend"
NOM_RESEAU="supabase_network_java-backend-api"
NOM_CONTENEUR="java-backend-api"

echo "===================================================="
echo "🚀 DÉMARRAGE DE L'ENVIRONNEMENT JAVA BACKEND & BDD"
echo "===================================================="

# 1. Démarrage de la base de données Supabase
echo "📂 Déplacement vers le dossier du projet..."
cd "$DOSSIER_PROJET" || { echo "❌ Dossier du projet introuvable !"; exit 1; }

echo "💾 Démarrage de Supabase..."
supabase start

# 2. Vérification et création du réseau Docker si nécessaire
echo "🌐 Vérification du réseau Docker '$NOM_RESEAU'..."
if ! docker network ls | grep -q "$NOM_RESEAU"; then
    echo "⚠️ Le réseau '$NOM_RESEAU' n'existe pas. Tentative de création..."
    docker network create "$NOM_RESEAU"
else
    echo "✅ Réseau Docker opérationnel."
fi

# 3. Nettoyage de l'ancien conteneur s'il existe
echo "🧹 Nettoyage de l'ancien conteneur Docker..."
docker rm -f "$NOM_CONTENEUR" 2>/dev/null

# 4. Démarrage du serveur Spring Boot dans Docker
echo "🏗️ Lancement du conteneur Spring Boot (Port 8080)..."
docker run -d \
    --name "$NOM_CONTENEUR" \
    -v "$DOSSIER_JAVA:/app" \
    -v "$HOME/.m2:/root/.m2" \
    -w /app \
    --network "$NOM_RESEAU" \
    --add-host=api.mokbank.internal:host-gateway \
    -p 8080:8080 \
    maven:3.9-eclipse-temurin-17 \
    mvn clean spring-boot:run

# Note : J'ai rajouté 'clean' avant 'spring-boot:run' pour s'assurer que les modifications
# sur les contrôleurs ou le fichier index.html soient bien recompilées à chaque démarrage.

echo "----------------------------------------------------"
echo "✅ Tout est lancé ! Passage sur l'affichage des logs..."
echo "💡 Astuce : Appuyez sur [Ctrl + C] pour quitter les logs sans éteindre le serveur."
echo "----------------------------------------------------"
sleep 2

# 5. Suivi des logs en direct
docker logs -f "$NOM_CONTENEUR"
