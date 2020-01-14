import os

class principal:
    def __init__(self):
        self.pk = PrimaryKey("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/genDB.sql")
        self.p = []
        self.pw = []
        self.l = os.listdir("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/")
        #print("Liste des fichier d'entrée : ")
        #print(l)
        for elt in self.l :
            p = Parser("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/"+elt)
            pw = parser_ecriture("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/"+elt)
            self.p.append(p)
            self.pw.append(pw)
        #print("taille :: " + str(len(self.p)) )
        #Parser("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/orderstatus.sql")
        self.liste_dependance = []
    
    def analyse_BD(self):
        print("Voici la liste des Tables avec leurs clés primaires ( si une table n'a pas de clé primaire , elle n'apparait pas ) : ")
        self.pk.lanceur()
    
    def analyse_lecture(self):
        print("\nLancement de l'analyse des attributs de Lecture :")
        for i in range(0,len(self.p)) :
            print("\n-----------" + self.p[i].name + " ---------------" )
            print("Lecture : ")
            self.p[i].lanceur()
            self.traite_cle_primaire_lecture()
            self.affiche_cle_primaire_lecture(i)
            print("Ecriture : ")
            self.pw[i].lanceur()

    def traite_cle_primaire_lecture(self):
        #print("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
        for i in range(0,len(self.p)) :
            for cle,valeur in self.p[i].liste_finale_attribut_lecture.items():
                liste_def = []
                for elt in valeur:
                    #print("elt :: " + elt )
                    if ( elt in self.pk.couple[cle]):
                        liste_def.append(elt)
                    self.p[i].liste_finale_attribut_lecture[cle] =  liste_def
                    
    def affiche_cle_primaire_lecture(self , i ):
        for cle,valeur in self.p[i].liste_finale_attribut_lecture.items():
            print(cle,valeur)
                
    def lanceur(self):
        main.analyse_BD()
        main.analyse_lecture()
        #print("\n***********************************************")
        #print("***********************************************")
        #main.analyse_ecriture()
        main.traite_cle_primaire_lecture()
        #main.affiche_cle_primaire_lecture()
        
    def affiche_dependance(self):
        for elt in self.liste_dependance :
            print("- " + elt ) 
    
    def create_dep_ww_insert(self):
        print("**************************************************************")
        print("**************** Création des dépendances WW  ****************")
        print("**************************************************************")
        for i in range ( 0 , len(self.pw)) :
            liste_table_touche_par_insert = self.pw[i].liste_table_insert
            for elt in liste_table_touche_par_insert:
                #print(self.pw[i].name + " :: ww;"+elt+"(*).*")
                self.liste_dependance.append(str(self.pw[i].name + " ->  " + self.pw[i].name + " :: ww;"+elt+"(*).*"))
                
        
    def create_dependance_wr(self):
        print("**************************************************************")
        print("**************** Création des dépendances WR  ****************")
        print("**************************************************************")
        for lecture in range (0,len(self.p)): 
            for ecriture in range (0,len(self.pw)):
                for table_r,attr_r in self.p[lecture].liste_finale_attribut_lecture.items() :
                    for table_w,attr_w in self.pw[ecriture].liste_attribut.items():
                        liste = []
                        tmp = 0 
                        for cle in attr_w :
                            tmp = 0
                            if ( cle in attr_r and table_w == table_r) :
                                #print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)
                                liste.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)))
                                tmp = 1               
                        if ( len(liste) == len(self.pk.couple[table_w])):
                            print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"').*") 
                            #print("Toute la table ajoutées")     
                        else :
                            #print("cas autre :: " + str(len(liste)))
                            for elt in liste:
                                print(elt)
                                
    def create_dependance_rw(self):
        print("**************************************************************")
        print("**************** Création des dépendances RW  ****************")
        print("**************************************************************")
        for ecriture in range (0,len(self.pw)):
            for lecture in range (0,len(self.p)): 
                for table_w,attr_w in self.pw[ecriture].liste_attribut.items():
                    for table_r,attr_r in self.p[lecture].liste_finale_attribut_lecture.items() :
                        liste = []
                        tmp = 0 
                        for cle in attr_r :
                            tmp = 0
                            if ( cle in attr_w and table_w == table_r) :
                                #print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)
                                liste.append(str((self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw,"+table_r+"("+str(self.pk.couple[table_r])+"')."+cle)))
                                tmp = 1               
                        if ( len(liste) == len(self.pk.couple[table_r])):
                            print(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw,"+table_r+"("+str(self.pk.couple[table_r])+"').*") 
                            #print("Toute la table ajoutées")     
                        else :
                            #print("cas autre :: " + str(len(liste)))
                            for elt in liste:
                                print(elt)
                                
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
if __name__ == "__main__":
    main = principal()
    main.lanceur()
    print("-------------------------------------Dépendances---------------------")
    main.create_dep_ww_insert()
    main.affiche_dependance()
    main.create_dependance_wr()
    main.create_dependance_rw()
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    