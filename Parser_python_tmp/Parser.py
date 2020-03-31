import os
import sys
from Read_file import *
from Primary_key import *

class Parser:
    def __init__(self, work_folder):
        self.work_folder = work_folder
        # all file will be with the .sql format
        self.files_list = os.listdir(work_folder)
        for e in self.files_list :
            if ( e == "genDB.sql" ):
                self.genDB = e 
                self.files_list.remove(e)
                break
        
    def play(self):
        print(".. working progress.. \n\nFind primary Key for each Table :")
        print("\n\tPath : ", self.work_folder+self.genDB)
        primary_key_obj = PrimaryKey(self.work_folder+self.genDB)
        print("\n-------------------------------------------------------------\n")
        self.dic_primary_key = primary_key_obj.dict_table_attr
        self.process()
        
        
    def process(self):
        for file in self.files_list :
            rf = Read_file(self.work_folder+file , self.dic_primary_key )
            
            

if __name__ == "__main__":  
    argv = sys.argv
    if (len(argv) != 2 ) :
         print("Please select a folder which contains : \n - a genDB.sql file containing your database \ n - a set of files representing the transactions (in .sql format; PLpgSQL)")
    else :
        folder = argv[1]
        print("We are going to work on the following directory: " , folder )
        main = Parser(folder)
        main.play()
    