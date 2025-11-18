# Commandes 

## Inscription

curl -i -X POST \
  http://127.0.0.1:8080/interface-aller/webapi/routeur/inscription \
  -H 'Content-Type: application/json' \
  -d '{"login":"toto","password":"secret"}'


## Connexion
  curl -i -X POST \
  http://127.0.0.1:8080/interface-aller/webapi/routeur/connexion \
  -H 'Content-Type: application/json' \
  -d '{"login":"toto","password":"secret"}'