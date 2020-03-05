import re
from primary import *
from parser import *
from parser_ecriture import *
from dependance import *

class Parser:
    def __init__(self , nom_fichier , dossier ):
        
        self.nom_fichier = nom_fichier
        # #print(nom_fichier)
        name = nom_fichier.split("/")
        name = name[len(name)-1]
        self.name = name
        # #print(name)
        self.dictionnaire_attributs = {}
        # dictionnaire des tables touchées par le from , au format "Nom_table , nom_raccourcie"
        self.table_from = {}
        self.liste_attributs_read = []
        # attributs r au format { attribut : table }
        self.liste_r = {}
        self.liste_finale_attribut_lecture = {}
        self.mot_cle = ["WHERE","SELECT","INSERT","INTO","DECLARE","FOR","IF","ELSE","CURSOR","ORDER","BY","DESC;","DESC"]
        self.file = None
        self.couple = dict()
        self.data = ""
        self.liste_param_fonction = []
        # on va stocker les couples de dependance [ table : parametre de fonction ] on sait au préalable que la correspondance avec la / les clés primaire sont correctes
        self.couple_dependance = dict()
        self.dependance_supplementaire = dict()
        self.dossier = dossier
        
    def lecture_fichier(self):
        F = open(self.nom_fichier,"r") 
        #for ligne in F :
             #print(ligne)
        F.close()
        
    def trouve_requete(self ):
        data = " "
        # on ouvre le fichier passer en argument .
        with open(self.nom_fichier,"r") as F :
            for ligne in F :
                #retire les espaces de la igne 
                ligne = ligne.strip()
                #si la igne est vide , equivalent à \n\n\n par exemple
                if not ligne :
                    # #print("#ligne vide.")
                    # rien a faire ...
                    a= 0 
                # sinon on traite la ligne et l'ajoute a notre liste
                else :
                    data += " "+ligne
                    # #print(sans_saut)
        # on se retrouve avec une tres grande ligne , que l'on va pouvoir parser .
        # privée des espaces inutiles , et des retours chariot
        self.data = data
        return data
    
    #..................................................................................................................
    #Parsing du fichier pour trouver la table et ces attributs
    def analyse_table(self , data ):
        # #print("Analyse Table : \n")
        # #print(data)
        # #print("\n")
        declare = re.findall("DECLARE .* BEGIN",data)
        # #print(declare)
        return declare
        
    def determine_attributs_du_fichier(self ,liste_du_declare):
        # #print("Dans la fonction pour determiner les attributs du fichier en cours : ")
        # #print(liste_du_declare[0]) # on affiche la liste , ici un seul declare donc le premier element
        # on retire les espaces inutiles , et on separe la ''sous_liste'' grace au " ; "
        liste = liste_du_declare[0].split(";")
        # #print(liste)
        # #print("On va separer le nom de l'attribut de son type ... ")
        # #print("Liste des attributs du fichier ( au format , nom : type ) : ")
        for elt in liste :
            #elt = elt.split()
            # voir pour modifier ce regex là et matcher les (x,y) des NUMERIC ou des VARCHAR
            couple = re.findall("[a-z]+.* .*[A-Z]",elt)
            if ( len(couple) == 1 ) :
                # #print(couple)
                res = couple[0].split()
                # #print("res : " + res[0] + " " + res[1] )
                self.dictionnaire_attributs[res[0]] = res[1]
                
    def affiche_liste_attributs(self):
        a=1
         #print("LISTE ATTRIBUTS DECLARE...")
        #for nom,type in self.dictionnaire_attributs.items() : # on recuperer le nom et le type de l'attribut
             #print(nom + " : " + type )
            
            
    #........................................................................................................................
    # pour les attributs du FROM        
    def analyse_table_du_from(self,data):
            #table_from = re.findall("FROM [A-Z]+ [A-Z]+ WHERE",data)
            # on recuperer le contenue entre le From et le Where
            table_from = re.findall("FROM .*? WHERE",data)
            # #print(table_from)
            #res_from = table_from[0].split()
            # #print("res == " + res_from )
            return table_from
    
    def determine_attributs_du_from(self,liste_du_from):
        # on obtiens toutes les {tables:abreviation} des requettes
        # voir por aussi gerer les orderby , ainsi que les declare en milieu de fichier 
        #................................................................................
        # #print(liste_du_from)
        # #print("OKKKKKKKKKKKKKKKKK")
        for i in range (0, len(liste_du_from)):
            liste = liste_du_from[i].split()
            # #print(liste)
            for j in range(0,len(liste)) :
                # #print("liste "+str(j)+" : " + str(liste[j]) )
                if ( len(liste) == 3 ):
                    self.table_from[liste[j+1]] = liste[j+1]
                    break ;
                
                if ( liste[j] == "FROM" or liste[j] == "WHERE" or liste[j+1] == "FROM" or liste[j+1] == "WHERE" ):
                    if ( liste[j] == "ORDER" ):
                        j = len(liste)
                         #print("rip")
                else :
                    if ( liste[j] not in self.table_from.values() and  liste[j] not in self.table_from.keys() and liste[j] not in self.mot_cle and liste[j+1] not in self.mot_cle):
                        tmp = re.match("[A-Z]+",liste[j])
                        # #print(liste[j])
                        # #print(tmp)
                        if ( tmp is not None ):
                            if ( liste[j+1][len(liste[j+1])-1] == ',' ) :
                                # si le dernier carractere est une virgule , on la retire 
                                liste[j+1] = liste[j+1][:-1]
                            self.table_from[liste[j+1]] = liste[j]
    
    def affiche_liste_from(self):
         #print("TABLE FROM...")
         a=1
        #for nom,type in self.table_from.items() : # on recuperer le nom et le type de l'attribut
             #print(nom + " : " + type )
            
    #...........................................................................................................................
    # determine les attributs en lecture :
    def analyse_select ( self,data):
        # #print("CONTENU SELECT... : ")
        select = re.findall("SELECT .*? [IA-Z]*?FROM",data)
        # #print(select)
        # on a la liste du select + le into si besoin
        return select
    
    def determine_attributs_select(self,liste_select):
        for i in range (0, len(liste_select)):
            liste = liste_select[i].split()
            # #print(liste)
            for elt in liste :
                res = re.findall("[A-Z]*\.*[a-z].*[a-z]",elt)
                # #print(res)
                if res != [] :
                    if ( res not in self.liste_attributs_read ):
                        self.liste_attributs_read.append(res)
                        if ( len(res[0].split(".")) > 1 ) :
                            l = res[0].split(".")
                            # #print(l[0] + " -> " + l[1] )
                            if ( l[0] in self.liste_r.values() ):
                                # #print("OK")
                                self.liste_r[l[1]] = (self.liste_r[l[1]] , l[0] )
                            
    def affiche_liste_select(self):
         #print("Liste attributs du Select ( attributs en lecture : READ ) :")
         a=1
        #for elt in self.liste_attributs_read :
             #print(elt)
            
    #...........................................................................................................................
    # on va determiner les attributs de la clauses Where
    def analyse_where ( self,data):
        # #print("CONTENU WHERE... : ")
        where = re.findall("WHERE .*?;",data)
        # #print(where)
        # on a la liste du select + le into si besoin
        return where
    
    def determine_attributs_where(self , liste_where ):
        # voir comment finir avec le where
        for i in range (0, len(liste_where)):
            liste = liste_where[i].split()
            # #print(liste)
            tmp = liste_where[i]
            y = re.findall("[A-z]*\.[A-z]* = [A-z]*\.[A-z]*",tmp)
            # #print("YYYYYYYYYYYYYYYYYYY = " + str(y) )
            if ( y != [] ):
                # nous avons une relation de la forme : ['C.wId = W.wId']
                for a in y :
                    l = a.split(" = ")
                    elt = l[0]
                    elt = elt.split(".")
                        #self.dependance_supplementaire[self.table_from[elt[0]]] = elt[1]
                    self.dependance_supplementaire[elt[1]] = [self.table_from[elt[0]],self.table_from[l[1].split(".")[0]]]
                
            for i in range(1,len(liste)) : # commencer a 1 pour eviter de matcher le WHERE de la clause
                res = re.findall("[A-Z]*\.*[a-z].*[a-z]",liste[i])
                if ( res == [] ) :
                    # si le regex ne match pas un attribut , c'est un operateur obligatoirement
                    op = re.findall(".*",liste[i])
                    # rien a faire avec l'operaeur , ne change en rien les dependances
                    # #print("operateur : " + op[0])
                else :
                    if ( res != "WHERE" ):
                        # #print(res)
                        # on s'interresse a ce cas là pour matcher quel attributs vont aller avec quelles tables / fonctions 
                        couple_table_attributs = res[0].split(".")
                        # #print(couple_table_attributs)
                        
                        if ( len(couple_table_attributs) > 1 ):
                            # #print("attributs : " + res[0])
                            # le cas ou le transaction regarde une autre table 
                            if ( couple_table_attributs[0] in self.liste_r.keys() ):
                                # #print("OK")
                                self.liste_r[couple_table_attributs[1]] = self.liste_r[couple_table_attributs[1]] , couple_table_attributs[0] 
                                #self.liste_r[couple_table_attributs[1]].append(couple_table_attributs[0] )
                            else :
                                self.liste_r[couple_table_attributs[1]] = couple_table_attributs[0]
                                # #print("OK2")
                        else :
                            a = 0 # pour occuper une ligne en attendant la réponse a notre euestion
                            self.liste_r[couple_table_attributs[0]] = "_"
                            # #print("Attribut de fonction : " + res[0] + " .. nothing to do ?")
                            #............................................................................................
                            # cas ou la transaction opere sur un attribut propre a elle meme , aucun conflits possible ?
                            #.............................................................................................
        
    def affiche_liste_r(self):
        a=1
         #print("Attributs de lecture...de la fonction en cours \n")
        #for nom,table in self.liste_r.items() : # on recuperer le nom et la table de l'attributs
            # #print(str(table)+"."+nom +" , " +nom + " dans " + str(table) )
             #print(nom,table)
            # ici on a la liste de toutes les tables ou l'attribut portant le nom "nom" a été trouvé
            #if ( table in self.table_from.keys() ):
                # #print(table + " correspondant a la table : " + self.table_from[table] )
                #a=1
                
    def met_a_jour_liste_r(self):
        for values,table in self.liste_r.items() :
            # #print(values)
            #liste_finale_attribut_lecture = [table[i][j] for i in range (0,len(table)) for j in range(0,len(table[i])) ]
            # #print(liste_finale_attribut_lecture)
            # #print("---------------------------------------")
            # #print(table)
            tmp = []
            if ( table not in self.table_from.keys() ) :
                table = [table[i] for i in range (0,len(table))]
                # #print(values,table)
                tmp = []
                for v in table :
                    # #print("v :" + str(v))
                    while ( type(v) is tuple and len(v) > 0 ): # nous avons encore un tuple
                        # #print(len(v))
                        if ( len(v) > 1 ):
                            # #print(v[0])
                            # #print(v[1])
                            tmp.append(v[1])
                            v = v[0]
                        else :
                            tmp.append(v[0])
                        #for i in range(0,len(v)) :
                            # #print(v[i])
                            #i = i+1
                            #for i in v :
                            # #print("cas plus 1")
                            # #print(i)
                            #if ( i not in tmp and type(i) is not tuple ) :
                             #    #print(str(i) + " ajouté")
                             
                             
                             #   tmp.append(i)
                           
                        # #print(v)
                        #v = v[1:len(v)]    
                        
                        if ( len(v) == 0 ) :
                            break
                    else :
                        if ( v not in tmp ):
                            # #print(v)
                            tmp.append(v)
                    #tmp.append(v)
            else :
                tmp.append(table)
                
            # #print(tmp)       
            # #print([values]) 
            self.liste_r[values] = tmp
            tmp = []
            # #print(table)
            # #print(" pour l'attribut -> " + values )
            
            
    def cree_liste_finale(self):
        self.liste_finale_attribut_lecture = { a : [] for a in self.table_from.values()}
        #for values,table in self.liste_finale_attribut_lecture.items() :
            # #print(values,table)
        
            
            
    def affiche_liste_finale(self):
        # #print("Affiche liste finale")
        #for values,table in self.liste_r.items() :
            # #print(values,table)
        # #print("Affichage de chaque attributs touchés par cette transaction pour chaque Table de la BDD :")
        for values,table in self.liste_r.items() :
            # #print(values)
            for t in table :
                # #print(t)
                # #print()
                if ( ( t in self.liste_r[values] or values in self.liste_r[t] ) and t in self.table_from.keys() ):
                    # #print(t)
                    # #print(values)
                    # #print(self.table_from[t])
                    # #print("On lit : " + t + " ( "+ self.table_from[t] + " ) -> " + values )
                    # #print(values)
                    if ( t not in self.liste_finale_attribut_lecture.keys() ) :
                        #if ( type(t) is not tuple and values not in self.liste_finale_attribut_lecture[self.table_from[t]]):
                        if ( type(t) is not tuple and values not in self.liste_finale_attribut_lecture[self.table_from[t]]):
                            self.liste_finale_attribut_lecture[self.table_from[t]].append(values)
                    
        # #print("Affichage final des Tables : liste Attributs ....\n" )
        #for values,table in self.liste_finale_attribut_lecture.items() :
            # #print(values,table)
            
    def lanceur(self):
        contenu_du_fichier = self.trouve_requete()
        contenu_declare = self.analyse_table(contenu_du_fichier)
        contenu_from = self.analyse_table_du_from(contenu_du_fichier)
        contenu_select = self.analyse_select(contenu_du_fichier)
        contenu_where = self.analyse_where(contenu_du_fichier)
        # #print("WHERE :: " + str(contenu_where) )
        liste_from = self.determine_attributs_du_from(contenu_from)
        liste_attributs = self.determine_attributs_du_fichier(contenu_declare)
        liste_select = self.determine_attributs_select(contenu_select)
        liste_where = self.determine_attributs_where(contenu_where)
        self.cree_liste_finale()
        #self.affiche_liste_select()
        self.affiche_liste_from()
        #self.affiche_liste_attributs()
        self.met_a_jour_liste_r()
        #self.affiche_liste_r()
        self.affiche_liste_finale()
        
        #self.pkey = PrimaryKey("./fichiers/genDB.sql")
        self.pkey = PrimaryKey("./"+self.dossier+"/genDB.sql") # pour nos test
        self.pkey.lanceur()
        #self.trouve_cle_primaire_associe_au_from()
        self.trouve_attribut_de_fonction()
        self.trouve_cle_dep_possible()
        self.affiche_couple_dep()

    def trouve_cle_primaire_associe_au_from(self):
        a=1
         #print("\nassociation clé primaire -> table utilisée ")
        #for nom,type in self.table_from.items() : # on recuperer le nom et le type de l'attribut
            # #print(nom + " : " + type )
             #print(type + " utilisé " + str(self.pkey.couple[type]) )
            
            
    def trouve_attribut_de_fonction(self):
         #print("\nrecherche des attributs de fonction")
        self.data = self.data.replace("NUMERIC(2)","INTEGER")
        self.data = self.data.replace("VARCHAR(16)","INTEGER")
        m = re.findall("FUNCTION .*?[{]*? RETURNS",self.data)
        # #print(m)
        n = re.findall("\(.*?\)",str(m))
        # #print(n)
        m = re.findall("[a-z]+[_]*[A-Za-z]+[_]*[A-Za-z]+",str(n))
         #print(m)
         #print("\nListe des parametre de la fonction : " )
        for elt in m :
            self.liste_param_fonction.append(elt)
             #print(elt )
            self.couple_dependance[elt] = []
        
            
    def trouve_cle_dep_possible (self):
         #print("OLOLOLLOLOLO")
        # #print('\n'+self.data)
        # #print("\n")
        # #print(self.liste_param_fonction)
        # #print("\n")
        m = re.findall("WHERE .*?;",self.data)
         #print("")
         #print(m)
        for elt in m :
            elt = elt.replace("WHERE","")
            elt = elt.replace(" AND ",",")
            elt = elt.replace(" OR ",",")
            elt = elt.strip().replace(" ","").replace(";","")
            # #print(elt)
            liste = elt.split(",")
             #print(str(liste))
            # on separe les clause du where : D.wid=w_id , pour regarder si la clause contient un parametre de la fonction
            for i in liste :
                 #print(" i ==== " + i )
                for item in self.liste_param_fonction :
                     #print("ITEM = " + item ) 
                    l = i.split("=")
                    if ( item in l ):
                         #print(item + " présent dans " + str(l) )
                        self.aux(l)
                    else :
                        l = i.split("<")
                        if ( item in l ):
                             #print(item + " présent dans " + str(l) )
                            self.aux(l)
                        else:
                            l = i.split(">")
                            if ( item in l ):
                                 #print(item + " présent dans " + str(l) )
                                self.aux(l)
                            else:
                                l = i.split(">=")
                                if ( item in l ):
                                     #print(item + " présent dans " + str(l) )
                                    self.aux(l)
                                else:
                                    l = i.split("<=")
                                    if ( item in l ):
                                         #print(item + " présent dans " + str(l) )
                                        self.aux(l)
                    #if ( ( l[0].split(".")[1] in self.pkey.couple[self.table_from[l[0].split(".")[0]]]  ) or ( l[0].split(".") in self.pkey.couple[l[0].split(".")[0]] ) ):
                    
                        #self.couple_dependance[self.table_from[l[0].split(".")[0]]] = [l[1]]
                    #elif ( l[0].split(".")[1] in self.pkey.couple[l[0].split(".")[0]] ):
                     #    #print("dependance de clé primaire trouvé")
    
    def aux(self,l):
         #print("AUX P ")
        cle = l[0].split(".")[1]
        table = l[0].split(".")[0]
        attr = l[1]
        if ( table in self.table_from.keys() ) :
             #print("ok")
            table = self.table_from[table]
        else :
             #print("rip")
             a=1
        if ( cle in self.pkey.couple[table] ) :
             #print("P Clé primaire : " + cle + " / " + attr + " dans : " + table)
            # on va ajouter a un dico les couple ( arg : [table touché] ) car nous savons deja que les clé touchés sont des clés primaires
            if ( self.couple_dependance[attr] == "" ):
                self.couple_dependance[attr] = str(table + " : " + cle)
            else :
                tmp = self.couple_dependance[attr]
                tmp.append(str(table + " : " + cle) )
                self.couple_dependance[attr] = tmp
                
            

    def affiche_couple_dep(self):
         #print("Dependance suuplementaire ::: ")
        # #print (self.dependance_supplementaire)
        #for c,d in self.dependance_supplementaire.items():
            # #print(c,d)
         #   for elt in d :
                 #print(str(elt + " : " + c))
                
            
         #print("Liste des parametres de fonction qui touche les clés primaires des tables suivantes : " )
        for a,b in self.couple_dependance.items():
            
            for c,d in self.dependance_supplementaire.items():
                # #print(c,d)
                for elt in d :
                    tmp = str(elt + " : " + c)
                    if ( tmp in b ) :
                         #print("Succes")
                        for o in d :
                            tmp = str(o + " : " + c)
                            b.append(tmp)
                        
                
            self.couple_dependance[a] =list(set(b))
             #print(a,b)
            
if __name__ == "__main__":
    # execute only if run as a script
    #main()
    
    # faire attention au clé primaire de chaque table car elle ne peut etre impacté par l'ecriture , et donc la lecture ne change rien
     #print("faire attention au clé primaire de chaque table car elle ne peut etre impacté par l'ecriture , et donc la lecture ne change rien , retirer le champs de lecture de chaque table pour leur clé primaire ")
     #print("une dependance ne peut arriver uniquement si les clé primaire sont identique , d'ou le idd = iid' , par exemple si viewitem(id) et viewitem(id') ont une contrainte , id = id' , car la clé primaire est unique ") 
     #print("Dans le main : \n" )
    p = Parser("./fichiers/neworder.sql")
    
    p.lanceur()