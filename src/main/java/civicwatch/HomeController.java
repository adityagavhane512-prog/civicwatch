
package civicwatch;

import civicwatch.model.Complaint;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class HomeController {

    private final ComplaintRepository complaintRepository;
    private final PhotoStorageService photoStorageService;

    public HomeController(
            ComplaintRepository complaintRepository,
            PhotoStorageService photoStorageService) {

        this.complaintRepository = complaintRepository;
        this.photoStorageService = photoStorageService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("complaint", new Complaint());
        return "home";
    }

    @PostMapping("/submit-complaint")
    public String submitComplaint(
            @ModelAttribute("complaint") Complaint complaint,
            BindingResult result,
            @RequestParam(value = "photo", required = false)
            MultipartFile photo,
            @RequestParam(value = "cameraPhoto", required = false)
            MultipartFile cameraPhoto,
            Model model) {

        // Check for binding errors.
        if (result.hasErrors()) {
            model.addAttribute(
                    "success",
                    "Invalid complaint details. Please check your entries.");
            return "home";
        }

        // Validate the required complaint details.
        if (complaint.getCategory() == null
                || complaint.getCategory().isBlank()) {

            model.addAttribute(
                    "success",
                    "Please select a complaint category.");
            return "home";
        }

        if (complaint.getDescription() == null
                || complaint.getDescription().isBlank()) {

            model.addAttribute(
                    "success",
                    "Please enter a complaint description.");
            return "home";
        }

        if (complaint.getDescription().length() > 2000) {
            model.addAttribute(
                    "success",
                    "Description must not exceed 2000 characters.");
            return "home";
        }

        if (complaint.getLocation() == null
                || complaint.getLocation().isBlank()) {

            model.addAttribute(
                    "success",
                    "Please enter the complaint location.");
            return "home";
        }

        if (complaint.getLocation().length() > 250) {
            model.addAttribute(
                    "success",
                    "Location must not exceed 250 characters.");
            return "home";
        }

        // Prevent submitting two photos for one complaint.
        if (photo != null && !photo.isEmpty()
                && cameraPhoto != null && !cameraPhoto.isEmpty()) {

            model.addAttribute(
                    "success",
                    "Please choose either an uploaded photo or a camera photo, not both.");
            return "home";
        }

        // Use whichever photo the user selected.
        MultipartFile selectedPhoto =
                (cameraPhoto != null && !cameraPhoto.isEmpty())
                        ? cameraPhoto
                        : photo;

        try {
            // PhotoStorageService handles photo storage and its rules.
            String filename =
                    photoStorageService.storePhoto(selectedPhoto);

            complaint.setPhotoFilename(filename);

            // Save the complaint in MySQL.
            Complaint savedComplaint =
                    complaintRepository.save(complaint);

            // Clear the form after successful submission.
            model.addAttribute("complaint", new Complaint());

            model.addAttribute(
                    "success",
                    "Complaint submitted successfully! Your complaint ID is "
                            + savedComplaint.getId());

            return "home";

        } catch (IOException e) {

            model.addAttribute(
                    "success",
                    "The photo could not be saved. Please check the photo and try again.");

            return "home";
        }
    }
}
