describe('Student Walkthrough', () => {
  beforeEach(() => {
    //clean database
    cy.deleteQuestionsAndAnswers();
    //create quiz
    cy.demoTeacherLogin();
    cy.createQuestion(
      'Weekly Score Question 1',
      'Question',
      'Option',
      'Option',
      'Option',
      'Correct'
    );
    cy.createQuestion(
      'Weekly Score Question 2',
      'Question',
      'Option',
      'Option',
      'Option',
      'Correct'
    );
    cy.createQuizzWith2Questions(
      'Weekly Score Quizz',
      'Weekly Score Question 1',
      'Weekly Score Question 2'
    );
    cy.contains('Logout').click();
  });

  afterEach( () => {
    cy.deleteWeeklyScores();
    cy.deleteQuestionsAndAnswers();
  });

  it('student answers quizz', () => {
    cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
    cy.intercept('GET', '/students/dashboards/*/weeklyScores').as('getWeeklyScores');
    cy.intercept('PUT', '/students/dashboards/*/updateWeeklyScores').as('updateWeeklyScores');
    cy.intercept('DELETE', '/weeklyScore/*').as('removeWeeklyScore');

    cy.demoStudentLogin();
    cy.solveQuizz('Weekly Score Quizz', 2);

    cy.contains('Logout').click();
    cy.demoStudentLogin();

    cy.contains('Dashboard').click();
    cy.wait('@getDashboard');

    cy.createWeeklyScore();

    cy.get('[data-cy="weeklyScoreButton"]').click();
    cy.wait('@getWeeklyScores');

    cy.get('[data-cy="deleteWeeklyScoreButton"]').should('be.visible').click();
    cy.wait('@removeWeeklyScore');

    cy.get('[data-cy="weeklyScoreRefreshButton"]').click();
    cy.wait('@updateWeeklyScores');

    cy.get('[data-cy="deleteWeeklyScoreButton"]').should('be.visible').click();
    cy.wait('@removeWeeklyScore');

    cy.closeErrorMessage();

    cy.contains('Logout').click();

    Cypress.on('uncaught:exception', (err, runnable) => {
      // returning false here prevents Cypress from
      // failing the test
      return false;
    });
  });
});
