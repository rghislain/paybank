FROM php:8.2-apache
# Activation du module de réécriture d'URL si besoin
RUN a2enmod rewrite
# Copie de vos fichiers HTML/PHP dans le dossier du serveur web
COPY . /var/www/html/
EXPOSE 80

