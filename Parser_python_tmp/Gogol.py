import re 

class Gogol:
    def __init__(self, parser , graphml , dossier):
        self.parser = parser 
        self.file_graphml = graphml
        self.dossier = dossier
        self.write_entete()
        self.find_relation()
        
    def write_entete(self):
        with open ( "graphs/MyDependencies.gogol","w+") as F :
            F.write("#-- Created by TRAORE Paul & CADIOU Léo-Paul --#\n\n\n")
        
    def find_relation(self):
        with open (self.file_graphml ) as F :
            src = "" 
            dst = ""
            relation = ""
            condi = False # if the relation is conditional or not
            
            for line in F :
                if ( "edge" in line ) :
                    # on va traiter les nom des sources / targets des relations
                    a = re.findall("[A-Za-z]+\.sql",line)
                    if ( a ) :
                        src,dst = a 
                if ("data" in line and "d1" in line ) :
                    # on traite les relation non condi
                    condi = False
                if ("data" in line and "d2" in line ) :
                    # on triate les relation condi
                    condi = True
                if ( "rw" in line or "wr" in line or "ww" in line ) :
                    relation = str(line)
                    #print(relation) 
                    self.find_reasons(src,dst,relation,condi)
                    
    def return_content_by_file_name(self , name ) :
        for elt in self.parser.list_readFile :
            if ( elt.file_name.split("/")[1] == name ) :
                return elt.new_content     
                    
                    
    def find_reasons(self , src , dst , relation , condi ):     
        if ( "rw" in relation ):
            self.find_reason_RW(src , dst , relation , condi)
        if ( "wr" in relation ):
            self.find_reason_RW(dst , src , relation , condi)
        if ( "ww" in relation ):
            self.find_reason_WW(src , dst , relation , condi)
            
            
    def find_reason_RW( self , src , dst , relation , condi ): 
        lsrc = []
        ldst = []
        content_src = self.return_content_by_file_name(src)
        content_dst = self.return_content_by_file_name(dst)
        t = re.findall("[A-Z]+[A-Z]+",relation )
        table = ""
        attr = ""
        if ( t ) :
            for elt in t :
                table = elt
        t = re.findall("\.\**[A-Za-z]*",relation )
        if ( t ) :
            for elt in t :
                attr = elt
                
        c = content_src.split(";")
        for elt in c :
            motif = table+attr
            if ( "SELECT" in elt and motif in elt ) :
                lsrc.append(elt)
            if ( attr == ".*" ) :
                if ( "SELECT" in elt and table+"." in elt ) :
                    lsrc.append(elt)
        
        c = content_dst.split(";")
        for elt in c :
            # update
            if ( "UPDATE" in elt ) :
                tmp = elt.split("WHERE")[0]
                at = attr.replace(".","")
                if ( table in tmp and at in tmp) :
                    ldst.append(elt)
                    
            if ( "INSERT INTO" in elt ) :
                if ( table in elt ) :
                    ldst.append(elt)
        
        lsrc = list(set(lsrc))
        ldst = list(set(ldst))
        
        self.write_relation(lsrc,ldst ,relation,condi, src , dst )
        
    def write_relation( self, lsrc,ldst ,relation,condi, src , dst) :
        with open ( "graphs/MyDependencies.gogol","a+") as F :
            F.write('<Relation ID="'+relation.strip()+'" SRC="' + src + '" DST="'+ dst + '" CONDITION='+str(condi)+' >\n')
            F.write('<SRC>\n')
            for elt in lsrc :
                F.write('\t' +elt+'\n')
            F.write('</SRC>\n')
            F.write('<DST>\n')
            for elt in ldst :
                F.write('\t' + elt+'\n')
            F.write('</DST>\n')
            F.write('</Relation>\n\n')
       
    def find_reason_WW( self , src , dst , relation , condi ):   
        lsrc = []
        ldst = []
        content_src = self.return_content_by_file_name(src)
        content_dst = self.return_content_by_file_name(dst)
        
        t = re.findall("[A-Z]+[A-Z]+",relation )
        table = ""
        attr = ""
        if ( t ) :
            for elt in t :
                table = elt
        t = re.findall("\.\**[A-Za-z]*",relation )
        if ( t ) :
            for elt in t :
                attr = elt
    
        c = content_src.split(";")
        for elt in c :
            # update
            if ( "UPDATE" in elt ) :
                tmp = elt.split("WHERE")[0]
                at = attr.replace(".","")
                if ( table in tmp and at in tmp) :
                    lsrc.append(elt)
                    
            if ( "INSERT INTO" in elt ) :
                if ( table in elt ) :
                    lsrc.append(elt)
        
        c = content_dst.split(";")
        for elt in c :
            # update
            if ( "UPDATE" in elt ) :
                tmp = elt.split("WHERE")[0]
                at = attr.replace(".","")
                if ( table in tmp and at in tmp) :
                    ldst.append(elt)
                    
            if ( "INSERT INTO" in elt ) :
                if ( table in elt ) :
                    ldst.append(elt)
        
        lsrc = list(set(lsrc))
        ldst = list(set(ldst))
        self.write_relation(lsrc,ldst ,relation,condi, src , dst )
       