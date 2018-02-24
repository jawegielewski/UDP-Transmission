package klient;

import java.io.*;                   //biblioteka umożliwiająca operacje na plikach
import java.net.*;                  //biblioteka do oblsugi komunikacji sieciowej
import java.util.*;                 //biblioteka zawierająca klase Scanner

public final class Klient 
{
    private FileInputStream stream;
    private FileOutputStream wyjscie;
    private File plik;
    private int rozmiar, port, dzialanie, licz = 0;        //port - port na którym dzialamy; działanie - wysyłanie/pobieranie; licznik - zlicza datagramy przychodzące;
    private String  odczyt_ip_string, nazwa_pliku, sciezka;
    Scanner odczyt_sciezki;


    
    public Klient() throws InterruptedException, IOException 
    {
                         
        System.out.println("::: KLIENT :::\n\nProsze wprowadzic nr portu [1-65535]: ");
        Scanner odczyt_portu = new Scanner(System.in);            //wprowadzenie numeru portu z klawiatury         
        do {
            while(!odczyt_portu.hasNextInt())            //wykonuje pętlę do momentu, gdy wprowadzona zostanie liczba
            {  System.out.println("Niepoprawna wartosc!"); odczyt_portu.next(); }
            port = odczyt_portu.nextInt();           //odczyt wprowadzonego numeru portu
        }  while (port <= 0 || port > 65535);          //dopóki nr portu nie mieści sie w zakresie
    
        
        System.out.println("Prosze wybrac dzialanie: wysylanie/odbieranie [1/2]");
        Scanner odczyt_dzialania = new Scanner(System.in);            //okreslenie dzialania (wysylanie/odbieranie) przyciskiem z klawiatury
        do {
            while(!odczyt_dzialania.hasNextInt())            //wykonuje pętlę do momentu, gdy wprowadzona zostanie liczba
            {  System.out.println("Niepoprawna wartosc!"); odczyt_dzialania.next(); }
            dzialanie = odczyt_dzialania.nextInt();           //odczyt wprowadzonego dzialania
        } while (dzialanie < 1 || dzialanie > 2);          //dopóki działanie jest różne od 1 lub 2
                        
        
        System.out.println("Prosze okreslic adres IP serwera:");
        Scanner odczyt_ip = new Scanner(System.in);            //okreslenie dzialania (wysylanie/odbieranie) przyciskiem z klawiatury
        odczyt_ip_string = odczyt_ip.nextLine();
        InetAddress adres = InetAddress.getByName(odczyt_ip_string);     //ustalanie adresu IP komputera lokalnego        
        DatagramSocket socket= new DatagramSocket();
       
        
        
        
        
        
        switch(dzialanie)
        {
            case 1:                                     //=:=:=:=:=:=:=:=:=:=:=:=.WYSYLANIE NA SERWER.=:=:=:=:=:=:=:=:=:=:=:=
                otworzPlik();                       //przywołanie funkcji otworzPlik()
                                //utworzenie nowego gniazda datagramowego
                Thread.sleep(1000);
                System.out.println("Rozmiar: " +rozmiar+ " bajtow");
                
                byte[] bufor = new byte[1];                 //tablica przechowujaca 1 bajt danych wyjsciowych
                for (int i =1; i<=rozmiar; i++)                   //petla wysylajaca datagramy
                {

                        stream.read(bufor);                                 //wczytywanie danych z pliku do bufora
                        DatagramPacket packet = new DatagramPacket(bufor, bufor.length, adres, port );      //tworzenie datagramu wyjściowego
                        
                        if(i==1)                                              //jeśli początek wysyłania na serwer
                        {
                            byte[] bufor2 = "na_serwer_INIT".getBytes();      //tab. przechowujaca "inicjalizacje" wysyłania na serwer
                            socket.send(new DatagramPacket(bufor2, bufor2.length, adres, port ));   //wysylanie "init" 
                            Thread.sleep(3000);
                            byte[] bufor3 = sciezka.getBytes();
                            socket.send(new DatagramPacket(bufor3, bufor3.length, adres, port ));    
                            System.out.println("\nRozpoczynanie wysylania!");

                        }
                        socket.send(packet);                                        //wysylanie datagramu wlasciwego
                        System.out.println("Wyslano datagram "+i + "/" +rozmiar );
                        Thread.sleep(1);      //IM WIEKSZE, TYM MNIEJSZE STRATY
                           
                        if(i==rozmiar)                                      //jeśli zakończenie wysylania na serwer
                        {
                            byte[] bufor4 = "na_serwer_OK".getBytes();        //tab. przechowujaca "zakonczenie" wysyłania na serwer
                            socket.send(new DatagramPacket(bufor4, bufor4.length, adres, port ));   //wysyłanie zakończenia
                            System.out.println("\nWyslano na serwer caly plik!");
                        }
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                br.readLine();
                break;
                
                
                
                
                
                
            case 2:                                                     //=:=:=:=:=:=:=:=:=:=:=:=.POBIERANIE Z SERWERA .=:=:=:=:=:=:=:=:=:=:=:=

                System.out.println("Prosze podac lokalizacje pliku [sciezka bez spacji]:");
                Scanner odczyt_sciezki = new Scanner(System.in);
            
                sciezka = odczyt_sciezki.next();
            
                plik = new File(sciezka);
                //stream = new FileInputStream(plik);                     //ładowanie pliku
                //rozmiar = stream.available();                             //sprawdzenie rozmiaru pliku


            
                
                
                byte[] bufor5 = "wyslij_plik".getBytes();                //tablica przechowujaca żądanie  
                socket.send(new DatagramPacket(bufor5, bufor5.length, adres, port ));        //wyslanie żądania pobrania pliku
                Thread.sleep(3000);
                byte[] bufor6 = sciezka.getBytes();
                socket.send(new DatagramPacket(bufor6, bufor6.length, adres, port ));
                
                System.out.println("\nWyslano zadanie pobrania pliku!");
                
                
                
                while(true)
                {
                    byte[] bufor7 = new byte[100];              //tablica na dane wejściowe
                
                    DatagramPacket packet = new DatagramPacket(bufor7, bufor7.length);        //ustawianie datagramu wejściowego
                    socket.receive(packet);                         //odbieranie datagramow
                    String odpowiedz = new String(packet.getData()).trim();        //zmienna "odpowiedz" typu tekstowego przechowuje polecenia serwera
                
                    if(odpowiedz.equals("do_klienta_INIT"))       //jeśli serwer zamierza wysyłać datagramy do klienta
                    {
                        System.out.println("aaa");
                        nazwa_pliku = plik.getName();
                        wyjscie = new FileOutputStream(System.getProperty("user.home")+File.separator+nazwa_pliku);
                        
                        System.out.println("\nRozpoczynanie pobierania z serwera: " +nazwa_pliku);
                        
                    }
                    
                    //jeśli odbieramy pakiety właściwe 
                    if(!odpowiedz.equals("do_klienta_OK")&&!odpowiedz.equals("do_klienta_INIT"))    //jeśli odbieramy pakiety właściwe
                    {
                        licz++;                                  //zliczanie datagramów przychodzących
                        System.out.println("Odebrano datagram "+"["+licz+"]");
                        wyjscie.write(packet.getData(), packet.getOffset(), packet.getLength());  
                    }
                                        
                    if(odpowiedz.equals("do_klienta_OK")) //jeśli serwer zakończył wysyłać datagramy
                    {
                        System.out.println("\nOdebrano plik! [lokalizacja: "+System.getProperty("user.home")+File.separator+nazwa_pliku+"]");
                        wyjscie.close();
                    }      
                } 
        }     
    }
    
    public void otworzPlik() throws FileNotFoundException, IOException
    {

        System.out.println("Prosze podac lokalizacje pliku [sciezka bez spacji]:" );
        odczyt_sciezki = new Scanner(System.in);
        sciezka = odczyt_sciezki.next();
        plik = new File(sciezka);
        stream = new FileInputStream(plik);                     //ładowanie pliku
        rozmiar = stream.available();                             //sprawdzenie rozmiaru pliku
        System.out.println("\nZaladowano plik do wyslania na serwer!");
        
    }
    public static void main(String args[]) throws InterruptedException, IOException 
    {
        new Klient();
    }
}