<template>
  <v-container fluid>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :custom-filter="customFilter"
        :items="failedAnswers"
        :sort-by="['collected']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <h2>Failed Answers</h2>
            <v-spacer />
            <v-card-actions>
              <v-btn
                  color="primary"
                  dark
                  data-cy="refreshFailedAnswersMenuButton"
                  @click="FailedAnswersRefresh"
              >Refresh</v-btn>
            </v-card-actions>
          </v-card-title>
        </template>
        
        <template v-slot:[`item.collected`]="{ item }">
          {{ item.collected }}
        </template>

        <template v-slot:[`item.questionAnswerDto`]="{ item }">
          {{ item.questionAnswerDto.question.title }}
        </template>

        <template v-slot:[`item.answered`]="{ item }">
          <v-chip
            :color="getAnsweredColor(item.answered)"
            dark
            >
            {{ item.answered == true ? 'YES' : 'NO' }}
          </v-chip>
        </template>

        <template v-slot:[`item.action`]="{ item }">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                  <v-icon
                    class="mr-2 action-button"
                    v-on="on"
                    data-cy="showQuestionMenuButton"
                    @click="showStudentViewDialog(item.questionAnswerDto.question)"
                  >school</v-icon>
              </template>
              <span>Student View</span>
            </v-tooltip>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  class="mr-2 action-button"
                  v-on="on"
                  data-cy="deleteFailedAnswerMenuButton"
                  @click="deleteFailedAnswer(item)"
                  color="red"
                  >delete</v-icon
                >
              </template>
            <span>Delete FailedAnswer</span>
            </v-tooltip>
        </template>

      </v-data-table>
      <student-view-dialog
        v-if="statementQuestion && studentViewDialog"
        v-model="studentViewDialog"
        :statementQuestion="statementQuestion"
        v-on:close-show-question-dialog="onCloseStudentViewDialog"
      />
      
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import FailedAnswer from '@/models/dashboard/FailedAnswer';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import Question from '@/models/management/Question';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';
import Dashboard from '@/models/dashboard/Dashboard';

@Component({
  components: { 'student-view-dialog': StudentViewDialog },
})
export default class FailedAnswersView extends Vue {
  failedAnswers: FailedAnswer[] = [];
  statementQuestion: StatementQuestion | null = null;
  studentViewDialog: boolean = false;
  @Prop() readonly dashboardId!: number;
  dashboard: Dashboard | null = null;

  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,
    },
    { 
      text: 'Question', 
      value: 'questionAnswerDto', 
      width: '50%', 
      align: 'left',
      sortable: false 
    },
    { 
      text: 'Answered', 
      value: 'answered', 
      width: '50%', 
      align: 'right',
      sortable: false 
    },
    {
      text: 'Collected',
      value: 'collected',
      width: '50%',
      align: 'right',

    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try{
        this.failedAnswers = await RemoteServices.getUserFailedAnswers(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async showStudentViewDialog(question: Question) {
    if (question.id) {
      try {
        this.statementQuestion = await RemoteServices.getStatementQuestion(
          question.id
        );
        this.studentViewDialog = true;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async FailedAnswersRefresh() {
    await this.$store.dispatch('loading');
    try{
        await RemoteServices.updateUserFailedAnswers(this.dashboardId);
        this.failedAnswers = await RemoteServices.getUserFailedAnswers(this.dashboardId);
        this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    
  }

  async deleteFailedAnswer(toDeleteFailedAnswer: FailedAnswer) {
    await this.$store.dispatch('loading');
    try{
        await RemoteServices.removeUserFailedAnswer(toDeleteFailedAnswer.id);
        this.failedAnswers = this.failedAnswers.filter(
          (failedAnswer) => failedAnswer.id != toDeleteFailedAnswer.id
        );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    
  }

  onCloseStudentViewDialog() {
    this.statementQuestion = null;
    this.studentViewDialog = false;
  }

  getAnsweredColor(answered: boolean) {
    if (answered == true) return 'green';
    else return 'red';
  }

}
</script>