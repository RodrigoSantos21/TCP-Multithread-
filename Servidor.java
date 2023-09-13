import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

// Server class
class Server {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args)
    {
        ServerSocket server = null;

        try {

            // servidor conectado na porta 1090
            server = new ServerSocket(1090);
            server.setReuseAddress(true);

            // loop infinito para pegar o pedido do cliente
            while (true) {

                // socket para receber pedidos do cliente
                Socket client = server.accept();

                // mostra a conexão de um novo cliente ao server
                System.out.println("Novo cliente conectado "
                        + client.getInetAddress()
                        .getHostAddress());

                // cria nova thread
                ClientHandler clientSock
                        = new ClientHandler(client);

                // essa thread cuida do cliente separadamente
                new Thread(clientSock).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (server != null) {
                try {
                    server.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private static String hash;
        // Constructor
        public ClientHandler(Socket socket)
        {
            this.clientSocket = socket;
        }

        public void run()
        {
            PrintWriter out = null;
            BufferedReader in = null;
            try {

                // pega o output do cliente
                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                // pega o input do cliente
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    dataInputStream = new DataInputStream(
                            clientSocket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            clientSocket.getOutputStream());
                    // escreve a mensagem recebida do cliente
                    System.out.printf(
                            "Mensagem enviada do cliente: %s\n",
                            line);
                    out.println("Mensagem recebida");

                    if(line.equalsIgnoreCase("Arquivo")) {
                        System.out.println(
                                "Enviando arquivo para o cliente...");
                        // Call SendFile Method
                        sendFile(
                                "C:/Users/Rodrigo/Desktop/txtRedes.txt");
                        out.println("Servidor enviou o arquivo");
                    }

                    if(line.equalsIgnoreCase("hash")) {
                        //System.out.println("Chegou aqui e o hash é:" + hash);
                        out.println(hash);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (out != null) {
                        out.close();

                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
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
            hash = stringHexa(Objects.requireNonNull(gerarHash(Arrays.toString(buffer), "SHA-256")));
            fileInputStream.close();
        }


        public static byte[] gerarHash(String frase, String algoritmo) {
            try {
                MessageDigest md = MessageDigest.getInstance(algoritmo);
                md.update(frase.getBytes());
                return md.digest();
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }

        private static String stringHexa(byte[] bytes) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                int parteAlta = ((bytes[i] >> 4) & 0xf) << 4;
                int parteBaixa = bytes[i] & 0xf;
                if (parteAlta == 0) s.append('0');
                s.append(Integer.toHexString(parteAlta | parteBaixa));
            }
            return s.toString();
        }
    }
}
