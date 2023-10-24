import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            mobileDevices.add(new MobileDevice(dataProcessingSpeed, downloadSpeed, uploadSpeed, dataSentSize));
        }

        // Create the edge servers
        for (int i = 0; i < NUM_EDGE_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 1000) + 1;
            int downloadSpeed = (int) (Math.random() * 500) + 1;
            int uploadSpeed = (int) (Math.random() * 100) + 1;

            edgeServers.add(new EdgeServer(dataProcessingSpeed, downloadSpeed, uploadSpeed));
        }

        // Create the cloud servers
        for (int i = 0; i < NUM_CLOUD_SERVERS; i++) {
            int dataProcessingSpeed = (int) (Math.random() * 10000) + 1;
            String hostname = "127.0.0.1";
            int port = 8080 + i;
        
            cloudServers.add(new CloudServer(dataProcessingSpeed, hostname, port));
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

                CloudServer cloudServer = findMostEfficientCloudServer(dataProcessingSpeed, downloadSpeed, uploadSpeed, dataSentSize);
                System.out.println("Sending data from mobile device to edge server...");
                edgeServer.sendDataToCloudServer(cloudServer, mobileDevice);
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

        public MobileDevice(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed, int dataSentSize) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.dataSentSize = dataSentSize;
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
    }

    public static class EdgeServer {

        private int dataProcessingSpeed;
        private int downloadSpeed;
        private int uploadSpeed;

        public EdgeServer(int dataProcessingSpeed, int downloadSpeed, int uploadSpeed) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
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

        public void sendDataToCloudServer(CloudServer cloudServer, MobileDevice mobileDevice) {
            try (Socket socket = new Socket(cloudServer.getHostname(), cloudServer.getPort())) {
                // Get the input and output streams
                try (InputStream inputStream = socket.getInputStream();
                     OutputStream outputStream = socket.getOutputStream()) {
                    // Read the data from the mobile device
                    byte[] data = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(data)) != -1) {
                        // Write the data to the cloud server
                        outputStream.write(data, 0, bytesRead);
                    }
                    System.out.println("Data sent successfully.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class CloudServer {

        private int dataProcessingSpeed;
        private String hostname;
        private int port;

        public CloudServer(int dataProcessingSpeed, String hostname, int port) {
            this.dataProcessingSpeed = dataProcessingSpeed;
            this.hostname = hostname;
            this.port = port;
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
    }
}