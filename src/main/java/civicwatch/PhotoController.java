package civicwatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PhotoController {

    private final Path uploadPath;

    public PhotoController(
            @Value("${civicwatch.upload-dir:uploads}") String uploadDir) {

        this.uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @GetMapping("/admin/photos/{filename:.+}")
    public ResponseEntity<Resource> viewPhoto(
            @PathVariable String filename) {

        try {
            // Accept only a filename, not a folder path.
            if (!Paths.get(filename).getFileName()
                    .toString().equals(filename)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid filename");
            }

            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)
                    || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Photo not found");
            }

            String contentType = Files.probeContentType(filePath);

            if (contentType == null
                    || !(contentType.equals("image/jpeg")
                    || contentType.equals("image/png")
                    || contentType.equals("image/webp"))) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unsupported image");
            }

            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read photo");
        }
    }
}