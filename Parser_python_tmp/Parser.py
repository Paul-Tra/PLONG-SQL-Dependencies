import os
import sys
from Read_file import *
from Primary_key import *
from Gogol import *

class Parser:
    def __init__(self, work_folder):
        self.work_folder = work_folder
        # all file will be with the .sql format
        self.files_list = os.listdir(work_folder)
        self.list_readFile = []
        for e in self.files_list :
            if ( e == "genDB.sql" ):
                self.genDB = e 
                self.files_list.remove(e)
                break
        self.Dependencies = dict() # [src,dst] = ["ww;balbla(PK).attr","wr;......",.....]
        self.conditional_Dependencies = dict() #  if request ... else : request .....       [src,dst] = ["ww;balbla(PK).attr","wr;......",.....]
        
    def play(self):
        #print(".. working progress.. \n\nFind primary Key for each Table :")
        print("\tPath : ", self.work_folder+self.genDB)
        self.primary_key_obj = PrimaryKey(self.work_folder+self.genDB)
        #print("\n-------------------------------------------------------------\n")
        self.dic_primary_key = self.primary_key_obj.dict_table_attr
        self.process()
        #print("OK process OK")
        #self.print_dependency()
        count,nb_edge = self.write_graphml()
        print("## ", count, "Edges were found with : "+ str(nb_edge) + " relations , please see the grampl file ( into 'graphs' repo. ) ##\n")
        print("Look at the .gogol file to see more details about relations")
        self.gogol = Gogol(self,"graphs/Mygraphml.graphml",self.work_folder)
        
        
    def process(self):
        for file in self.files_list :
            self.list_readFile.append(Read_file(self.work_folder+file , self.dic_primary_key )) # create all the Read_file object for each file contains in ''work_folder'' exept ''GenDB.sql''
        
        for src in self.list_readFile :
            for dst in self.list_readFile :
                if ( src.file_name == dst.file_name ) : # process "ww" relation on the same file . ( update / insert )
                    self.analyze_UPDATE(src) # update on a same file -> ww on this same file
                    self.analyze_INSERT1(src,dst) # we have to check if there is a 'select' on the same attr on this file
                    self.analyze_SELECT1(src,dst) # if there are a select according to an update or insert case
                if ( src.file_name != dst.file_name ) : # check if src and dst are diff.
                    self.analyze_SELECT1(src,dst)
                    self.analyze_SELECT2(dst,src)
                    self.analyze_SELECT3(src,dst)
                    
    def check_condional_dependencies(self,  src , request): # check if the 'request' in 'src' file is into an IF (.... ) else : ( ) ;
        for elt in request :
            #print("CHECK condi dans : " , src.file_name , " pour : " , elt)
            tmp = src.new_content.split(";")
            cpt = 0
            for line in tmp :
                elt = elt.replace(";","").strip()
                if ( "IF (" in line ):
                    cpt = cpt+1
                if ( elt in line and cpt > 0 ):
                    return True
                if ("END IF" in line ):
                    cpt = cpt-1
            return False
    
    
    def analyze_SELECT1(self , file_src, file_dst):
        for elt in file_src.select_liste :
            for up_table,up_attr in file_dst.dict_update_table_attr.items() :
                for at in up_attr :
                    str = up_table+"."+at
                    res = re.findall("SELECT.*?"+str+"FROM.*?;",elt)
                    if ( res ) :
                        condi = self.check_condional_dependencies(file_src,res)
                        if ( condi ):
                            str = self.returnPk(up_table)
                            string = ["rw,"+up_table+"("+str(str)+")"+at]
                            if ( (file_src,file_dst) not in self.conditional_Dependencies.keys() ) :
                                self.conditional_Dependencies[file_src,file_dst] = [string]
                            else :
                                self.conditional_Dependencies[file_src,file_dst].append(string)
                        else :
                            str = self.returnPk(up_table)
                            string = ["rw,"+up_table+"("+str(str)+")"+at]
                            if ( (file_src,file_dst) not in self.Dependencies.keys() ) :
                                self.Dependencies[file_src,file_dst] = [string]
                            else :
                                self.Dependencies[file_src,file_dst].append(string)
                    else :
                        str = up_table+".*"
                        res = re.findall("SELECT.*?"+str+"FROM.*?;",elt)
                        if ( res ) :
                            condi = self.check_condional_dependencies(file_src,res)
                            a = self.returnPk(up_table)
                            string = "rw,"+up_table+"(["
                            for elt in a :
                                string = string +"'"+ elt +"',"
                            string = string[:-1] + "])." + at 
                            if ( condi ) :
                                if ( (file_src,file_dst) not in self.conditional_Dependencies.keys() ) :
                                    self.conditional_Dependencies[file_src,file_dst] = [string]
                                else :
                                    if ( string not in self.conditional_Dependencies[file_src,file_dst] ) :
                                        self.conditional_Dependencies[file_src,file_dst].append(string)
                            else :
                                if ( (file_src,file_dst) not in self.Dependencies.keys() ) :
                                    self.Dependencies[file_src,file_dst] = [string]
                                else :
                                    if ( string not in self.Dependencies[file_src,file_dst] ) :
                                        self.Dependencies[file_src,file_dst].append(string)
    
    def analyze_SELECT2(self , file_src, file_dst):
        for elt in file_dst.select_liste :
            for up_table,up_attr in file_src.dict_update_table_attr.items() :
                for at in up_attr :
                    str = up_table+"."+at
                    res = re.findall("SELECT.*?"+str+"FROM.*?;",elt)
                    if ( res ) :
                        
                        str = self.returnPk(up_table)
                        string = ["wr,"+up_table+"("+str(str)+")"+at]
                        condi = self.check_condional_dependencies(file_src,res)
                        if ( condi ) :
                            if ( (file_src,file_dst) not in self.conditional_Dependencies.keys() ) :
                                self.conditional_Dependencies[file_src,file_dst] = [string]
                            else :
                                self.conditional_Dependencies[file_src,file_dst].append(string)
                        else : 
                            if ( (file_src,file_dst) not in self.Dependencies.keys() ) :
                                self.Dependencies[file_src,file_dst] = [string]
                            else :
                                self.Dependencies[file_src,file_dst].append(string)
                    else :
                        str = up_table+".*"
                        res = re.findall("SELECT.*?"+str+"FROM.*?;",elt)
                        self.check_condional_dependencies(file_src,res)
                        if ( res ) :
                            a = self.returnPk(up_table)
                            string = "wr,"+up_table+"(["
                            for elt in a :
                                string = string +"'"+ elt +"',"
                            string = string[:-1] + "])." + at 
                            condi = self.check_condional_dependencies(file_src,res)
                            if ( condi ) :
                                if ( (file_src,file_dst) not in self.conditional_Dependencies.keys() ) :
                                    self.conditional_Dependencies[file_src,file_dst] = [string]
                                else :
                                    if ( string not in self.conditional_Dependencies[file_src,file_dst] ) :
                                        self.conditional_Dependencies[file_src,file_dst].append(string) 
                            else :
                                if ( (file_src,file_dst) not in self.Dependencies.keys() ) :
                                    self.Dependencies[file_src,file_dst] = [string]
                                else :
                                    if ( string not in self.Dependencies[file_src,file_dst] ) :
                                        self.Dependencies[file_src,file_dst].append(string)                        
    
    def analyze_SELECT3(self,file_src,file_dst):
        for elt in file_src.select_liste :
            for ins in file_dst.list_table_insert :
                res = re.findall("SELECT.*?"+ins+".*?FROM.*?"+ins+".*?;",elt)
                if ( res ):
                    string = "rw,"+ins+"(*).*"
                    condi = self.check_condional_dependencies(file_src,res)
                    if ( condi ) :
                        if ( (file_src,file_dst) not in self.conditional_Dependencies.keys() ) :
                            self.conditional_Dependencies[file_src,file_dst] = [string]
                        else :
                            self.conditional_Dependencies[file_src,file_dst].append(string)
                        string = "wr,"+ins+"(*).*"
                        if ( (file_dst,file_src) not in self.conditional_Dependencies.keys() ) :
                            self.conditional_Dependencies[file_dst,file_src] = [string]
                        else :
                            self.conditional_Dependencies[file_dst,file_src].append(string)
                    else :
                        if ( (file_src,file_dst) not in self.Dependencies.keys() ) :
                            self.Dependencies[file_src,file_dst] = [string]
                        else :
                            self.Dependencies[file_src,file_dst].append(string)
                        string = "wr,"+ins+"(*).*"
                        if ( (file_dst,file_src) not in self.Dependencies.keys() ) :
                            self.Dependencies[file_dst,file_src] = [string]
                        else :
                            self.Dependencies[file_dst,file_src].append(string)
                    
        
    ## voir les conditions sur les insert / update
    def analyze_INSERT1(self,file_src,file_dst):
        for table in file_src.list_table_insert :
            condi = self.check_condional_dependencies_INSERT(file_src,table)
            if ( condi ) :
                if ( (file_src,file_src) not in self.conditional_Dependencies.keys() ) :
                        string = ["ww,"+table+"(*).*"]
                        self.conditional_Dependencies[file_src,file_src] = string
                else :
                    string = "ww,"+table+"(*).*"
                    if ( string not in self.conditional_Dependencies[file_src,file_src] ):
                        self.conditional_Dependencies[file_src,file_src].append(string)
            else :
                if ( (file_src,file_src) not in self.Dependencies.keys() ) :
                        string = ["ww,"+table+"(*).*"]
                        self.Dependencies[file_src,file_src] = string
                else :
                    string = "ww,"+table+"(*).*"
                    if ( string not in self.Dependencies[file_src,file_src] ):
                        self.Dependencies[file_src,file_src].append(string)
                    
    def check_condional_dependencies_INSERT(self,  src , table ): # check if the 'update' in 'src' file is into an IF (.... ) else : ( ) ;
        string = "INSERT INTO "+table
        tmp = src.new_content.split(";")
        cpt = 0
        for line in tmp :
            if ( "IF (" in line ):
                cpt = cpt+1
            if ( string in line and cpt > 0 ):
                #print("#################",line)
                return True
            if ("END IF" in line ):
                cpt = cpt-1
        return False
                    
    def check_condional_dependencies_UPDATE(self,  src , table , attr ): # check if the 'update' in 'src' file is into an IF (.... ) else : ( ) ;
        if ( "=" in attr ) :
            attr = attr.split("=")[0].strip()
        #print("Check : " , table , attr )
        string = "UPDATE "+table+" SET "+attr
        tmp = src.new_content.split(";")
        cpt = 0
        for line in tmp :
            if ( "IF (" in line ):
                cpt = cpt+1
            if ( string in line and cpt > 0  ):
                #print("OK condi : " , table , attr )
                return True
            elif( table in line and attr in line and cpt > 0 ) :
                return True
            if ("END IF" in line ):
                cpt = cpt-1
        return False
    
    def analyze_UPDATE(self,file_src):
        for table,attr in file_src.dict_update_table_attr.items():
            if ( (file_src,file_src) not in self.Dependencies.keys() ) :
                for a in attr :
                    if ( "=" in a ):
                        a = a.split("=")[0].strip()
                    tmp = self.returnPk(table)
                    string = "ww,"+table+"("+str(tmp)+")."+a
                    condi = self.check_condional_dependencies_UPDATE(file_src , table , a )
                    if ( condi ) :
                        if ( (file_src,file_src) not in self.conditional_Dependencies.keys() ) :
                            self.conditional_Dependencies[file_src,file_src] = [string]
                        else :
                            self.conditional_Dependencies[file_src,file_src].append(string)
                    else :
                        self.Dependencies[file_src,file_src] = [string]
            else :
                for a in attr :
                    if ( "=" in a ):
                        a = a.split("=")[0].strip()
                    tmp = self.returnPk(table)
                    string = "ww,"+table+"("+str(tmp)+")."+a
                    condi = self.check_condional_dependencies_UPDATE(file_src , table , a )
                    if ( condi ) :
                        if ( (file_src,file_src) not in self.conditional_Dependencies.keys() ) :
                            self.conditional_Dependencies[file_src,file_src] = [string]
                        else :
                            self.conditional_Dependencies[file_src,file_src].append(string)
                    else :        
                        if ( string not in self.Dependencies[file_src,file_src] ):
                            self.Dependencies[file_src,file_src].append(string)
    
                        
    def write_en_tete(self,F):
        F.write("<?xml version='1.0' encoding='UTF-8'?>\n")
        F.write("<graphml xmlns='http://graphml.graphdrawing.org/xmlns'>\n")
        F.write('\t<key id="d0" for="node" attr.name="weight" attr.type="string"/>\n')
        F.write('\t<key id="d1" for="edge" attr.name="weight" attr.type="string"/>\n')
        F.write('\t<key id="d2" for="edge" attr.name="weight" attr.type="string"/>\n')
        F.write('<graph id="G" edgedefault="directed">\n')
    
    def print_list(self):
        #print("####### CONDI \n")
        for key,val in self.conditional_Dependencies.items() :
            print(key[0].file_name,key[1].file_name,val,'\n')
        print("##### REGULAR \n")
        for key,val in self.Dependencies.items() :
            print(key[0].file_name,key[1].file_name,val,'\n')
        
    def write_graphml(self):
        dict_tmp = dict()
        dict_tmp_condi = dict()
        
        #for k,v in self.conditional_Dependencies.items():
        #    dict_tmp_condi[k] = v
        #self.print_list()
        
        #if ( dict_tmp_condi != None ) :
        if (  self.Dependencies != None ) :
            for k , v in self.conditional_Dependencies.items() :
                #print("##",k[0].file_name ,k[1].file_name ,v)
                if ( ( k[1],k[0] ) in self.conditional_Dependencies.keys() ):
                    for elt in v :
                        #print("###",elt)
                        if ( ( elt.replace("rw","wr") != elt ) and elt.replace("rw","wr") not in self.conditional_Dependencies[k[1],k[0]] ) :
                            b = elt.replace("rw","wr")
                            if ( b not in self.conditional_Dependencies[k[1],k[0]] and b != elt ):
                                self.conditional_Dependencies[k[1],k[0]].append(b)
                                a = elt.replace("rw","wr")
                                if ( (k[1],k[0]) in self.Dependencies.keys() and a in self.Dependencies[k[1],k[0]] ):
                                    self.Dependencies[k[1],k[0]].remove(a)
                           
                else :
                    for elt in v :
                        c = elt.replace("rw","wr")
                        if ( c  != elt ):
                            dict_tmp_condi[k[1],k[0]] = [c]
                            
                            
            for k ,v in self.conditional_Dependencies.items() :
                for elt in v :
                    if ( k in self.Dependencies.keys() and elt in self.Dependencies[k] ) :
                        self.Dependencies[k].remove(k)
            
            
            for k , v in dict_tmp_condi.items():
                if ( k in self.conditional_Dependencies.keys() ) :
                    for elt in v :
                        self.conditional_Dependencies[k].append(elt)
                else :
                    self.conditional_Dependencies[k] = v
            
            for k , v in self.Dependencies.items() :
                for elt in v :
                    a = elt.replace("wr","rw") 
                    if ( (k[1],k[0]) in self.Dependencies.keys() and a != elt and a not in self.Dependencies[k[1],k[0]] ):
                        self.Dependencies[k].remove(elt)
                        
            
        for k,v in dict_tmp.items():
            for val in v :
                if ( k in dict_tmp_condi.keys() ) :
                    for cv  in dict_tmp_condi[k] :
                        if ( val.split(",")[1] in cv ):
                            if ( val in dict_tmp[k] ) :
                                dict_tmp[k].remove(val)
        
        for k , v in self.conditional_Dependencies.items() :
            if ( k[0] == k[1] ): 
                for elt in v :
                    if ( "ww" in elt ):
                        for key , val in self.Dependencies.items() :
                            for a in val :
                                if ( elt.replace("ww","wr") == a ):
                                    val = val.remove(a)
                                    if ( key in dict_tmp_condi.keys() ):
                                        dict_tmp_condi[key].append(a)
                                    else :
                                        dict_tmp_condi[key] = [a]
                                elif ( elt.replace("ww","rw") == a ):
                                    val = val.remove(a)
                                    if ( key in dict_tmp_condi.keys() ):
                                        dict_tmp_condi[key].append(a)
                                    else :
                                        dict_tmp_condi[key] = [a]
                                        
                        
        for k,v in self.conditional_Dependencies.items():
            self.conditional_Dependencies[k] = list(set(v))
        for k,v in self.Dependencies.items():
            self.Dependencies[k] = list(set(v)) 
        
        for k , v in dict_tmp_condi.items():
            if ( k in self.conditional_Dependencies.keys() ) :
                for elt in v :
                    self.conditional_Dependencies[k].append(elt)
            else :
                self.conditional_Dependencies[k] = v        
        
        
        dict_tmp_condi = dict(self.conditional_Dependencies)
        
        for k , v in dict_tmp.items():
            #print("$$$$$$$$$$$$$$$ :",v)
            if ( k in self.Dependencies.keys() ) :
                for elt in v :
                    self.Dependencies[k].append(elt)
            else :
                self.Dependencies[k] = v        
        
        for k , v in self.conditional_Dependencies.items() :
            l = []
            if ( k[0] == k[1] ):
                for elt in v :
                    if ( "wr" in elt ) :
                        a = elt.replace("wr","rw")
                        l.append(a)
                    elif ( "rw" in elt ) :
                        a = elt.replace("rw","wr")
                        l.append(a)
            for elt in l :
                self.conditional_Dependencies[k].append(elt)
         
                        
        dict_tmp = dict(self.Dependencies)
        
        for k , v in dict_tmp.items():
            #print("OKKKKKKKKKKKKKKKKKKK")
            if ( (k[1],k[0]) not in self.Dependencies.keys() ) :
                self.Dependencies[k[1],k[0]] = []
                for elt in v :
                    #print("elt :" , elt )
                    a = elt.replace("wr","rw")
                    if ( a != elt ):
                        self.Dependencies[k[1],k[0]].append(a)
                        
                    else :
                        a = elt.replace("wr","rw")
                        if ( a != elt ):
                            self.Dependencies[k[1],k[0]].append(a)
                            
            else :
                for elt in v :
                    a = elt.replace("wr","rw")
                    if ( a != elt and a not in self.Dependencies[k[1],k[0]] ):
                        self.Dependencies[k[1],k[0]].append(a)
                        
                    else :
                        a = elt.replace("rw","wr")
                        if ( a != elt and a not in self.Dependencies[k[1],k[0]]):
                            self.Dependencies[k[1],k[0]].append(a)
                            
        for k , v in self.Dependencies.items():
            for elt in v :
                if ( k in self.conditional_Dependencies.keys() and elt in self.conditional_Dependencies[k] ):
                    self.conditional_Dependencies[k].remove(elt)
                        
        
        self.Dependencies = dict(dict_tmp)                
        self.conditional_Dependencies = dict(dict_tmp_condi)
        
        
        
        for k,v in self.conditional_Dependencies.items():
            self.conditional_Dependencies[k] = list(set(v))
        for k,v in self.Dependencies.items():
            self.Dependencies[k] = list(set(v)) 
            
            
        self.check_dep(self.conditional_Dependencies)
        self.check_dep(self.Dependencies)
        self.add_reason(self.Dependencies)
        self.add_reason(self.conditional_Dependencies)
        
        #self.print_list()
        
        
        with open ( "graphs/Mygraphml.graphml","w+") as F :
            self.write_en_tete(F)
            cpt = 0
            cpt2 = 0
            check = True
            for key,val in self.Dependencies.items() :
                for elt in val :
                    if ( "=" in elt ) :
                        check = True
                        break
                    else :
                        check = False
                if ( check ) :
                    for elt in val :
                        if ( "(" in elt ) :
                            check = True
                            break
                        else :
                            check = False
                if ( check and val != [] ) :
                    src = key[0].file_name.split("/")[1].strip()
                    dst = key[1].file_name.split("/")[1].strip()
                    F.write ('<node id="'+src+'">\n')
                    F.write ('\t<data key="d0">"'+src+'"</data>\n')
                    F.write ('</node>\n')
                    F.write ('<edge source="'+src+'" target="'+dst+'">\n')
                    F.write ('\t<data key="d1">\n')
                    for elt in val :
                        #if ( ( "rw" in elt or "wr" in elt ) and "=" in elt ):
                        #    elt = elt.split("=")[0].strip() 
                        F.write ('\t'+elt+'\n')
                        if ( ',' in elt ) :
                            cpt2 = cpt2 +1
                    F.write ('</data>\n')
                    F.write ('</edge>\n\n')
                    cpt = cpt + 1
                    
            for key,val in self.conditional_Dependencies.items() :
                for elt in val :
                    if ( "=" in elt ) :
                        check = True
                        break
                    else :
                        check = False
                if ( check ) :
                    for elt in val :
                        if ( "(" in elt ) :
                            check = True
                            break
                        else :
                            check = False
                if ( check and val != []) :
                    src = key[0].file_name.split("/")[1].strip()
                    dst = key[1].file_name.split("/")[1].strip()
                    F.write ('<node id="'+src+'">\n')
                    F.write ('\t<data key="d0">"'+src+'"</data>\n')
                    F.write ('</node>\n')
                    F.write ('<edge source="'+src+'" target="'+dst+'">\n')
                    F.write ('\t<data key="d2">\n')
                    for elt in val :
                        #if ( ( "rw" in elt or "wr" in elt ) and "=" in elt ):
                        #    elt = elt.split("=")[0].strip() 
                        F.write ('\t'+elt+'\n')
                        if ( ',' in elt ) :
                            cpt2 = cpt2 +1
                    F.write ('</data>\n')
                    F.write ('</edge>\n\n')
                    cpt = cpt + 1
                    
            
            F.write ('</graph>\n')
            F.write ('</graphml>\n')
        return cpt , cpt2
    
    def add_reason(self, dicto ):
        for key,val in dicto.items() :
            #print("Recherche de raison pour les dependances : " , key[0].file_name.split("/")[1] , key[1].file_name.split("/")[1] )
            for v in val :
                if ( "ww" in v and key[0] == key[1] ) :
                    #print("WW in",v)
                    self.search_reason_WW(key[0],key[1],v,dicto)
                elif ( "ww" in v and key[0] != key[1] ) :
                    #print("WW in",v)
                    self.search_reason_WW(key[0],key[1],v,dicto)
                elif ( "rw" in v) :
                    #print("RW in",v)
                    self.search_reason_RW(key[0],key[1],v,dicto)
                elif ( "wr" in v) :
                    #print("WR in",v)
                    self.search_reason_RW(key[0],key[1],v,dicto)
                    
    def search_reason_WW(self,src,dst,dependence,dicto):
        table = dependence.split(",")[1].split("(")[0].strip()
        attr = dependence.split(".")[1].strip()
        motif = table+"."+attr
        list_src = dict()
        list_dst = dict()
        
        if ( "*" in dependence and src == dst ) :
            string = src.file_name.split("/")[1].replace(".sql","")+".*"
            string = string + " = " + dst.file_name.split("/")[1].replace(".sql","")+".*"
            if ( string not in dicto[dst,src] ) :
                dicto[dst,src].append(string)
            return
            
        for key,val in src.dict_update_table_attr.items():
            if ( key == table ):
                l = src.find_where_case_in_update(table,attr)
                for e in l :
                    a = re.findall(table+"\..*?=.*?[A-Za-z_]+",e)
                    if ( a ) :
                        e = e.split("=")
                        e1 = e[0].replace(";","").strip()
                        e2 = e[1].replace(";","").strip()
                        # and e2 in src.function_attr
                        if ( e1 not in list_src.keys()  ) :
                            list_src[e1] = [e2.split(" ")[0].replace(table,"")]
                        else :
                            if ( e2 in src.function_attr ) :
                                list_src[e1].append(e2.split(" ")[0].replace(table,""))
                                
        for key,val in dst.dict_update_table_attr.items():
            if ( key == table ):
                l = dst.find_where_case_in_update(table,attr)
                for e in l :
                    a = re.findall(table+"\..*?=.*?[A-Za-z_]+",e)
                    if ( a ) :
                        e = e.split("=")
                        e1 = e[0].replace(";","").strip()
                        e2 = e[1].replace(";","").strip()
                        # and e2 in dst.function_attr
                        if ( e1 not in list_dst.keys()  ) :
                            list_dst[e1] = [e2.split(" ")[0].replace(table,"")]
                        else :
                            if ( e2 in dst.function_attr ) :
                                list_dst[e1].append(e2.split(" ")[0].replace(table,""))
        for k , v in list_src.items():
            for k2 , v2 in list_dst.items() :
                if ( k == k2 ):
                    string = src.file_name.split("/")[1].replace(".sql","")+"."+str(v).replace(table,"")
                    string = string + " = " + dst.file_name.split("/")[1].replace(".sql","")+"."+str(v2).replace(table,"")
                    string = string.strip()
                    if ( "rw" in dependence or "ww" in dependence and string not in dicto[src,dst] ) :
                        dicto[src,dst].append(string)
                    if ( "wr" in dependence or "ww" in dependence and string not in dicto[dst,src]) :
                        dicto[dst,src].append(string)
                    
    
    
    def search_reason_RW(self,src,dst,dependence,dicto):
        
        #print("\n",src.file_name.split("/")[1] ,dst.file_name.split("/")[1] ,"|::|",dependence)
        if ( "wr" in dependence ) :
            tmp = src 
            src = dst 
            dst = tmp
            
        table = dependence.split(",")[1].split("(")[0].strip()
        attr = dependence.split(".")[1].strip()
        motif = table+"."+attr
        list_src = dict()
        list_dst = dict()
        
        for elt in src.select_liste :
            if ( motif in elt or ( "*" in dependence and table in elt ) ) :
                r = re.findall("WHERE.*?" + table+".*?;",elt)
                for elt in r :
                    mot = re.findall(table+"\..*?=.*?[A-Za-z_]+",elt)
                    if ( mot ) :
                        for a in mot :
                            elt = a.replace("WHERE","").split("=")
                            e1 = elt[0].replace(";","").strip()
                            e2 = elt[1].replace(";","").strip()
                            if ( e1 not in list_src.keys() ) :
                                list_src[e1] = [e2.split(" ")[0].replace(table,"")]
                            else :
                                if ( e2 in src.function_attr ) :
                                        list_src[e1].append(e2.split(" ")[0].replace(table,""))
                            
        for key,val in dst.dict_update_table_attr.items():
            if ( key == table ):
                l = dst.find_where_case_in_update(table,attr)
                for e in l :
                    a = re.findall(table+"\..*?=.*?[A-Za-z_]+",e)
                    if ( a ) :
                        e = e.split("=")
                        e1 = e[0].replace(";","").strip()
                        e2 = e[1].replace(";","").strip()
                        if ( e1 not in list_dst.keys() ) :
                            list_dst[e1] = [e2.split(" ")[0]]
                        else :
                            if ( e2 in dst.function_attr ) :
                                list_dst[e1].append(e2.split(" ")[0].replace(table,""))
                        
        for ins in dst.list_table_insert :
            if ( ins == table ) :
                for attr in dst.function_attr :
                    res = re.findall("INSERT INTO "+table+".*?VALUES.*?"+attr+".*?;" ,dst.file_content )
                    for e in res :
                        i = e.split(";")
                        for elt in i :
                            if ( attr in elt and table in elt ):
                                for b in self.dic_primary_key[table] :
                                    e1 = table+"."+b
                                    e2 = attr.strip()
                                    position = 0
                                    p = e.split("(")[1]
                                    p = p.split(",")
                                    for elt in range (0,len(p)):
                                        if ( b in p[elt] ):
                                            position = elt
                                    p = e.split("(")[2]
                                    p = p.split(",")
                                    e2 = p[position]
                                    if ( e1 not in list_dst.keys() ) :
                                        list_dst[e1] = [e2.replace(" ","").replace(table,"")]
                                    else :
                                        if ( e2 in dst.function_attr and "*" not in list_dst[e1] ) :
                                            list_dst[e1].append(e2.replace(" ","").replace(table,""))
                             
        for k , v in list_src.items():
            for k2 , v2 in list_dst.items() :
                if ( k == k2 ):
                    string = src.file_name.split("/")[1].replace(".sql","")+"."+str(v).replace(table,"")
                    #print("String1 " , string )
                    for elt in self.primary_key_obj.table_list :
                        if ( elt+"." in string ) :
                            string = string.replace( elt+"." , "" )
                    string = string + " = " + dst.file_name.split("/")[1].replace(".sql","")+"."+str(v2).replace(table,"")
                    #print("String2 " , string )
                    if ( (src,dst) in dicto.keys() and string not in dicto[src,dst] ) :
                        dicto[src,dst].append(string)
                        
                    if ( (dst,src) in dicto.keys() and string not in dicto[dst,src] ) :
                        dicto[dst,src].append(string)
        
        
                        
        
    def check_dep(self, dictio ):
        dictTmp = dict()
        for key1,val1 in dictio.items() :
            for key2,val2 in dictio.items() :
                if ( key1 != key2 ) :
                    for v in val1 :
                        if ( v in val2 and key1[0] != key2[0]):
                            if ( (key1[0],key2[0]) not in dictio.keys() ) :
                                    dictTmp[key1[0],key2[0]] = [v]
        for key1,val1 in dictio.items() :
            for key2,val2 in dictio.items() :
                for v1 in val1 :
                    for v2 in val2 :
                        if ( "ww," in v1 and v1 == v2 ) :
                            if ( (key1[0],key2[0]) not in dictio.keys() ) :
                                    dictTmp[key1[0],key2[0]] = [v1]
                            else :
                                if ( v1 not in dictio[key1[0],key2[0]] ):
                                    dictio[key1[0],key2[0]].append(v1)
                            if ( (key2[0],key1[0]) not in dictio.keys() ) :
                                    dictTmp[key2[0],key1[0]] = [v1]
                            else :
                                if ( v1 not in dictio[key2[0],key1[0]] ):
                                    dictio[key2[0],key1[0]].append(v1)
                            
        for key,val in dictTmp.items() :
            dictio[key[0],key[1]] = val
            
            
            
    def returnPk(self,nom_table):
        if ( nom_table in self.dic_primary_key.keys() ):
            return self.dic_primary_key[nom_table]
        else :
            return []

    def print_dependency(self):
        for key,val in self.Dependencies.items() :
            print ("\n\t" ,key[0].file_name , key[1].file_name ,'\n' , val )
            

if __name__ == "__main__":  
    argv = sys.argv
    if (len(argv) != 2 ) :
         print("Please select a folder which contains : \n - a genDB.sql file containing your database \ n - a set of files representing the transactions (in .sql format; PLpgSQL)")
    else :
        folder = argv[1]
        print("We are going to work on the following directory: " , folder )
        main = Parser(folder)
        main.play()
    