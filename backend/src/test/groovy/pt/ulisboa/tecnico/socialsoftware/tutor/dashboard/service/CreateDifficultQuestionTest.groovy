package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Unroll
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND

@DataJpaTest
class CreateDifficultQuestionTest extends SpockTest {
    def student
    def dashboard
    def question

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, false)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        question = new Question()
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setCourse(externalCourse)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    @Unroll
    def "create difficult question with difficulty #difficulty"() {
        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), difficulty)

        then:
        difficultQuestionRepository.count() == 1L
        def result = difficultQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getDashboard().getId() == dashboard.getId()
        result.getQuestion().getId() == question.getId()
        result.isRemoved() == false
        result.getRemovedDate() == null
        result.getPercentage() == difficulty
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getDifficultQuestions().contains(result)

        where:
        difficulty << [0, 12, 24]
    }


    def "cannot create two difficult questions for the same question"() {
        given:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), 13)

        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), 24)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DIFFICULT_QUESTION_ALREADY_CREATED
        and:
        difficultQuestionRepository.count() == 1L
    }

    def "cannot create a difficult question that does not belong to the student course"() {
        given:
        def alienCourse = new Course(COURSE_1_NAME, Course.Type.TECNICO)
        courseRepository.save(alienCourse)
        and:
        def alienQuestion = new Question()
        alienQuestion.setTitle(QUESTION_1_TITLE)
        alienQuestion.setContent(QUESTION_1_CONTENT)
        alienQuestion.setStatus(Question.Status.AVAILABLE)
        alienQuestion.setNumberOfAnswers(2)
        alienQuestion.setNumberOfCorrect(1)
        alienQuestion.setCourse(alienCourse)
        def questionDetails = new MultipleChoiceQuestion()
        alienQuestion.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(alienQuestion)

        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), alienQuestion.getId(), 22)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION
        and:
        difficultQuestionRepository.count() == 0L
    }

    @Unroll
    def "cannot create difficult question with invalid percentage=#percentage"() {
        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), question.getId(), percentage)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == CANNOT_CREATE_DIFFICULT_QUESTION
        difficultQuestionRepository.count() == 0L

        where:
        percentage << [-100, -1, 25, 50, 150]

    }

    @Unroll
    def "cannot create difficult question with invalid dashboardId=#dashboardId"() {
        when:
        difficultQuestionService.createDifficultQuestion(dashboardId, question.getId(), 20)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == DASHBOARD_NOT_FOUND
        difficultQuestionRepository.count() == 0L

        where:
        dashboardId << [0, 100]
    }

    @Unroll
    def "cannot create difficult question with invalid questionId=#questionId"() {
        when:
        difficultQuestionService.createDifficultQuestion(dashboard.getId(), questionId, 20)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == QUESTION_NOT_FOUND
        difficultQuestionRepository.count() == 0L

        where:
        questionId << [0, 100]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}