package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.WeeklyScoreService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;

import java.security.Principal;
import java.util.List;

@RestController
public class WeeklyScoreController {
    private static final Logger logger = LoggerFactory.getLogger(WeeklyScoreController.class);

    @Autowired
    private WeeklyScoreService weeklyScoreService;

    WeeklyScoreController(WeeklyScoreService weeklyScoreService) {
        this.weeklyScoreService = weeklyScoreService;
    }


    @PutMapping("/students/dashboards/{dashboardId}/updateWeeklyScores")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> updateWeeklyScore(@PathVariable int dashboardId) {
        return this.weeklyScoreService.updateWeeklyScore(dashboardId);
    }

    @DeleteMapping("/weeklyScore/{weeklyScoreId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#weeklyScoreId, 'WEEKLYSCORE.ACCESS')")
    public void removeWeeklyScore(@PathVariable int weeklyScoreId) {
        this.weeklyScoreService.removeWeeklyScore(weeklyScoreId);
    }

    @GetMapping("/students/dashboards/{dashboardId}/weeklyScores")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> getWeeklyScores(@PathVariable int dashboardId) {
        return this.weeklyScoreService.getWeeklyScores(dashboardId);
    }
}
