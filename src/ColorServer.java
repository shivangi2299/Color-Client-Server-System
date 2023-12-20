/*
Name : Shivangi Patel
Date: 2023-03-06 (YYYY-MM-DD)
Java Version: 20
*

Color Server is an implementation of socket Programming in java.
Socket Programming is a two machine communications over a channel using socket. Here, a server(a socket) listens for clients(another socket) to get connected on a particlar port at an IP continously.
Here, Color Server continously listens for the clients to connect with the server for process on a particular port at an IP. No more than 6 client connect to the server at the same time. After, connection each process are spawn to ColorWorker
for further. ColorClient sends a color to server and server responds with a random color.
The Color/Server program uses statesless protocol.
There a concept of Multithreading used by ColorWorker class. There are thousands of simultaneous client threads running to process the client request.

----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Run the program:
Use multiple terimal for server and clients to experience client and server communication
Execute javac a command to read source file and compile it.
Terminal 1:
>javac ColorServer
>java ColorServer

Termial 2:
>java ColorClient

Terminal 3:
>java ColorClient

Run program on internet:
>java ColorServer
>java ColorClient IP  [USE ACTUAL IP INSTEAD OF IP]

--------------------------------------------------------------------------------------------------------------------------------
Thanks:

https://www.comrevo.com/2019/07/Sending-objects-over-sockets-Java-example-How-to-send-serialized-object-over-network-in-Java.html (Code dated 2019-07-09, by Ramesh)
https://rollbar.com/blog/java-socketexception/#
Also: Hughes, Shoffner and Winslow for Inet code.

Took reference and help from other resources:
https://www.youtube.com/playlist?list=PLn1qdyDdgpgMrZO_cKiSPuVLhEOsob9o3
https://www.geeksforgeeks.org/socket-programming-in-java/
https://www.javatpoint.com/socket-programming
https://www.tutorialspoint.com/java/java_networking.html

---------------------------------------------------------------------------------------------------------------------------------
* */


//Import essential libraries

import java.awt.*;
import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.Scanner;

/**
 * Class ColorClient establish client connection with ColorServer and send color and username to server
 */
class ColorClient{
    private static int clientColorCount=0;
    public static void main(String[] args){  // Main method to invoke the clientjop
        // program
        ColorClient cc=new ColorClient(args);
        cc.run(args);
    }

    /**
     * ColorClient constructor to initialize properties of ColorClient at runtime
     * @param args
     */
    public ColorClient(String[] args){    // Parameterized Constructor with one argument
        System.out.println("\n This a constructor if you want to use it");
    }
    /**
     **/



    /**
     * Run method makes connection with server by taking serverName from user through command line and
     * @param args Take arguments form user through command-line
     */
    public void run(String[] args){  // Run method with void return type
        String serverName;             //local variable serverName to store IP address or serverName
        if(args.length<1){              //Condition to check whether we have any data passed in the array to connect server using serverName or IP address
            serverName="localhost";     //ByDefault connect to localHost if no args provided
        }
        else{
            serverName=args[0];        //Else Connect to provided serverName or IP passed in args array
        }

        String colorFromClient="";
        Scanner sc=new Scanner(System.in);  // Taking data from client(user) using
        System.out.println("Enter your Name :");
        System.out.flush();
        String userName=sc.nextLine();   //Storing username given by the client
        System.out.println("Hi "+ userName);
        // Run loop until client quits
        do{
            System.out.println("Enter color to proceed, quit to end");
            colorFromClient = sc.nextLine();
            if(colorFromClient.indexOf("quit")<0){  // If quit is not entered by the client
                getColor(userName, colorFromClient, serverName);
            }
        }while(colorFromClient.indexOf("quit")<0);
        System.out.println("Cancelled by user request ");
        System.out.println(userName+" You sent and received "+clientColorCount+" colors."); // Displays count of colors received by client

    }

    /**
     * getColor() Pass the color in Serialized manner and Deserialized the object sent from server
     * @param userName Username of Client
     * @param colorFromClient Color Entered by client inorder to get random color from the server
     * @param serverName Address on which Server is hosted
     */
    void getColor(String userName, String colorFromClient,String serverName) {
        try {
            ColorData cd = new ColorData(); //Compile colorServer primarily as definitions is in their.
            cd.userName = userName;
            cd.colorSent = colorFromClient;
            cd.colorCount = clientColorCount;
            // Socket socket = new Socket("JUNKhost", 45565); // Exception is explained below.
            Socket socket = new Socket(serverName, 45565);
            System.out.println("\nWe have successfully connected to the ColorServer at port 45,565");

            OutputStream opStream = socket.getOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(opStream); //Serializing object
            objectOS.writeObject(cd);  //Passing the serialized ColorData object over the channel to server
            System.out.println("We have sent the serialized values to the ColorServer's server socket");

            InputStream inStream = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inStream);
            //Type casting of Deserialized object into ColorData object
            ColorData InObject = (ColorData) ois.readObject(); //ColorData has client details and history. This call provides access to whole ColorData object.



            //Remember : Maintain the last state of conversation as we break connection after each request.
            clientColorCount = InObject.colorCount; // Storing count in class variable.

            System.out.println("\nFROM THE SERVER:");
            System.out.println(InObject.messageToClient);
            System.out.println("The color sent back is: " + InObject.colorSentBack);
            System.out.println("The color count is: " + InObject.colorCount + "\n");
            System.out.println("Closing the connection to the server.\n");
            socket.close();

        } catch (ConnectException CE){
            System.out.println("\nOh no. The ColorServer refused our connection! Is it running? \n");
            CE.printStackTrace();
        } catch (UnknownHostException UH){
            System.out.println("\nUnknown Host problem.\n"); //Host Not determined
            UH.printStackTrace();
        } catch(ClassNotFoundException CNF){//Class not found on the path or file
            CNF.printStackTrace();
        } catch (IOException IOE){
            IOE.printStackTrace(); // Stack trace will describe problem on console
        }
    }

    }

/**
 * Class ColorData represents data object for sending and receiving color during client-server communication
 * It also implements interface Serializable to send data objects in form of bytes
 */
class ColorData implements Serializable{ //ColorData should be serializable in order to send each bit after one another over the Network.
    String userName;
    String colorSent;
    String colorSentBack;
    String messageToClient;
    int colorCount;
}
/**
 * Class ColorWorker extends Thread which handles communication with single client
 * It receives color from the client, processes it, generates a random color, and sends the modified color data back to the client.
 * Multiple threads running simultaneously (Multithreading concept).
 */
class ColorWorker extends Thread{  //Class for ColorWorker thread which executes concurrently for many of the clients
    Socket sock;     //local variable of current class. It is a reference variable.
    /**
     * Constructor to initialize socket
     * @param s
     */
    ColorWorker(Socket s){ //Parameterized Constructor
        sock=s;         // Assigning Parameter argument to local variable
    }
    /**
     * Overrides run() method from Thread Class and Deserialize the input into colorData Object and prints details of ColorData Object
     */
    public void run(){
        try{
            //Generating Input/output streams inorder to communicate with the sockets
            //Use Input Stream read in from the socket ,then deserialize the client object
            InputStream ipstream=sock.getInputStream();
            ObjectInputStream objIS=new ObjectInputStream(ipstream);

            ColorData InObject=(ColorData) objIS.readObject();  //ColorData has client details. This call provides access to ColorData object.
            OutputStream opstream=sock.getOutputStream();
            ObjectOutputStream objOP=new ObjectOutputStream(opstream);

            System.out.println("\nFROM THE CLIENT: \n");
            System.out.println("Username: " + InObject.userName);
            System.out.println("Color sent from the client: " + InObject.colorSent);
            System.out.println("Connections count (State!): " + (InObject.colorCount + 1));

            InObject.colorSentBack = getRandomColor();
            InObject.colorCount++;
            InObject.messageToClient =
                    String.format("Thanks %s for sending the color %s", InObject.userName, InObject.colorSent);
            objOP.writeObject(InObject); // Passing random color back to client

            System.out.println("Closing the client socket connection...");
            sock.close();

        }catch(ClassNotFoundException CNF){
            CNF.printStackTrace();  //Class not found on path
        } catch (IOException x){
            System.out.println("Server error.");
            x.printStackTrace();
    }
}
    /**
     * Method generates random color and send it to the client
     * @return String of random color
     */
    String getRandomColor(){   //Class defined to randomly selecting color when client asks.
        String[] colorArray = new String[]
                {
                        "Red", "Blue", "Green", "Yellow", "Magenta", "Silver", "Aqua", "Gray", "Peach", "Orange"
                };

        int randomArrayIndex = (int) (Math.random() * colorArray.length);
        return (colorArray[randomArrayIndex]);
    }
}
/**
 * Class ColorServer listen to client connection
 */
public class ColorServer {

    public static void main(String args[])throws Exception {
    int q_len=6 ; // Maximum simultaneous(exactly same time) requests of clients to connect the server before giving it away to ColorWorker thread
    int serverPort=45565;
    Socket sock;                  //reference variable of class Socket

     System.out.println("Shivangi Patel's Color Server 1.0 starting up, listening at port " + serverPort + ".\n");

     // Looking for the connections at the serverPort from the clients.
        ServerSocket ss=new ServerSocket(serverPort,q_len);  //Doorbell socket continuously looking for new connections
        System.out.println("ServerSocking is waiting for new client connections");   //Inviting connections as server is all set to connect

        // Loop runs until it is true(forever)
        //Press ctrl+ C inorder to manually terminate the loop
       while(true){     //runs until quit or stopped manually
           sock= ss.accept();  // Client is trying to connect. Accept the connection. Ding Dong!
           //Displaying the server port given for the client connection, also the next available port
           System.out.println("Connection from " + sock);
           new ColorWorker(sock).start(); //Invokes a new Client worker thread . Passed the process. Again looking for new connections.
       }
    }
}



//Outputs
//----------------------------------------------------------------------
/*Server :Terminal 1


> javac ColorServer.java
> java ColorServer
Shivangi Patel's Color Server 1.0 starting up, listening at port 45565.

ServerSocking is waiting for new client connections
Connection from Socket[addr=/127.0.0.1,port=49234,localport=45565]

FROM THE CLIENT:

Username: Shivangi
Color sent from the client: blue
Connections count (State!): 1
Closing the client socket connection...
Connection from Socket[addr=/127.0.0.1,port=49235,localport=45565]

FROM THE CLIENT:

Username: Shivangi
Color sent from the client: red
Connections count (State!): 2
Closing the client socket connection...
Connection from Socket[addr=/127.0.0.1,port=49240,localport=45565]

FROM THE CLIENT:

Username: Kanki
Color sent from the client: purple
Connections count (State!): 1
Closing the client socket connection...
Connection from Socket[addr=/127.0.0.1,port=49241,localport=45565]

FROM THE CLIENT:

Username: Kanki
Color sent from the client: blue
Connections count (State!): 2
Closing the client socket connection...


* */

/*---------------------------------------------------------------------------------------------
Client 1:Terminal 2:

> java ColorClient

 This a constructor if you want to use it
Enter your Name :
Shivangi
Hi Shivangi
Enter color to proceed, quit to end
blue

We have successfully connected to the ColorServer at port 45,565
We have sent the serialized values to the ColorServer's server socket

FROM THE SERVER:
Thanks Shivangi for sending the color blue
The color sent back is: Blue
The color count is: 1

Closing the connection to the server.

Enter color to proceed, quit to end
red

We have successfully connected to the ColorServer at port 45,565
We have sent the serialized values to the ColorServer's server socket

FROM THE SERVER:
Thanks Shivangi for sending the color red
The color sent back is: Peach
The color count is: 2

Closing the connection to the server.

Enter color to proceed, quit to end
quit
Cancelled by user request
Shivangi You sent and received 2 colors.

* */
 /*---------------------------------------------------------------------------------
 Client 2:Terminal 3

> java ColorClient

 This a constructor if you want to use it
Enter your Name :
Kanki
Hi Kanki
Enter color to proceed, quit to end
purple

We have successfully connected to the ColorServer at port 45,565
We have sent the serialized values to the ColorServer's server socket

FROM THE SERVER:
Thanks Kanki for sending the color purple
The color sent back is: Magenta
The color count is: 1

Closing the connection to the server.

Enter color to proceed, quit to end
blue

We have successfully connected to the ColorServer at port 45,565
We have sent the serialized values to the ColorServer's server socket

FROM THE SERVER:
Thanks Kanki for sending the color blue
The color sent back is: Blue
The color count is: 2

Closing the connection to the server.

Enter color to proceed, quit to end


         */

/*
MY D2L COLORSERVER DISCUSSION FORUM POSTINGS:

My Thread:
1)How to run TLL plagiarism checker
Hi All,

I know I am very late, but can anybody help me in how to run TLL plagiarism checker to run the report. Let me know if I am missing something.

2) My response to my Thread
Thank you Abhenai, I was confused how to check it.

3)	Learning Java Socket Programming  by Tauseef Mohiuddin Mohammed
My reply:Thanks for the resource. I did go through this and I find it very helpful in clearing my doubts.

4)Client Sending Random colors by Tirumala Vamsi Abhishek Kuppili
My reply: I am also facing same difficulty when I am entering random letter instead of the color, I am still getting a color back and it is counted.
* */