import { QuestionAnswer } from '@/models/management/QuestionAnswer';
import { ISOtoString } from '@/services/ConvertDateService';

export default class FailedAnswer {
  id!: number;
  answered!: boolean;
  questionAnswerDto!: QuestionAnswer;
  collected!: string;

  constructor(jsonObj?: FailedAnswer) {
    if (jsonObj) {
        this.id = jsonObj.id;
        this.answered = jsonObj.answered;
        this.questionAnswerDto = new QuestionAnswer(jsonObj.questionAnswerDto);
        this.collected = ISOtoString(jsonObj.collected);
    }
  }
}
