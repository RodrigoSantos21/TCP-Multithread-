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
        private static long tamanho;
        private static String[] nome;
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
                    out.println("Mensagem recebida");

                    nome = line.split(" ", 2);
                    if(line.contains("Arquivo")) {
                        System.out.println(
                                "Enviando arquivo para o cliente...");
                        // Call SendFile Method
                        sendFile(
                                "C:/Users/Rodrigo/Desktop/" + nome[1]);
                        out.println("Arquivo: " + nome[1] + ", Com o hash: " + hash + ", Tamanho: " + tamanho + " bytes");
                    }

                    if(line.contains("CRC")){
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


            dataOutputStream.writeLong(file.length());
            tamanho = file.length();
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer))
                    != -1) {

                dataOutputStream.write(buffer, 0, bytes);

            }

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
