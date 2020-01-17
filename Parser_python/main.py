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
        self.liste_file = []
        for elt in self.l :
            self.liste_file.append(elt.split(".")[0])
        self.liste_dependance = []
        self.dependance = []
        self.dep_sans_doublons = dict()
        
    
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
            
            
            
    #
    #   orderstatus.sql {}
    #   neworder.sql {'DISTRICT': ['dId', 'wId'], 'STOCK': ['iId', 'wId']}
    #   stocklevel.sql {}
    #   payment.sql {'WAREHOUSE': ['wId'], 'DISTRICT': ['wId', 'dId'], 'CUSTOMER': ['wId', 'dId', 'cId']}
    #   delivery.sql {'ORDERS': ['oId', 'dId', 'wId'], 'ORDERLINE': ['oId', 'dId', 'wId'], 'CUSTOMER': ['cId', 'dId', 'wId']}
    
    
    def traite_dep_ww_entrefonctions(self):
        liste_dep_ww = dict()
        
        for i in range (0,len(self.pw)):
            for j in range (0,len(self.pw)):
                for cle,val in self.pw[i].liste_attribut.items() :
                    for clej,valj in self.pw[j].liste_attribut.items() :
                        if ( val.sort() == valj.sort() ):
                            if ( self.pw[i].name.split(".")[0] != self.pw[j].name.split(".")[0] ):
                                #if ( liste_dep_ww[self.pw[i].name.split(".")[0],self.pw[j].name.split(".")[0]] != None ) :
                                    
                                print("ok : " + self.pw[i].name.split(".")[0] + " " + self.pw[j].name.split(".")[0] )
                                    #liste_dep_ww[self.pw[i].name,self.pw[j].name] = val
                                    


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
        #print("**************************************************************")
        #print("**************** Création des dépendances WW  ****************")
        #print("**************************************************************")
        liste_tmp = []
        for i in range ( 0 , len(self.pw)) :
            liste_table_touche_par_insert = self.pw[i].liste_table_insert
            for j in range(0,len(liste_table_touche_par_insert)):
                #print(self.pw[i].name + " :: ww;"+elt+"(*).*")
                table = liste_table_touche_par_insert[j].split("(")[0].strip()
                condi = self.pw[i].liste_condition
                print(condi[j])
                self.liste_dependance.append(str(self.pw[i].name + " ->  " + self.pw[i].name + " :: ww;"+table+"(*).* / " + str(condi[j])))

                    
    def maj_dep_wr_rw_en_ww(self):
        for elt in self.liste_dependance :
            fin = elt.split(" :: ")[1]
            src = elt.split("->")[0].strip()
            dst = elt.split("->")[1].split("::")[0].strip()
            wr = elt.split(":: ")[1].split(";")[0].strip()
            
            if ( wr == "wr" and str(dst + " -> " + src + " :: "+fin) in self.liste_dependance ) :
                #print(str(dst + " -> " + src + " :: "+fin))
                self.liste_dependance.remove(str(src + " -> " + dst + " :: "+fin))
                fin2 = fin.replace("wr","rw")
                if ( str(dst + " -> " + src + " :: "+fin2) in self.liste_dependance ) :
                    self.liste_dependance.remove(str(dst + " -> " + src + " :: "+fin2))
                    
                fin = fin.replace("wr","ww")
                self.liste_dependance.append(str(src + " -> " + dst + " :: "+fin ) )
        #print(".................................................3")
        
    def create_dependance_wr(self):
        #print("**************************************************************")
        #print("**************** Création des dépendances WR  ****************")
        #print("**************************************************************")
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
                                liste.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)))
                                tmp = 1               
                        if ( len(liste) == len(self.pk.couple[table_w])):
                            #print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"').*") 
                            #print("Toute la table ajoutées")     
                            self.liste_dependance.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"').*")))
                        else :
                            #print("cas autre :: " + str(len(liste)))
                            for elt in liste:
                                self.liste_dependance.append(elt)
                                
    def create_dependance_rw(self):
        #print("**************************************************************")
        #print("**************** Création des dépendances RW  ****************")
        #print("**************************************************************")
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
                                liste.append(str((self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw;"+table_r+"("+str(self.pk.couple[table_r])+"')."+cle)))
                                tmp = 1               
                        if ( len(liste) == len(self.pk.couple[table_r])):
                            #print(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw,"+table_r+"("+str(self.pk.couple[table_r])+"').*") 
                            #print("Toute la table ajoutées")     
                            self.liste_dependance.append(str(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw;"+table_r+"("+str(self.pk.couple[table_r])+"').*"))
                        else :
                            #print("cas autre :: " + str(len(liste)))
                            for elt in liste:
                                self.liste_dependance.append(elt)
                                
        
        
        
        
    def affiche_dependance(self):    
        for elt in self.liste_dependance :
            print(elt)
        
    def parse_dependance(self):
        for elt in self.liste_dependance :
            new_dep = dependance()
            new_dep.source = elt.split(".")[0]
            new_dep.target = elt.split("->")[1].split(".")[0].strip()
            new_dep.type = elt.split(":: ")[1].split(";")[0].strip()
            new_dep.table = elt.split(";")[1].split("(")[0].strip()
            new_dep.id = elt.split("(")[1].split(".")[0]
            new_dep.id = new_dep.id[:-1]
            if ( new_dep.id[-1] == "'" ):
                new_dep.id = new_dep.id[:-1]
            new_dep.complement = elt.split(".")[3]
            self.dependance.append(new_dep)
            
         
         
    def verifie_dep_ww(self):
        for cle,value in self.dep_sans_doublons.items():
            if ( self.dep_sans_doublons[cle[1],cle[0]] != None ):
                if ( self.dep_sans_doublons[cle[1],cle[0]] != self.dep_sans_doublons[cle[0],cle[1]] ) :
                    #print("presence d'un WR et d'un RW + " + str(self.dep_sans_doublons[cle[1],cle[0]]) + '\n' + str(self.dep_sans_doublons[cle[0],cle[1]])  )
                    print(cle[0],cle[1])
                    
                    
                    
        
    def genere_couple_source_target(self):
        # init du disctionnaire { ( source , target ) : [ liste de contraintes ....]
        for elt in self.dependance :
            self.dep_sans_doublons[elt.source,elt.target] = None
            
        for elt in self.dependance :
            if self.dep_sans_doublons[elt.source,elt.target] != None:
                li = []
                for a in self.dep_sans_doublons[elt.source,elt.target] :
                    #print("a::::::: " + a)
                    li.append(a)
                li.append(elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement)
                self.dep_sans_doublons[elt.source,elt.target] = li
            else :
                self.dep_sans_doublons[elt.source,elt.target] = [elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement]
            
            
        
        
    def generer_graphml(self):
        with open(os.getcwd()+"/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/graph.graphml", "w") as fichier:
            fichier.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            fichier.write("<graphml xmlns='http://graphml.graphdrawing.org/xmlns\'>\n")
            fichier.write('\t<key id="d0" for="node" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d1" for="edge" attr.name="weight" attr.type="string"/>\n')
            fichier.write('<graph id="G" edgedefault="directed\">\n')
            
            #creation node
            for elt in self.liste_file :
                fichier.write('<node id="'+elt+'">\n')
                fichier.write('<data key="d0">"'+elt+'"</data>\n</node>\n')
                for dep in self.dependance :
                    if ( dep.source == elt ) :
                        #<edge source="RegItem" target="RegItem">
                        fichier.write('\n<edge source="'+dep.source+'" target="'+dep.target+'">\n')
                        fichier.write('<data key="d1">\n')
                        fichier.write(dep.type +';'+dep.table+'('+str(dep.id)+').'+dep.complement+'\n\n</data></edge>')
                    
                
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.fichier = fichier
    
    def genere_graphml_sans_doublons(self):
        with open(os.getcwd()+"/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/graph.graphml", "w") as fichier:
            fichier.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            fichier.write("<graphml xmlns='http://graphml.graphdrawing.org/xmlns\'>\n")
            fichier.write('\t<key id="d0" for="node" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d1" for="edge" attr.name="weight" attr.type="string"/>\n')
            fichier.write('<graph id="G" edgedefault="directed\">\n')
            
            #creation node
            for cle , value in self.dep_sans_doublons.items() :
                fichier.write('\n<node id="'+cle[0]+'">\n')
                fichier.write('<data key="d0">"'+cle[0]+'"</data>\n</node>')
                fichier.write('\n<edge source="'+cle[0]+'" target="'+cle[1]+'">\n')
                fichier.write('<data key="d1">\n')
                # on traite ensuite toutes les dependances une a une 
                for v in value :
                    fichier.write(v+'\n')
                fichier.write('\n\n</data>\n</edge>')    
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.fichier = fichier
        
    def affiche_lise_sans_doublon(self):
        for cle,value in self.dep_sans_doublons.items():
            print("\nSource : " + cle[0] + " , Destination : " + cle[1] )
            for v in  value : 
                print('\t'+v)
                
        
    def lanceur_f(self):
        self.lanceur()
        print("-------------------------------------Dépendances---------------------")
        self.create_dep_ww_insert()
        self.create_dependance_wr()
        self.create_dependance_rw()
        self.maj_dep_wr_rw_en_ww()
        #self.affiche_dependance()
        self.parse_dependance()
        self.generer_graphml()
        self.genere_couple_source_target()
        print("############################################")
        self.affiche_lise_sans_doublon()
        self.genere_graphml_sans_doublons()
        #self.verifie_dep_ww()
        #self.traite_dep_ww_entrefonctions()
        
        
if __name__ == "__main__":
    main = principal()
    main.lanceur_f()
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    