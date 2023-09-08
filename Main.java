import java.io.*;
import java.net.*;
import java.util.*;

class Client {


    private static DataOutput dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        // Estabelecendo conexão com host e a porta
        try (Socket socket = new Socket("localhost", 1090)) {
            // escrevendo pro servidor
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            // lendo do servidor
            BufferedReader in
                    = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));


            Scanner sc = new Scanner(System.in);
            String line = null;

            while (!"exit".equalsIgnoreCase(line)) {
                dataInputStream = new DataInputStream(
                        socket.getInputStream());
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                // lendo do usuário
                line = sc.nextLine();

                // envia o input do usuário pro servidor
                out.println(line);
                out.flush();

                // mostra a resposta do servidor
                System.out.println("Servidor respondeu "
                        + in.readLine());

                if(line.equalsIgnoreCase("enviar")) {
                    System.out.println(
                            "Sending the File to the Server");
                    // Call SendFile Method
                    sendFile(
                            "C:/Users/Rodrigo/Desktop/txtRedes.txt");
                }
            }


            sc.close();
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendFile(String path)
            throws Exception
    {
        int bytes = 0;
        // Open the File where he located in your pc
        File file = new File(path);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        dataOutputStream.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            dataOutputStream.write(buffer, 0, bytes);
        }
        // close the file here
        fileInputStream.close();
    }

}