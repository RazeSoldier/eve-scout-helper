package razesoldier.scouthelper.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import razesoldier.scouthelper.data.SolarSystemRepository;
import razesoldier.scouthelper.data.dto.SolarSystemNameSuggestion;

import java.util.List;

@RestController
@RequestMapping(value = "/api/system/name-suggest", produces = "application/json")
public class SolarSystemNameSuggestionController {
    private final SolarSystemRepository solarSystemRepository;

    public SolarSystemNameSuggestionController(SolarSystemRepository solarSystemRepository) {
        this.solarSystemRepository = solarSystemRepository;
    }

    @GetMapping
    public List<SolarSystemNameSuggestion> invoke(@RequestParam("startWith") String startWith) {
        return solarSystemRepository.findByNameStartingWith(startWith)
                .stream()
                .map(system -> new SolarSystemNameSuggestion(system.getName(), system.getId()))
                .toList();
    }
}
