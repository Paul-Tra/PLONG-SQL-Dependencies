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
        m = re.findall("INSERT .*?\(",self.data)
        for elt in m :
            elt = elt.split(" ")
            elt = elt[2]
            elt = elt[:-1]
            #print(elt)
            self.liste_table_insert.append(elt)
            #print(elt+'\n')
        
        
    def analyse_contenue(self):
        for elt in self.liste_update :
            #print(elt)
            self.determine_table(elt)
            self.analyse_set(elt)
    
    def affiche(self):
        for elt,a in self.liste_attribut.items():
            print(elt,a)
            
    def lanceur(self):
        self.trouve_update()
        self.analyse_req()
        self.analyse_contenue()
        self.trouve_insert()
        #print(p_write.data)
        self.affiche()
        
if __name__ == "__main__":
    p_write = parser_ecriture("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/payment.sql")
    p_write.lanceur()
    
    