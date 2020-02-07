import os
from primary import *
from parser import *
from parser_ecriture import *
from dependance import *
import sys

class principal:
    def __init__(self,dossier):
        ### Nous avons changé le rep. pour verifier que notre algo est correct , en se basant sur l'exemple fournie dans l'enoncé ###
        #self.pk = PrimaryKey("./fichiers/genDB.sql")
        print("dossier :: " + dossier )
        self.pk = PrimaryKey("./"+dossier+"/genDB.sql") # pour nos test
        self.p = []
        self.pw = []
        #self.l = os.listdir("./fichiers/")
        self.l = os.listdir("./"+dossier+"/") # pour nos test
        #print("Liste des fichier d'entrée : ")
        #print(l)
        for elt in self.l :
            if ( elt != "genDB.sql" ):
                #p = Parser("./fichiers/"+elt)
                #pw = parser_ecriture("./fichiers/"+elt)
                
                p = Parser("./"+dossier+"/"+elt,dossier) # pour nos test
                pw = parser_ecriture("./"+dossier+"/"+elt,dossier) # pour nos test
                
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
            #print("Lecture : ")
            self.p[i].lanceur()
            self.traite_cle_primaire_lecture()
            self.affiche_cle_primaire_lecture(i)
            #print("Ecriture : ")
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
                                a=1
                                #if ( liste_dep_ww[self.pw[i].name.split(".")[0],self.pw[j].name.split(".")[0]] != None ) :
                                    
                                #print("ok : " + self.pw[i].name.split(".")[0] + " " + self.pw[j].name.split(".")[0] )
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
        l =[]
        for i in range ( 0 , len(self.pw)) :
            liste_table_touche_par_insert = self.pw[i].liste_table_insert
            for j in range(0,len(liste_table_touche_par_insert)):
                #print(self.pw[i].name + " :: ww;"+elt+"(*).*")
                table = liste_table_touche_par_insert[j].split("(")[0].strip()
                condi = self.pw[i].liste_condition
                l.append(str(self.pw[i].name + " ->  " + self.pw[i].name + " :: ww;"+table+"(*).* / " + str(condi[j])))
                        
                    #print(condi[j])
        for elt in l :
            if ( elt not in self.liste_dependance ) :
                self.liste_dependance.append(elt)
                    
    def maj_dep_wr_rw_en_ww(self):
        l = []
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
                l.append(str(src + " -> " + dst + " :: "+fin ))
        for elt in l :
            if ( elt not in self.liste_dependance ) :
                self.liste_dependance.append(elt)
                    
                    
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
                                if ( table_w in self.pw[ecriture].cle_impacte_set.keys() ) :
                                    for a in self.pw[ecriture].cle_impacte_set[table_w] :
                                        #if ( a in self.p[lecture].liste_attributs_read ) :
                                         #   print("aaaaa " + a )
                                        liste.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))

                        liste = list(set(liste))
                        print("size :: " + str(len(liste)))
                        #if ( len(liste) == len(self.pk.couple[table_w])):
                        #    self.liste_dependance.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"').*")))
                        #else :
                            #print("cas autre :: " + str(len(liste)))
                        for elt in liste:
                            if ( elt not in self.liste_dependance ) :
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
                                if ( table_w in self.pw[ecriture].cle_impacte_set.keys() ) :
                                    for a in self.pw[ecriture].cle_impacte_set[table_w] :
                                        if ( self.p[lecture].name == self.pw[ecriture].name ):
                                            liste.append(str((self.pw[lecture].name + " -> " + self.pw[ecriture].name + " :: ww;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))
                                        else:
                                            liste.append(str((self.pw[lecture].name + " -> " + self.pw[ecriture].name + " :: rw;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))
                                    tmp = 1               
                        #if ( len(liste) == len(self.pk.couple[table_r])):
                            #print(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw,"+table_r+"("+str(self.pk.couple[table_r])+"').*") 
                            #print("Toute la table ajoutées")     
                            #self.liste_dependance.append(str(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw;"+table_r+"("+str(self.pk.couple[table_r])+"').*"))
                        #else :
                            #print("cas autre :: " + str(len(liste)))
                        for elt in liste:
                            if ( elt not in self.liste_dependance ) :
                                self.liste_dependance.append(elt)
                                
        
        
        
        
    def affiche_dependance(self):    
        for elt in self.liste_dependance :
            #print(elt)
            a=1
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
            new_dep.complement = elt.split(".")[3].split("/")[0]
            
            if ( "/" in elt ) :
                condi = elt.split("/")[1]
                condi = condi.split(",")
                #print(str(condi))
                for elt in condi :
                    #print(elt.replace("'","").replace("[","").replace("]","").strip())
                    a = ""
                    a = elt.replace("'","").replace("[","").replace("]","").strip()
                    #print("aaaaaaaaaaaaaaaaaaaaaaa : "+  a)
                    new_dep.condition.append(a)
                    
            self.dependance.append(new_dep)
            #print(new_dep.source , new_dep.target , new_dep.table ,str(new_dep.condition))
            
         
         
    def verifie_dep_ww(self):
        for cle,value in self.dep_sans_doublons.items():
            if ( self.dep_sans_doublons[cle[1],cle[0]] != None ):
                if ( self.dep_sans_doublons[cle[1],cle[0]] != self.dep_sans_doublons[cle[0],cle[1]] ) :
                    #print(cle[0],cle[1])
                    a=1
                    
    
    def genere_couple_source_target(self):
        # init du disctionnaire { ( source , target ) : [ liste de contraintes ....]
        for elt in self.dependance :
            self.dep_sans_doublons[elt.source,elt.target] = None
            
        for elt in self.dependance :
            if self.dep_sans_doublons[elt.source,elt.target] != None:
                li = []
                for a in self.dep_sans_doublons[elt.source,elt.target] :
                    li.append(a)
                li.append(elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement )
                self.dep_sans_doublons[elt.source,elt.target] = li
            else :
                self.dep_sans_doublons[elt.source,elt.target] = [elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement ]
            
            
    def genere_condition_dep(self):
        for src in self.p :
            for dst in self.pw : 
                source = src.name.split(".")[0]
                target = dst.name.split(".")[0]
                #print("----------"+name +"-------------")
                for a1,b1 in src.couple_dependance.items():
                    for  a2,b2 in dst.couple_dependance.items():
                        for el in b2 :
                            for e in b1 :
                                if ( el == e and (source,target) in self.dep_sans_doublons.keys()):
                                    el = el.replace(" : " , ":")
                                    e =e.replace(" : ",":")
                                    #print("src : " + source + ' , ' + e + " // dst : " + target + " , " + el )
                                    #if ( str(source + "." + a1 + " = " + target +"." +  a2) not in self.dep_sans_doublons[source,target] ) :
                                    self.dep_sans_doublons[source,target] = self.dep_sans_doublons[source,target] + [ source + "." + a1 + " = " + target +"." +  a2 ]
                                    
        for src in self.pw :
            for dst in self.p : 
                source = src.name.split(".")[0]
                target = dst.name.split(".")[0]
                #print("----------"+name +"-------------")
                for a1,b1 in src.couple_dependance.items():
                    for  a2,b2 in dst.couple_dependance.items():
                        for el in b1 :
                            for e in b2 :
                                if ( el == e and (source,target) in self.dep_sans_doublons.keys()):
                                    el = el.replace(" : " , ":")
                                    e =e.replace(" : ",":")
                                    #if ( str(source + "." + a1 + " = " + target +"." +  a2) not in self.dep_sans_doublons[source,target] ) :
                                    self.dep_sans_doublons[source,target] = self.dep_sans_doublons[source,target] + [ source + "." + a1 + " = " + target +"." +  a2 ]
    def generer_graphml(self):
        with open(os.getcwd()+"/graph.graphml", "w") as fichier:
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
        with open(os.getcwd()+"/graph.graphml", "w") as fichier:
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
                l = []
                for v in value :
                    if ( v not in l ) :
                        l.append(v)
                    
                for elt in l :
                    fichier.write(elt+'\n')
                
                
                fichier.write('\n\n</data>\n</edge>')    
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.fichier = fichier
        
    def affiche_lise_sans_doublon(self):
        with open ("./dependences.txt","w") as fichier :
            fichier.write('## Fichier généré dans le cadre de Plong2019/2020 pour etudier les raisons de dépendances entre 2 transactions , ID = "nom_de_la_relation" SRC = "fichier_source" DST = "fichier_destination"\n## ( SRC et DST sont en rapport avec les fleches du graphes).\n\n')
        for cle,value in self.dep_sans_doublons.items():
            print("\nSource : " + cle[0] + " , Destination : " + cle[1] )
            value = list(set(value))
            i=0
            for v in  value : 
                print('\t' + str(i) + " :: "+v.replace("/ ",""))
                i+=1
                self.write_raison_dependances(v,cle[0],cle[1])
                
    def write_raison_dependances(self,v ,src,dst):
        #print ( src ) 
        fsrc = src
        fdst = dst
        src = src + ".sql"
        dst = dst + ".sql"
        #print( dst ) 
        l_dep_src = []
        l_dep_dst = []
        
        with open ("./dependences.txt","a+") as fichier :
            
            if ( "wr;" in v ) :
                t = re.findall(";.*?\(",v)
                table = t[0].replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                #print(tmp)
                attr = tmp[0].replace(").","")
                t = ""
                #print(table)
                for elt in self.p :
                    #print(elt.name)
                    if ( elt.name == dst ):
                        print("\t\tRaison de la dependance DST : "+dst+" : ")
                        for c,val in elt.table_from.items():
                            if ( table in val ):
                                t = c
                                
                        if ( t != "" ):
                            tmp = "SELECT .*? FROM .*? "+table+" .*? WHERE "+attr+".*?;"
                            #print(tmp)
                            m = re.findall("SELECT.*?"+attr+".*?FROM.*?"+table+".*?;",elt.data)
                            if ( m != [] ) :
                                for li in m :
                                    l = li.split(";")
                                    for e in l :
                                        if ( "SELECT" in e ):
                                            print('\t\t\t'+e)
                                            l_dep_dst.append(e)
                for elt in self.pw :
                    if ( elt.name == src ):
                        tmp = "UPDATE "+table+" SET " +attr
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                print('\t\t\t'+li)
                                l_dep_src.append(li)
                    
                                
            if ( "rw;" in v ) :
                t = re.findall(";.*?\(",v)
                table = t[0].replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                #print(tmp)
                attr = tmp[0].replace(").","")
                t = ""
                #print(table)
                for elt in self.pw :
                    if ( elt.name == dst ):
                        tmp = "UPDATE "+table+" SET " +attr
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            print("\t\tRaison de la dependance DST : "+dst+" : ")
                            for li in m :
                                print('\t\t\t'+li)
                                l_dep_dst.append(li)
                                
                for elt in self.p :
                    #print(elt.name)
                    if ( elt.name == src ):
                        print("\t\tRaison de la dependance SRC : "+src+" : ")
                        for c,val in elt.table_from.items():
                            if ( table in val ):
                                t = c
                                
                        if ( t != "" ):
                            tmp = "SELECT .*? FROM .*? "+table+".*?;"
                            #print(tmp)
                            #print("ATTRRR : " + attr )
                            m = re.findall("SELECT.*?"+attr+".*?FROM.*?"+table+".*?;",elt.data)
                            if ( m != [] ) :
                                for li in m :
                                    l = li.split(";")
                                    for e in l :
                                        if ( "SELECT" in e ):
                                            print('\t\t\t'+e)
                                            l_dep_src.append(e)
                                    
            if ( "ww;" in v ) :
                t = re.findall(";.*?\(",v)
                table = t[0].replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                #print(tmp)
                attr = tmp[0].replace(").","")
                #print(table)
                for elt in self.pw :
                    if ( elt.name == src and src == dst):
                        tmp = "INSERT INTO "+table
                        #print(tmp)
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                print('\t\t\t'+li)
                                l_dep_src.append(li)
                                l_dep_dst.append(li)
                    else:
                        tmp = "UPDATE "+table
                        #print(tmp)
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                print('\t\t\t'+li)
                                l_dep_src.append(li)
                                l_dep_dst.append(li)
                if ( ".*" not in t[0] ):
                    for elt in self.pw :
                        if ( elt.name == src and src == dst):
                            tmp = "UPDATE "+table+" SET " +attr
                            m = re.findall(tmp+".*?;",elt.data)
                            if ( m != [] ):
                                print("\t\tRaison de la dependance DST : "+dst+" : ")
                                for li in m :
                                    print('\t\t\t'+li)
                                    l_dep_src.append(li)
                                    l_dep_dst.append(li)
                    
                    
                    
            else :  
                type = 'relation'
                # du type table.attr = table.attr
            if ( "=" not in v ) :
                fichier.write('\n<Relation ID="'+v+'" SRC="'+fsrc+'" DST="'+fdst+'">\n')
                fichier.write('<SRC>\n')
                for elt in l_dep_src :
                    fichier.write('\t'+elt+'\n')
                fichier.write('</SRC>\n')
                fichier.write('<DST>\n')
                for elt in l_dep_dst :
                    fichier.write('\t'+elt+'\n')
                fichier.write('</DST>\n')
                fichier.write('</Relation>\n\n')
                
                
            
        
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
        
        
        #self.verifie_dep_ww()
        #self.traite_dep_ww_entrefonctions()
        self.genere_condition_dep()
        self.affiche_lise_sans_doublon()
        self.genere_graphml_sans_doublons()
        
        
if __name__ == "__main__":
    argv = sys.argv
    if (len(argv) != 2 ) :
        print("Merci d'entrer un dossier contenant : \n - un fichier genDB.sql contenant votre base de données \n - un ensemble de fichier reprensetant les transactions ( au format .sql ; PLpgSQL ) ")
    else :
        
        dossier = argv[1].replace("/","")
        main = principal(dossier)
        main.lanceur_f()
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    