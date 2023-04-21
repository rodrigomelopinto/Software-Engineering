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
class UpdateWeeklyScoreWebServiceIT extends SpockTest {
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
    }

    def "demo student gets its weekly scores"() {
        
        given: 'a demo student'
        demoStudentLogin()
        
        when: 'gets weeklyScore'
        response = restClient.put(path: '/students/dashboards/' + dashboardDto.getId() + '/updateWeeklyScores',requestContentType: 'application/json')
        
        then: 'the request returns 200'
        response.status == 200  
        and: 'it is in the database'
        weeklyScoreRepository.count()==1L

        cleanup:
        cleanup()
    }

    def "demo teacher does not have access"() {
        given:
        authUserService.demoTeacherAuth().getUser()
        demoTeacherLogin()

        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboards/' + dashboardDto.getId() + '/updateWeeklyScores',
                requestContentType: 'application/json'
        )

        then:"the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "weekly scores were not updated"
        weeklyScoreRepository.count() == 0L

        cleanup:
        cleanup()

    }

    def "student cant update another students failed answers"() {
        given:
        def student2 = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student2.getAuthUser().setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        userRepository.save(student2)
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)


        when: 'the web service is invoked'
        response = restClient.put(
                path: '/students/dashboards/' + dashboardDto.getId() + '/updateWeeklyScores',
                requestContentType: 'application/json'
        )

        then:"the request returns 403"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "weekly scores were not updated"
        weeklyScoreRepository.count() == 0L

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