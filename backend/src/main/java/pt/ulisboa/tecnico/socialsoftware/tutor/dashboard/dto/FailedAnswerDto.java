package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuestionAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

public class FailedAnswerDto implements Serializable {

    private Integer id;

    private boolean answered;

    private QuestionAnswerDto questionAnswerDto;

    private String collected;

    public FailedAnswerDto(){
    }

    public FailedAnswerDto(FailedAnswer failedAnswer){
        setId(failedAnswer.getId());
        setAnswered(failedAnswer.getAnswered());
        setQuestionAnswerDto(new QuestionAnswerDto(failedAnswer.getQuestionAnswer()));
        collected = DateHandler.toISOString(failedAnswer.getCollected());
    }

    public Integer getId() {
        return id;
    }

    public boolean getAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public String getCollected(){
        return this.collected;
    }

    public void setCollected(String collected){
        this.collected = collected;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public QuestionAnswerDto getQuestionAnswerDto() {
        return questionAnswerDto;
    }

    public void setQuestionAnswerDto(QuestionAnswerDto questionAnswerDto) {
        this.questionAnswerDto = questionAnswerDto;
    }

    @Override
    public String toString() {
        return "FailedAnswerDto{" +
            "id=" + id +
            ", answered=" + answered +
            "}";
    }
}
