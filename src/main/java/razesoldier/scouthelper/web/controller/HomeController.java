package razesoldier.scouthelper.web.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import razesoldier.scouthelper.security.oauth2.EVEOAuth2User;

@Controller
public class HomeController {
    @GetMapping("/")
    public String show(@AuthenticationPrincipal @NotNull EVEOAuth2User user, @NotNull Model model) {
        model.addAttribute("name", user.getName());
        return "home";
    }
}
