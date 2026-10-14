
package civicwatch;

import civicwatch.model.Complaint;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrackingController {

    private final ComplaintRepository complaintRepository;

    public TrackingController(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @GetMapping("/track")
    public String showTrackingPage() {
        return "track";
    }

    @PostMapping("/track")
    public String trackComplaint(
            @RequestParam(
                    value = "complaintId",
                    required = false)
            String complaintIdInput,
            Model model) {

        // Validate empty input.
        if (complaintIdInput == null
                || complaintIdInput.isBlank()) {

            model.addAttribute(
                    "error",
                    "Please enter your complaint ID.");

            return "track";
        }

        // Convert and validate the complaint ID.
        Long complaintId;

        try {
            complaintId = Long.parseLong(
                    complaintIdInput.trim());
        } catch (NumberFormatException e) {

            model.addAttribute(
                    "error",
                    "Complaint ID must contain numbers only.");

            return "track";
        }

        if (complaintId <= 0) {
            model.addAttribute(
                    "error",
                    "Please enter a valid complaint ID.");

            return "track";
        }

        // Search for the complaint.
        Complaint complaint = complaintRepository
                .findById(complaintId)
                .orElse(null);

        if (complaint == null) {

            model.addAttribute(
                    "error",
                    "No complaint found with ID "
                            + complaintId + ".");

        } else {
            model.addAttribute("complaint", complaint);
        }

        return "track";
    }
}
