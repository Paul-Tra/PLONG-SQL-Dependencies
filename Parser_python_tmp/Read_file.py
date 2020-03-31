import re 

class Read_file:
    def __init__(self, file_name , primary_dict ):
        self.file_name = file_name
        self.function_attr = []
        self.file_content = ""
        self.primary_dict = primary_dict
        print("----------------------------------\n----------------------------------\nWorking on : " , self.file_name ,"\n")
        self.find_function_attr()
        
        self.list_table_insert = []
        self.find_table_insert()
        
        self.dict_update_table_attr = dict()
        self.find_update()
        
        self.dict_select_table_attr = dict() #ex dict_select_table_attr['ITEMS'] = ["nbis" , "iId" ]  , for "Select ITEMS.nbis , ITEMS.iId" .
        self.table_name = dict() # ex table_name[ITEMS] = "I"
        self.dict_where_table_attr = dict() #ex dict_select_table_attr['ITEMS'] = ["nbis : x " , "iId : y" ]  , for "Where ITEMS.nbis = x AND  ITEMS.iId = y " .
        
        self.select_liste = [] # store the select request with th new format , like :
        # Select find : SELECT I.nbids FROM ITEMS I WHERE I.iId = i_id;
        # New Select : SELECT ITEMS.ITEMS.nbids FROM ITEMS I WHERE ITEMS.iId = i_id;

        self.find_select()
        self.print_new_select()

    def find_function_attr(self):
        # store the content into a string 
        with open (self.file_name) as file :
            for line in file :
                self.file_content = self.file_content + line.strip()+" "
        res = re.findall("CREATE.*?RETURNS",self.file_content)
        attr = re.findall("[a-z]+_*[A-Za-z]*", res[0] )
        attr.remove(attr[0])
        self.function_attr = attr
        print("\tFunction parameter : " , self.function_attr ) 
    
    def find_table_insert(self):
        res = re.findall("INSERT INTO.*?;",self.file_content)
        if ( res ):
            for i in res :
                table = i.split("INTO")[1].split("(")[0].strip()
                print("\tInsert table :" ,  table )
                self.list_table_insert.append(table)
                
    def find_update(self):
        res = re.findall("UPDATE (?P<table>[A-Za-z]+) SET (?P<attr>[A-Za-z]+_*[A-Za-z]+)",self.file_content)
        if ( res ):
            for i in res :
                if ( i[0] in self.dict_update_table_attr.keys() ) :
                    self.dict_update_table_attr[i[0]].append(i[1])
                else :
                    self.dict_update_table_attr[i[0]] = [i[1]]
                print("\n\tUpdate on " , i[0] ," , about : " , self.dict_update_table_attr[i[0]] )
                
    def find_select(self):
        res = re.findall("SELECT .*?;",self.file_content)
        if ( res ):
            for i in res :
                print("\n\tSelect find :", i)
                self.processSelect(i)
                print("")
                
                
                
                
                
                
    def processSelect(self,select):
        self.table_name = dict()
        res = re.findall(".*?FROM.*?;",select)
        if ( res ) :
            for elt in res :
                l_table = elt.split("FROM")[1].split("WHERE")[0].split("ORDER BY")[0].split(";")[0].strip()
                decomposition = l_table.split(",")
                for elt in decomposition :
                    if ( len ( elt.split(" ")) >=2 ): 
                        table,n = elt.strip().replace(',','').split(' ')
                        if ( len(table) >= 1 and len(n) >= 1 ):
                            self.table_name[table] = n
                    else :
                        # if there is one table in the from case , this one concern all the select case 
                        self.table_name[elt] = elt
                       
        for a,b in self.table_name.items() :
                select = select.replace(b+".", a+".")
            # we only have 1 table from so , add XXX.attr in the select case
            #if ( len(self.table_name) == 1 ):
                r = select.split("SELECT")[1].split("INTO")[0].split("FROM")[0].replace("COUNT(DISTINCT(","").replace(")","").replace("COUNT(","").replace("SUM(","").strip()
                #select = select.replace("SELECT "+r, "SELECT "+a+"."+r)
                select_content = select.split("SELECT")[1].split("INTO ")[0].split("FROM")[0].replace("COUNT(DISTINCT(","").replace(")","").replace("COUNT(","").replace("SUM(","").strip()
                
                res = re.findall("[A-Za-z]+[_]*[A-Za-z]+",select_content )
                if ( res ):
                    for elt in res :
                        if ( a not in elt ) :
                            new = elt.replace(elt,a+"."+elt )
                            #print(new)
                            select = select.replace(" "+elt," "+new) # normal case
                            select = select.replace("("+elt,"("+new) # count / distinct case
                            select = select.replace(","+elt,","+new) # if there is no space before the attr
                
        #print("\tNew Select :",select)
        self.select_liste.append(select)
        
        
    def print_new_select(self):
        print("---------- List of new select request -------- \n")
        for elt in self.select_liste :
            print("\t",elt,"\n")
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                