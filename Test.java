import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.function.server.FilePart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public MultipartFile getMultiPartFile(FilePart filePart) throws IOException {
    // Create a temporary file to hold the uploaded content
    File tempFile = File.createTempFile("temp", null);
    // Copy the contents of the FilePart to the temporary file
    filePart.transferTo(tempFile);

    // Return the MultipartFile implementation
    return new MultipartFile() {
        @Override
        public String getName() {
            return filePart.filename();
        }

        @Override
        public String getOriginalFilename() {
            return filePart.filename();
        }

        @Override
        public String getContentType() {
            return filePart.headers().getContentType().toString();
        }

        @Override
        public boolean isEmpty() {
            return tempFile.length() == 0;
        }

        @Override
        public long getSize() {
            return tempFile.length();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(tempFile.toPath());
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(tempFile.toPath());
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.copy(tempFile.toPath(), dest.toPath());
        }
    };
}






import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.server.FilePart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public MultipartFile getMultiPartFile(FilePart filePart) throws IOException {
    // Create a temporary file
    File tempFile = File.createTempFile("temp", filePart.filename());
    
    // Transfer the contents of FilePart to the temporary file
    filePart.transferTo(tempFile.toPath());
    
    // Return a MultipartFile implementation
    return new MultipartFile() {
        @Override
        public String getName() {
            return filePart.name(); // The name of the file part
        }

        @Override
        public String getOriginalFilename() {
            return filePart.filename(); // The original filename
        }

        @Override
        public String getContentType() {
            return filePart.headers().getContentType().toString(); // Content type from headers
        }

        @Override
        public boolean isEmpty() {
            return tempFile.length() == 0; // Check if file is empty
        }

        @Override
        public long getSize() {
            return tempFile.length(); // Get the size of the file
        }

        @Override
        public byte[] getBytes() throws IOException {
            return Files.readAllBytes(tempFile.toPath()); // Read bytes from temp file
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(tempFile.toPath()); // Get input stream from temp file
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.copy(tempFile.toPath(), dest.toPath()); // Transfer contents to destination
        }
    };
}






