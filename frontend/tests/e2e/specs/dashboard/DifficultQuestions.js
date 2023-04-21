describe('Student Walkthrough', () => {
    beforeEach(() => {
        //clean database
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

    afterEach(() => {
        cy.deleteDifficultQuestions();
        cy.deleteQuestionsAndAnswers();
    });

    it('student solves quiz, accesses dashboard and then accesses difficult questions', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
        cy.intercept('GET', '**/difficultquestions').as('getDifficultQuestions');
        cy.intercept('DELETE', '**/remove').as('removeDifficultQuestion');
        cy.intercept('PUT', '**/update').as('updateDifficultQuestions');



        cy.demoStudentLogin();
        cy.solveQuizzDQ('Quiz Title', 2, 1);

        cy.get('[data-cy="dashboardMenuButton"]').should('be.visible').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="DifficultQuestionsMenuButton"]').should('be.visible').click();
        cy.wait('@getDifficultQuestions');

        cy.get('[data-cy="refreshDifficultQuestionsButton"]').should('be.visible').click();
        cy.wait('@updateDifficultQuestions');

        cy.get('[data-cy="showQuestionMenuButton"]').should('be.visible').first().click();

        cy.get('[data-cy="closeButton"]').click();

        cy.get('[data-cy="deleteDifficultQuestionButton"]').should('have.length.at.least', 1).eq(0).click();
        cy.wait('@removeDifficultQuestion');

        cy.get('[data-cy="deleteDifficultQuestionButton"]').should('have.length.at.least', 1).eq(0).click();
        cy.wait('@removeDifficultQuestion');

        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});