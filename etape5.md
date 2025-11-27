curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/inscription \
  -H "Content-Type: application/json" \
  -d '{"login":"alice","password":"pass123"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/inscription \
  -H "Content-Type: application/json" \
  -d '{"login":"bob","password":"pass456"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/connexion \
  -H "Content-Type: application/json" \
  -d '{"login":"alice","password":"pass123"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/connexion \
  -H "Content-Type: application/json" \
  -d '{"login":"bob","password":"pass456"}'


-------------------------------------------------------------------------------------------------

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/subscribe \
  -H "Content-Type: application/json" \
  -d '{"channel":"general","token":"z25p4myu6jc5fg1"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/subscribe \
  -H "Content-Type: application/json" \
  -d '{"channel":"general","token":"kvyw6j20hoj9vrr"}'

nc 127.0.0.1 9090

-------------------------------------------------------------------------------------------------------

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/channel \
  -H "Content-Type: application/json" \
  -d '{"message":"[general] (Alice) Bonjour tout le monde!","token":"z25p4myu6jc5fg1"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/channel \
  -H "Content-Type: application/json" \
  -d '{"message":"[general] (Bob) Salut Alice, comment vas-tu?","token":"kvyw6j20hoj9vrr"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/unsubscribe \
  -H "Content-Type: application/json" \
  -d '{"channel":"general","token":"z25p4myu6jc5fg1"}'

curl -X POST http://127.0.0.1:8080/interface-aller/webapi/routeur/channel \
  -H "Content-Type: application/json" \
  -d '{"message":"[general] (Bob) Alice ne reçoit plus ce message","token":"kvyw6j20hoj9vrr"}'