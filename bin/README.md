## Installation et lancement

Les droits des deux repository ont été donné à M.Martinet en tant que Maintainer.

**Spring** : git clone https://gitlab.utc.fr/gharbwas/sr03_devoir2.git
> lancer le projet depuis ChatApplication.java

**React** : git clone https://gitlab.utc.fr/wuhanlin/sr03-devoir2-react.git

Depuis le dossier crée :
> $ npm install react-scripts --save (facultatif si déjà installer)
> $ npm start

## Architecture

Nous avons un serveur Spring pour traiter les opérations de l'application et accèder à l'interface d'administration. Il offre une API Rest qui permet à React d'offir une interface utilisateur interactive aux clients et de les connecter aux chatrooms par websockets. 
On arrive à l'architecture suivante : 

 ![image](images/archi.png)




Notre architecture s'inspire du modèle MVC : MySQL (Modele) - React (Vue) - Spring (Controller) 

#### Organisation du projet : ####

- Package config : Lien entre le back (Spring) et le front (React)
- Package controller :
    - Class AdminController : Toutes les fonctionnalité de l'Admin
    - Class LoginController : Accès à l'interface Utilisateur ou Admin 
- Package dao : Communication avec la base de données, tri avant envoi de la requete
    - Class ChatroomRepository : Infos chatrooms
    - Class UserRepository : Infos User
- Package user : Modele des objets User et Chatrooms
    - Class Chatroom
    - Class User
- Package security : Fonctinnalités de sécurité sur l'authentification 
- Package service : interfaces et leurs implémentations
- Package utils : facilite certaines méthodes
- Package websocket : Etablissement des Websockets

## Interactions entre React, Spring et les WebSockets

React est la bibliothèque JavaScript nous permettant de construire l'interface utilisateur de manière interactive, coté client.
Dans notre application, Spring est utilisé pour créer une API REST qui expose les fonctionnalités de votre application au client React. Il nous facilite la gestion des requêtes HTTP. On se situe alors plutot du coté serveur.
Spring renvoie ainsi une websocket aux clients pour connecter les utilisateurs aux chats


## Modele relationnel

![image](images/UML.png))

## Eco-index

On réalise une analyse sur 3 parties pour lesquels on obtient la note de A pour tous. Cependant, il faut les mettre en perspective avec le fait que l'application est assez simple.

- Partie Utilisateur avec React : 

![image](images/react.png)

- Partie Utilisateur avec Spring : 

![image](images/chatroom.png)

- Partie Chatroom avec Websockets : 

![image](images/spring.png)





