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
         #print("dossier :: " + dossier )
        self.pk = PrimaryKey(dossier+"/genDB.sql") # pour nos test
        self.p = []
        self.pw = []
        #self.l = os.listdir("./fichiers/")
        self.l = os.listdir(dossier+"/") # pour nos test
        # #print("Liste des fichier d'entrée : ")
        # #print(l)
        for elt in self.l :
            if ( elt != "genDB.sql" ):
                #p = Parser("./fichiers/"+elt)
                #pw = parser_ecriture("./fichiers/"+elt)
                
                p = Parser(dossier+"/"+elt,dossier) # pour nos test
                pw = parser_ecriture(dossier+"/"+elt,dossier) # pour nos test
                
                self.p.append(p)
                self.pw.append(pw)
        # #print("taille :: " + str(len(self.p)) )
        #Parser("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/orderstatus.sql")
        self.liste_file = []
        for elt in self.l :
            self.liste_file.append(elt.split(".")[0])
        self.liste_dependance = []
        self.dependance = []
        self.dep_sans_doublons = dict()
        self.dict_finale = dict()
        self.dossier = dossier
        
    
    def analyse_BD(self):
         #print("Voici la liste des Tables avec leurs clés primaires ( si une table n'a pas de clé primaire , elle n'apparait pas ) : ")
        self.pk.lanceur()
    
    def analyse_lecture(self):
         #print("\nLancement de l'analyse des attributs de Lecture :")
        for i in range(0,len(self.p)) :
             #print("\n-----------" + self.p[i].name + " ---------------" )
            # #print("Lecture : ")
            self.p[i].lanceur()
            self.traite_cle_primaire_lecture()
            #self.affiche_cle_primaire_lecture(i)
            # #print("Ecriture : ")
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
                                    
                                # #print("ok : " + self.pw[i].name.split(".")[0] + " " + self.pw[j].name.split(".")[0] )
                                    #liste_dep_ww[self.pw[i].name,self.pw[j].name] = val
                                    


    def traite_cle_primaire_lecture(self):
        # #print("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
        for i in range(0,len(self.p)) :
            for cle,valeur in self.p[i].liste_finale_attribut_lecture.items():
                liste_def = []
                for elt in valeur:
                    # #print("elt :: " + elt )
                    if ( elt in self.pk.couple[cle]):
                        liste_def.append(elt)
                    self.p[i].liste_finale_attribut_lecture[cle] =  liste_def
                    
    #def affiche_cle_primaire_lecture(self , i ):
    #    for cle,valeur in self.p[i].liste_finale_attribut_lecture.items():
             #print(cle,valeur)
                
    def lanceur(self):
        main.analyse_BD()
        main.analyse_lecture()
        # #print("\n***********************************************")
        # #print("***********************************************")
        #main.analyse_ecriture()
        main.traite_cle_primaire_lecture()
        #main.affiche_cle_primaire_lecture()
        
    #def affiche_dependance(self):
    #    for elt in self.liste_dependance :
             #print("- " + elt ) 
    
    def create_dep_ww_insert(self):
        # #print("**************************************************************")
        # #print("**************** Création des dépendances WW  ****************")
        # #print("**************************************************************")
        liste_tmp = []
        l =[]
        for i in range ( 0 , len(self.pw)) :
            liste_table_touche_par_insert = self.pw[i].liste_table_insert
            for j in range(0,len(liste_table_touche_par_insert)):
                # #print(self.pw[i].name + " :: ww;"+elt+"(*).*")
                table = liste_table_touche_par_insert[j].split("(")[0].strip()
                condi = self.pw[i].liste_condition
                if ( table in self.pk.couple.keys() ):
                    l.append(str(self.pw[i].name + " ->  " + self.pw[i].name + " :: ww;"+table+"("+str(self.pk.couple[table])+").* / " + str(condi[j])))
                        
                    # #print(condi[j])
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
                # #print(str(dst + " -> " + src + " :: "+fin))
                self.liste_dependance.remove(str(src + " -> " + dst + " :: "+fin))
                fin2 = fin.replace("wr","rw")
                if ( str(dst + " -> " + src + " :: "+fin2) in self.liste_dependance ) :
                    self.liste_dependance.remove(str(dst + " -> " + src + " :: "+fin2))
                    
                fin = fin.replace("wr","ww")
                l.append(str(src + " -> " + dst + " :: "+fin ))
        for elt in l :
            if ( elt not in self.liste_dependance ) :
                self.liste_dependance.append(elt)
                    
                    
        # #print(".................................................3")
        
    def create_dependance_wr(self):
        # #print("**************************************************************")
        # #print("**************** Création des dépendances WR  ****************")
        # #print("**************************************************************")
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
                                         #    #print("aaaaa " + a )
                                        liste.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))

                        liste = list(set(liste))
                         #print("size :: " + str(len(liste)))
                        #if ( len(liste) == len(self.pk.couple[table_w])):
                        #    self.liste_dependance.append(str((self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr;"+table_w+"("+str(self.pk.couple[table_w])+"').*")))
                        #else :
                            # #print("cas autre :: " + str(len(liste)))
                        for elt in liste:
                            if ( elt not in self.liste_dependance ) :
                                # #print ("||||| " + elt)
                                self.liste_dependance.append(elt)
                                
    def create_dependance_rw(self):
        # #print("**************************************************************")
        # #print("**************** Création des dépendances RW  ****************")
        # #print("**************************************************************")
        for ecriture in range (0,len(self.pw)):
            for lecture in range (0,len(self.p)): 
                for table_w,attr_w in self.pw[ecriture].liste_attribut.items():
                    for table_r,attr_r in self.p[lecture].liste_finale_attribut_lecture.items() :
                        liste = []
                        tmp = 0 
                        for cle in attr_r :
                            tmp = 0
                            if ( cle in attr_w and table_w == table_r) :
                                # #print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)
                                if ( table_w in self.pw[ecriture].cle_impacte_set.keys() ) :
                                    for a in self.pw[ecriture].cle_impacte_set[table_w] :
                                        if ( self.p[lecture].name == self.pw[ecriture].name ):
                                            liste.append(str((self.pw[lecture].name + " -> " + self.pw[ecriture].name + " :: ww;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))
                                        else:
                                            liste.append(str((self.pw[lecture].name + " -> " + self.pw[ecriture].name + " :: rw;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))
                                    tmp = 1               
                        #if ( len(liste) == len(self.pk.couple[table_r])):
                            # #print(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw,"+table_r+"("+str(self.pk.couple[table_r])+"').*") 
                            # #print("Toute la table ajoutées")     
                            #self.liste_dependance.append(str(self.p[lecture].name + " -> " + self.p[ecriture].name + " :: rw;"+table_r+"("+str(self.pk.couple[table_r])+"').*"))
                        #else :
                            # #print("cas autre :: " + str(len(liste)))
                        for elt in liste:
                            if ( elt not in self.liste_dependance ) :
                                self.liste_dependance.append(elt)
                                
    def create_dependance_ww(self):
        # #print("**************************************************************")
        # #print("**************** Création des dépendances RW  ****************")
        # #print("**************************************************************")
        for ecriture in range (0,len(self.pw)):
            for table_w,attr_w in self.pw[ecriture].liste_attribut.items():
                liste = []
                tmp = 0
                for cle in attr_w :
                    # #print(self.pw[ecriture].name + " -> " + self.pw[lecture].name + " :: wr,"+table_w+"("+str(self.pk.couple[table_w])+"')."+cle)
                    if ( table_w in self.pw[ecriture].cle_impacte_set.keys() ) :
                        for a in self.pw[ecriture].cle_impacte_set[table_w] :
                            liste.append(str((self.pw[ecriture].name + " -> " + self.pw[ecriture].name + " :: ww;"+table_w+"("+str(self.pk.couple[table_w])+"')."+a)))
                            
                for elt in liste:
                    if ( elt not in self.liste_dependance ) :
                        self.liste_dependance.append(elt)
        
        
        
    def affiche_dependance(self):    
        for elt in self.liste_dependance :
            # #print(elt)
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
                # #print(str(condi))
                for elt in condi :
                    # #print(elt.replace("'","").replace("[","").replace("]","").strip())
                    a = ""
                    a = elt.replace("'","").replace("[","").replace("]","").strip()
                    # #print("aaaaaaaaaaaaaaaaaaaaaaa : "+  a)
                    new_dep.condition.append(a)
                    
            self.dependance.append(new_dep)
            # #print(new_dep.source , new_dep.target , new_dep.table ,str(new_dep.condition))
            
         
         
    def verifie_dep_ww(self):
        for cle,value in self.dep_sans_doublons.items():
            if ( self.dep_sans_doublons[cle[1],cle[0]] != None ):
                if ( self.dep_sans_doublons[cle[1],cle[0]] != self.dep_sans_doublons[cle[0],cle[1]] ) :
                    # #print(cle[0],cle[1])
                    a=1
                    
    
    def genere_couple_source_target(self):
        for elt in self.dependance :
            self.dep_sans_doublons[elt.source,elt.target] = None
            
        for elt in self.dependance :
            # #print(elt.source , elt.target)
            if self.dep_sans_doublons[elt.source,elt.target] != None:
                li = []
                for a in self.dep_sans_doublons[elt.source,elt.target] :
                    li.append(a)
                    # #print(a)
                li.append(elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement )
                self.dep_sans_doublons[elt.source,elt.target] = li
            else :
                self.dep_sans_doublons[elt.source,elt.target] = [elt.type +';'+elt.table+'('+str(elt.id)+').'+elt.complement ]
            
            
    def genere_condition_dep(self):
         #print("#######################################")
         #print("#######################################")
         #print("#######################################")
         #print("#######################################")
        #for src in self.p :
        #   for a1,b1 in src.couple_dependance.items():
                #print(a1,b1)
        
        for src in self.p :
            for dst in self.pw : 
                # #print("GENERE condition Dep 1 ")
                source = src.name.split(".")[0]
                target = dst.name.split(".")[0]
                # #print("----------"+name +"-------------")
                for a1,b1 in src.couple_dependance.items():
                    for  a2,b2 in dst.couple_dependance.items():
                        for el in b2 :
                            for e in b1 :
                                if ( el == e and (source,target) in self.dep_sans_doublons.keys()):
                                    el = el.replace(" : " , ":")
                                    e =e.replace(" : ",":")
                                     #print("src : " + source + ' , ' + e + " // dst : " + target + " , " + el )
                                    self.dep_sans_doublons[source,target] = self.dep_sans_doublons[source,target] + [ source + "." + a1 + " = " + target +"." +  a2 ]
                                    
        for src in self.pw :
            for dst in self.p : 
                source = src.name.split(".")[0]
                target = dst.name.split(".")[0]
                # #print("GENERE condition Dep 2 ")
                # #print("----------"+name +"-------------")
                # #print(dst.couple_dependance)
                for a1,b1 in src.couple_dependance.items():
                    # #print(a1,b1)
                    for  a2,b2 in dst.couple_dependance.items():
                        # #print(a2,b2)
                        # #print(len(b1),len(b2))
                        for el in b1 :
                            for e in b2 :
                                # #print(el,e)
                                if ( el == e and (source,target) in self.dep_sans_doublons.keys()):
                                    el = el.replace(" : " , ":")
                                    e =e.replace(" : ",":")
                                     #print("src : " + source + ' , ' + e + " // dst : " + target + " , " + el )
                                    #if ( str(source + "." + a1 + " = " + target +"." +  a2) not in self.dep_sans_doublons[source,target] ) :
                                    self.dep_sans_doublons[source,target] = self.dep_sans_doublons[source,target] + [ source + "." + a1 + " = " + target +"." +  a2 ]
                                    
    def generer_graphml(self):
        with open(os.getcwd()+"/graph.graphml", "w") as fichier:
            fichier.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            fichier.write("<graphml xmlns='http://graphml.graphdrawing.org/xmlns\'>\n")
            fichier.write('\t<key id="d0" for="node" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d1" for="edge" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d2" for="edge" attr.name="weight" attr.type="string"/>\n')
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
                        fichier.write(dep.type +';'+dep.table+'('+str(dep.id)+').'+dep.complement.strip()+'\n\n</data></edge>')
                    
                
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.fichier = fichier
    
    
        
    def affiche_lise_sans_doublon(self):
        with open ("./graphs/dependences.gogol","w") as fichier :
            fichier.write('## Fichier généré dans le cadre de Plong2019/2020 pour etudier les raisons de dépendances entre 2 transactions , ID = "nom_de_la_relation" SRC = "fichier_source" DST = "fichier_destination"\n## ( SRC et DST sont en rapport avec les fleches du graphes).\n\n')
        for cle,value in self.dep_sans_doublons.items():
            #print(cle)
            for v in value :
                v = v.strip()
                #print("v :" ,v )
            i = list(set(value))
            for v in  i : 
                self.write_raison_dependances(v,cle[0],cle[1])
                #if ( "rw" in v or 'wr' in v or 'ww' in v ):
                    #print(cle[0],cle[1] ,"v :: " , v)
                        
        return self.dep_sans_doublons
                
    def write_raison_dependances(self,v ,src,dst):
        # #print ( src ) 
        fsrc = src
        fdst = dst
        src = src + ".sql"
        dst = dst + ".sql"
        # #print( dst ) 
        l_dep_src = []
        l_dep_dst = []
        # #print("------------------- " + v )
        
        #if ( "rw" in v or 'wr' in v or 'ww' in v and src != dst ):
         #   print(src , dst ,"v :: " , v)
        #print(src,dst)
        with open ("./graphs/dependences.gogol","a+") as fichier :
            
            
            if ( "wr" in v ) :
                #t = re.findall(";.*?\(",v)
                if ( ";" in v ):
                    t = v.split(";")[1].split("(")[0]
                else:
                    t = v.split(",")[1].split("(")[0]
                #print("t : " , t )
                #print('t|' ,t,'|' )
                #print("v : " ,v )
                if ( t != None and t != [] ):
                    table = t.replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                # #print(tmp)
                attr = tmp[0].replace(").","").strip()
                t = ""
                #print("attr = " , attr )
                #print(table)
                #print(v)
                for elt in self.p :
                    # #print(elt.name)
                    if ( elt.name == dst ):
                        # #print("\t\tRaison de la dependance DST : "+dst+" : ")
                        for c,val in elt.table_from.items():
                            if ( table in val ):
                                t = c
                                
                        if ( t != "" ):
                            # #print(tmp)
                            if ( attr == "*" ):
                                #print('ok' + table)
                                m = re.findall("SELECT.*? FROM "+table+".*?;",elt.data)
                                #print(m)
                            else :
                                m = re.findall("SELECT.*? "+attr+" FROM "+table+".*?;",elt.data)
                            if ( m != [] ) :
                                for li in m :
                                    l = li.split(";")
                                    #print("On cherche : " ,attr , "\nDans :: ",str(m))
                                    for e in l :
                                        e = e.strip()
                                        if ( "SELECT" in e and attr in e ):
                                            l_dep_dst.append(e)
                                        elif ( attr == "*" ):
                                            if ( "SELECT" in e and table in e):
                                                #print('e = ' ,e , table)
                                                l_dep_dst.append(e)
                for elt in self.pw :
                    if ( elt.name == src ):
                        tmp = "UPDATE "+table+" SET " +attr
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            # #print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                #print('li : ',li, '\n')
                                l_dep_src.append(li)
                    
                                
            if ( "rw" in v or "rw," in v ) :
                
                if ( ";" in v ):
                    t = v.split(";")[1].split("(")[0]
                else:
                    t = v.split(",")[1].split("(")[0]
                #print("t : " , t )
                #print('t|' ,t,'|' )
                #print("v : " ,v )
                if ( t != None and t != [] ):
                    table = t.replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                # #print(tmp)
                attr = tmp[0].replace(").","").strip()
                t = ""
                l_dep_src = []
                l_dep_dst = []
                for elt in self.pw :
                    if ( elt.name == dst ):
                        tmp = "UPDATE "+table+" SET " +attr
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            # #print("\t\tRaison de la dependance DST : "+dst+" : ")
                            for li in m :
                                # #print('\t\t\t'+li)
                                l_dep_dst.append(li)
                                
                for elt in self.p :
                    # #print(elt.name)
                    if ( elt.name == src ):
                        # #print("\t\tRaison de la dependance SRC : "+src+" : ")
                        #print(attr)
                        if ( attr == "*" ):
                            #print('ok' + table)
                            m = re.findall("SELECT.*? FROM "+table+".*?;",elt.data)
                            
                        else :
                            m = re.findall("SELECT.*? "+attr+" FROM "+table+".*?;",elt.data)
                        #print(v)
                        if ( m != [] ) :
                            for li in m :
                                l = li.split(";")
                                #print("On cherche : " ,attr )
                                for e in l :
                                    e = e.strip()
                                    if ( "SELECT" in e and attr in e ):
                                        l_dep_dst.append(e)
                                    elif ( attr == "*" ):
                                        if ( "SELECT" in e and table in e):
                                            #print('e = ' ,e)
                                            l_dep_dst.append(e)          
            if ( "ww;" in v ) :
                t = re.findall(";.*?\(",v)
                table = t[0].replace(";","").replace("(","")
                tmp = re.findall("\)\..*",v)
                # #print(tmp)
                attr = tmp[0].replace(").","").strip()
                # #print(table)
                
                ## test ##
                attr = attr.strip()
                l_dep_src = []
                l_dep_dst = []
                # #print("ATTR =" + attr +".")
                for lect in self.p :
                    if ( attr != "*" ):
                        # #print("!= * , "+attr)
                        m = re.search("SELECT.*?"+attr+".*?;",lect.data)
                        # #print((fsrc,lect.name.split(".")[0]))
                        if (m != None and m[0] != None):
                            a = m[0].split(";")
                            for elt in a :
                                r = re.findall(" "+attr+" ",elt )
                                if ( r == [] ):
                                    break 
                                if ( attr in elt ):
                                    # #print(elt)
                                    a = (fsrc,lect.name.split(".")[0]) 
                                    g = (lect.name.split(".")[0],fsrc) 
                                    # #print(a)
                                    if ( a in self.dict_finale.keys() ):
                                        b = v.replace("ww","wr")
                                        h = v.replace("ww","rw")
                                        self.dict_finale[a].append(b)
                                        self.dict_finale[g].append(h)
                                        l_dep_src.append(v)
                                        l_dep_dst.append(elt)
                                        
                                    else : 
                                        b = v.replace("ww","wr")
                                        h = v.replace("ww","rw")
                                        self.dict_finale[a] = []
                                        self.dict_finale[a].append(b)
                                        self.dict_finale[g] = []
                                        self.dict_finale[g].append(h)
                                        l_dep_src.append(v)
                                        l_dep_dst.append(elt)
                                                        
                    else :
                        m = re.search("SELECT.*?"+table+".*?;",lect.data)
                        if ( m != None and m[0] != None):
                            a = m[0].split(";")
                            for elt in a :
                                if ( table in elt and "SELECT" in elt ) :
                                     #print(elt)
                                    a = (fsrc,lect.name.split(".")[0]) 
                                    g = (lect.name.split(".")[0],fsrc) 
                                    # #print(a)
                                    if ( a in self.dict_finale.keys() ):
                                        # #print("OK , elt = " + elt)
                                        b = v.replace("ww","wr")
                                        h = v.replace("ww","rw")
                                        # #print(b)
                                        if ( b not in self.dict_finale[a] ):
                                            self.dict_finale[a].append(b)
                                        if ( h not in self.dict_finale[g] ):
                                            self.dict_finale[g].append(h)
                                        # #print(";;;;;;;;;;;;;;" + str(self.dict_finale[a]))
                                        l_dep_src.append(v)
                                        l_dep_dst.append(elt)
                                        
                                    else : 
                                        b = v.replace("ww","wr")
                                        h = v.replace("ww","rw")
                                         #print(b)
                                        self.dict_finale[a] = []
                                        if ( b not in self.dict_finale[a] ):
                                            self.dict_finale[a].append(b)
                                        self.dict_finale[g] = []
                                        if ( h not in self.dict_finale[g] ):
                                            self.dict_finale[g].append(h)
                                        l_dep_src.append(v)
                                        l_dep_dst.append(elt)
                            
                            
                        
                ## fin test
                
                for elt in self.pw :
                    if ( elt.name == src and src == dst):
                        tmp = "INSERT INTO "+table
                        # #print(tmp)
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            # #print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                # #print('\t\t\t'+li)
                                l_dep_src.append(li)
                                l_dep_dst.append(li)
                    else:
                        tmp = "UPDATE "+table
                        # #print(tmp)
                        m = re.findall(tmp+".*?;",elt.data)
                        if ( m != [] ):
                            # #print("\t\tRaison de la dependance SRC : "+src+" : ")
                            for li in m :
                                # #print('\t\t\t'+li)
                                l_dep_src.append(li)
                                l_dep_dst.append(li)
                                
                            
                    
                
                if ( ".*" not in t[0] ):
                    for elt in self.pw :
                        if ( elt.name == src and src == dst):
                            tmp = "UPDATE "+table+" SET " +attr
                            m = re.findall(tmp+".*?;",elt.data)
                            if ( m != [] ):
                                # #print("\t\tRaison de la dependance DST : "+dst+" : ")
                                # #print("m :::: :: :: :: : : :" + str(m))
                                l_dep_src = []
                                l_dep_dst = []
                                for li in m :
                                    # #print('\t\t\t'+li)
                                    l_dep_src.append(li)
                                    l_dep_dst.append(li)
                if ( ").*" in v ):
                    #print("trouvé")
                    for elt in self.pw :
                        if ( elt.name == src and src == dst):
                            tmp = "INSERT INTO "+table
                            # #print(tmp)
                            m = re.findall(tmp+".*?;",elt.data)
                            if ( m != [] ):
                                # #print("\t\tRaison de la dependance SRC : "+src+" : ")
                                for li in m :
                                    # #print('\t\t\t'+li)
                                    l_dep_src.append(li)
                                    l_dep_dst.append(li)
                        
                
            if ( "=" in v ):
                if ( (fsrc,fdst) in self.dict_finale.keys() ):
                    self.dict_finale[fsrc,fdst].append(v)
                    # #print("VALUE : " + v )
                else : 
                    self.dict_finale[fsrc,fdst] = []
                    # #print("VALUE : " + v )
                    self.dict_finale[fsrc,fdst].append(v)
                return 0
                
                
                
            # #print("//////////////////////////////////////")
            # #print(len(l_dep_src))
            # #print(len(l_dep_dst))
            # #print("//////////////////////////////////////")
            
            if ( l_dep_src == [] or l_dep_dst == [] ):
                # #print("RIP")
                return -1
            if ( 1 == 0 ):
                a=1
            else :
                if ( (fsrc,fdst) in self.dict_finale.keys() ):
                    self.dict_finale[fsrc,fdst].append(v)
                    # #print("VALUE : " + v )
                else : 
                    self.dict_finale[fsrc,fdst] = []
                    # #print("VALUE : " + v )
                    self.dict_finale[fsrc,fdst].append(v)
                    
                l_ecriture = []
                #fichier.write('\n<Relation ID="'+v.strip().replace(";",",")+'" SRC="'+fsrc+'" DST="'+fdst+'">\n')
                #fichier.write('<SRC>\n')
                l_ecriture.append('<SRC>\n')
                check_condi2 = 0
                count2 = 0
                for elt in l_dep_src :
                    if ( "wr" not in elt.strip() and "rw" not in elt.strip() and "ww" not in elt.strip() ):
                        count2 = count2 +1
                        debut , fin = self.trouve_ligne_fichier(elt,fsrc)
                        #fichier.write('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                        #l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                        if ( self.isRelationIfElse(v,elt.strip() , fsrc) == True or self.isRelationIfElse(v,elt.strip() , fdst) == True):
                            #fichier.write('2 ! \t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            check_condi2 =check_condi2 +1
                        else :
                            #fichier.write('1 ! \t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                        
                #fichier.write('</SRC>\n')
                l_ecriture.append('</SRC>\n')
                #fichier.write('<DST>\n')
                l_ecriture.append('<DST>\n')
                # cariable pour ensuite tester si TOUTE les Operations sont dans des "If" , si oui , cette relation est dites '-' , a savoir RW- ou RW+
                check_condi = 0
                count = 0
                for elt in l_dep_dst :
                    if ( "wr" not in elt.strip() and "rw" not in elt.strip() and "ww" not in elt.strip() ):
                        count = count +1
                        debut , fin = self.trouve_ligne_fichier(elt,fsrc)
                        # #print("test" + elt.strip())
                        #fichier.write('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                        #l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                        if ( self.isRelationIfElse(v,elt.strip() , fsrc) == True ):
                            #fichier.write('2 ! \t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            check_condi =check_condi +1
                        else :
                            #fichier.write('1 ! \t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            l_ecriture.append('\t l: '+str(debut) + '\t' +elt.strip()+'\n')
                            
                #fichier.write('</DST>\n')
                l_ecriture.append('</DST>\n')
                #fichier.write('</Relation>\n\n')
                l_ecriture.append('</Relation>\n\n')
                
                # si TOUTES les operation sont non obligatoires
                l_fin = [""]
                if ( count == check_condi or count2 == check_condi2):
                    l_fin[0] = ('\n<Relation ID="'+v.strip().replace(";",",")+'" SRC="'+fsrc+'" DST="'+fdst+'" CONDITION=True >\n')
                    l_fin.append(l_ecriture)
                else:
                    l_fin[0] = ('\n<Relation ID="'+v.strip().replace(";",",")+'" SRC="'+fsrc+'" DST="'+fdst+'" CONDITION=False >\n')
                    l_fin.append(l_ecriture)
                
                
                for elt in l_fin:
                    for a in elt :
                        fichier.write(a)
                    
                    
                fichier.write("\n\n\n\n")
                return 0
                
    def isRelationIfElse(self,relation ,requette , file):
        if ( "wr" in requette or "rw" in requette or "ww" in requette ):
            return False
        data =""
        for elt in self.p:
            if elt.name.split(".")[0] == file :
                data = elt.data
                if ( data == None ):
                    break
                p = "IF .*? END IF;"
                m = re.findall(p,data)
                if ( m ):
                    for a in m :
                        if ( re.findall(requette,str(a))):
                            return True
                p = "ELSE .*? END IF;"
                m = re.findall(p,data)
                if ( m ):
                    for a in m :
                        if ( re.findall(requette,str(a))):
                            return True
            
        return False
                
                
    def trouve_ligne_fichier(self , elt , file ):
        # #print("elt :: " + elt )
        debut = 0 
        fin = 0
        cpt = 0 
        check = True
        
        with open ( dossier+"/"+file+".sql" ,"r+" ) as myFile:
            for num, line in enumerate(myFile, 1):
                a = line.strip()
                if ( a != "" and a in elt and debut == 0 ):
                    for i in range(0,len(a)) :
                        if ( a[i] != elt[i] ):
                            check = False
                        else :
                            # #print(elt)
                            check = True    
                    if ( check == True ):
                        debut = num
                
                if ( ";" in a and debut <= num ):
                    fin = num
    
        return debut,fin
                
    def genere_graphml_sans_doublons(self):
        with open("./graphs/graph_sous_doub.graphml", "w") as fichier:
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
                    fichier.write(elt.strip().replace(";",",")+'\n')
                    fichier.write('\n\n</data>\n</edge>')
                        
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.fichier = fichier
            
    def genere_graphml_sans_doublons_plus_Raisons_dependances(self):
         #print("OK")
        l = []
        d = dict()
        for c1 , v1 in self.dict_finale.items() :
            # #print(c1)
            for elt in v1 :
                if ( "ww" in elt ) :
                    t = re.findall("ww;.*?\(",elt)
                    tmp = t[0]
                    # #print("WW trouvé : " + tmp )
                    for c2 , v2 in self.dict_finale.items() :
                        for el in v2 :
                            if ( tmp in el ):
                                l.append(tmp)
                                table = tmp.split(";")[1]
                                table = table[:-1]
                                 #print(table)
                                if ( table in self.pk.couple.keys() ):
                                    if ( (c1[0],c2[0]) in d.keys() ) :
                                        d[c1[0],c2[0]].append(tmp+str(self.pk.couple[table])+").*")
                                    else :
                                        d[c1[0],c2[0]] = [tmp+str(self.pk.couple[table])+").*"]
                                
        for c,v in d.items() :
             #print("- " + c[0],c[1] + " - " + str(v)  )
            for elt in v :
                if ( elt not in self.dict_finale[c[0],c[1]] ) :
                    self.dict_finale[c[0],c[1]].append(elt)
        for c1 , v1 in self.dict_finale.items() :
            self.dict_finale[c1] = list(set(v1))
            
        
        #print("##########################################")
        lgaph = []
        with open("./graphs/graph.graphml", "w") as fichier:
            fichier.write('<?xml version="1.0" encoding="UTF-8"?>\n')
            fichier.write("<graphml xmlns='http://graphml.graphdrawing.org/xmlns\'>\n")
            fichier.write('\t<key id="d0" for="node" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d1" for="edge" attr.name="weight" attr.type="string"/>\n')
            fichier.write('\t<key id="d2" for="edge" attr.name="weight" attr.type="string"/>\n')
            fichier.write('<graph id="G" edgedefault="directed\">\n')
            
            #creation node
            for cle , value in self.dict_finale.items() :
                src = self.p[0]
                dst = self.p[0]
                
                for elt in self.p :
                    if ( elt.name == cle[0]+".sql" ):
                        src = elt
                    if ( elt.name == cle[1]+".sql" ):
                        dst = elt
                        
                s = src.name.split(".")[0]
                d = dst.name.split(".")[0]
                for a1,b1 in src.couple_dependance.items():
                    for  a2,b2 in dst.couple_dependance.items():
                        for el in b2 :
                            for e in b1 :
                                
                                if ( el == e ):
                                    el = el.replace(" : " , ":")
                                    e =e.replace(" : ",":")
                                    value = value + [ s + "." + a1 + " = " + d +"." +  a2 ]

                if ( 1 == 1 ):
                    l = []
                    for v in value :
                        if ( v not in l ) :
                            l.append(v)
                    
                    l_condi = []
                    l_non_condi = []
                    l_non_condi.append('\n<node id="'+cle[0]+'">\n')
                    l_non_condi.append('<data key="d0">"'+cle[0]+'"</data>\n</node>')
                    l_non_condi.append('\n<edge source="'+cle[0]+'" target="'+cle[1]+'">\n')
                    l_non_condi.append('<data key="d1">\n')
                    
                    
                    for elt in l :
                        if ( ").*" in elt ):
                            if ( (cle[0],cle[1]) in self.dep_sans_doublons.keys() and elt not in self.dep_sans_doublons[cle[0],cle[1]] ):
                                self.dep_sans_doublons[cle[0],cle[1]].append(elt.strip())
                            #print("ok .* ajouté")
                            
                        if ( ';' in elt or ',' in elt ):
                            new_list = value.copy()
                            p = self.maj_graph_IfElse(elt,cle[0],cle[1] , new_list )
                            
                            if ( p == [] or p == None):
                                if ( elt.replace(";",",") not in l_non_condi ):
                                    l_non_condi.append(elt.replace(";",",").strip()+'\n')
                                #fichier.write(elt.replace(";",",")+'\n')
                                
                            else :
                                if ( elt.replace(";",",").strip() not in l_condi ):
                                    l_condi.append(p)
                        else :
                            if ( elt.replace(";",",") not in l_non_condi ):
                                l_non_condi.append(elt.strip().replace(";",",").strip()+'\n')
                        
                    if ( l_condi != [] and l_condi != None ) :
                        size = (len(l_condi))
                        #print(size)
                        
                        tmp = l_condi[4:(size-1)]
                        tmp = list(set(tmp))
                        #for e in tmp :
                        #    print(e)
                            
                        l_condi[4:(size-1)] = tmp
                        for a in l_condi :
                            for b in a :
                                fichier.write(b.replace(";",","))
                        lgaph.append(l_condi)
                        
                    if ( l_non_condi != [] and l_non_condi != None ) :
                        check = False
                        for elt in l_non_condi :
                            for b in elt :
                                if (',' in b ) :
                                    check = True
                        l_non_condi.append('\n</data>\n</edge>\n\n')  
                        if ( check == True ):
                            
                            size = (len(l_non_condi))
                            #print(size)
                            
                            tmp = l_non_condi[4:(size-1)]
                            tmp = list(set(tmp))
                            #for e in tmp :
                                #print(e)
                                
                            l_non_condi[4:(size-1)] = tmp
                            for a in l_non_condi :
                                #print(a)
                                
                                for b in a :
                                    fichier.write(b.replace(";",","))
                            ## on retraite ls dépendance en fonction du grapml
                            lgaph.append(l_non_condi)   
            fichier.write("</graph>\n\t</graphml>")
        fichier.close()
        self.maj_gogol(lgaph)
        self.fichier = fichier
        
        
        
        
        
    def maj_gogol(self, l):
        src =""
        dst =""
        for elt in l:
            for a in elt :
                #print(a)
                if ( "source" in a ):
                    src = a.split('"')[1]
                    dst = a.split('"')[3]
                    #print(src,dst)
                if ( "wr" in a or "rw" in a or "ww" in a ):
                    #print (src , ' - ', dst , ' : ' ,a)
                    self.raison_dependance(a,src,dst)
    
    def raison_dependance(self,a,src,dst):
        fsrc = src+".sql"
        fdst = dst + ".sql"
        t = ""
        
        if ( ";" in a ):
            t = a.split(";")[1].split("(")[0]
        else:
            t = a.split(",")[1].split("(")[0]
        if ( t != None and t != [] ):
            table = t.replace(";","").replace("(","")
        tmp = re.findall("\)\..*",a)
        attr = tmp[0].replace(").","").strip()
        if ( "wr" in a ):
            l_src = ""
            raison_source = []
            raison_dst = []
            with open ( self.dossier + "/" + fdst ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and "SELECT" in elt):
                        #print(elt)
                        raison_source.append(elt)
                        
                    elif ( attr == "*" and "SELECT" in elt ):
                        if ( table in elt ):
                            #print(elt)
                            raison_source.append(elt)
            with open ( self.dossier + "/" + fsrc ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and "UPDATE" in elt):
                        #print(elt)
                        raison_dst.append(elt)
                        
                    elif ( attr == "*" and "INSERT" in elt ):
                        if ( table in elt ):
                            #print(elt)
                            raison_dst.append(elt)
                            
            with open ("./graphs/dependences.gogol","a+") as fichier :
                fichier.write('\n<Relation ID="'+a.strip()+'" SRC="'+src+'" DST="'+dst+'" CONDITION=False >\n')
                fichier.write('<SRC>\n')
                raison_dst = list(set(raison_dst))
                for elt in raison_dst :
                    d,f = self.trouve_ligne_fichier(elt , src )
                    fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                fichier.write('</SRC>\n')
                fichier.write('<DST>\n')
                raison_source = list(set(raison_source))
                for elt in raison_source :
                    d,f = self.trouve_ligne_fichier(elt , dst )
                    fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                fichier.write('</DST>\n')
                fichier.write("</Relation>\n\n")
        if ( "rw" in a ):
            l_src = ""
            raison_source = []
            raison_dst = []
            with open ( self.dossier + "/" + fsrc ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and "SELECT" in elt):
                        #print(elt)
                        raison_source.append(elt)
                        
                    elif ( attr == "*" and "SELECT" in elt ):
                        if ( table in elt ):
                            #print(elt)
                            raison_source.append(elt)
            with open ( self.dossier + "/" + fdst ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and "UPDATE" in elt):
                        #print(elt)
                        raison_dst.append(elt)
                        
                    elif ( attr == "*" and "INSERT" in elt ):
                        if ( table in elt ):
                            #print(elt)
                            raison_dst.append(elt)
                            
            with open ("./graphs/dependences.gogol","a+") as fichier :
                fichier.write('\n<Relation ID="'+a.strip()+'" SRC="'+src+'" DST="'+dst+'" CONDITION=False >\n')
                fichier.write('<SRC>\n')
                raison_dst = list(set(raison_dst))
                for elt in raison_dst :
                    d,f = self.trouve_ligne_fichier(elt , src )
                    fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                fichier.write('</SRC>\n')
                fichier.write('<DST>\n')
                raison_source = list(set(raison_source))
                for elt in raison_source :
                    d,f = self.trouve_ligne_fichier(elt , dst )
                    fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                fichier.write('</DST>\n')
                fichier.write("</Relation>\n\n")
        if ( "ww" in a ):
            l_src = ""
            raison_source = []
            raison_dst = []
            with open ( self.dossier + "/" + fsrc ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and ( "INSERT" in elt or "UPDATE" in elt ) ):
                        #print(elt)
                        raison_dst.append(elt)
                        
                    elif ( attr == "*" and ( "INSERT" in elt or "UPDATE" in elt ) ):
                        if ( table in elt ):
                            #print(elt)
                            raison_dst.append(elt)
                            
            with open ( self.dossier + "/" + fdst ) as F :
                for ligne in F :
                    l = ligne.strip()
                    if ( l != '' ):
                        l_src = l_src + l
                l = l_src.split(";")
                
                for elt in l :
                    if ( attr in elt and ( "INSERT" in elt or "UPDATE" in elt ) ):
                        #print(elt)
                        raison_source.append(elt)
                        
                    elif ( attr == "*" and ( "INSERT" in elt or "UPDATE" in elt )  ):
                        if ( table in elt ):
                            #print(elt)
                            raison_source.append(elt)
                            
            with open ("./graphs/dependences.gogol","a+") as fichier :
                if ( src == dst ) :
                    fichier.write('\n<Relation ID="'+a.strip()+'" SRC="'+src+'" DST="'+dst+'" CONDITION=False >\n')
                    fichier.write('<SRC>\n')
                    raison_source = list(set(raison_source))
                    for elt in raison_source :
                        d,f = self.trouve_ligne_fichier(elt , dst )
                        # //////
                        l = self.isRelationIfElse(elt,elt,dst)
                        if ( l == True ): 
                            print("Is_If_Else : ",l )
                        # /////
                        fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                    fichier.write('</SRC>\n')
                    fichier.write('<DST>\n')
                    raison_source = list(set(raison_source))
                    for elt in raison_source :
                        d,f = self.trouve_ligne_fichier(elt , dst )
                        fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                    fichier.write('</DST>\n')
                    fichier.write("</Relation>\n\n")
                else :
                    fichier.write('\n<Relation ID="'+a.strip()+'" SRC="'+src+'" DST="'+dst+'" CONDITION=False >\n')
                    fichier.write('<SRC>\n')
                    raison_dst = list(set(raison_dst))
                    for elt in raison_dst :
                        d,f = self.trouve_ligne_fichier(elt , src )
                        # //////
                        l = self.isRelationIfElse(elt,elt,dst)
                        if ( l == True ): 
                            print("Is_If_Else : ",l )
                        # /////
                        fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                    fichier.write('</SRC>\n')
                    fichier.write('<DST>\n')
                    raison_source = list(set(raison_source))
                    for elt in raison_source :
                        d,f = self.trouve_ligne_fichier(elt , dst )
                        fichier.write('\t'+"l: "+str(d)+'\t' +elt+';\n')
                    fichier.write('</DST>\n')
                    fichier.write("</Relation>\n\n")
                    
        ## ajouter les If/else pour les nouvelles relations
                    
        
    def maj_graph_IfElse(self,relation,s,d,value): # s = source, d = destination
        l = []
        with open ("./graphs/dependences.gogol","r") as fichier :
            for line in fichier :
                # #print(line)
                # #print ( relation)
                if ( relation.replace(";",",") in line and s in line and d in line and "CONDITION=True" in line):
                    l.append('\n<node id="'+s+'">\n')
                    l.append('<data key="d0">"'+s+'"</data>\n</node>')
                    l.append('\n<edge source="'+s+'" target="'+d+'">\n')
                    l.append('<data key="d2">\n')
                    l.append(relation+'\n')
                    
                    l_tmp = []
                    for v in value :
                        if ( v not in l_tmp and "=" in v ) :
                            l.append(v+'\n')
                    l.append('\n</data>\n</edge>\n\n')
                    return l
        fichier.close()
        return []            
                
    def maj_doublon(self,dict ):
        with open ("./graphs/dependences.gogol","w") as fichier :
            fichier.write('## Fichier généré dans le cadre de Plong2019/2020 pour etudier les raisons de dépendances entre 2 transactions , ID = "nom_de_la_relation" SRC = "fichier_source" DST = "fichier_destination"\n## ( SRC et DST sont en rapport avec les fleches du graphes).\n\n')
        for cle,v in dict.items():
            
            #print("cle : ----------- " , cle)
            for a in v :
                a = a.strip()
            v = list(set(v))
            #for a in v:
                #print(a)
            self.dep_sans_doublons = dict
        for cle,value in dict.items():
            #print(cle)
            for v in value :
                v = v.strip()
                #print("v :" ,v )
            i = list(set(value))
            for v in  i : 
                self.write_raison_dependances(v,cle[0],cle[1])
        
    def lanceur_f(self):
        self.lanceur()
        #print("-------------------------------------Dépendances---------------------")
        self.create_dep_ww_insert()
        self.create_dependance_wr()
        self.create_dependance_rw()
        self.create_dependance_ww()
        self.maj_dep_wr_rw_en_ww()
        #self.affiche_dependance()
        self.parse_dependance()
        #self.generer_graphml()
        self.genere_couple_source_target()
         #print("############################################")
        self.verifie_dep_ww()
        self.traite_dep_ww_entrefonctions()
        self.genere_condition_dep()
        dict = self.affiche_lise_sans_doublon()
        self.genere_graphml_sans_doublons()
        self.genere_graphml_sans_doublons_plus_Raisons_dependances()
        
        #self.maj_doublon(dict)
        
        
if __name__ == "__main__":  
    argv = sys.argv
    if (len(argv) != 2 ) :
         print("Merci d'entrer un dossier contenant : \n - un fichier genDB.sql contenant votre base de données \n - un ensemble de fichier reprensetant les transactions ( au format .sql ; PLpgSQL ) ")
    else :
        #dossier = argv[1].replace("/","")
        dossier = argv[1]
        print("Nous allons travailler sur le repertoire suivant : " , dossier )
        main = principal(dossier)
        main.lanceur_f()
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    