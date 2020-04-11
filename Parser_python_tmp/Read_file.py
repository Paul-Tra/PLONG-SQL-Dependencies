import re 

class Read_file:
    def __init__(self, file_name , primary_dict ):
        self.file_name = file_name
        self.function_attr = []
        self.file_content = ""
        self.new_content = ""
        self.primary_dict = primary_dict
        #print("----------------------------------\n----------------------------------\nWorking on : " , self.file_name ,"\n")
        self.find_function_attr()
        
        self.content_line_by_line = []
        self.new_content_line_by_line = [] 
        
        self.read_by_line()
        
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
        self.prev_select_list = []

        self.find_select()
        #self.print_new_select()
        
    def return_line_numbor_of(self,select ) :
        select = select.strip()
        return self.read_line_numbers(select)

    def read_by_line(self) :
        with open(self.file_name) as file :
            for line in file :
                self.content_line_by_line.append(line.strip())
                self.new_content_line_by_line.append(line.strip())
        
    def read_line_numbers(self,select):
        check = False
        tmp = ""
        line = self.new_content_line_by_line
        cpt = 0 
        match = 0
        a = 0
        if ( "SELECT" in select ) :
            for i in range(0,len(self.select_liste)):
                if ( select in self.select_liste[i] ) :
                    a = i
            select = str(self.prev_select_list[a].replace(";","").strip())
            #print(select)
        for i in range(0,len(line)):
            
            l = line[i].replace(";","").strip()
            if ( l in select ) :
                
                if ( "UPDATE" in l ) :
                    print("Looking for : " , select )
                    #print(select , "\nL:",line[i],'\n\n')
                    match = i
                    for j in range(i,(len(line)) ) :
                        tmp = tmp+" "+ line[j]
                        print("t:",line[j])
                        if ( ";" in line[j] ) :
                            tmp = "UPDATE "+tmp.split("UPDATE")[1].strip().replace(";","")
                            print("TT:",tmp)
                            print("SS:" , select )
                            if ( tmp.strip() == select.strip() ) :
                                return match+1
                            else :
                                break
                            
                if ( "SELECT" in l or "INSERT" in l  ) :
                    match = i
                tmp = tmp+" " + l
                check = True
                cpt = i
                if ( tmp.strip() == select.strip() ) :
                    #print("Tmp : " , tmp , '\n select : ' , select )
                    return match+1
            else :
                if ( cpt > 0 and tmp == select ) :
                    cpt = i
                    return cpt
                    
            
        
    def find_function_attr(self):
        # store the content into a string 
        with open (self.file_name) as file :
            for line in file :
                self.file_content = self.file_content + line.strip()+" "
                self.new_content = self.new_content + line.strip()+" "
                
        res = re.findall("CREATE.*?RETURNS",self.file_content)
        attr = re.findall("[a-z]+_*[A-Za-z]*", res[0] )
        attr.remove(attr[0])
        self.function_attr = attr
    
    def find_table_insert(self):
        res = re.findall("INSERT INTO.*?;",self.file_content)
        if ( res ):
            for i in res :
                table = i.split("INTO")[1].split("(")[0].strip()
                #print("\tInsert table :" ,  table )
                self.list_table_insert.append(table)
                
    def find_update(self):
        res = re.findall("UPDATE (?P<table>[A-Za-z]+) SET (?P<attr>([A-Za-z]+_*[A-Za-z]+).*?)WHERE.*?;",self.file_content)
    
        #print(res)
        if ( res ) :
            for i in res :
                for elt in i :
                    if ( "," in elt ) :
                        a = elt.split(",")
                        for item in a :
                            item = item.strip()
                            a,b = item.split("=")
                            a = a.strip()
                            #print('A : ', a)
                            if ( i[0] in self.dict_update_table_attr.keys() ) :
                                self.dict_update_table_attr[i[0]].append(a)
                            else :
                                self.dict_update_table_attr[i[0]] = [a]
                    
        if ( res ):
            for i in res :
                tmp = ""
                if ( "=" in i[1] ):
                    #print("OKKK",i[1])
                    tmp = i[1].split("=")[0].strip()
                    #print(tmp)
                else :
                    tmp = i[1].strip()
                if ( i[0] in self.dict_update_table_attr.keys() ) :
                    self.dict_update_table_attr[i[0]].append(tmp)
                else :
                    self.dict_update_table_attr[i[0]] = [tmp]
                    
            for elt in self.dict_update_table_attr[i[0]]:
                if ( "=" in elt ) :
                    elt = elt.replace(elt,elt.split("=")[0].strip())
        
        #for k , v in self.dict_update_table_attr.items() :
        #    print(k,v)
    
    def find_where_case_in_update(self,table , attr ):
        if ( attr == "*" ):
            return []
        if ( "=" in attr ) :
            attr = attr.split("=")[0].strip()
        #print(table,attr)
        res = re.findall("UPDATE "+table+" SET.*?"+attr+".*? WHERE.*?;",self.file_content)
        #print(res)
        l = []
        if ( res ):
            for i in res :
                a = i.split("WHERE")[1].strip()
                a  = a.split("AND")
                for e in a :
                    e =e.replace(";","").strip() 
                    l.append(e)
        return l
                
    def find_select(self):
        res = re.findall("SELECT .*?;",self.file_content)
        if ( res ):
            for i in res :
                #print("\n\tSelect find :", i)
                self.processSelect(i)
                
    def processSelect(self,select):
        prev = str(select)
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
                select = select.replace("*",a+".*")
                # we only have 1 table from so , add XXX.attr in the select case
                r = select.split("SELECT")[1].split("INTO")[0].split("FROM")[0].replace("COUNT(DISTINCT(","").replace(")","").replace("COUNT(","").replace("SUM(","").strip()
                select_content = select.split("SELECT")[1].split("INTO ")[0].split("FROM")[0].replace("COUNT(DISTINCT(","").replace(")","").replace("COUNT(","").replace("SUM(","").strip()
                res = re.findall("[A-Za-z]+[_]*[A-Za-z]+",select_content )
                if ( res ):
                    for elt in res :
                        if ( a not in elt and elt not in self.table_name.keys() ) :
                            new = elt.replace(elt,a+"."+elt,1 )
                            select = select.replace(" "+elt," "+new) # normal case
                            select = select.replace("("+elt,"("+new) # count / distinct case
                            select = select.replace(","+elt,","+new) # if there is no space before the attr
                            
                self.select_liste.append(select)
                self.prev_select_list.append(prev)
                self.new_content = self.new_content.replace(prev,select)
                
        
        
    def print_new_select(self):
        print("---------- List of new select request -------- \n")
        for elt in self.select_liste :
            print("\t",elt,"\n")
 