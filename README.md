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

_If you have any problem, try with **OpenJDK** 14._


## Installation

Use the git-commande clone to recover the App folders from the 
[GitLab](https://gaufre.informatique.univ-paris-diderot.fr/cadiou/cadiou-traore-plong-1920) with the URL :

#### with SSH URL :

```bash
$	git clone git@gaufre.informatique.univ-paris-diderot.fr:cadiou/cadiou-traore-plong-1920.git	
```
#### with HTTPS URL :

```bash
$	git clone https://gaufre.informatique.univ-paris-diderot.fr/cadiou/cadiou-traore-plong-1920.git
```
## Usage

The python part is inside the App , so you don't have to process it before.
( all folders you want to process must be in **Parser_python_tmp** folder )

So , for example , if you want to study a folder name **toto** , just copy your folder in **Parser_python_tmp**.
``` bash 
$ 	cp -r Path_to_toto/toto Path_to_app/../Parser_python_tmp
```
Make sur your folder will be in the Parser folder.

## Launch

You must go to the **app** directory, and then execute the command: 
```bash
$ 	cd cadiou-traore-plong-1920/app/
$ 	make
```
### During the use

* To generate a graph from your SQL files you have to select the 'folder' menu , then choose "Parser_python_tmp" , then choose your folder ( try with different test ).

* Then press the 'Open' button to generate the graph.

* You can delete and generate the same graph with an other configuration by using the 'clear/launch' button.

### Additional Information

Feel free to see the **Tools** menu , it give you some possibilities to custom your graph.
Also , you can export your graph in the **Edit** menu.
Then , if you only want to study graph without taking care about dependencies , you can load the **graphml file** by choosing **File** in the **File** menu ( and then , choose your _.gaphml_ file ).

## Authors

TRAORE Paul & CADIOU Léo-Paul
Fell free to see our video in YouTube : 
```
https://www.youtube.com/watch?v=v1lFgFxjT8U&feature=youtu.be
```

