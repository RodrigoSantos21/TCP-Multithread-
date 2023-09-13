import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

                if(line.equalsIgnoreCase("Arquivo")) {
                    receiveFile("NewFile1.txt");
                    System.out.println(in.readLine());
                }

                if(line.equalsIgnoreCase("hash")) {
                    System.out.println("Hash do arquivo é: "
                            + in.readLine());
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

    private static void receiveFile(String fileName)
            throws Exception
    {
        int bytes = 0;
        FileOutputStream fileOutputStream
                = new FileOutputStream(fileName);

        long size
                = dataInputStream.readLong(); // read file size
        byte[] buffer = new byte[4 * 1024];
        while (size > 0
                && (bytes = dataInputStream.read(
                buffer, 0,
                (int)Math.min(buffer.length, size)))
                != -1) {
            // Here we write the file using write method
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }

        // Here we received file
        //System.out.println("O arquivo foi recebido");
        fileOutputStream.close();
    }

}
