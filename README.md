# Labirinto_game

riporto le note e idee sul gioco

Obbiettivi del gioco:
- trovare il tesori/imprevisti  (tipo una caccia al tesoro, oppure trovare oggetti utili??)
- trovare la via per il livello due ipotetico  (potrebbe essere un macro labirinto di labirinti)
- raggiungere un punteggio
  - creare molliche (poco interessante!!)
  - aumentare il numero di connessioni, quindi la % celle raggiungibili (punti) , ruotando le caselle/creando ponti 

La chiave del gioco e' ruotare opportunamente le celle!!  Costruire ponti!

> SALVARE LO STATO , per riprenderlo successivamente (o fare DEBUG) <<<<<<<< Salvare oggetti!!
> Prevedere due utenti (da selezionare)
> prevedere un thead per un fantasmino che "innaffia" o "mangia" le molliche (bad ghost go faster) 
> possibilita' di cosrtuire muri (lato[x]=0; //muro invalicabile)  , 1:muro normale , 2:libero
> creare un passaggio-ponte costa "10 fiorini"


> Diventa un solitario se va avanti finche la fortuna lo permette, concentrarsi sulle abilita'
  - lasciare una traccia (molliche) e contare le caselle percorse e raggiungibili 
  - poter ruorare solo due volte la stessa cella (dopo muro?)
  - poter ruotare gruppi di 4 celle (?)
  - permettere lo shift ori/vert (?)
  - creare ponti di 2 celle??
  - creare dei tombini collegati fra loro casualmente (entri in uno esci non si sa dove) (?)
  - creare delle porte e delle chiavi (?)

Attenzione a circoli viziosi!!!  >> Eliminare i circoli viziosi aggiungento un muro interno?? <<
variante:
- text adventure  
  - possibilita di creare delle domande e modificare la mappa aggiungento elementi (porte, tesori, muri invalicabili)
  - modificare la forma ed i confini del labirinto ...
- allargare i confini  con un effetto riempimento progressivo ed aggiungere l'effetto rotazione  (NON VA!)
eventualmente:
- introdurre elementi di disturbo (fantasmi,  chiusura dei percorsi possibili,  restringimento dei confini del maze)
- allargare i confini (o restringerli) al passare delle mosse effettuate
- fare calcolo dei percorsi possibili (all'inizio, ad ogni rotazione di un elemento o apertura di una porta)

IMPRESSIONI: 
- bello l'effetto estetico della casella (0,0) gialla, come riferimento visivo

  vedi: http://lagrandebiblioteca.com/tempo-libero-e-sport/algoritmo-maze-generazione.php
        https://it.wikipedia.org/wiki/Spazio_semplicemente_connesso
       (Bin-Tree): http://www.java2s.com/Code/Java/Collections-Data-Structure/BinaryTree.htm
   
