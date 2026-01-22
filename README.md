Postgres

docker compose down
#WARN -v supprime le volume, à utiliser en conscience
docker compose down -v

docker compose up -d

Log
docker logs -f ticketing-postgres

Run
mvn spring-boot:run -e


ARCHI Hexagonale

api : Concerne tout ce qui est visible de l'extérieur, endpoint, DTO, record, response, controlleur, endpoint
        - Dépendance : appelle application pour les use case et les DTO (modèle entré/sortie)
        - Indépendance : L'infrastructure doit être totalement décorélé de l'api
        - Pas de logique, que des actions listant les use case

domain : Contient les règles métiers, zéro dépendance technique
        - TODO : Ajouter les règles de changement d'état pour donner du sens au dommaine, invariants (règles tjrs vrais), validation métier.
        - Aucun lien avec spring/JPA/HTTP
        - Contient idéalement : Entité, Value Object, Service domaine si nécessaire, exceptions métier

infrastructure : Contient les liens externe vers la BDD et les autres action possible vers des systèmes externes (impl)
        - JPA entities + Spring Data repos
        - détails techniques (DB (JPA/Spec), files, HTTP clients). Implémente les ports.

application : Orcherstre la logique métier et ressence les cas d'utilisation (contrat)
        - Ne dois pas faire du métier, elle coordonne le domaine
        - Elle devrait dépendre d'interfaces (ports), pas de classes infra concrètes


Norme hexagonale, c’est :

 - Dépendances dirigées vers le cœur

 - Ports/adapters aux frontières

 - Domaine isolé
