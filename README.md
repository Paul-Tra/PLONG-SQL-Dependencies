#	Static SQL Analysis

## Description

Static SQL Analysis is a SQL Graph Maker Application (App). This App allows you to visualize a generated graph from SQL 
files which contained as a whole all the queries and operations on your Database (DB) 
which permitted to identify the shape of your DB and the behaviour inside it.
The purpose of this project-App is to bring out __*critical cycles*__ in your  DB.
With this App you are able to describe and assess the dependancies between the different Transactions of your DB. 
This project is an answer of the subject called 
[*"Requêtes en SQL et leur graphe des dépendences"*](https://www.irif.fr/~gio/teaching/2018-19/plong/projet_SQL_FR.pdf) .


## Prerequisites 

To use the App you must have a recent version of **java** (java 14 is needed).


## Installation

Use the git-commande clone to recover the App folders from the 
[GitLab](https://gaufre.informatique.univ-paris-diderot.fr/cadiou/cadiou-traore-plong-1920) with the URL :

####with SSH URL :

```bash
	git clone git@gaufre.informatique.univ-paris-diderot.fr:cadiou/cadiou-traore-plong-1920.git	
```
####with HTTPS URL :

```bash
	git clone https://gaufre.informatique.univ-paris-diderot.fr/cadiou/cadiou-traore-plong-1920.git
```
## Usage

### Before the launch :

Enter to the 'app' directory and run the app by the command **make**, then the App windows must appears and you can begin to use it.

### During the use :

To generate a graph from your SQL files you have to go to : *"File"*>*"Open File* and choose the folder which contains your
SQL files.

## Authors and acknoledgment 


