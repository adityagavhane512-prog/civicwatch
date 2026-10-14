package civicwatch;

import civicwatch.model.Complaint;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final ComplaintRepository complaintRepository;

    public AdminController(
            ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {

        List<Complaint> complaints =
                complaintRepository.findAll();

        long totalComplaints = complaints.size();

        long submittedCount = complaints.stream()
                .filter(c -> "Submitted".equals(c.getStatus()))
                .count();

        long inProgressCount = complaints.stream()
                .filter(c -> "In Progress".equals(c.getStatus()))
                .count();

        long resolvedCount = complaints.stream()
                .filter(c -> "Resolved".equals(c.getStatus()))
                .count();

        model.addAttribute("complaints", complaints);
        model.addAttribute("totalComplaints", totalComplaints);
        model.addAttribute("submittedCount", submittedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("resolvedCount", resolvedCount);

        return "admin";
    }

    @PostMapping("/admin/update-status")
    public String updateStatus(
            @RequestParam Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        if (!status.equals("Submitted")
                && !status.equals("In Progress")
                && !status.equals("Resolved")) {

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Invalid complaint status.");

            return "redirect:/admin";
        }

        Complaint complaint = complaintRepository
                .findById(id)
                .orElse(null);

        if (complaint == null) {
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Complaint not found.");

            return "redirect:/admin";
        }

        complaint.setStatus(status);
        complaintRepository.save(complaint);

        redirectAttributes.addFlashAttribute(
                "message",
                "Complaint status updated successfully!");

        return "redirect:/admin";
    }
}
