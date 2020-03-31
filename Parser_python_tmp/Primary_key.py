import re 

class PrimaryKey :
    def __init__(self , file_name ):
        self.file = file_name
        self.table_list = []
        self.dict_table_attr = dict()
        self.search_pk()
        self.print_table_primaryKey()
        
        
    def search_pk(self):
        with open(self.file) as pk_file :
            for line in pk_file :
                # finding Table name
                res = re.findall("CREATE TABLE .*\(",line )
                a = ""
                if ( res != [] and res != None ):
                    a = res[0].split("TABLE")[1].split('(')[0].strip()
                    self.table_list.append(a)
                    self.dict_table_attr[a] = []
                
                # finding primary key in this table
                res = re.findall("PRIMARY KEY.*?",line )
                if ( res != [] and res != None ):
                    elt = line.split(" ")
                    check = []
                    for word in elt :
                        check = re.findall("[a-z]+[A-Za-z]*",word.strip() )
                        if ( check != '' and check != [] and check != None  ):
                            for elt in check :
                                self.dict_table_attr[self.table_list[len(self.table_list)-1]].append(elt) 
                                
    def print_table_primaryKey (self ) :
        for a,b in self.dict_table_attr.items() :
            print ("\n--- Table :" , a , "\nPrimary_Key :" ,b )        