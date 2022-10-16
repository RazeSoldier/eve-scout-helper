package razesoldier.scouthelper.web.controller;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DrifterReportBuilderController {
    @GetMapping("/drifter-report/build")
    public String show(@AuthenticationPrincipal AuthenticatedPrincipal authenticationPrincipal, Model model) {
        model.addAttribute("isLogon", authenticationPrincipal != null);
        return "drifter-report-builder";
    }
}
