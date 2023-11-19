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

import java.io.FileWriter;
import java.io.PrintWriter;

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
            int xCoord = (int) (Math.random() * 100) + 1;
            int yCoord = (int) (Math.random() * 100) + 1;
            long mn = 0;
            for (int m = 0; m < 10; m++)
                mn = mn * 10 + (long) (Math.random() * 10);
            long mobileNumber = mn;

            mobileDevices
                    .add(new MobileDevice(dataProcessingSpeed, downloadSpeed, uploadSpeed, dataSentSize, mobileNumber,
                            xCoord, yCoord));
        }

        // Create the edge servers
        for (int i = 0; i < NUM_EDGE_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 1000) + 1;
            int downloadSpeed = (int) (Math.random() * 500) + 1;
            int uploadSpeed = (int) (Math.random() * 100) + 1;
            int xCoord = (int) (Math.random() * 100) + 1;
            int yCoord = (int) (Math.random() * 100) + 1;

            // Get the mobile number of any random mobile device and use it as the key for a
            // particular edge server
            int randDevice = (int) (Math.random() * 20);
            long mobileNumber = mobileDevices.get(randDevice).getMobileNumber();

            edgeServers.add(new EdgeServer(dataProcessingSpeed, downloadSpeed, uploadSpeed, xCoord, yCoord,
                    new AES_Encryption256(String.valueOf(mobileNumber))));
        }

        // Create the cloud servers
        for (int i = 0; i < NUM_CLOUD_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 10000) + 1;
            int downloadSpeed = (int) (Math.random() * 500) + 1;
            int uploadSpeed = (int) (Math.random() * 100) + 1;
            String hostname = "127.0.0.1";
            int port = 8080;

            // Similar process for
            int randDevice = (int) (Math.random() * 20);
            long mobileNumber = mobileDevices.get(randDevice).getMobileNumber();

            cloudServers.add(new CloudServer(dataProcessingSpeed, hostname, port, downloadSpeed, uploadSpeed,
                    new AES_Decryption256(String.valueOf(mobileNumber))));
        }

        saveMobileDeviceDataToCSV("Mobile_devices/mobile_device_data(20).csv", mobileDevices);
        saveEdgeServerDataToCSV("Edge_servers/edge_server_data(8).csv", edgeServers);
        saveCloudServerDataToCSV("Cloud_servers/cloud_server_data(3).csv", cloudServers);

        // Creating a thread pool to handle the mobile devices

        ExecutorService executorService = Executors.newFixedThreadPool(NUM_EDGE_SERVERS);

        for (MobileDevice mobileDevice : mobileDevices) {
            executorService.submit(() -> {
                EdgeServer edgeServer = findNearestEdgeServer(mobileDevice);

                int dataProcessingSpeed = mobileDevice.getDataProcessingSpeed();
                int downloadSpeed = mobileDevice.getDownloadSpeed();
                int uploadSpeed = mobileDevice.getUploadSpeed();
                int dataSentSize = mobileDevice.getDataSentSize();

                CloudServer cloudServer = findMostEfficientCloudServer(dataProcessingSpeed,
                        downloadSpeed, uploadSpeed,
                        dataSentSize);
                System.out.println(
                        "Sending data for Mobile Device " + mobileDevice.getMobileNumber() +
                                " to Edge Server...");

                // Generating a random string for this mobile device
                String randomString = generateRandomString();
                System.out.println(
                        "Random String for Mobile Device " + mobileDevice.getMobileNumber() + ": " +
                                randomString);

                edgeServer.sendDataToCloudServer(cloudServer, mobileDevice, randomString);

                // Processing data received from the cloud server
                System.out.println("Processing data for Mobile Device " +
                        mobileDevice.getMobileNumber()
                        + " received from the cloud server...");
                cloudServer.processDataFromCloudServer(cloudServer, mobileDevice);
            });
        }

        executorService.shutdown();

        // Without using Thread concept: -
        /*
         * for (MobileDevice mobileDevice : mobileDevices) {
         * EdgeServer edgeServer = findNearestEdgeServer(mobileDevice);
         * 
         * int dataProcessingSpeed = mobileDevice.getDataProcessingSpeed();
         * int downloadSpeed = mobileDevice.getDownloadSpeed();
         * int uploadSpeed = mobileDevice.getUploadSpeed();
         * int dataSentSize = mobileDevice.getDataSentSize();
         * 
         * CloudServer cloudServer = findMostEfficientCloudServer(dataProcessingSpeed,
         * downloadSpeed, uploadSpeed,
         * dataSentSize);
         * System.out.println(
         * "Sending data for Mobile Device " + mobileDevice.getMobileNumber() +
         * " to Edge Server...");
         * 
         * // Generating a random string for this mobile device
         * String randomString = generateRandomString();
         * System.out.println(
         * "Random String for Mobile Device " + mobileDevice.getMobileNumber() + ": " +
         * randomString);
         * 
         * edgeServer.sendDataToCloudServer(cloudServer, mobileDevice, randomString);
         * 
         * // Processing data received from the cloud server
         * System.out.println("Processing data for Mobile Device " +
         * mobileDevice.getMobileNumber()
         * + " received from the cloud server...");
         * cloudServer.processDataFromCloudServer(cloudServer, mobileDevice);
         * }
         */
    }

    private static void saveMobileDeviceDataToCSV(String filename, List<MobileDevice> devices) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println(
                    "DataProcessingSpeed,DownloadSpeed,UploadSpeed,DataSentSize,MobileNumber,XCoordinate,YCoordinate");

            // Write data
            for (MobileDevice device : devices) {
                writer.println(String.format("%d,%d,%d,%d,%d,%d,%d",
                        device.getDataProcessingSpeed(),
                        device.getDownloadSpeed(),
                        device.getUploadSpeed(),
                        device.getDataSentSize(),
                        device.getMobileNumber(),
                        device.getXCoordinate(),
                        device.getYCoordinate()));
            }
        }
    }

    private static void saveEdgeServerDataToCSV(String filename, List<EdgeServer> servers) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("DataProcessingSpeed,DownloadSpeed,UploadSpeed,XCoordinate,YCoordinate");

            // Write data
            for (EdgeServer server : servers) {
                writer.println(String.format("%d,%d,%d,%d,%d",
                        server.getDataProcessingSpeed(),
                        server.getDownloadSpeed(),
                        server.getUploadSpeed(),
                        server.getXCoordinate(),
                        server.getYCoordinate()));
            }
        }
    }

    private static void saveCloudServerDataToCSV(String filename, List<CloudServer> servers) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("DataProcessingSpeed,Hostname,Port,DownloadSpeed,UploadSpeed");

            // Write data
            for (CloudServer server : servers) {
                writer.println(String.format("%d,%s,%d,%d,%d",
                        server.getDataProcessingSpeed(),
                        server.getHostname(),
                        server.getPort(),
                        server.getDownloadSpeed(),
                        server.getUploadSpeed()));
            }
        }
    }

    private static EdgeServer findNearestEdgeServer(MobileDevice mobileDevice) {
        // return edgeServers.get(0);
        double minDistance = Double.MAX_VALUE;
        EdgeServer nearestServer = null;

        for (EdgeServer edgeServer : edgeServers) {
            double distance = calculateDistance(mobileDevice, edgeServer);
            if (distance < minDistance) {
                minDistance = distance;
                nearestServer = edgeServer;
            }
        }

        return nearestServer;
    }

    private static double calculateDistance(MobileDevice mobileDevice, EdgeServer edgeServer) {
        int deviceX = mobileDevice.getXCoordinate();
        int deviceY = mobileDevice.getYCoordinate();
        int serverX = edgeServer.getXCoordinate();
        int serverY = edgeServer.getYCoordinate();

        return Math.sqrt(Math.pow(deviceX - serverX, 2) + Math.pow(deviceY - serverY, 2));
    }

    private static CloudServer findMostEfficientCloudServer(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed,
            int dataSentSize) {
        // return cloudServers.get(0);
        double maxEfficiency = Double.MIN_VALUE;
        CloudServer mostEfficientServer = null;

        for (CloudServer cloudServer : cloudServers) {
            double efficiency = calculateEfficiency(dataProcessingSpeed, downloadSpeed, uploadSpeed, dataSentSize,
                    cloudServer);
            if (efficiency > maxEfficiency) {
                maxEfficiency = efficiency;
                mostEfficientServer = cloudServer;
            }
        }

        return mostEfficientServer;
    }

    private static double calculateEfficiency(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed,
            int dataSentSize, CloudServer cloudServer) {
        double processingEfficiency = cloudServer.getDataProcessingSpeed() / dataProcessingSpeed;

        double downloadEfficiency = cloudServer.getDownloadSpeed() / downloadSpeed;

        double uploadEfficiency = cloudServer.getUploadSpeed() / uploadSpeed;

        double overallEfficiency = (processingEfficiency + downloadEfficiency + uploadEfficiency) / 3.0;

        return overallEfficiency;
    }

    public static class MobileDevice {

        private int dataProcessingSpeed;
        private int downloadSpeed;
        private int uploadSpeed;
        private int dataSentSize;
        private long mobileNumber;
        private int xCoord;
        private int yCoord;

        public MobileDevice(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed, int dataSentSize,
                long mobileNumber, int xCoord, int yCoord) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.dataSentSize = dataSentSize;
            this.mobileNumber = mobileNumber;
            this.xCoord = xCoord;
            this.yCoord = yCoord;
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

        public int getXCoordinate() {
            return xCoord;
        }

        public int getYCoordinate() {
            return yCoord;
        }
    }

    public static class EdgeServer {

        private int dataProcessingSpeed;
        private int downloadSpeed;
        private int uploadSpeed;
        private int xCoord;
        private int yCoord;

        private AES_Encryption256 encryption;

        public EdgeServer(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed, int xCoord, int yCoord,
                AES_Encryption256 encryption) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.encryption = encryption;
            this.xCoord = xCoord;
            this.yCoord = yCoord;
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

        public int getXCoordinate() {
            return xCoord;
        }

        public int getYCoordinate() {
            return yCoord;
        }

        public void sendDataToCloudServer(CloudServer cloudServer, MobileDevice mobileDevice, String randomString) {
            try (Socket socket = new Socket(cloudServer.getHostname(), cloudServer.getPort())) {
                // Getting the input and output streams
                try (InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream()) {
                    // Encrypting the random string
                    String encryptedData = encryptData(randomString);

                    // Printing the encrypted string
                    System.out.println("Encrypted String for Mobile Device " + mobileDevice.getMobileNumber() + ": "
                            + encryptedData);

                    // Writing the encrypted data to the cloud server
                    outputStream.write(encryptedData.getBytes(StandardCharsets.UTF_8));
                    System.out.println(
                            "Data sent successfully for Mobile Device " + mobileDevice.getMobileNumber() + ".");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String encryptData(String data) {
            return encryption.encrypt(data);
        }
    }

    public static class CloudServer {

        private int dataProcessingSpeed;
        private String hostname;
        private int port;
        private int donwnloadSpeed;
        private int uploadSpeed;

        private AES_Decryption256 decryption;

        public CloudServer(int dataProcessingSpeed, String hostname, int port, int downloadSpeed, int uploadSpeed,
                AES_Decryption256 decryption) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.hostname = hostname;
            this.port = port;
            this.decryption = decryption;
            this.donwnloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
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

        public int getDownloadSpeed() {
            return donwnloadSpeed;
        }

        public int getUploadSpeed() {
            return uploadSpeed;
        }

        public void processDataFromCloudServer(CloudServer cloudServer, MobileDevice mobileDevice) {
            try (Socket socket = new Socket(cloudServer.getHostname(), cloudServer.getPort())) {

                byte[] data = new byte[1024];
                int bytesRead;
                try (InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream()) {
                    while ((bytesRead = inputStream.read(data)) != -1) {
                        String dataToDecrypt = new String(data, 0, bytesRead, StandardCharsets.UTF_8);
                        String decryptedData = decryptData(dataToDecrypt);

                        outputStream.write(decryptedData.getBytes(StandardCharsets.UTF_8));
                    }
                    System.out.println(
                            "Data processed successfully for mobile device : " + +mobileDevice.getMobileNumber());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
        int length = 20;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }
}
