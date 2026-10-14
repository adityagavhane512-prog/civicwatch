package civicwatch;

import civicwatch.model.Complaint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    public ComplaintController(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @GetMapping("/complaints")
    public String viewComplaints(Model model) {
        List<Complaint> complaints = complaintRepository.findAll();
        model.addAttribute("complaints", complaints);
        return "complaints";
    }
}