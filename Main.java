import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class Client {


    private static DataOutput dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static String hash;

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
            String nomeArquivo = null;
            String hashOriginal = null;

            while (!"exit".equalsIgnoreCase(line)) {
                dataInputStream = new DataInputStream(
                        socket.getInputStream());
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                // lendo do usuário
                line = sc.nextLine();

                // envia o input do usuário pro servidor
                if(line.equalsIgnoreCase("Arquivo")) {
                    System.out.println("Digite o nome do arquivo da área de trabalho:");
                    nomeArquivo = sc.nextLine();
                }
                out.println(line + " " + nomeArquivo);
                out.flush();

                // mostra a resposta do servidor
                System.out.println("Servidor respondeu "
                        + in.readLine());

                if(line.equalsIgnoreCase("Arquivo")) {
                    String a = in.readLine();
                if(a.equalsIgnoreCase("true")){
                        receiveFile("nomeArquivo.txt");
                        System.out.println(in.readLine());
                    }
                    else{
                        System.out.println(a);
                    }
                }

                if(line.equalsIgnoreCase("CRC")){
                    out.println("CRC");
                    hashOriginal = in.readLine();
                    if(hashOriginal.equalsIgnoreCase(hash)){
                        System.out.println("Check sum OK");
                    }
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
        hash = stringHexa(Objects.requireNonNull(gerarHash(Arrays.toString(buffer), "SHA-256")));
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
