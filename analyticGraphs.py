import pandas as pd
import matplotlib.pyplot as plt


mobile_devices_data = pd.read_csv('Mobile_devices/mobile_device_data(100).csv')
edge_servers_data = pd.read_csv('Edge_servers/edge_server_data(50).csv')
cloud_servers_data = pd.read_csv('Cloud_servers/cloud_server_data(31).csv')

# Scatter Plots
plt.figure(figsize=(12, 5))

# Data Processing Speed vs. Data Transfer Time
plt.subplot(1, 2, 1)
plt.scatter(mobile_devices_data['DataProcessingSpeed'], mobile_devices_data['DataSentSize'], label='Mobile Devices', color='#494623')
plt.scatter(cloud_servers_data['DataProcessingSpeed'], cloud_servers_data['DownloadSpeed'], label='Cloud Servers', color='#c3892b')
plt.xlabel('Data Processing Speed (MIPS)')
plt.ylabel('Download Speed (Mbps)')
plt.title('Data Processing Speed vs. Data Transfer Time')
plt.legend()

# Download Speed vs. Upload Speed
plt.subplot(1, 2, 2)
plt.scatter(edge_servers_data['DownloadSpeed'], edge_servers_data['UploadSpeed'], label='Edge Servers', color='#494623')
plt.scatter(cloud_servers_data['DownloadSpeed'], cloud_servers_data['UploadSpeed'], label='Cloud Servers', color='#c3892b')
plt.xlabel('Download Speed (Mbps)')
plt.ylabel('Upload Speed (Mbps)')
plt.title('Download Speed vs. Upload Speed')
plt.legend()

plt.tight_layout()
plt.show()


# Histograms
plt.figure(figsize=(12, 5))

# Data Sent Size Distribution
plt.subplot(1, 2, 1)
plt.hist(mobile_devices_data['DataSentSize'], bins=20, edgecolor='k', color='#494623')
plt.xlabel('Data Sent Size (MB)')
plt.ylabel('Frequency (No. of Occurrences)')
plt.title('Data Sent Size Distribution')

# Data Processing Speed Distribution
plt.subplot(1, 2, 2)
plt.hist(mobile_devices_data['DataProcessingSpeed'], bins=20, edgecolor='k', color='#c3892b')
plt.xlabel('Data Processing Speed (MIPS)')
plt.ylabel('Frequency (No. of Occurrences)')
plt.title('Data Processing Speed Distribution')

plt.tight_layout()
plt.show()

