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

                    // escreve a mensagem recebida do cliente
                    System.out.printf(
                            "Mensagem enviada do cliente: %s\n",
                            line);
                    out.println("Mensagem recebida");
                    if(line.equalsIgnoreCase("enviar")) {
                        dataInputStream = new DataInputStream(
                                clientSocket.getInputStream());
                        dataOutputStream = new DataOutputStream(
                                clientSocket.getOutputStream());
                        // Here we call receiveFile define new for that
                        // file
                        receiveFile("NewFile1.txt");
                        out.println("Arquivo recebido");
                    }

                    if(line.equalsIgnoreCase("hash")) {
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
                        dataInputStream.close();
                        dataOutputStream.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
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
            hash = stringHexa(Objects.requireNonNull(gerarHash(Arrays.toString(buffer), "SHA-256")));
            // Here we received file
            System.out.println("O arquivo foi recebido");
            fileOutputStream.close();
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
