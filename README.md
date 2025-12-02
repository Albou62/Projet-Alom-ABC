# Projet-Alom


1. votre architecture de code (noms des projets et lien avec le microservice sur votre schéma). Les noms de package dans votre code doivent se suffire à eux-même pour comprendre ce dont il s'agit.

Notre application est composée de 5 microservices:

- authentification
- channel-producer
- interface-aller
- interface-retour
- message

2. les difficultés/blocages rencontrées (que vous les aillez résolus ou non)

Connexion en webservice avec l'interace retour
Solution: mise en place d'un listener pour lancer la partie socket tcp avec le web service

Différenciation entre message direct et channel
Solution: séparer en 2 microservices

Kafka Consumer
Rassemblement en 1 seul consumer au lieu de 2 pour chaque microservices (message et channel)



