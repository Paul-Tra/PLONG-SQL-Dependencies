class parser_ecriture:
    def __init__(self,nom_fichier):
        self.nom_fichier = nom_fichier
        #print(nom_fichier)
        name = nom_fichier.split("/")
        name = name[len(name)-1]
        self.name = name
        self.liste_update = []
        self.liste_table_insert = []
        self.liste_attribut = dict()
        self.liste_condition = []
        self.data = ""
        self.pkey = PrimaryKey("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/genDB.sql")
        self.pkey.lanceur()
        self.liste_param_fonction = []
        # on va stocker les couples de dependance [ table : parametre de fonction ] on sait au préalable que la correspondance avec la / les clés primaire sont correctes
        self.couple_dependance = dict()
        
    def trouve_update(self):
        data = " "
        # on ouvre le fichier passer en argument .
        with open(self.nom_fichier,"r") as F :
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
        self.data = data
        
    def analyse_req(self):
        m = re.findall("UPDATE .*?;",self.data)
        for elt in m :
            self.liste_update.append(elt)
            #print(elt+'\n')
        
    def determine_table(self,data):
        d = data.split(" ")[1]
        self.liste_attribut[d]=[]
        #print(d)
    
    def analyse_set(self,data):
        #print("data :: " + data)
        d = data.split(" ")[1]
        #print("nom : " + d )
        m = re.findall("[A-Za-z]+\.[A-Za-z]+",data)
        #print(m)
        attribut = []
        for elt in m :
            attribut.append(elt.split(".")[1])
            #print(attribut)
        
        self.liste_attribut[d] = attribut
        #print(self.liste_attribut[d])
        #print(self.liste_attribut)
            
            
        #print(m)
        
    def trouve_insert(self):
        m = re.findall("INSERT .*?\(?.*?\)",self.data)
        for elt in m :
            condi = elt
            elt = elt.split(" ")
            elt = elt[2]
            elt = elt[:-1]
            self.liste_table_insert.append(elt)
            #print(elt+'\n')
            # traitement des conditions
            liste =[]
            liste = condi.split("(")[1].split(")")[0].split(",")
            self.liste_condition.append(liste)
            #print(self.liste_condition)
            
    def trouve_attribut_de_fonction(self):
        #print("\nrecherche des attributs de fonction")
        self.data = self.data.replace("NUMERIC(2)","INTEGER")
        self.data = self.data.replace("VARCHAR(16)","INTEGER")
        m = re.findall("FUNCTION .*?[{]*? RETURNS",self.data)
        #print(m)
        n = re.findall("\(.*?\)",str(m))
        #print(n)
        m = re.findall("[a-z]+.*? ",str(n))
        #print(m)
        print("\nListe des parametre de la fonction : " )
        for elt in m :
            elt =elt.replace(",","")
            self.liste_param_fonction.append(elt)
            print(elt )
            self.couple_dependance[elt] = []
            
    def trouve_cle_dep_possible (self):
        for elt in self.liste_update :
            #print(elt)
            elt = elt.split("WHERE")[1]
            #print(elt)
            elt = elt.replace("AND",",")
            elt = elt.replace("OR ",",")
            elt = elt.strip().replace(" ","").replace(";","")
            #print(elt)
            l = elt.split(",")
            for a in l : 
                a = a.split("=")
                self.aux(a)
            
    def aux(self,l):
        #print("cle avant changement " + str(l))
        cle = l[0].split(".")[1]
        table = l[0].split(".")[0]
        attr = l[1]
        #print("ok cle = " + cle + ", attr : " + attr )
        #if ( table in self.table_from.keys() ) :
            #print("ok")
         #   table = self.table_from[table]
        if ( cle in self.pkey.couple[table] ) :
            print("Clé primaire : " + cle + " / " + attr + " dans : " + table)
            # on va ajouter a un dico les couple ( arg : [table touché] ) car nous savons deja que les clé touchés sont des clés primaires
            if ( attr in self.couple_dependance.keys()):
                tmp = self.couple_dependance[attr]
            else:
                self.couple_dependance[attr] = [] 
                tmp = self.couple_dependance[attr]
            tmp.append(str(table + " : " + cle) )
            self.couple_dependance[attr] = tmp
                
            
        
    def trouve_dependance_dans_le_update(self):
        for elt in self.liste_update :
            
            elt = elt.split("WHERE")[1]
            print(elt)
    
    def analyse_contenue(self):
        for elt in self.liste_update :
            #print(elt)
            self.determine_table(elt)
            self.analyse_set(elt)
    
    def affiche(self):
        #for elt,a in self.liste_attribut.items():
         #   print(elt,a)
        #for elt in self.liste_table_insert:
        #    print(elt)
        for a,b in self.couple_dependance.items():
            print(a,b)    
        
    def lanceur(self):
        self.trouve_update()
        self.analyse_req()
        self.analyse_contenue()
        self.trouve_insert()
        #print(p_write.data)
        self.trouve_attribut_de_fonction()
        
        #self.trouve_dependance_dans_le_update()
        self.trouve_cle_dep_possible()
        self.affiche()
if __name__ == "__main__":
    p_write = parser_ecriture("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/neworder.sql")
    p_write.lanceur()
    
    