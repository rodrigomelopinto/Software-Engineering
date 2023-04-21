package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetWeeklyScoreWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response

    def authUserDto
    def courseExecutionDto
    def dashboardDto

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        courseExecutionDto = courseService.getDemoCourse()
        authUserDto = authUserService.demoStudentAuth(false).getUser()
        dashboardDto = dashboardService.getDashboard(courseExecutionDto.getCourseExecutionId(), authUserDto.getId())
        weeklyScoreService.updateWeeklyScore(dashboardDto.getId())
    }

    def "demo student gets weekly scores"() {
        given: 'a student'
        demoStudentLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/' + dashboardDto.getId() + '/weeklyScores',
                requestContentType: 'application/json'
        )

        then: "the request returns 200"
        response.status == 200
        and: "has value"
        response.data != null
        response.data.size() == 1
        and: 'it is in the database'
        weeklyScoreRepository.findAll().size() == 1
        weeklyScoreRepository.existsById(response.data.get(0).id)


        cleanup:
        cleanup()

    }

    def "demo teacher does not have access"() {
        given: 'a teacher'
        authUserDto = authUserService.demoTeacherAuth().getUser()
        demoTeacherLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/' + dashboardDto.getId() + '/weeklyScores',
                requestContentType: 'application/json'
        )

        then: 'the request returns 403'
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "does not have value"
        response == null

        cleanup:
        cleanup()

    }

    def "new demo student does not have access"() {
        given: 'a new demo student'
        def student2 = new Student(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, false, AuthUser.Type.DEMO)
        student2.getAuthUser().setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student2)
        createdUserLogin(USER_2_USERNAME,USER_2_PASSWORD)



        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/' + dashboardDto.getId() + '/weeklyScores',
                requestContentType: 'application/json'
        )

        then: 'the request returns 403'
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "does not have value"
        response == null

        cleanup:
        cleanup()
    }

    def cleanup() {
        weeklyScoreRepository.deleteAll()
        userRepository.deleteAll()
        dashboardRepository.deleteAll()
        courseExecutionRepository.deleteAll()
        courseRepository.deleteAll()
    }

}