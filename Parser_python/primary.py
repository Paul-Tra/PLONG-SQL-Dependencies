import re

class PrimaryKey :
    def __init__(self , nom_fichier ):
        self.file = nom_fichier
        self.contenue = ""
        self.couple = {}
        
    def lecture_file(self):
        data = " "
        # on ouvre le fichier passer en argument .
        with open(self.file,"r") as F :
            for ligne in F :
                #retire les espaces de la igne 
                ligne = ligne.strip()
                #si la igne est vide , equivalent à \n\n\n par exemple
                if not ligne :
                    #print("#ligne vide.")
                    # rien a faire ...
                    a= 0 
                # sinon on traite la ligne et l'ajoute a notre liste
                else :
                    data += " "+ligne
                    #print(sans_saut)
        # on se retrouve avec une tres grande ligne , que l'on va pouvoir parser .
        # privée des espaces inutiles , et des retours chariot
        return data
        
    def trouve_table(self):
        # on va chercher toute les tables de cette BD 
        create = re.findall("CREATE TABLE [A-Z]+[_]*[A-Z]*",self.contenue)
        #print(create)
        check = re.findall("CREATE TABLE [A-Z]+[_]*[A-Z]*.*?\);",self.contenue)
        #print(check)
        create = []
        for elt in check :
            #print(elt)
            a = re.findall("PRIMARY KEY",elt)
            if ( a != [] ):
                b = re.findall("CREATE TABLE [A-Z]+[_]*[A-Z]*",elt)
                #print(b)
                create.append(b)
                
        
                
        #print(create)
        self.create = create
        return create
    
    def liste_table(self,table):
        liste_table = []
        for elt in self.create :
            elt = elt[0].split(" ")
            #print(elt[2])
            liste_table.append(elt[2])
        self.liste_table = liste_table
        #print(self.liste_table)
        
    def trouve_primary(self):
        # on va finir avec une liste de la forme ( table : [liste_clé_primaire] )
        i = 0
        with open(self.file,"r") as F :
            for ligne in F :
                #print(ligne)
                primary = re.search("PRIMARY KEY",ligne)
                # cas ou c'est de la forme : "iId INTEGER PRIMARY KEY,"
                p = re.search("PRIMARY KEY,",ligne)
                if ( p != None ):
                    #print("trouvé dans :" + ligne)
                    m = re.compile(r"(?P<attribut>[a-z]+[A-Za-z]*)")
                    pk = m.search(ligne)
                    #print(pk.group("attribut"))
                    liste = pk.group("attribut")
                    
                    self.couple[self.liste_table[i]] =  [liste.strip() ]
                    i+=1
                
                # cas ou c'est de la forme "PRIMARY KEY (oId, dId, wId, number),"
                elif ( primary != None ):
                    #print("autre cas")
                    m = re.compile(r"(?P<attribut>\(.+\))")
                    pk = m.search(ligne)
                    liste = pk.group("attribut")
                    liste = liste[1:len(liste)-1]
                    liste = liste.split(",")
                    #print(liste)
                    
                    l = [] 
                    for elt in liste :
                        l.append(elt.strip() )
                    self.couple[self.liste_table[i]] = l
                    i+=1
                
                    
        
    def affiche_couple(self):
        for elt,a in self.couple.items() :
            print(elt,a)
            
    def lanceur(self):
        self.contenue = self.lecture_file()
        #print(fichier)
        liste_create = self.trouve_table()
        #print(liste_create)
        self.liste_table(liste_create)
        #print(self.liste_table)
        self.trouve_primary()
        #print(len(self.couple))
        #self.affiche_couple()
    
if __name__ == "__main__":
    pk = PrimaryKey("./fichiers/genDB.sql")
    pk.lanceur()
   