<template>
  <v-container fluid>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :custom-filter="customFilter"
        :items="difficultQuestions"
        class="elevation-3"
        :sort-by="['percentage']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="10"
        :footer-props="{ itemsPerPageOptions: [10, 25, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <h2>Difficult Questions</h2>
            <v-spacer />
            <v-card-actions>
              <v-btn
                  color="primary"
                  dark
                  data-cy="refreshDifficultQuestionsButton"
                  v-on:click = "DifficultQuestionsRefresh"
              >Refresh</v-btn>
            </v-card-actions>
          </v-card-title>
        </template>

        <template v-slot:[`item.action`]="{ item }">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                  <v-icon
                    class="mr-2 action-button"
                    v-on="on"
                    data-cy="showQuestionMenuButton"
                    v-on:click="showStudentViewDialog(item.questionDto)"
                  >school</v-icon>
              </template>
              <span>Student View</span>
            </v-tooltip>
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  class="mr-2 action-button"
                  v-on="on"
                  data-cy="deleteDifficultQuestionButton"
                  v-on:click="deleteDifficultQuestion(item)"
                  color="red"
                  >delete</v-icon
                >
              </template>
            <span>Delete DifficultQuestion</span>
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
import { Component, Vue, Prop } from 'vue-property-decorator';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import Question from '@/models/management/Question';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';
import Dashboard from '@/models/dashboard/Dashboard';
@Component({
  components: { 'student-view-dialog': StudentViewDialog },
})
export default class DifficultQuestionsView extends Vue {
  @Prop() readonly dashboardId!: number;
  difficultQuestions: DifficultQuestion[] = [];
  statementQuestion: StatementQuestion | null = null;
  studentViewDialog: boolean = false;
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
      value: 'questionDto.title', 
      width: '75%', 
      align: 'left',
      sortable: false 
    },
    {
      text: 'Percentage',
      value: 'percentage',
      width: '15%',
      align: 'center',
    },
  ];
  async created() {
    await this.$store.dispatch('loading');
    try{
        this.difficultQuestions = await RemoteServices.getDifficultQuestions(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async DifficultQuestionsRefresh() {
    await this.$store.dispatch('loading');
    try{
        await RemoteServices.updateDifficultQuestions(this.dashboardId);
        this.difficultQuestions = await RemoteServices.getDifficultQuestions(this.dashboardId);
        this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    
  }

  async deleteDifficultQuestion(toDeleteDifficultQuestion: DifficultQuestion) {
    await this.$store.dispatch('loading');
    try{
        await RemoteServices.removeDifficultQuestions(toDeleteDifficultQuestion.id);
        this.difficultQuestions = this.difficultQuestions.filter(
          (DifficultQuestion) => DifficultQuestion.id != toDeleteDifficultQuestion.id
        );
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

  
  onCloseStudentViewDialog() {
    this.statementQuestion = null;
    this.studentViewDialog = false;
  }
}
</script>