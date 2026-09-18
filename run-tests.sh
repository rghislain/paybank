#!/bin/bash

PROJECT_DIR="$HOME/eclipse-workspace/java-backend-api/java-backend"
M2_DIR="$HOME/.m2"
NETWORK="supabase_network_java-backend-api"
IMAGE="maven:3.9-eclipse-temurin-17"

FAILED=0

run_test() {
    local TEST_NAME=$1

    echo "========================================"
    echo "Exécution de $TEST_NAME"
    echo "========================================"

    # Exécution isolée et propre dans le conteneur Docker MAVEN
    if docker run --rm \
        -v "$PROJECT_DIR:/app" \
        -v "$M2_DIR:/root/.m2" \
        -w /app \
        --network "$NETWORK" \
        --add-host=api.mokbank.internal:host-gateway \
        "$IMAGE" \
        mvn test -Dtest="$TEST_NAME" -e; then
        echo "✅ $TEST_NAME : OK"
    else
        echo "❌ $TEST_NAME : ÉCHEC"
        FAILED=1
    fi
    echo
}

# --- Liste des tests à exécuter via Docker ---
run_test "PaiementIntegrationTest"
run_test "MultiUtilisateursPaiementTest"
run_test "RapprochementServiceGestionPaiementTest"
run_test "JournalisationBDDAspectTest"
run_test "SecuriteAspectTest"
run_test "FinancialReportControllerTest"
run_test "DroitsAopIntegrationTest"
run_test "MontantCentimesTest"
run_test "ServicePaiementTest"

# --- Résultat Global ---
echo "========================================"
if [ $FAILED -eq 1 ]; then
    echo "❌ Au moins un test a échoué."
    exit 1
else
    echo "🎉 Tous les tests sont passés avec succès !"
    exit 0
fi
