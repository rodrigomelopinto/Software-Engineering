package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DifficultQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;
import java.util.List;

import java.security.Principal;

@RestController
public class DifficultQuestionController {
    private static final Logger logger = LoggerFactory.getLogger(DifficultQuestionController.class);

    @Autowired
    private DifficultQuestionService difficultQuestionService;

    DifficultQuestionController(DifficultQuestionService difficultQuestionService) {
        this.difficultQuestionService = difficultQuestionService;
    }

    @GetMapping("/students/dashboards/{dashboardId}/difficultquestions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<DifficultQuestionDto> getDifficultQuestions(@PathVariable int dashboardId) {
        return this.difficultQuestionService.getDifficultQuestions(dashboardId);
    }

    @DeleteMapping("/students/dashboards/difficultquestions/{difficultQuestionId}/remove")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#difficultQuestionId, 'DIFFICULT_QUESTION.ACCESS')")
    public void removeDifficultQuestion(@PathVariable int difficultQuestionId) {
        this.difficultQuestionService.removeDifficultQuestion(difficultQuestionId);
    }

    @PutMapping("/students/dashboards/{dashboardId}/difficultquestions/update")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public void updateDifficultQuestions(@PathVariable int dashboardId) {
        this.difficultQuestionService.updateDifficultQuestions(dashboardId);
    }
}
