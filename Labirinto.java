// improvements and ideas
// <editor-fold defaultstate="collapsed" desc="improvements and ideas">                          
/* improvements and ideas:
migliorare il codice x renderlo piu' leggibile. 

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
   
*/// </editor-fold>

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author adr0
 */
public class Labirinto extends javax.swing.JFrame {   // implements ActionListener {
    //Map<Point,Cella> mondo = new HashMap<>();
    // non posso verificare se delle coordinate gia sono presenti con mondo.containskey(<point>)
    // poiche <point> e' un riferimento all'oggetto MA non il punto di coord x,y.
    //Per far questo posso  usare delle stringa: "x y" come keys
   
    static Map<String,Cella> mondo = new HashMap<>();  //static or NOT?  differences
    static int u = 4; //da 2 4 6 pixel (small medium large)
    static Punto centro_r = new Punto(158-u*2,100);
    static int conteggio_mosse = 0; //conteggio mosse totali
    static int conteggio_molliche = 0; //conteggio molliche consecutive
    static Boolean stepBack=false;
    static Boolean freeMove = false;
    static int trovato=0; //numero percorsi con uscita trovati
    static int lato_estremo=0; //lato estremo sul percorso fatto  //NOT_USED
    //static String target="0 0";
    static Boolean toggle_nascondi=true;
    static Boolean toggle_calcola=true;
    static int oriz_size = 11; // da -5 a +5  //Cilindro
    static int vert_size = 20;
        static int vert_size_limit = 40;
    private String previous_dir;
    private Punto previous_pos=new Punto(0,0);
    private Punto current_pos;
    private long currentTime;
    
    private final int DELAY = 2000;         //NOT_USED
    private int DELAY_step=1;  //da 1 a 10  //NOT_USED
    private int rubaMollica_step=10;        //NOT_USED
    private int NullaAvanza_step=4;         //NOT_USED
    private int hiderow_num=-vert_size/2;  //prima riga da disattivare  //NOT_USED
    private int hideinc=1;                  //NOT_USED
    private Timer timer;                    //NOT_USED
    static private Punto ghost_pos= new Punto(-oriz_size/2,-vert_size/2);
    static private int ghost_6step=0;          
    static int dx=0, dy=0;
    private Boolean gameover=false;         //NOT_USED
    //static javax.swing.JPanel G_Panel;
    

    /**
     * Creates new form Labirinto
     */
    public Labirinto() {
        initComponents();
        addKlistener();
        
        /* Al momento il Timer non è usato!
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = getTimer();
                timer.stop();
            }
        });        
        initTimer(); //ogni xx millisecondi lancia un thread 
        */
        
        current_pos = new Punto(0,0);     // coordinate 0,0 (centro del labirinto) - ancora non esiste la cella
        mondo.put(current_pos.toString(), new Cella(current_pos).SetParetiCella(2,2,2,2)); //creo la cella(0,0) con le vie aperte!
        mondo.get("0 0").SetCurrent(true);
        mondo.get("0 0").mollica = "END-PATH";
        
        allargaConfine(current_pos,"init");  //allargo il confine del mondo (creando le celle adiacenti)
        //quadro.setBounds(-u*7, 0, quadro.getHeight(), quadro.getWidth()+(u*8));  //allarga il riquardo per contenere il bordo copia
        
        //current_pos = new Punto(1,0);
        //mondo.get("0 0").SetCurrent(false); //Cancella la vecchia posizione!!
        //mondo.get("1 0").SetCurrent(true);  //Nuova posizione!!
        //this.requestFocus();  //imparare "significato" e come gestire il focus!!
        
        //configura..
        semplifica(1,new Punto(9,9)); //individua le celle isolate -> nascondi=true (opz1: all)
        semplifica(11,new Punto(9,9)); //definisce le celle con Imprevisti (all)
        Labirinto.trovato=0; //resetPercorso("0 0");
        calcoloPercorso(0,current_pos.x, current_pos.y , 9);  //calcola le celle raggiungibili (tratteggiate).
        /*
        //valori iniziali per dx,dy (incremento ghost_pos)   
        while ((dx!=0 && dy!=0) || dx+dy==0){  //direzione possibile: oriz. oppure vert.
            //ghost_dir = (int) ((Math.random()*4)-0.1);  //valori da 0 a 3 -> (Nord Sud Ovest Est)
            dx=(int) ((Math.random()*3)-0.1)-1;  //intervallo random: -1 +1
            dy=(int) ((Math.random()*3)-0.1)-1;
        }
        */
        
    }
    
    //al momento non ci sono task x il timer
    // <editor-fold defaultstate="collapsed" desc="Not used code">        
    /**
    private void initTimer() {  
        timer = new Timer(DELAY, this);
        timer.start();
    }
    public Timer getTimer() {   
        return timer;
    }
   
    
    //invocata da Timer della classe Labirinto
    @Override
    public void actionPerformed(ActionEvent e) {  //DELAY => 1000 oppure 10000 //a seconda del task
    //- disattiva le celle partendo dal TOP verso il basso (che diventano di colore lightGray)
    //  ..non vengono disattivate le celle gia' scoperte (o raggiungibili)
        if (DELAY_step <= 10) DELAY_step ++;
        else DELAY_step=1;
        //if (conteggio_mosse > 1 ) rubaMollica(); //sostiure con la scadenza delle molliche (20 mosse)
        //if (conteggio_mosse > 20 ) NullaAvanza();   //sistituire con GhostAvanza come competitor!!
        //
        //Alternativa usata: ghost muove quando chi gioca muove()!
        if (conteggio_mosse > 1 ) GhostAvanza();  
        
        repaint();
    }
    
    //nasconde la cella contenente la prima mollica
    private void rubaMollica(){
        String prev_xy = null;
        String xy=current_pos.x+" "+current_pos.y;
        
        if (mondo.get(xy).mollica.equals("END-PATH")) rubaMollica_step=10;  //wait 10 secondi prima di riprendere a rubare!
        else {
            if (rubaMollica_step != 0 && DELAY_step%rubaMollica_step == 0) {  //verifica se il suo turno
                //percorre a ritroso le molliche 
                while (xy != null && !mondo.get(xy).mollica.equals("END-PATH") &&  !xy.equals(prev_xy) ){    //BUG..: !xy.equals(prev_xy)
                    prev_xy = xy;
                    xy=mondo.get(xy).mollica;
                    System.out.println("--rubaM: "+xy);
                }
                System.out.println("rubaM: "+prev_xy+" -> "+xy);                
                if (prev_xy == null || xy == null ) System.out.println(">>ANOMALIA!!");  //non dovrebbe verificarsi (prev_xy=null)!

                if (prev_xy != null && xy != null ){  //non dovrebbe verificarsi (prev_xy=null)!
                    mondo.get(xy).mollica = null;  //BUG? Liberare sempre la cella per essere usata??
                    mondo.get(prev_xy).mollica = "END-PATH";
                    //mondo.get(xy).nascondi = true;   //Non mi piace (il "nulla" che avanza sul path!!)
                }
                rubaMollica_step=2; //works for DELAY_step=( 2 4 6 8 10 ); ..per successiva rubata!
            }
        }
    }
    
    //disattiva le celle partendo da sopra (solo quelle non raggiungibili, ne scoperte)
    private void   NullaAvanza(){
        if (NullaAvanza_step != 0 && DELAY_step%NullaAvanza_step == 0) {  //verifica se il suo turno
            for (String key : mondo.keySet()){
                if (key.substring(key.indexOf(" ")).equals((" "+hiderow_num))){
                    if (mondo.get(key).mollicaB == null){
                        mondo.get(key).attiva=false;                        
                    }
                }
            }
            System.out.println("hiderow num: "+hiderow_num);
            
            hiderow_num += hideinc;   //da -vert_size a +vert_size  e ritorno!! 
            if (hiderow_num == +vert_size/2) {
                hideinc= -1; 
                NullaAvanza_step= 1;
            }
            if (hiderow_num == -vert_size/2) gameover=true;  //-FINE-
        }
    }
    */// </editor-fold>
    
    //disattiva le celle partendo da sopra (se non gia' raggiungibili da chi gioca)
    private void   GhostAvanza(){  //usa flag: NullaAvanza_step
        if (NullaAvanza_step != 0 && DELAY_step%NullaAvanza_step == 0) {  //verifica se il suo turno
            Labirinto.trovato=0; //resetPercorso("0 0");
            calcoloPercorso(0,ghost_pos.x, ghost_pos.y , 9);  //calcola le celle raggiungibili (tratteggiate in red)
            //TODO.. cerca-cella-da-girare()!!
            System.out.println("GhostAvanza: "+ghost_pos);

            //if (...) gameover=true;  //-FINE-
        }
    }
    
    static private Punto muovi_ghost(int gx,int gy){
        //muove ghost  di 6-step (x simulare il movimento) per ogni cella
        //posizione t0 x ghost_pos ! (spostare nel costruttore)
            while (gx == 999 || !mondo.get(gx+" "+gy).nascondi && mondo.get(gx+" "+gy).tesori!=0 ) { //gx=999 x entrare nel while{}
                gx = -oriz_size/2+1;            
                gy = (int) ((Math.random()*10)-5); //range y: -5 +5
            }
        
        if (ghost_6step < 5) {
            ghost_6step++;
        } else {
            //individua la prossima direzione
            ghost_6step=0; 
            //mondo.get(gx+" "+gy).ghost = false;  //serve se soluzione 2!
            dx=dy=0; // per entrare nel while{}
            while (!mondo.containsKey((gx+dx)+" "+(gy+dy)) || (dx!=0 && dy!=0) || dx+dy==0){
                //dir = (int) ((Math.random()*4)-0.1);  //valori da 0 a 3 -N.S.O.Est
                dx=(int) ((Math.random()*3)-0.1)-1;
                dy=(int) ((Math.random()*3)-0.1)-1;
                //System.out.println("--ghost_pos dx dy: "+gx+"+"+dx+" "+gy+"+"+dy);
            }
            gx += dx;  
            gy += dy;
            //mondo.get(gx+" "+gy).ghost = true;   //serve se soluzione 2! 
        }
        
        System.out.println(">>ghost_pos dx dy: "+dx+" "+dy);

        return new Punto(gx,gy);
    }
    
    
    static public void DoPaint(Graphics g){    
        Graphics2D g2d = (Graphics2D) g; 
        copiaBordo(g2d,"LEFT");
        copiaBordo(g2d,"RIGHT");
        //GhostPaint(g2d,"?");  //spostato in:  Timer->actionPerformed->surface.repaint()  -> ??
        //g2d.setPaint(Color.black); //linea sul bordo sinistro
        //g2d.drawLine(0,-vert_size_limit*u*3,0,+vert_size_limit*u*3);  //se x=-1 (invece di 0), la riga non si vede!
    }
    
    static private void copiaBordo(Graphics2D g2d,String side){  //side: LEFT o RiGHT
        //Graphics2D g2d = (Graphics2D) g;        
        Cella rif_cella;
        int inc=1,sidex = oriz_size/2;
        int xoff,  yoff;
        if (side == "LEFT") inc=-1;
        else inc=1;
        xoff = centro_r.x +inc*u*6 +inc*sidex*u*6;
        yoff = centro_r.y;
        g2d.setPaint(Color.lightGray);

       //copiaBordo       
        for (String key : mondo.keySet()){
            if (key.indexOf(inc*-sidex+" ") == 0) {   // -1: stringa non trovata
                rif_cella = mondo.get(key);
                yoff = centro_r.y + Integer.parseInt(key.substring(key.indexOf(" ")+1))*(u*6);
                //cella nascosta
                if (rif_cella.nascondi) {
                    g2d.fillRect(xoff+0, yoff+0, u*6, u*6);
                } else {
                    if (rif_cella.mollicaB != null) { 
                        //g2d.setPaint(Color.blue);
                        g2d.drawLine(xoff+0, yoff+u*2, xoff+u*2, yoff+0); g2d.drawLine(xoff+0, yoff+u*4, xoff+u*4, yoff+0); g2d.drawLine(xoff+0, yoff+u*6, xoff+u*6, yoff+0);
                        g2d.drawLine(xoff+u*2, yoff+u*6, xoff+u*6, yoff+u*2); g2d.drawLine(xoff+u*4, yoff+u*6, xoff+u*6, yoff+u*4);
                        //g2d.setPaint(Color.lightGray);
                    };         
                    //ombra delle mura
                    g2d.fillRect(xoff+0, yoff+0, u, u);  g2d.fillRect(xoff+0, yoff+u*5, u, u);  g2d.fillRect(xoff+u*5, yoff+u*5, u, u);  g2d.fillRect(xoff+u*5, yoff+0, u, u);
                    if (rif_cella.lati[0]!=2) g2d.fillRect(xoff+0, yoff+0, u*6, u);     // muro a Nord   
                    if (rif_cella.lati[3]!=2) g2d.fillRect(xoff+u*5, yoff+0, u, u*6);   // muro a Est
                    if (rif_cella.lati[2]!=2) g2d.fillRect(xoff+0, yoff+0, u, u*6);     // muro a Ovest 
                    if (rif_cella.lati[1]!=2) g2d.fillRect(xoff+0, yoff+u*5, u*6, u);   // muro a Sud                       
                }
                //ombra mollica
                if (rif_cella.mollica!=null) {
                    g2d.setPaint(Color.lightGray);
                    g2d.fillOval(xoff+u*2, yoff+u*2, u*2, u*2);
                    //g2d.setPaint(Color.lightGray);
                }
                //ombra del cursore
                if (rif_cella.CurrentVal()) {
                    g2d.setPaint(Color.orange);
                    g2d.fillRect(xoff+u*2, yoff+u*2, u*2, u*2);
                    g2d.setPaint(Color.lightGray);
                }
                //ombra di imprevisto
                if (rif_cella.tesori!=0) { //cella imprevisto
                    g2d.setPaint(Color.gray);
                    g2d.fillRect((int)(xoff+u*2.5), yoff+u+1, u, u);
                    g2d.fillRect((int)(xoff+u*2.5), (int)(yoff+u*2.5), u, u*2); 
                    g2d.drawLine(xoff+u*2,(int)(yoff+u*4.5), xoff+u*4, (int)(yoff+u*4.5));
                    g2d.setPaint(Color.lightGray);
                }
            }
        }
        //return g2d;  //soluzione alternativa!
    }

/** Non usata!
    static private void GhostPaint(Graphics2D g2d,String var1){  
        // disegna il fantasmino!!
            int x=u*2,y=u*2;
            int x_off=centro_r.x +ghost_pos.x*u*6;// +u;
            int y_off=centro_r.y +ghost_pos.y*u*6;
            if (dx ==0) x = u*2;
            if (dx < 0) x = (u*2)+(6-ghost_6step)*u;
            if (dx > 0) x = (u*2)-(6-ghost_6step)*u;
            if (dy ==0) y = u*2;
            if (dy < 0) y = (u*2)+(6-ghost_6step)*u;
            if (dy > 0) y = (u*2)-(6-ghost_6step)*u;
            //
            g2d.setPaint(Color.black);
            g2d.fillOval(x_off+x, y_off+y, u*2, u*2);
            
            //System.out.println("passa di qui"+x_off+" "+y_off);
    }
*/

    
    public void calcoloPercorso(int i,int x, int y , int entrata){
        // soluzione ricorsiva
        if  (entrata == 9) Labirinto.trovato = 0;  // se la prima chiamata azzera il flag 
        if  (Labirinto.trovato > 7) return; //x trovare 1 o piu' uscite!! 
        if (!mondo.containsKey(x+" "+y) ) {  Labirinto.trovato++;  //percorso trovato
            System.out.println("Uscita: "+x+" "+y);
            
            return;  
        }
        mondo.get(x+" "+y).mollicaB="0 0"; //default (anche fine-ramo invece che null)
        if (Math.abs(y) > Math.abs(Labirinto.lato_estremo))  Labirinto.lato_estremo = y;
        for (int m=0; m<4; m++)
            if (m != entrata  && mondo.get(x+" "+y).lati[ m ] == 2) {  //via aperta (primo muro)
                int n_entrata=0, inc_x, inc_y;
                inc_x=inc_y=0;
                switch (m){
                    case 0:  inc_y= -1;  n_entrata=1;
                        break;
                    case 1:  inc_y= +1;  n_entrata=0;
                        break;
                    case 2:  inc_x= -1;  n_entrata=3;
                        break;
                    case 3:  inc_x= +1;  n_entrata=2;
                        break;
                    default:   break;    
                }
                //verifica secondo muro se esiste//
                String key=(x+inc_x)+" "+(y+inc_y);
                //
                //il labirinto e' un cilindro SULL ASSE X
                Boolean change_side = false;
                if ((x+inc_x) > oriz_size/2 || (x+inc_x) < -oriz_size/2) {  
                    change_side = true;
                    inc_x = -2*x; // cambiare lato 
                    key=(x+inc_x)+" "+(y+inc_y);
                }        

                //System.out.println("calcola ("+i+", "+x+", "+y+", "+entrata+")");
                
                if (!mondo.containsKey(key))  calcoloPercorso( i+1, x+inc_x,  y+inc_y ,  n_entrata);
                else
                if ( mondo.get(key).lati[ n_entrata ] == 2  || (mondo.get(key).pass_ponte && !change_side))  {  //via aperta (secondo muro) oppure pass_ponte
                    if (mondo.get(key).pass_ponte )  { //salta ponte
                        inc_x = inc_x*2;
                        inc_y = inc_y*2;
                        key=(x+inc_x)+" "+(y+inc_y);  // NON ESISTONO ponti a cavallo dei bordi: inc_x =[1|0] 
                        
                        System.out.println("calcola >passaggio_ponte x "+key);
                        
                    }
                    if ( mondo.get(key).mollicaB == null) {  //mai passato di qui!
                        mondo.get(x+" "+y).mollicaB = key;
                        calcoloPercorso( i+1, x+inc_x,  y+inc_y ,  n_entrata);
                    } else {
                        //mondo.get(key).lati[ n_entrata ] = 1;  // come se ci fosse un muro
                    }
                }
            }
        repaint();
        //if (m == 4  && i == 0)     System.out.println("Non trovata uscita");  //variabile "m" non esiste piu'!!
        if (i == 0 && Labirinto.trovato == 0)     System.out.println("Non trovata uscita.  Estremo:"+lato_estremo);
        molliche.setText("<html><hr>Molliche: "+conteggio_molliche+"<br>Fiorini: 100<hr>Punti: 900");
    }
    
    public void resetPercorso(String key){  //cancella il percorso calcolato in precedenza (mollicaB)// key="x y"
        for (String k : mondo.keySet()) {
            if (mondo.get(k).mollicaB != null) mondo.get(k).mollicaB=null;
        }
    }
    //---------
    
    public void allargaConfine(Punto cp, String effetto) {  
        int no,su,ov,es;
        // cases: "init" "9celle" "simmetrico"
        switch (effetto){
        case "init" :
            allargaConfine(new Punto(0,0),"1-riga"); //crea riga con y=0 
            int v_size=vert_size;   //ATTENZIONE: allargaConfine() modifica -> Labirinto.vert_size +=2;
            for (int y=0; y<=v_size/2; y++)  {
                allargaConfine(new Punto(0,y),"2-righe");
            }
        break;
        //        
        case "9celle":  //questo case non viene piu' utilizzato!
            int edge_x=0;
            if (cp.x > -oriz_size/2) edge_x=-1;
            for (int r=cp.x+edge_x; (r<=cp.x+1 && r<=oriz_size/2); r++)  //limita x in base a oriz_size
                for (int c=cp.y-1; c<=cp.y+1; c++) {
                    aggiungiCella(r,c);
                }
        break; //fine switch-case "9celle"
        //
        case "1-riga": default:
            if (!mondo.containsKey("-2 "+(cp.y))) { 
                for (int r=-oriz_size/2; (r<=oriz_size/2); r++)  { //limita x in base a oriz_size
                    aggiungiCella(r,cp.y);
                }
            }
        break; //fine switch-case "1-riga"
        //
        case "2-righe": 
            int y = Math.abs(cp.y);
            if (!mondo.containsKey("0 "+(-y-1)) || !mondo.containsKey("0 "+(y+1))) {  //sfora sotto o sopra
                y += 1;
                if (y*2 > vert_size) {
                    vert_size = y*2;
                    calcoloPercorso(0,current_pos.x, current_pos.y , 9); //verificare!!
                }
                int[] add_col ={-y,+y};  //add col.  top  and/or  bottom
                for (int c : add_col)
                    for (int r=-oriz_size/2; (r<=oriz_size/2); r++)  { //limita x in base a oriz_size
                        aggiungiCella(r,c);
                    }
            }
        break; //fine switch-case "2-righe"
        }
    }
    
    private void aggiungiCella(int r, int c){
        int no,su,ov,es;
        if (!mondo.containsKey(r+" "+c)) {  //x non sovrascrive una cella esistente!!
            //
            switch ((int) (Math.random()*18)){  //16 combinazioni
                case 1: case 2:    no=2; su=2; ov=1; es=1;    break;
                case 4:            no=2; su=2; ov=2; es=1;    break;
                case 8:            no=2; su=2; ov=1; es=2;    break;
                case 3:            no=1; su=2; ov=2; es=2;    break;
                case 7:            no=2; su=1; ov=2; es=2;    break;
                case 5: case 6:    no=1; su=1; ov=2; es=2;    break;
                case 9:       no=2; su=1; ov=2; es=1;    break;
                case 10:      no=2; su=1; ov=1; es=2;    break;
                case 11:      no=1; su=2; ov=2; es=1;    break;
                case 12:      no=1; su=2; ov=1; es=2;    break;
                case 13:      no=1; su=2; ov=2; es=2;    break;
                case 14:      no=2; su=1; ov=2; es=2;    break;
                case 15:      no=2; su=2; ov=2; es=1;    break;
                case 16:      no=2; su=2; ov=2; es=1;    break;
                    default:  no=1; su=2; ov=2; es=2;    break;
                    //default:  no=1; su=1; ov=1; es=1;    break;
            }
            //System.out.println("N:"+no+" S:"+su+" O:"+ov+" E:"+es);
            Punto rc=new Punto(r,c); 
            Cella cel = new Cella(rc).SetParetiCella(no,su,ov,es);
            cel.scoperta = conteggio_mosse; 
            mondo.put(rc.toString(), cel );
        }
    }

    
    private Boolean verCella(int x, int y){
        Boolean nascondi = false; 
        int nc_x,nc_y,z=0;
        int count=0;  //viene azzerato ad ogni ciclio, credo!!
        //x= Integer.parseInt(key.substring(0, key.indexOf(" ")));
        //y=Integer.parseInt(key.substring(key.indexOf(" ")+1));

        for (int i=0; i<4; i++) {
            Boolean closed=false;
            nc_x= x; 
            nc_y= y;
            if (mondo.get(x+" "+y).lati[i] == 2){
                switch (i){
                    case 0:   nc_y = -1+y; z=1;   break;
                    case 1:   nc_y = +1+y; z=0;   break;
                    case 2:   nc_x = -1+x; z=3;   break;
                    case 3:   nc_x = +1+x; z=2;   break;
                    default:   break;    
                }
                //Punto c = new Punto(inc_x,inc_y);
                if (mondo.containsKey(nc_x+" "+nc_y) )  
                    if (mondo.get(nc_x+" "+nc_y).lati[z] != 2){ 
                        closed=true; 
                    }
            }   else  { 
                closed=true;
            }
            if (closed) count++;
        }
        //if (count == 4) mondo.get(x+" "+y).nascondi=true;
        if (count == 4) nascondi=true;
        return nascondi;
    }
    
    private void disattiva(Punto pp){
        if (previous_pos != pp){
            semplifica(-1,pp); //disattiva     
        }
    }

    private Boolean semplifica(int opz, Punto cp){
        // variante:  disegna i "ponti ipotetici" 
        // Un "ipotetico ponte" si ha quando, si incontra un elemento "||" di traverso (perpendicolare alla direzione di movimento) 
        // e non incontriamo muro interno nella cella successiva a quella di traverso.
        int x,y;
        int edge_x=0;
        int[][] check_list ={{0,-1,0},{0,+1,1},{-1,0,2},{+1,0,3}};  // 4 celle da verificare (e rispettivo lato interno)
        //
        if (opz == 0){    //esamina solo "4celle" a croce intorno al punto cp. 
            System.out.println("passa "+cp);
            // trasforma dove NON ci sono vie di entrata nasconde la cella  // verra disegnato tutto grigio!!
            Boolean esiste,  almeno_uno=false;
            for (int[] check : check_list ) {
                x=check[0]+cp.x; y=check[1]+cp.y;
                if ((x > -oriz_size/2) && (x < oriz_size/2)) { //limita x in base a oriz_size
                    if (mondo.containsKey(x+" "+y) ) {
                        Boolean nas = verCella(x,y);  mondo.get(x+" "+y).nascondi=nas; //puo' cambiare a seguito di una rotazione!   
                        //GIA' deciso  all'inizio le celle con dei tesori!! (nascoste o meno)!
                        //mondo.get(x+" "+y).tesori  =((nas && Math.random()*4>= 2) ? ((int) (Math.random()*4))  : 0);
                        if (check[2]== 2 || check[2]== 3) {
                        // verifica se presente ponte EST-OVEST                        
                        esiste = ((Math.abs(x) < oriz_size/2) && mondo.get(x+" "+y).lati[2]!=2 && mondo.get(x+" "+y).lati[3]!=2
                                && mondo.get(x-1+" "+y).lati[3]==2 && mondo.get(x+1+" "+y).lati[2]==2
                                //&& !mondo.get(x-1+" "+y).nascondi && !mondo.get(x+1+" "+y).nascondi
                                );
                        } else {
                        // verifica se presente ponte NORD-SUD                
                        esiste = ((mondo.containsKey(x+" "+(y-1)) && mondo.containsKey(x+" "+(y+1))
                                && mondo.get(x+" "+y).lati[1]!=2 && mondo.get(x+" "+y).lati[0]!=2
                                && mondo.get(x+" "+(y-1)).lati[1]==2 && mondo.get(x+" "+(y+1)).lati[0]==2
                                
                                //&& !mondo.get(x+" "+(y-1)).nascondi && !mondo.get(x+" "+(y+1)).nascondi
                                ));
                        }
                        esiste = (esiste && (x+y != 0) && (mondo.get(cp.toString()).lati[check[2]] == 2) ); //si accede al ponte (o passi sotto)
                        
                        System.out.println("esiste ed e' accessibile ponte in ("+x+" "+y+"): "+esiste);

                        if (esiste && mondo.get(x+" "+y).tesori==0) {  //viene attivato SE NON NASCONDE UN IMPREVISTO
                            mondo.get(x+" "+y).ponte = true;
                            almeno_uno = true;
                        }
                    }
                }
            }
            return almeno_uno;  //esiste almeno un ponte nell'intorno di cp
        }
        if (opz == -1){    //esamina solo "4celle" a croce intorno al punto cp. 
            for (int[] check : check_list ) {
                x=check[0]+cp.x; y=check[1]+cp.y;
                if ((x > -oriz_size/2) && (x < oriz_size/2)) { //limita x in base a oriz_size
                    if (mondo.containsKey(x+" "+y) ) {
                        if (mondo.get(x+" "+y).ponte) {                            
                            if (!mondo.get(x+" "+y).pass_ponte) {  //ULT.MOD non ha effetto!! //"|| mondo.get(x+" "+y).tesori!=0"
                                mondo.get(x+" "+y).ponte = false;
                            }
                        }
                    }
                }
            }
            return false;  //non utile!
        }
        if (opz == 11){  //chiama una volta sola all'inizio: per definire i tesori!!
            for (String key : mondo.keySet()) {
                x= Integer.parseInt(key.substring(0, key.indexOf(" ")));
                y=Integer.parseInt(key.substring(key.indexOf(" ")+1));
                Boolean nas = verCella(x,y);  // mondo.get(key).nascondi=nas; //Non setta la cella come nascosta!
                mondo.get(x+" "+y).tesori  =((nas && Math.random()*4 >= 2) ? ((int) (Math.random()*4))  : 0);
            }
        }
        if (opz == 1){  //chiama per le celle da nascondere (scan all)
            for (String key : mondo.keySet()) {
                x= Integer.parseInt(key.substring(0, key.indexOf(" ")));
                y=Integer.parseInt(key.substring(key.indexOf(" ")+1));
                Boolean nas = verCella(x,y);   mondo.get(key).nascondi=nas;  //puo' essere nascosta da una rotazione
                //mondo.get(x+" "+y).tesori  =((nas && Math.random()*4>= 2) ? ((int) (Math.random()*4))  : 0);
            }
        }
        if (opz == 1){  //crea i ponti in automatico            // trasforma dove c'e' "ipotetico ponte" 

            Boolean esiste,flipflop=true;
            for (String key : mondo.keySet()) {
                x= Integer.parseInt(key.substring(0, key.indexOf(" ")));
                y=Integer.parseInt(key.substring(key.indexOf(" ")+1));
                // verifica se presente ponte EST-OVEST                
                esiste = ((Math.abs(x) < oriz_size/2) && mondo.get(key).lati[2]!=2 && mondo.get(key).lati[3]!=2
                        && mondo.get(x-1+" "+y).lati[3]==2 && mondo.get(x+1+" "+y).lati[2]==2
                        && (!mondo.get(x-1+" "+y).nascondi && !mondo.get(x+1+" "+y).nascondi)
                        );
                // verifica se presente ponte EST-OVEST                
                esiste = (esiste || (mondo.containsKey(x+" "+(y-1)) && mondo.containsKey(x+" "+(y+1))
                        && mondo.get(key).lati[1]!=2 && mondo.get(key).lati[0]!=2
                        && mondo.get(x+" "+(y-1)).lati[1]==2 && mondo.get(x+" "+(y+1)).lati[0]==2
                        && (!mondo.get(x+" "+(y-1)).nascondi && !mondo.get(x+" "+(y+1)).nascondi)
                        ));
                esiste = (esiste && x+y != 0 && mondo.get(x+" "+y).tesori==0);   //ver.ult.mod.=> //&& mondo.get(x+" "+y).tesori!=0
                if (flipflop && esiste) {  //ogni due ponti uno viene attivato
                    flipflop = false;
                    mondo.get(key).ponte = true;
                } else flipflop = true;
            }
        }
        repaint();
        return false;  //NON IMPORTA il valore
    }    
    
    private void Zoom(){
        // TODO: .. calcolare il nuovo centro_r (se zoom out)
        // >> usare il drawstring(pos) x il debug
        Labirinto.u -= 2;
        if (Labirinto.u < 2) {
            Labirinto.u = 6;
        }
        //default: centra !!  se esterno al quadro va sul bordo!!
        centro_r.x=158-u*2; // 
        centro_r.y=100-current_pos.y*u*6;
        //int quadro_size = ((158-u*2)*2)/(u*6);
        int quadro_osize = (quadro.getSize().width)/(u*6);
        int quadro_vsize = (quadro.getSize().height)/(u*6);
        
        System.out.println("Quadro pos - size"+quadro.getX()+" "+quadro.getY()+" - "+quadro.getSize().width+" "+quadro.getSize().height);

        System.out.println("quadro_size current_pos.x:"+quadro_osize+" "+current_pos.x);
        if (oriz_size > quadro_osize) {
            if (current_pos.x >= quadro_osize/2)       centro_r.x=158-u*2 - (quadro_osize/2-1)*u*6; // bordo !!
            if (current_pos.x <= -quadro_osize/2)      centro_r.x=158-u*2 + (quadro_osize/2-1)*u*6; 
        }
        if (vert_size > quadro_vsize) {
            if (current_pos.y >= quadro_vsize/2)       centro_r.y=100-current_pos.y*u*6 + (quadro_vsize/2)*u*6; // bordo !!
            if (current_pos.y <= -quadro_vsize/2)      centro_r.y=100-current_pos.y*u*6 - (quadro_vsize/2-1)*u*6; 
        }
        
        for (String key : mondo.keySet()) {
            mondo.get(key).newLocation();    
        }
        
        System.out.println("Centro Riq2:"+centro_r);
        repaint();  //non basta!!
    }
    
    public Punto Muovi(String dir) {
        //Punto new_cp=current_pos; //ERRORE: Attenzione non copio l'occetto ma il riferimento !!!!  E' sempre lo stesso oggetto!!
        //Punto new_cp = (Punto) current_pos.clone(); // copio l'oggeto!!
        Punto new_cp = new Punto(0,0);  //oggetto di appoggio
        String[] direzione = {"Nord","Sud","Ovest","Est"};
        int inc_x=0,inc_y=0;

        switch (dir){
            case "Sud":
                inc_y += 1;
                break;
            case "Nord": 
                inc_y -= 1;
                break;
            case "Ovest": 
                inc_x -= 1;
                break;
            case "Est": 
                inc_x += 1;
                break;
            default:   break;    
        }
        
        new_cp.x = current_pos.x + inc_x;
        new_cp.y = current_pos.y + inc_y;
        //
        if (Math.abs(new_cp.y)>vert_size_limit/2-2) {
            mosse.setText(" LIMITE RAGGIUNTO!! - Mosse: "+conteggio_mosse+" ");
            return current_pos;
        }  //limite top/bottom
        //
        //il labirinto e' un cilindro SULL ASSE X
        Boolean change_side = false;
        if (new_cp.x > oriz_size/2 || new_cp.x < -oriz_size/2) {  
            change_side = true;
            new_cp.x -= inc_x; //annulla l'incremento prima di cambiare lato 
            new_cp.x= - new_cp.x ;
        }        
        
        allargaConfine(new_cp,"2-righe");  //Non sempre occorre questa chiamata //

        // Verifica se direzione aperta
        Boolean ok = true;
        switch (dir){
            case "Sud":
                ok = ((mondo.get(current_pos.toString()).lati[1] == 2) ? (mondo.get(new_cp.toString()).lati[0] == 2) : false);
                break;
            case "Nord": 
                ok = ((mondo.get(current_pos.toString()).lati[0] == 2) ? (mondo.get(new_cp.toString()).lati[1] == 2) : false);
                break;
            case "Ovest": 
                ok = ((mondo.get(current_pos.toString()).lati[2] == 2) ? (mondo.get(new_cp.toString()).lati[3] == 2) : false);
                break;
            case "Est": 
                ok = ((mondo.get(current_pos.toString()).lati[3] == 2) ? (mondo.get(new_cp.toString()).lati[2] == 2) : false);
                break;
            default:   break;    
        }

        //>>Ponte?
        // si puo' aggiungere il metodo Boolean <cella>.ponte(cp, <dir>); // restituisce true se presente ponte non di traverso!
        if (!ok && mondo.get(new_cp.toString()).ponte && previous_pos.equals(current_pos)&& (previous_dir.equals(dir)) ){  // CONFERMATO passaggio ponte 
            ok = true;  
            mondo.get(new_cp.toString()).pass_ponte = true; 
            mondo.get(new_cp.toString()).ponte=false; //se pass_ponte=true; flag ponte=true; (non utile?)
            new_cp.x += inc_x;  //supera il ponte
            new_cp.y += inc_y;
            System.out.println("calcola da "+new_cp);
            calcoloPercorso(0,new_cp.x, new_cp.y , 9); //NON VA??  //serve QUI per conteggio molliche NON raggiungibili??!!
        }
        
        if (ok || mondo.get(new_cp.toString()).pass_ponte || freeMove) {  //via libnera oppure passaggio ponte (attivo)
            disattiva(current_pos); // disattivare eventuale flag ponte (se pass_ponte NON avvenuto)
            if (!ok && mondo.get(new_cp.toString()).pass_ponte )  { //supera il ponte (x il suo verso)
                new_cp.x += inc_x;  
                new_cp.y += inc_y;                
            }
            
            int cpath=0;
            for (String k : mondo.keySet()) 
                if (mondo.get(k).mollicaB!=null && !mondo.get(k).nascondi)cpath ++;
            
            posizione.setText("<html>("+new_cp+") - ["+oriz_size+"x"+vert_size+"]<hr> Path:"+(cpath*100/(oriz_size*vert_size))+"%");  
            posizione.repaint();

            // Verifica se new_cp fuori dal "quadro" :
            // - scorrere il riguardo  - sposta il centro_r /oppure/ quadro.translate(u*6) //
            if (!new Rectangle(quadro.getSize()).contains(new Punto(new_cp.x*(u*6)+centro_r.x+(u*3)+inc_x*u*6, new_cp.y*(u*6)+centro_r.y+(u*3)+inc_y*20))  ) {
                centro_r.y -= inc_y*u*6;
                centro_r.x -= inc_x*u*6;
                int xoff = (((oriz_size+2)*(u*6)) - quadro.getSize().width)/2;
                if (change_side) centro_r.x = 158-u*2 + inc_x*xoff;

                // alt:1 - muovo tutte le celle //
                for (String key : mondo.keySet()) {
                    mondo.get(key).newLocation();    //setLocation() in base al nuovo centro_r
                }
                // alt:2 - creo un panel dentro a quadro (di 1000x1000) e muovo questo una volta sola //
                // Todo ...
            }


            //modificare la posizione di tutti gli elementi , oppure ....
            //surface.setLocation(centro_r.x+(pos.x*(u*6)), centro_r.y+(pos.y*(u*6)));

            mondo.get(new_cp.toString()).SetCurrent();  //non modifica current_pos

            conteggio_mosse ++;
            mosse.setText(" Mosse:"+conteggio_mosse+" ");

            //allargaConfine(new_cp,"1riga");  //Non sempre occorre questa chiamata - sposto in alto//
            semplifica(0,new_cp); //attiva flag ponte + nascondi
        //current_pos = new_cp;  //>> Quasi ERRORE (non e' una copia del valore)!!  Meglio: current_pos = Muovi("Nord");  // 
            
        } else {
            //verifica se presente un ponte (o tesoro) // 
            String msg = "<html>";  
            if (mondo.get(new_cp.toString()).ponte) msg = msg+" -> Vai a "+dir+" x PASSARE il ponte (10 fiorini) <br>";
            else {
                //aggiungere il movimento del rimbalzo sul muro!!  
                int unodue = ((mondo.get(current_pos.toString()).lati[latoDirezione(dir)] == 2) ? 2 : 1 );
                mondo.get(current_pos.toString()).surface.xo=(new_cp.x-current_pos.x)*unodue;  //valore da -2 a +2
                mondo.get(current_pos.toString()).surface.yo=(new_cp.y-current_pos.y)*unodue;
                repaint(); //.. questo repaint() e' "invoked Later"  (dopo il return;?)!!
                
                //LA SOLUZIONE usata in "ruota()" E' migliore perche lo sleep() non avviene in AWT-ED!!
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        System.out.println("passo_go"+current_pos);  //attente che wait sia terminato??
                        //Surface.threadMessage("passo1"+current_pos);
                        try { 
                        Thread.sleep(200); //NON HA SENSO uno sleep in AWT-ED queue!!
                        } catch (InterruptedException ev) {  System.err.println("Caught Exception: " + ev.getMessage());   }    
                        mondo.get(current_pos.toString()).surface.xo=0;
                        mondo.get(current_pos.toString()).surface.yo=0;
                        //non avviene un repaint() automatico!!
                        repaint();
                    }
                });
                //} catch (Exception ev) {   //Catch "Taget" richiesta da InvokeAndWait()??
                //    System.err.println("Caught Exception: " + ev.getMessage());
                //}
                
                //mondo.get(current_pos.toString()).surface.xo=0;
                //mondo.get(current_pos.toString()).surface.yo=0;
                msg = msg+" -> Impossibile andare verso "+dir+" <br>";
            }
            //controlla se presente un tesoro .. e quale e la via x accedervi!!
            int t = mondo.get(new_cp.toString()).tesori;
            if (t >= 1 && mondo.get(current_pos.toString()).lati[latoDirezione(dir)] == 2) {
                if (current_pos.equals(previous_pos)){
                    msg = msg+" -> ('Imprevisto' tipo:"+t+") ..prova un'altro accesso!";
                } else {
                    msg ="<html>";
                    msg = msg+" -> Trovato 'Imprevisto' (tipo:"+t+") <br>..andare verso "+dir+" to GET it!";                    
                }
            }            
            mosse.setText(msg);
            //rimane nella stessa posizione
            new_cp.x = current_pos.x;
            new_cp.y = current_pos.y;
        }
        
        //indica imprevisto/tesoro trovato !!
        if (mondo.get(new_cp.toString()).tesori > 0 && mondo.get(new_cp.toString()).tesori < 10) {
            mondo.get(new_cp.toString()).tesori += 10;
            mondo.get(new_cp.toString()).nascondi = false;            
            String msg ="<html>";
            msg = msg+" -> PRESO! (nuove indicazioni per altro obbiettivo o premio o imprevisto)";                    
            mosse.setText(msg);
        } 
        
        repaint();
        if (!new_cp.equals(current_pos)) { //si sposta di cella ,ed azzera xy_offset  //BUG verificare!
                mondo.get(current_pos.toString()).surface.xo=0;  //valore da -2 a +2
                mondo.get(current_pos.toString()).surface.yo=0;
        }
        previous_dir=dir;
        previous_pos= (Punto) current_pos.clone();  // NON e' molto utile <punto>.clone(), dato che sono solo due gli attributi (x,y)//

        return (new_cp);  //modifica: current_pos= Muovi("Nord");
    }

    private int latoDirezione(String dir){  //creare una classe apposta o un ENUM ??
        switch(dir){
            case "Nord": return 0;
            case "Sud": return 1;
            case "Ovest": return 2;
            case "Est": return 3;
            default: return 9;
        }
    }
    
    private void stepIndietro() {
        Labirinto.stepBack=true;
        String p = mondo.get(current_pos.toString()).mollica; //puntatore alla cella precedente
        mondo.get(p).SetCurrent();
        current_pos.x=Integer.parseInt(p.substring(0, p.indexOf(" ")));
        current_pos.y=Integer.parseInt(p.substring(p.indexOf(" ")+1)); 
        Labirinto.stepBack=false;
        
        repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        quadro = new javax.swing.JPanel() {
            public void paintComponent(Graphics g){ 
                Labirinto.DoPaint(g);
            }
        };
        mosse = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        posizione = new javax.swing.JLabel();
        molliche = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        quadro.setPreferredSize(new java.awt.Dimension(260, 260));

        javax.swing.GroupLayout quadroLayout = new javax.swing.GroupLayout(quadro);
        quadro.setLayout(quadroLayout);
        quadroLayout.setHorizontalGroup(
            quadroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 317, Short.MAX_VALUE)
        );
        quadroLayout.setVerticalGroup(
            quadroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 260, Short.MAX_VALUE)
        );

        mosse.setBackground(new java.awt.Color(255, 255, 255));
        mosse.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        mosse.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mosse.setText("<html><b>O</b>:Rotate-left  <b>P</b>:Rotate-right <br><b>Q</b>:Show-bridges  <b>Z</b>:Zoom     <br> Mosse:1 ");
        mosse.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        mosse.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mosse.setOpaque(true);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/immagini/cassa.jpg"))); // NOI18N
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jLabel1MouseMoved(evt);
            }
        });

        posizione.setText("(0 0)");
        posizione.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        molliche.setText("molliche:");
        molliche.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mosse)
                    .addComponent(quadro, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posizione)
                    .addComponent(molliche))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(quadro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(posizione, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(molliche, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mosse))
        );

        pack();
    }// </editor-fold>                        

    private void jLabel1MouseMoved(java.awt.event.MouseEvent evt) {                                   
        // TODO add your handling code here:
        if (currentTime != System.currentTimeMillis()/1000) {
            System.out.println("Mouse-(move)-OVER: "+currentTime);  //usare un flag x catturare mouse-over
            currentTime = System.currentTimeMillis()/1000;
        }
        /*
        if (evt.getY() <= jLabel1.getY()+50) centro_r.y -= u*6; 
        if (evt.getY() >= jLabel1.getY()+50) centro_r.y += u*6; 
        Labirinto.this.repaint();
        */
    }                                  

    public void addKlistener() {
                this.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    //
                    try {
                    //calcoloPercorso(0,current_pos.x, current_pos.y , 9);  //per aggiungere i puntini blu all'inizio gioco
                    switch(e.getKeyCode()) {

                        case KeyEvent.VK_W:
                        case KeyEvent.VK_UP:
                            current_pos = Muovi("Nord");
                            break;

                         case KeyEvent.VK_S:
                        case KeyEvent.VK_DOWN:
                            current_pos = Muovi("Sud");
                            break;

                        case KeyEvent.VK_A:
                        case KeyEvent.VK_LEFT:
                            current_pos = Muovi("Ovest");
                            break;

                        case KeyEvent.VK_D:
                        case KeyEvent.VK_RIGHT:
                            current_pos = Muovi("Est");
                            break;
                        /*
                         * If the game is not over, toggle the paused flag and update
                         * the logicTimer's pause flag accordingly.
                         */
                        case KeyEvent.VK_O:  //rotate Ovest
                            mondo.get(current_pos.toString()).ruota("LEFT");  //modifica labirinto
                            break;
                        case KeyEvent.VK_P:  //rotate Ovest
                            mondo.get(current_pos.toString()).ruota("RIGHT");  //modifica labirinto                            
                            break;
                        // Back - percorre indietro il path delle molliche
                        case KeyEvent.VK_B:  //Back (molliche) opuure usare cursore
                            stepIndietro();
                            break;
                        case KeyEvent.VK_Z:  //Zoom
                            Zoom();
                            break;
                           
                        case KeyEvent.VK_U:  //cerca Uscita
                            /*
                            Labirinto.trovato=0; resetPercorso("0 0");
                            calcoloPercorso(0,current_pos.x, current_pos.y,9);
                            */
                            if (!toggle_calcola) {
                                toggle_calcola=true;
                                Labirinto.trovato=0; resetPercorso("0 0");
                                calcoloPercorso(0,current_pos.x, current_pos.y,9);
                            }
                            else {
                                toggle_calcola=false;
                                Labirinto.trovato=0; //?? >resetPercorso("0 0");
                                for (String key : mondo.keySet()){
                                    mondo.get(key).mollicaB=null;
                                }
                            }
                            repaint();
                            break;
                        case KeyEvent.VK_ENTER:
                            if (!freeMove) freeMove=true;
                            else freeMove=false;
                            break;
                        case KeyEvent.VK_Q:  //Semplifica()
                            if (!toggle_nascondi) {
                                toggle_nascondi=true;
                                semplifica(1,new Punto(9,9)); //individua le celle isolate. (1=all)
                            }
                            else {
                                System.out.println("nascondi toggle");  //debug
                                toggle_nascondi=false;
                                for (String key : mondo.keySet()){
                                    //mondo.get(key).nascondi=false;
                                    mondo.get(key).ponte=false;
                                }
                            }
                            
                            repaint();
                            break;
                    }
                    } catch (InterruptedException ev) {
                    System.out.println("I wasn't done!");
                    System.err.println("Caught Exception: " + ev.getMessage());
                    }

                }

        });
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Labirinto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Labirinto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Labirinto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Labirinto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Labirinto().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel molliche;
    private javax.swing.JLabel mosse;
    private javax.swing.JLabel posizione;
    private javax.swing.JPanel quadro;
    // End of variables declaration                   

    //Inner class Cella (posso cosi' usare "add(<jLabel-surface>)" che agisce sul jFrame-Labirinto) //
    class Cella {
        protected int[] lati = {0,0,0,0}; //0:muro(confine-mondo)  1:muretto  2:libero  3:porta_apribile //Ordine: (N,S,O,E)
        Punto pos;
        Surface surface;
        private Boolean current = false;
        protected String mollica = null;  // indica il passaggio sul Path (percorso contiguo di molliche)
        protected String mollicaB = null;  // indica il passaggio sul Path (percorso contiguo di molliche)
        protected int scoperta = 0;  // registra il numero di mossa che ha scoperto la cella!!
        protected Boolean nascondi=false;
        protected int tesori = 0; //valore da 0 a 4: tipo di tesoro posto sulla cella! // preferenza alle celle nascoste(??)
        protected Boolean ponte = false; // viene disegnato come ponte 
        protected Boolean pass_ponte = false; // passaggio ponte e' stato acquistato!! (modif. calcoloPercorso) 
        protected Boolean ghost=false;
        protected Boolean attiva=true;  //se disattiva, il nulla avanza!
        protected String rotate=null;  //(left,right,null)
        //final int u = 10;
        
        Cella(Punto p){
            // la prima cella ha per default muri di confino (unica cella del mondo)
            pos = p;
            surface = new Surface(this);
            quadro.add(surface);
            //surface.setBounds(0, 0, u*6, u*6);  //spostato nel costruttore di surface!

            surface.setLocation(centro_r.x+(pos.x*(u*6)), centro_r.y+(pos.y*(u*6)));
        }

        public Cella SetParetiCella(int nord, int sud, int est, int ovest){
            lati[0]=nord; lati[1]=sud; lati[2]=est; lati[3]=ovest;

            //surface.repaint();
            return this;
        }
        
        public void SetCurrent() { // agrega le due operazione (azzera e set)
            mondo.get(current_pos.toString()).SetCurrent(false);
            SetCurrent(true);
        }
        public void SetCurrent(Boolean stato) {
            //Sposto le 2 righe sotto in Labirinto ..perche' mi pare meglio!!
            //mondo.get(current_pos.toString()).current=false;
            //mondo.get(current_pos.toString()).surface.repaint();                       
            this.current = stato;           
            // se torna indietro sui suoi passi col cursore:  cancellare le molliche.
            if ((stato && mollica != null) && (pos.toString().equals(mondo.get(current_pos.toString()).mollica)) ) {   //torna indietro sui suoi passi col cursore
                mondo.get(current_pos.toString()).mollica = null;
                Labirinto.conteggio_molliche --;
                //molliche.setText("Molliche: "+conteggio_molliche);
                molliche.setText("<html><hr>Molliche: "+conteggio_molliche+"<br>Fiorini: 100<hr>Punti: 900");
            }
            if (stato && mollica == null) {
                Labirinto.conteggio_molliche ++;
                //molliche.setText("Molliche: "+conteggio_molliche);
                molliche.setText("<html><hr>Molliche: "+conteggio_molliche+"<br>Fiorini: 100<hr>Punti: 900");
                this.scoperta = conteggio_mosse;
                this.mollica = current_pos.toString();  //puntatore alla cella/mollica precedente (x creare il path)
                //se la cella e' ad un incrocio, NON modifica il puntatore! Ignorando cosi' il circolo vizioso!!   
            }
            
            if (!stato && stepBack) {   //non va bene se incrocio:  rimane appeso il circolo vizioso!
                mollica = null;
                Labirinto.conteggio_molliche --;
                //molliche.setText("Molliche: "+conteggio_molliche);
                molliche.setText("<html><hr>Molliche: "+conteggio_molliche+"<br>Fiorini: 100<hr>Punti: 900");
            }
            //surface.repaint(); //non e' necessario?
        }
        public Boolean CurrentVal() {
            return this.current;
        }
        
        protected void newLocation(){
            surface.setLocation(centro_r.x+(pos.x*(u*6)), centro_r.y+(pos.y*(u*6)));

            // Posso usare una surface.scale() ??
            surface.u=Labirinto.u;  // PERCHE' serve in caso di ZOOM()??
            //surface.setBounds(surface.getX()-u, surface.getY()-u, u*8, u*8);  //Approfondire! allarga il Bound!
            surface.setBounds(surface.getX(), surface.getY(), u*6, u*6);  //Approfondire!
            // //surface.repaint();  // BUG problema forse con Bound(); ??
        } 
        
        
        protected void ruota(String dir) throws InterruptedException {  //Left or Right
        //aggiungere l'effetto rotazione (in 2step (+45°+90°) usando un thread)
            // Delay, in milliseconds before we interrupt MessageLoop thread (default one hour).
            long patience = 1000 * 60 * 60;
            long startTime = System.currentTimeMillis();
            final String ruota_dir = dir;
            //surface.repaint();// NON ha effetto!!  Provare: "SwingUtilities.invokeAndWait(surface);"!

             Thread appThread = new Thread() {
                 public void run() {
                     try {
                        rotate=ruota_dir;  //step1 - verificare se l'attesa e' 200 millisec!!
                        repaint();// HA effetto!!  Sostituire con: invokeAndWait(surface)???
                        //SwingUtilities.invokeAndWait(surface);  //il repaint() e' implicito!!
                        Thread.sleep(100);
                        //SwingUtilities.invokeAndWait(surface);
                        //cambia la posizione dei muri a seguito di ruota(left/right)
                        int temp=lati[0];
                        switch (ruota_dir) {
                            case "LEFT" :
                                lati[0]=lati[3];  lati[3]=lati[1];  lati[1]=lati[2];  lati[2]=temp;
                                break;
                            default :  
                                lati[0]=lati[2];  lati[2]=lati[1];  lati[1]=lati[3];  lati[3]=temp;
                                break;
                        }            

                        rotate=null; 
                        repaint(); //se omesso, verra' poi eseguito il frame.repaint();
                        Labirinto.trovato=0; resetPercorso("0 0");
                        calcoloPercorso(0,current_pos.x, current_pos.y , 9);  //TODO.. calcolare il lato di entrata 
                     }
                     catch (Exception e) {
                         e.printStackTrace();
                     }
                     System.out.println("Finished on " + Thread.currentThread());
                     
                 }
             };
             appThread.start();
             
             //Un alternativa x thread "t.join()":  https://docs.oracle.com/javase/tutorial/essential/concurrency/simple.html
            
            //Le due istruzioni sotto, verrebbero erroneamente eseguite prima di invokeAndWait->repaint();   (vedi sopra)
            //rotate=true; 
            //surface.repaint();
        }  
        
        /*
        protected void Ghost(Graphics2D g2d,String var1){  
            // disegna il fantasmino!!  
            int x=u*2,y=u*2;  //-> dx o dy = 0
            if (dx < 0) x = (u*6)-u*ghost_6step;
            if (dx > 0) x = 0+u*ghost_6step;
            if (dy < 0)  y = (u*6)-u*ghost_6step;
            if (dy > 0)  y = u*ghost_6step;
            //
            g2d.setPaint(Color.darkGray);
            g2d.fillOval(x, y, u*2, u*2);
        }
        */   
        

}


class Surface extends JPanel implements ActionListener,Runnable {

    //private final int DELAY = 150;
    //private Timer timer;
    private Labirinto.Cella rif_cella;
    public int u = Labirinto.u;  //Viene eseguito solo una volta??  >> "field can be filnal"? <<
    private long currentTime;
    protected int xo=0,yo=0; //offset per il cursore rosso (verso la cella adiacente) //val. ammessi da -2 a +2

    Surface(Labirinto.Cella c) {  //
        //initTimer();
        rif_cella=c;
        //add mouse listener alla cella
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                //jLabel1MouseMoved(evt);
                CellaMouseMoved(evt);
            }
        });
        //allargo il Bound per vedere il fantasmino!  Ma viene sovrascritto dalle celle di lato.
        this.setBounds(0, 0, u*6, u*6);
    }

    private void CellaMouseMoved(java.awt.event.MouseEvent evt) {                                   
        //usare un flag x catturare mouse-over
        if (rif_cella.tesori == 0 ) return;
        if (currentTime != System.currentTimeMillis()/1000)
                System.out.println("Mouse-(move)-OVER: ("+this.rif_cella.pos+") "+currentTime);  
        currentTime = System.currentTimeMillis()/1000;
        //
        // TODO ...
    }                                  
    
    
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g); //se lo metto cancella tutto il bound!
        Graphics2D g2d = (Graphics2D) g;

        //ruota intorno al centro della cella
        //Come evitare che la cella venga coperta da quelle adiacenti(durante la rotazione)??
        if (rif_cella.rotate != null) {
          //setBounds(this.getX(), this.getY(), u*8, u*8);
            g2d.setPaint(Color.gray);  //g2d.setPaint(Color.yellow);
            g2d.fillRect(0, 0, u*6, u*6);
          //g2d.translate(0,u);             //serve per "y" ?? //forse a causa del bug-saltino iniziale!?
            g2d.rotate((rif_cella.rotate.equals("LEFT") ? -Math.PI/4 : Math.PI/4) ,u*3, u*3); //dovrebbe fare translate(u*4)+rotate(45°) assieme.
          //g2d.translate(0, 0);
            
            threadMessage("PASSA DI QUI! - rotate");
        } 
        
        
        //Color wallcolor = ((rif_cella.attiva) ? Color.gray : Color.lightGray);  
        Color wallcolor = ((rif_cella.attiva) ? Color.gray : Color.LIGHT_GRAY);  
        //sostituire con: Color[] bgcolor 
        
        g2d.setPaint(Color.lightGray); 
        if (rif_cella.nascondi) { 
            g2d.setPaint(wallcolor);
        }
        
        if (rif_cella.pos.equals("0 0")) g2d.setPaint(((rif_cella.attiva) ? Color.yellow : Color.white));
        
        if (rif_cella.attiva) g2d.fillRect(0, 0, u*6, u*6); //background  
        else if (rif_cella.nascondi){ 
            g2d.setPaint(Color.lightGray);
            g2d.fillRect(0, 0, u*6, u*6); //background 
        }
        g2d.setPaint(wallcolor);


        /*
        //if ((rif_cella.scoperta != 0) && rif_cella.mollicaB == null ) { //non considere l'uso del cursore indietro!! 
        if (!rif_cella.CurrentVal() && rif_cella.mollica != null  && rif_cella.mollicaB == null) {  //percorso interrotto da una rotazione cella? //BUG?
            System.out.println("white "+rif_cella.pos+" "+rif_cella.mollica+" "+rif_cella.mollicaB);
            //operazione messa in paintComponent() per non dover aggiungere un ciclo-mondo! (o mettere nel ciclo reset?)
            //rif_cella.mollica = null;  Labirinto.conteggio_molliche --; //rimuovere flag in modo permanente??
            g2d.setPaint(Color.white);
        }
        */
        //if (rif_cella.pos.equals(Labirinto.target)) g2d.setPaint(Color.yellow);
        //g2d.fillRect(0, 0, u*6, u*6);   //background  
        
        //disegna I di Imprevisto (ex Tesoro)
        if (rif_cella.tesori != 0) {
            if (rif_cella.tesori >= 10) g2d.setPaint(((rif_cella.attiva) ? Color.orange : Color.gray));
            else    g2d.setPaint(((rif_cella.attiva) ? Color.black : Color.gray)); 
            g2d.fillRect((int)(u*2.5), u+1, u, u);
            g2d.fillRect((int)(u*2.5), (int)(u*2.5), u, u*2); 
            g2d.drawLine(u*2,(int)(u*4.5), u*4, (int)(u*4.5));
    }

// From:  https://docs.oracle.com/javase/tutorial/2d/images/index.html   (approfondire)
// <editor-fold defaultstate="collapsed" desc="* Create an ARGB BufferedImage *">                          
/* Create an ARGB BufferedImage *
BufferedImage img = ImageIO.read(imageSrc);
int w = img.getWidth(null);
int h = img.getHeight(null);
BufferedImage bi = new
    BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
Graphics g = bi.getGraphics();
g.drawImage(img, 0, 0, null);

/*
 * Create a rescale filter op that makes the image
 * 50% opaque.
 *
float[] scales = { 1f, 1f, 1f, 0.5f };
float[] offsets = new float[4];
RescaleOp rop = new RescaleOp(scales, offsets, null);

/* Draw the image, applying the filter *
g2d.drawImage(bi, rop, 0, 0);

*/ // </editor-fold>
        
        if (rif_cella.mollicaB != null){   // && rif_cella.rotate == null) { 
            g2d.setPaint(Color.blue);
            g2d.drawLine(0, u*2, u*2, 0); g2d.drawLine(0, u*4, u*4, 0); g2d.drawLine(0, u*6, u*6, 0);
            g2d.drawLine(u*2, u*6, u*6, u*2); g2d.drawLine(u*4, u*6, u*6, u*4);
            //g2d.setPaint(Color.lightGray);
        };         
        g2d.setPaint(wallcolor);
        g2d.fillRect(0, 0, u, u);  g2d.fillRect(0, u*5, u, u);  g2d.fillRect(u*5, u*5, u, u);  g2d.fillRect(u*5, 0, u, u);
        if (rif_cella.lati[0]!=2) g2d.fillRect(0, 0, u*6, u);     // muro a Nord   
        if (rif_cella.lati[3]!=2) g2d.fillRect(u*5, 0, u, u*6);
        if (rif_cella.lati[2]!=2) g2d.fillRect(0, 0, u, u*6);
        if (rif_cella.lati[1]!=2) g2d.fillRect(0, u*5, u*6, u);            
                
        //g2d.setPaint(Color.black);
        //g2d.drawString( rif_cella.pos.toString(), 2, 20);  //debug
        
        if (rif_cella.mollica != null) { 
            g2d.setPaint(Color.orange);
            g2d.fillOval(u*2,u*2,u*2,u*2);
        }; 
        
        if ( rif_cella.pass_ponte || (rif_cella.ponte)) { //  BUG!! .. && Labirinto.toggle_nascondi)) { 
            if (rif_cella.lati[0] == 2){
                g2d.setPaint(((rif_cella.attiva) ? Color.orange : Color.lightGray));  g2d.fillRect(0,u,u*6,u*4);
                g2d.setPaint(Color.gray);   g2d.drawLine(0,u,u*6,u);
                g2d.setPaint(Color.gray);   g2d.drawLine(0,u*5,u*6,u*5);
            } else {
                g2d.setPaint(((rif_cella.attiva) ? Color.orange : Color.lightGray));  g2d.fillRect(u,0,u*4,u*6);
                g2d.setPaint(Color.gray);   g2d.drawLine(u,0,u,u*6);
                g2d.setPaint(Color.gray);   g2d.drawLine(u*5,0,u*5,u*6);
            }
        }; 

        if (rif_cella.pos.y == hiderow_num) {
            g2d.setPaint(Color.cyan);
            g2d.fillRect(0,0,u*6,u);
        }
        
        
        if (rif_cella.CurrentVal() && !rif_cella.pass_ponte) { 
            g2d.setPaint(Color.red);
            g2d.fillRect(u*(2+xo),u*(2+yo),u*2,u*2);
            System.out.println("-repaint-");
        };    
    }

    //azione lanciata da timer della classe surface
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
    
    
    /* From: http://docs.oracle.com/javase/7/docs/api/javax/swing/SwingUtilities.html#invokeAndWait(java.lang.Runnable)
    //
    final Runnable doHelloWorld = new Runnable() {
         public void run() {
             System.out.println("Hello World on " + Thread.currentThread());
         }
     };

     Thread appThread = new Thread() {
         public void run() {
             try {
                 SwingUtilities.invokeAndWait(doHelloWorld);
             }
             catch (Exception e) {
                 e.printStackTrace();
             }
             System.out.println("Finished on " + Thread.currentThread());
         }
     };
     appThread.start();
    */
    
        public void run() {
            //step (+45° rotate surface/cella;)
            //vedi di capire bene:
            //  http://stackoverflow.com/questions/4905263/how-to-rotate-with-affinetransform-and-keep-the-orignal-coordinates-system
            threadMessage("passo 45°");
            rif_cella.rotate=null; //per sicurezza!!
        }
        
        // Display a message, preceded by
        // the name of the current thread
        private void threadMessage(String message) {
            String threadName =
                Thread.currentThread().getName();
            System.out.format("%s: %s%n",
                              threadName,
                              message);
        }

    }
    
}


class Punto extends Point {
    Punto(int x, int y){
        super(x,y);
    }
    @Override
    public String toString(){
        return this.x+" "+this.y;
    }   
    public Boolean equals(Punto p){
        return (this.x == p.x && this.y == p.y);
    }   
    public Boolean equals(String s){  //s="x y"
        if (s == null) s = "0 0";
        int x1,y1;
        x1=Integer.parseInt(s.substring(0, s.indexOf(" ")));
        y1=Integer.parseInt(s.substring(s.indexOf(" ")+1));
        return (this.x == x1 && this.y == y1);
    }   

    public String vediLato(String dir) {
        int dx=0,dy=0;
        switch (dir){
            case "Nord": dx=0;dy=-1; break;
            case "Sud":  dx=0;dy=1; break;
            case "Ovest":  dx=-1;dy=0; break;
            case "Est":  dx=1;dy=0; break;
        }
        return (this.x+dx)+" "+(this.y+dy);
    }
}
