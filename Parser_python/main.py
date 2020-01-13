class principal:
    def __init__(self):
        self.pk = PrimaryKey("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/genDB.sql")
        self.p = Parser("/home/cadiou/Documents/Projet_long/cadiou-traore-plong-1920/Parser_python/fichiers/stocklevel.sql")
    
    def analyse_BD(self):
        print("Voici la liste des Tables avec leurs clés primaires ( si une table n'a pas de clé primaire , elle n'apparait pas ) : ")
        self.pk.contenue = pk.lecture_file()
        #print(fichier)
        liste_create = self.pk.trouve_table()
        #print(liste_create)
        self.pk.liste_table(liste_create)
        #print(pk.liste_table)
        self.pk.trouve_primary()
        #print(len(pk.couple))
        self.pk.affiche_couple()
    

if __name__ == "__main__":
    main = principal()
    main.analyse_BD()