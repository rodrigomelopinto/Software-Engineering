package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service.FailedAnswersSpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def quiz
    def quizQuestion

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        createExternalCourseAndExecution()
        and:
        student = new Student(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        and:
        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
        and:
        quiz = createQuiz(1)
        quizQuestion = createQuestion(1, quiz)
        answerQuiz(true, false, true, quizQuestion, quiz)

    }

    def "student updates failed answers"() {
        given: 'a student'
        createdUserLogin(student.getAuthUser().getEmail(),USER_1_PASSWORD)

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers/update',
                query: [
                        startDate: STRING_DATE_BEFORE,
                        endDate: STRING_DATE_TOMORROW,
                ],
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response.status == 200
        and: 'it is in the database'
        failedAnswerRepository.findAll().size() == 1
        

        cleanup:
        failedAnswerRepository.deleteAll()
        userRepository.deleteAll()
        dashboardRepository.deleteAll()
        courseExecutionRepository.deleteAll()
        courseRepository.deleteAll()
    }

    def "teacher cant update student's failed answers"() {
        given: 'a demo teacher'
        demoTeacherLogin()

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers/update',
                query: [
                        startDate: STRING_DATE_BEFORE,
                        endDate: STRING_DATE_TOMORROW,
                ],
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "does not have value"
        response == null
        failedAnswerRepository.findAll().size() == 0

        cleanup:
        userRepository.deleteAll()
        dashboardRepository.deleteAll()
        courseExecutionRepository.deleteAll()
        courseRepository.deleteAll()
    }

    def "student cant update another students failed answers"() {
        given: 'a demo student'
        demoStudentLogin()

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboards/' + dashboard.getId() + '/failedanswers/update',
                query: [
                        startDate: STRING_DATE_BEFORE,
                        endDate: STRING_DATE_TOMORROW,
                ],
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "does not have value"
        response == null
        failedAnswerRepository.findAll().size() == 0

        cleanup:
        userRepository.deleteAll()
        dashboardRepository.deleteAll()
        courseExecutionRepository.deleteAll()
        courseRepository.deleteAll()
    }

}