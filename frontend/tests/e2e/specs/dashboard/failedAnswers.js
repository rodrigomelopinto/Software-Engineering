describe('Student FailedAnswers Access', () => {
    beforeEach(() => {
        cy.deleteQuestionsAndAnswers();
        //create quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question Title',
            'Question',
            'Option',
            'Option',
            'Option',
            'Correct'
          );
        cy.createQuestion(
            'Question Title2',
            'Question',
            'Option',
            'Option',
            'Option',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'Quiz Title',
            'Question Title',
            'Question Title2'
        );
        cy.contains('Logout').click();
    });

    afterEach(() =>{
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    });

    it('student accesses failed answers', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
        cy.intercept('GET', '**/failedanswers').as('getFailedAnwers');
        cy.intercept('DELETE', '**/remove').as('removeFailedAnswer');
        cy.intercept('PUT', '**/update').as('updateFailedAnswers');

        cy.demoStudentLogin();
        cy.solveQuizz('Quiz Title', 2);

        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="FailedAnswersMenuButton"]').click();
        cy.wait('@getFailedAnwers');

        cy.get('[data-cy="refreshFailedAnswersMenuButton"]').click();
        cy.wait('@updateFailedAnswers');

        cy.get('[data-cy="showQuestionMenuButton"]').should('be.visible').first().click();

        cy.get('[data-cy="closeButton"]').click();
        
        cy.get('[data-cy="deleteFailedAnswerMenuButton"]').should('be.visible').first().click();
        cy.wait('@removeFailedAnswer');

        cy.closeErrorMessage();

        cy.setFailedAnswersAsOld();

        cy.get('[data-cy="deleteFailedAnswerMenuButton"]').should('be.visible').first().click();
        cy.wait('@removeFailedAnswer');


        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});