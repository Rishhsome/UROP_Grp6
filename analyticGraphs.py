import pandas as pd
import matplotlib.pyplot as plt


mobile_devices_data = pd.read_csv('Mobile_devices/mobile_device_data(100).csv')
edge_servers_data = pd.read_csv('Edge_servers/edge_server_data(50).csv')
cloud_servers_data = pd.read_csv('Cloud_servers/cloud_server_data(31).csv')

# Scatter Plots
plt.figure(figsize=(12, 5))

# Data Processing Speed vs. Data Transfer Time
plt.subplot(1, 2, 1)
plt.scatter(mobile_devices_data['DataProcessingSpeed'], mobile_devices_data['DataSentSize'], label='Mobile Devices')
plt.scatter(cloud_servers_data['DataProcessingSpeed'], cloud_servers_data['DownloadSpeed'], label='Cloud Servers')
plt.xlabel('Data Processing Speed')
plt.ylabel('Data Sent Size / Download Speed')
plt.title('Data Processing Speed vs. Data Transfer Time')
plt.legend()

# Download Speed vs. Upload Speed
plt.subplot(1, 2, 2)
plt.scatter(edge_servers_data['DownloadSpeed'], edge_servers_data['UploadSpeed'], label='Edge Servers')
plt.scatter(cloud_servers_data['DownloadSpeed'], cloud_servers_data['UploadSpeed'], label='Cloud Servers')
plt.xlabel('Download Speed')
plt.ylabel('Upload Speed')
plt.title('Download Speed vs. Upload Speed')
plt.legend()

plt.tight_layout()
plt.show()

# X and Y Coordinates
plt.figure(figsize=(10, 6))
plt.scatter(mobile_devices_data['XCoordinate'], mobile_devices_data['YCoordinate'], label='Mobile Devices')
plt.scatter(edge_servers_data['XCoordinate'], edge_servers_data['YCoordinate'], label='Edge Servers')
plt.scatter(cloud_servers_data['Hostname'], cloud_servers_data['Port'], label='Cloud Servers')
plt.xlabel('X Coordinate / Hostname')
plt.ylabel('Y Coordinate / Port')
plt.title('X and Y Coordinates')
plt.legend()
plt.show()

# Line Charts
plt.figure(figsize=(12, 5))

# Data Processing Speed Over Time
plt.subplot(1, 2, 1)
plt.plot(mobile_devices_data['MobileNumber'], mobile_devices_data['DataProcessingSpeed'], label='Mobile Devices')
plt.xlabel('Mobile Number')
plt.ylabel('Data Processing Speed')
plt.title('Data Processing Speed Over Time')
plt.legend()

# Download Speed Over Time
plt.subplot(1, 2, 2)
plt.plot(cloud_servers_data['Hostname'], cloud_servers_data['DownloadSpeed'], label='Cloud Servers')
plt.xlabel('Hostname')
plt.ylabel('Download Speed')
plt.title('Download Speed Over Time')
plt.xticks(rotation=45)
plt.legend()

plt.tight_layout()
plt.show()

# Histograms
plt.figure(figsize=(12, 5))

# Data Sent Size Distribution
plt.subplot(1, 2, 1)
plt.hist(mobile_devices_data['DataSentSize'], bins=20, edgecolor='k')
plt.xlabel('Data Sent Size')
plt.ylabel('Frequency')
plt.title('Data Sent Size Distribution')

# Data Processing Speed Distribution
plt.subplot(1, 2, 2)
plt.hist(mobile_devices_data['DataProcessingSpeed'], bins=20, edgecolor='k')
plt.xlabel('Data Processing Speed')
plt.ylabel('Frequency')
plt.title('Data Processing Speed Distribution')

plt.tight_layout()
plt.show()

# Bar Charts
plt.figure(figsize=(10, 6))

# Comparison of Data Processing Speed
data_processing_speeds = [mobile_devices_data['DataProcessingSpeed'].mean(),
                          edge_servers_data['DataProcessingSpeed'].mean(),
                          cloud_servers_data['DataProcessingSpeed'].mean()]
labels = ['Mobile Devices', 'Edge Servers', 'Cloud Servers']

plt.bar(labels, data_processing_speeds)
plt.xlabel('Server Type')
plt.ylabel('Average Data Processing Speed')
plt.title('Comparison of Data Processing Speed')
plt.show()
