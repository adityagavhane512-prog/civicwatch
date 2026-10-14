
package civicwatch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final Path uploadPath;

    public PhotoStorageService(
            @Value("${civicwatch.upload-dir:uploads}") String uploadDir) {

        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public String storePhoto(MultipartFile photo) throws IOException {

        if (photo == null || photo.isEmpty()) {
            return null;
        }

        // Check file size.
        if (photo.getSize() > MAX_FILE_SIZE) {
            throw new IOException("Photo must be 5 MB or smaller.");
        }

        // Check the declared image type.
        String contentType = photo.getContentType();

        if (contentType == null
                || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException(
                    "Only JPG, PNG, and WEBP images are allowed.");
        }

        // Determine the extension from the original filename.
        String originalName = photo.getOriginalFilename();
        String extension;

        if (originalName == null) {
            throw new IOException("Invalid image filename.");
        }

        String lowerName = originalName.toLowerCase();

        if (lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")) {
            extension = ".jpg";
        } else if (lowerName.endsWith(".png")) {
            extension = ".png";
        } else if (lowerName.endsWith(".webp")) {
            extension = ".webp";
        } else {
            throw new IOException(
                    "Only JPG, PNG, and WEBP images are allowed.");
        }

        // Ensure the extension matches the declared content type.
        boolean validPair =
                (extension.equals(".jpg")
                        && contentType.equalsIgnoreCase("image/jpeg"))
                || (extension.equals(".png")
                        && contentType.equalsIgnoreCase("image/png"))
                || (extension.equals(".webp")
                        && contentType.equalsIgnoreCase("image/webp"));

        if (!validPair) {
            throw new IOException(
                    "The image file extension and type do not match.");
        }

        // Check the actual file signature.
        byte[] header = new byte[12];

        try (InputStream input = photo.getInputStream()) {
            int count = input.readNBytes(header, 0, header.length);

            if (!hasValidSignature(header, count, extension)) {
                throw new IOException(
                        "The uploaded file is not a valid supported image.");
            }
        }

        // Create the upload directory if needed.
        Files.createDirectories(uploadPath);

        // Generate a unique filename to avoid overwriting files.
        String filename = UUID.randomUUID() + extension;
        Path destination = uploadPath.resolve(filename).normalize();

        if (!destination.getParent().equals(uploadPath)) {
            throw new IOException("Invalid upload path.");
        }

        // Copy without replacing an existing file.
        try (InputStream input = photo.getInputStream()) {
            Files.copy(input, destination);
        } catch (IOException e) {
            Files.deleteIfExists(destination);
            throw e;
        }

        return filename;
    }

    private boolean hasValidSignature(
            byte[] header, int count, String extension) {

        if (extension.equals(".jpg")) {
            return count >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;
        }

        if (extension.equals(".png")) {
            return count >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P'
                    && header[2] == 'N'
                    && header[3] == 'G'
                    && (header[4] & 0xFF) == 0x0D
                    && (header[5] & 0xFF) == 0x0A
                    && (header[6] & 0xFF) == 0x1A
                    && (header[7] & 0xFF) == 0x0A;
        }

        if (extension.equals(".webp")) {
            return count >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P';
        }

        return false;
    }
}
