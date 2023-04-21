package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.WeeklyScoreRepository;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.WEEKLY_SCORE_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_REMOVE_WEEKLY_SCORE;



@Service
public class WeeklyScoreService {

  @Autowired
  private WeeklyScoreRepository weeklyScoreRepository;

  @Autowired
  private DashboardRepository dashboardRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public WeeklyScoreDto createWeeklyScore(Integer dashboardId) {
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();

    WeeklyScore weeklyScore = new WeeklyScore(dashboard, week);
    weeklyScoreRepository.save(weeklyScore);

    return new WeeklyScoreDto(weeklyScore);
  }

  private WeeklyScoreDto createPreviousWeeklyScore(Integer dashboardId, LocalDate week) {
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

    WeeklyScore weeklyScore = new WeeklyScore(dashboard, week);
    weeklyScoreRepository.save(weeklyScore);

    return new WeeklyScoreDto(weeklyScore);
  }
  
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void removeWeeklyScore(Integer weeklyScoreId) {
    if (weeklyScoreId == null) {
      throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
    }

    WeeklyScore weeklyScore = weeklyScoreRepository.findById(weeklyScoreId)
            .orElseThrow(() -> new TutorException(WEEKLY_SCORE_NOT_FOUND, weeklyScoreId));

    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    final LocalDate currentWeek = DateHandler.now().with(weekSunday).toLocalDate();

    if (weeklyScore.getWeek().isEqual(currentWeek)) {
      throw new TutorException(CANNOT_REMOVE_WEEKLY_SCORE);
    }
    weeklyScore.remove();
    weeklyScoreRepository.delete(weeklyScore);
  }
  
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ArrayList<WeeklyScoreDto> updateWeeklyScore(Integer dashboardId) {

    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));
    
    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    LocalDate currentWeek = DateHandler.now().with(weekSunday).toLocalDate();

    if(dashboard.getLastCheckWeeklyScores()==null){

      Set<QuizAnswer> weeklyQuizAnswers = dashboard.getStudent().getQuizAnswers().stream()
            .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution() == dashboard.getCourseExecution())
            .collect(Collectors.toSet());

      LocalDate oldestNotUpdatedAnswer=currentWeek;

      for(QuizAnswer qa : weeklyQuizAnswers){
        if(qa.getAnswerDate().with(weekSunday).toLocalDate().isBefore(oldestNotUpdatedAnswer)){
          oldestNotUpdatedAnswer=qa.getAnswerDate().with(weekSunday).toLocalDate();
        }
      }

      final LocalDate oldestNotUpdatedAnswer_final = oldestNotUpdatedAnswer;

      if (weeklyScoreRepository.findAll().stream().filter(weeklyScore -> weeklyScore.getDashboard().getId().equals(dashboard.getId())).noneMatch(weeklyScore -> weeklyScore.getWeek().isEqual(oldestNotUpdatedAnswer_final))){
        this.createPreviousWeeklyScore(dashboardId, oldestNotUpdatedAnswer);
      }

      dashboard.setLastCheckWeeklyScores(oldestNotUpdatedAnswer.atStartOfDay());

    }

    LocalDate lastChecked = dashboard.getLastCheckWeeklyScores().with(weekSunday).toLocalDate();

    for (LocalDate week = currentWeek ; week.isAfter(lastChecked); week = week.minusWeeks(1)){
      final LocalDate week_final = week;

      if (weeklyScoreRepository.findAll().stream().filter(weeklyScore -> weeklyScore.getDashboard().getId().equals(dashboard.getId())).noneMatch(weeklyScore -> weeklyScore.getWeek().isEqual(week_final))) {
        this.createPreviousWeeklyScore(dashboardId, week);
      }
    }


    weeklyScoreRepository.findAll().stream().filter(weeklyScore -> weeklyScore.getDashboard().getId().equals(dashboard.getId())).filter(weeklyScore -> !weeklyScore.isClosed()).forEach(weeklyScore -> weeklyScore.computeStatistics());

    weeklyScoreRepository.findAll().stream().filter(weeklyScore -> weeklyScore.getDashboard().getId().equals(dashboard.getId())).filter(weeklyScore -> ((weeklyScore.isClosed()) && (weeklyScore.getNumberAnswered() == 0))).forEach(closedWeeklyScore -> removeWeeklyScore(closedWeeklyScore.getId()));

    dashboard.setLastCheckWeeklyScores(DateHandler.now());

    return this.getWeeklyScores(dashboardId);
  }


  @Transactional(isolation = Isolation.READ_COMMITTED)
  public ArrayList<WeeklyScoreDto> getWeeklyScores(Integer dashboardId){
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }
    
    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

    Set<WeeklyScore> weeklyScores = dashboard.getWeeklyScores();

    ArrayList<WeeklyScoreDto> weeklyScoreDtos = new ArrayList<WeeklyScoreDto>();

    for (WeeklyScore weeklyScore : weeklyScores){
      weeklyScoreDtos.add(new WeeklyScoreDto(weeklyScore));
    }
    
    Collections.sort(weeklyScoreDtos);

    return weeklyScoreDtos;
  }
}