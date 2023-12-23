# Pay My Buddy
Repository pour le projet 6 de la formation développeur d'application Java

### Instructions d'installation de la BDD
- Créer la BDD paymybuddy sur son serveur MySQL
- Exécuter le fichier [init_db.sql](src/test/resources/init_db.sql)
- Modifier le mot de passe dans le fichier application.properties

### Utilisation de l'application
- Exécuter l'application et aller sur http://localhost:8080/
- La BDD contient des comptes utilisateurs (exemple : pauline.test@mail.com), le mot de passe par défaut est password
- Sinon, il est possible de créer un nouveau compte (page http://localhost:8080/register) ou de se connecter avec un compte Google

### Prépararer son environement pour les tests unitaires
- Créer la BDD paymybuddy_test sur son serveur MySQL
- Modifier le mot de passe dans le fichier test.properties

## Diagramme de classes
![diagramme de classes p6](https://github.com/git-mg-dev/galvan-marc-projet-6-java/assets/144458198/3ae9b5a2-e3ae-4962-b3f1-1e234e1aa707)

## Modèle physique de données
![modele physique donnees](https://github.com/git-mg-dev/galvan-marc-projet-6-java/assets/144458198/fe38adea-4e5e-4d17-8b7c-f148083bc552)

## Script pour construire et remplire la BDD
[init_db.sql](src/test/resources/init_db.sql)
