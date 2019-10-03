/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxgame;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Isaac A. Marin
 */
public class HundirBarcosController implements Initializable {
    Alert alert;
    protected int port=6000, port2=6002;    //default port
    //server socket
    protected ServerSocket serverSocket, serverSocket2;
    //data inputstream
    protected DataInputStream dataInput, dataInput2;
    //client socket
    protected Socket clientSocket=null, clientSocket2=null;
    //data outputStream to send client
    protected DataOutputStream outputStream, outputStream2;
    //thread
    protected StartThreadToRecieve startThread;    
    protected SecondThreadToRecieve start2Thread;
    //to play
    protected Partida jugador1, jugador2;    
    protected int[] barcosEscogidos2;    //array of boats
    protected int possitionArray=0, possitionArray2=0;
    //the player is playing
    protected boolean player1Playing = false, player2Playing=false;
    protected Turno turno;
    @FXML
    private Circle clion1, clion2;
    @FXML
    private Label jugador2Label, player1Label,player2Label,jugador1Label;
    @FXML
    private TextArea boatTextArea;
    
    
    @FXML
    private void PonerVerde(ActionEvent e){
            clion1.setFill(javafx.scene.paint.Color.GREEN);
    }
    
    
    @FXML
    private void mostrarBotones(ActionEvent e){
           try {
            jugador1.printBarcos();
            jugador2.printBarcos();
            showTheBoats1();
            showTheBoats2();
        } catch (NullPointerException ex) { 
              System.out.println("No hay jugadores");
              Alert asd = new Alert(Alert.AlertType.ERROR);
              asd.setTitle("Espera!");
              asd.setContentText("Aún no hay jugadores");
              asd.setHeaderText(null);
              asd.showAndWait();
              
              
            //alert.setTitle("No puede comenzar el juego");
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startThread = new StartThreadToRecieve();
        start2Thread = new SecondThreadToRecieve();
        //class to partida                                
        barcosEscogidos2 = new int[3];        
        //second thread        
        startThread.start();  
        start2Thread.start();
        //turno
        turno = new Turno();
        // TODO|
    }

    public synchronized void winTheGame() throws InterruptedException {
        //wait();
        if (jugador1.hePerdido()) {
            Alert asd2 = new Alert(Alert.AlertType.ERROR);
              asd2.setTitle("El ganador es");
              asd2.setContentText("¡¡Jugador 2 gana la partida!!");
              asd2.setHeaderText(null);
              asd2.showAndWait();
            closeGame();            
        } else if (jugador2.hePerdido()){
            Alert asd3 = new Alert(Alert.AlertType.ERROR);
              asd3.setTitle("El ganador es");
              asd3.setContentText("¡¡Jugador 1 gana la partida!!");
              asd3.setHeaderText(null);
              asd3.showAndWait();
            closeGame();            
        }
        //notify();
    }//check who wins
    
    public void closeGame() {
        try {
            serverSocket.close();
            serverSocket2.close();
            clientSocket2.close();
            clientSocket.close();
            startThread=null;
            start2Thread=null;
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(FxGame.class.getName()).log(Level.SEVERE, null, ex);
        }                
    }//close all
    /*PLAYER 1*/
    class StartThreadToRecieve extends Thread {
        @Override
        public void run() {
            try {                   
                serverSocket = new ServerSocket(port);
                //espera al cliente
                clientSocket = serverSocket.accept();
                //start the player
                jugador1 = new Partida();
                jugador1Label.setText("Jugador 1 conectado");
                clion1.setFill(javafx.scene.paint.Color.GREEN);
                player1Playing=true;
                dataInput = new DataInputStream(clientSocket.getInputStream());
                //to send message to client
                outputStream =new DataOutputStream(clientSocket.getOutputStream());                
                //Read from the client                
                while (this.isAlive()) {                                        
                    if (possitionArray<3) {
                        jugador1.cargarBarcos(possitionArray, dataInput.readInt());                    
                        possitionArray++;
                    } else {  
                        synchronized(turno) {
                            if (player2Playing  && turno.isQuienJuega()) {                                                           
                                if ( jugador2.meDisparan (dataInput.readInt()) ) {
                                    player2Label.setText("Tocado\n");
                                    System.out.println("acertaste el disparo al jugador 2"); 
                                    winTheGame();
                                    turno.setQuienJuega(false);
                                    turno.notify();
                                } else {
                                    player2Label.setText("Agua\n");
                                    System.out.println("Fallaste el tiro");
                                    turno.setQuienJuega(false);
                                    turno.notify();
                                }  //read the shoot 
                                
                            }//end sync
                        }//player 2 is playing
                        //possitionArray=0;
                    } //restart the possition array
                }                
                //startSocketServer(port);
            } catch (EOFException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(FxGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(HundirBarcosController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//
        
        public void setPort(int port) {
            port=port;
        }        
        
        public void sendMessage(String message) throws IOException {
            outputStream.writeUTF(message);
        }                
        
    }//thread
    
    class SecondThreadToRecieve extends Thread {
        @Override
        public void run() {
            try {                   
                serverSocket2 = new ServerSocket(port2);
                //espera al cliente
                clientSocket2 = serverSocket2.accept();
                //start player2
                jugador2 = new Partida();
                jugador2Label.setText("Jugador 2 conectado");
                clion2.setFill(javafx.scene.paint.Color.GREEN);
                player2Playing=true;    //enable player 2 to play
                dataInput2 = new DataInputStream(clientSocket2.getInputStream());
                //para enviar al cliente 
                outputStream2 =new DataOutputStream(clientSocket2.getOutputStream());                
                //lee del cliente                
                while (this.isAlive()) {                    
                    //load the boats
                    if (possitionArray2<3) {
                        jugador2.cargarBarcos(possitionArray2, dataInput2.readInt());                                        
                        possitionArray2++;                                                                        
                    } else {
                        synchronized(turno) {
                            while (turno.isQuienJuega()) {
                                dataInput2.readInt();
                                turno.wait();
                            }
                            if (player1Playing && turno.isQuienJuega()==false ) {                            
                                
                                if ( jugador1.meDisparan (dataInput2.readInt()) ) {
                                    player1Label.setText("Tocado\n");
                                    System.out.println("acertaste el disparo al jugador 1");
                                    winTheGame();
                                    turno.setQuienJuega(true);
                                } else {
                                    player1Label.setText("Agua\n");
                                    System.out.println("Fallaste el tiro");
                                    turno.setQuienJuega(true);
                                }  //read the shoot   
                                                                
                                
                            }
                        }//player 2 is playing
                        //possitionArray=0;
                    } //restart the possition array
                }                
                //startSocketServer(port);
            } catch (EOFException ex) {
                System.out.println(ex.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(FxGame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
                Logger.getLogger(FxGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }//
        
        public void setPort(int port) {
            port=port;
        }        
        
        public void sendMessage(String message) throws IOException {
            outputStream.writeUTF(message);
        }                        
    }//end 2nd thread
    
    //to use wait and notify
    
    public void showTheBoats1() {
        int[] boatArray;        
        boatTextArea.appendText("Barcos Jugador 1\n");
        boatArray = jugador1.getArrayBarcos();
        for (int i=0; i<3; i++) {
            boatTextArea.appendText("Posicion: "+boatArray[i]+"\n");                        
        }        
    }//end show the boats
    
    public void showTheBoats2() {   
        int[] boatArray;        
        boatTextArea.appendText("Barcos Jugador 2\n");
        boatArray = jugador2.getArrayBarcos();
        for (int i=0; i<3; i++) {
            boatTextArea.appendText("Posicion: "+boatArray[i]+"\n");
        }
    }//end show the boats
    
}
