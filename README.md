<div align="center">

  <img src="assets/logo.webp" alt="FTP System Logo" width="200" height="auto" />
  <h1>File Transfer System</h1>

</div>

<!-- Table of Contents -->

# Table of Contents

- [About the Project](#about-the-project)
    - [Description](#description)
    - [Features](#features)
    - [Tech Stack](#tech-stack)
    - [Environment Variables](#environment-variables)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Setup](#setup)
    - [Run](#run)

<!-- About the Project -->

## About the Project

<!-- Description -->

### Description

<p>
An FTP Server and Client application built with Java. 
Upon initialization, the server will automatically generate a designated folder for file storage, should this folder not already be present. 
Subsequently, it will proceed to accept client connections, ready to receive and store files sent by the clients.
</p>

<!-- Features -->

### Features

- Support for concurrent client connections.
- File upload, download, and deletion capabilities.
- Directory navigation and file listing.
- Graphical User Interface for the client application.
- Docker support.
- Selection of host and port.

<!-- TechStack -->

### Tech Stack

 <ul>
    <li><a href="https://www.java.com/">Java</a></li>
    <li><a href="https://maven.apache.org/">Maven</a></li>
    <li><a href="https://www.docker.com/">Docker</a></li>
    <li>Swing (for Client GUI)</li>
  </ul>

<!-- Env Variables -->

### Environment Variables

To run this project, you will need to add the following environment variables to your .env file

`DEFAULT_PORT`

`DEFAULT_SERVER_NAME`

`STORAGE_DIRECTORY_NAME`

<!-- Getting Started -->

## Getting Started

<!-- Prerequisites -->

### Prerequisites

 <ul>
   <li>Java JDK 17 or newer</li>
   <li>Maven</li>
   <li>Docker (optional for containerization)</li>
 </ul>

### Setup

Clone the project

```bash
  git clone https://github.com/nidorbRotkiv/FileTransferSystem.git
```

### Run

#### Run the server

```bash
mvn exec:java@run-server
```

#### Run a client in a new terminal window (or tab)

```bash
mvn exec:java@run-client-gui
```


