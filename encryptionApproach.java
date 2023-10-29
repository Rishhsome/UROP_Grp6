import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class encryptionApproach {

    private static final int NUM_EDGE_SERVERS = 8;
    private static final int NUM_CLOUD_SERVERS = 3;

    private static List<MobileDevice> mobileDevices = new ArrayList<>();
    private static List<EdgeServer> edgeServers = new ArrayList<>();
    private static List<CloudServer> cloudServers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Create the mobile devices
        for (int i = 0; i < 20; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 100) + 1;
            int downloadSpeed = (int) (Math.random() * 50) + 1;
            int uploadSpeed = (int) (Math.random() * 10) + 1;
            int dataSentSize = (int) (Math.random() * 1000) + 1;
            long mn = 0;
            for (int m = 0; m < 10; m++)
                mn = mn * 10 + (long) (Math.random() * 10);
            long mobileNumber = mn;

            mobileDevices
                    .add(new MobileDevice(dataProcessingSpeed, downloadSpeed, uploadSpeed, dataSentSize, mobileNumber));
        }

        // Create the edge servers
        for (int i = 0; i < NUM_EDGE_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 1000) + 1;
            int downloadSpeed = (int) (Math.random() * 500) + 1;
            int uploadSpeed = (int) (Math.random() * 100) + 1;

            // Get the mobile number of any random mobile device and use it as the key for a
            // particular edge server
            int randDevice = (int) (Math.random() * 20);
            long mobileNumber = mobileDevices.get(randDevice).getMobileNumber();

            edgeServers.add(new EdgeServer(dataProcessingSpeed, downloadSpeed, uploadSpeed,
                    new AES_Encryption256(String.valueOf(mobileNumber))));
        }

        // Create the cloud servers
        for (int i = 0; i < NUM_CLOUD_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 10000) + 1;
            String hostname = "127.0.0.1";
            int port = 8080 + i;

            // Similar process for
            int randDevice = (int) (Math.random() * 20);
            long mobileNumber = mobileDevices.get(randDevice).getMobileNumber();

            cloudServers.add(new CloudServer(dataProcessingSpeed, hostname, port,
                    new AES_Decryption256(String.valueOf(mobileNumber))));
        }

        // Create a thread pool to handle the mobile devices
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_EDGE_SERVERS);

        for (MobileDevice mobileDevice : mobileDevices) {
            executorService.submit(() -> {
                EdgeServer edgeServer = findNearestEdgeServer(mobileDevice);

                int dataProcessingSpeed = mobileDevice.getDataProcessingSpeed();
                int downloadSpeed = mobileDevice.getDownloadSpeed();
                int uploadSpeed = mobileDevice.getUploadSpeed();
                int dataSentSize = mobileDevice.getDataSentSize();

                CloudServer cloudServer = findMostEfficientCloudServer(dataProcessingSpeed, downloadSpeed, uploadSpeed,
                        dataSentSize);
                System.out.println(
                        "Sending data for Mobile Device " + mobileDevice.getMobileNumber() + " to Edge Server...");

                // Generate a random string for this mobile device
                String randomString = generateRandomString();
                System.out.println(
                        "Random String for Mobile Device " + mobileDevice.getMobileNumber() + ": " + randomString);

                edgeServer.sendDataToCloudServer(cloudServer, mobileDevice, randomString);

                // Process data received from the cloud server
                System.out.println("Processing data for Mobile Device " + mobileDevice.getMobileNumber()
                        + " received from the cloud server...");
                cloudServer.processDataFromCloudServer(cloudServer, mobileDevice);
            });
        }

        executorService.shutdown();
    }

    private static EdgeServer findNearestEdgeServer(MobileDevice mobileDevice) {
        return edgeServers.get(0);
    }

    private static CloudServer findMostEfficientCloudServer(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed,
            int dataSentSize) {
        return cloudServers.get(0);
    }

    public static class MobileDevice {

        private int dataProcessingSpeed;
        private int downloadSpeed;
        private int uploadSpeed;
        private int dataSentSize;
        private long mobileNumber;

        public MobileDevice(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed, int dataSentSize,
                long mobileNumber) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.dataSentSize = dataSentSize;
            this.mobileNumber = mobileNumber;
        }

        public int getDataProcessingSpeed() {
            return dataProcessingSpeed;
        }

        public int getDownloadSpeed() {
            return downloadSpeed;
        }

        public int getUploadSpeed() {
            return uploadSpeed;
        }

        public int getDataSentSize() {
            return dataSentSize;
        }

        public long getMobileNumber() {
            return mobileNumber;
        }
    }

    public static class EdgeServer {

        private int dataProcessingSpeed;
        private int downloadSpeed;
        private int uploadSpeed;

        private AES_Encryption256 encryption;

        public EdgeServer(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed, AES_Encryption256 encryption) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.encryption = encryption;
        }

        public int getDataProcessingSpeed() {
            return dataProcessingSpeed;
        }

        public int getDownloadSpeed() {
            return downloadSpeed;
        }

        public int getUploadSpeed() {
            return uploadSpeed;
        }

        public void sendDataToCloudServer(CloudServer cloudServer, MobileDevice mobileDevice, String randomString) {
            try (Socket socket = new Socket(cloudServer.getHostname(), cloudServer.getPort())) {
                // Get the input and output streams
                try (InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream()) {
                    // Encrypt the random string
                    String encryptedData = encryptData(randomString);

                    // Print the encrypted string
                    System.out.println("Encrypted String for Mobile Device " + mobileDevice.getMobileNumber() + ": "
                            + encryptedData);

                    // Write the encrypted data to the cloud server
                    outputStream.write(encryptedData.getBytes(StandardCharsets.UTF_8));
                    System.out.println(
                            "Data sent successfully for Mobile Device " + mobileDevice.getMobileNumber() + ".");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add a method to encrypt data
        private String encryptData(String data) {
            return encryption.encrypt(data);
        }
    }

    public static class CloudServer {

        private int dataProcessingSpeed;
        private String hostname;
        private int port;

        private AES_Decryption256 decryption;

        public CloudServer(int dataProcessingSpeed, String hostname, int port, AES_Decryption256 decryption) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.hostname = hostname;
            this.port = port;
            this.decryption = decryption;
        }

        public int getDataProcessingSpeed() {
            return dataProcessingSpeed;
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public void processDataFromCloudServer(CloudServer cloudServer, MobileDevice mobileDevice) {
            try (Socket socket = new Socket(cloudServer.getHostname(), cloudServer.getPort())) {
                // Read the data from the edge server
                byte[] data = new byte[1024];
                int bytesRead;
                try (InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream()) {
                    while ((bytesRead = inputStream.read(data)) != -1) {
                        // Decrypt the data using the provided decryption object
                        String dataToDecrypt = new String(data, 0, bytesRead, StandardCharsets.UTF_8);
                        String decryptedData = decryptData(dataToDecrypt);

                        // Process the decrypted data as needed
                        // For example, you can write it to another stream or perform other operations.
                        outputStream.write(decryptedData.getBytes(StandardCharsets.UTF_8));
                    }
                    System.out.println(
                            "Data processed successfully for mobile device : " + +mobileDevice.getMobileNumber());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add a method to decrypt data
        private String decryptData(String data) {
            return decryption.decrypt(data);
        }
    }

    static class AES_Encryption256 {
        private String SECRET_KEY;
        private static final String SALTVALUE = "abcdefg";

        AES_Encryption256(String SecretKey) {
            this.SECRET_KEY = SecretKey;
        }

        public String encrypt(String strToEncrypt) {
            try {
                byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
                byte[] encryptedData = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(encryptedData);
            } catch (Exception e) {
                System.out.println("Error occurred during encryption: " + e.toString());
            }
            return null;
        }
    }

    static class AES_Decryption256 {
        private String SECRET_KEY;
        private static final String SALTVALUE = "abcdefg";

        AES_Decryption256(String SecretKey) {
            this.SECRET_KEY = SecretKey;
        }

        public String decrypt(String strToDecrypt) {
            try {
                byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
                SecretKey tmp = factory.generateSecret(spec);
                SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
                byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
                return new String(decryptedData, StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println("Error occurred during decryption: " + e.toString());
            }
            return null;
        }
    }

    private static String generateRandomString() {
        StringBuilder randomString = new StringBuilder();
        int length = 10; // Change the length as needed
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }
}
