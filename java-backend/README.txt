**************************README********************************
Bonjour,

pour lancer l'application et respecter les standards de sécurité,
j'ai stocké les clefs secretes dans les variables d'environnement
suivant la liste ci dessous. 
Les clefs secretes sont dans le fichiers .env lu par le fichier
demarrer-backend.sh au demarrage de l'application.
Elles sont également disponibles dans le fichier compressé intitulé
PASSWORDS.txt car GIT n'autorise pas le type de fichier .env stockant
les clefs secretes et les mots de passes.

# Modèle de configuration des variables d'environnement
JWT_SECRET=remplacer_par_votre_cle_jwt
STRIPE_API_KEY=sk_test_remplacer_par_votre_cle_stripe
STRIPE_WEBHOOK_SECRET=whsec_remplacer_par_votre_webhook
EMAIL_PASSWORD=remplacer_par_votre_mdp_mail

# Mots de passe par défaut
ADMIN_PASSWORD=adminPass1
MANAGER1_PASSWORD=managerPass1
EMPLOYE1_PASSWORD=employePass1

Enfin, dans cette nouvelle version des points ont été améliorés:
*respect de l'architecture héxagonale,
*protection des clefs secretes,
*unification du mécanisme d'autorisation.

Merci.

Cordialement,

Ghislain Rochette
****************************************************************