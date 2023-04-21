<template>
  <div class="container">
    <h2>Dashboard</h2>

    <v-card class="table">
      <v-row>
        <v-col>
          <v-btn
            color="primary"
            dark
            v-on:click="show = 'Global'"
          >
            Global Statistics
          </v-btn>
        </v-col>
        <v-col>
          <v-btn color="primary"
                 dark
                 data-cy="weeklyScoreButton"
                 v-on:click="show = 'Weekly'"
          >Weekly Scores <br> {{dashboard != null && dashboard.lastCheckWeeklyScores != null ? dashboard.lastCheckWeeklyScores : '-'}}</v-btn
          >
        </v-col>
        <v-col>
          <v-btn
            color="primary"
            dark
            data-cy="FailedAnswersMenuButton"
            v-on:click="show = 'Failed'"

          >Failed Answers <br>  {{dashboard.lastCheckFailedAnswers != null ? dashboard.lastCheckFailedAnswers : '-'}}</v-btn
          ></v-col
        >
        <v-col>
          <v-btn 
            color="primary"
            dark
            data-cy="DifficultQuestionsMenuButton"
            v-on:click="show = 'Difficult'"
            >Difficult Questions <br/> 
            {{dashboard.lastCheckDifficultQuestions != null ? dashboard.lastCheckDifficultQuestions : '-'}}
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <div v-if="show === 'Global'" class="stats-container">
      <global-stats-view></global-stats-view>
    </div>

    <div v-if="show === 'Difficult'" class="stats-container">
      <difficult-questions-view :dashboardId="dashboard.id" v-on:refresh="onDifficultQuestionsRefresh">
      </difficult-questions-view>
    </div>
    
    <div v-if="show === 'Failed'" class="stats-container">
      <failed-answers-view :dashboardId="dashboard.id" v-on:refresh="onFailedAnswersRefresh">
      </failed-answers-view>
    </div>

    <div v-if="show === 'Weekly'" class="stats-container">
      <weekly-scores-view
        :dashboardId="dashboard.id"
        v-on:refresh="onWeeklyScoresRefresh"
      >
      </weekly-scores-view>
    </div>


  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import GlobalStatsView from '@/views/student/GlobalStatsView.vue';
import FailedAnswersView from '@/views/student/FailedAnswersView.vue';
import WeeklyScoresView from '@/views/student/WeeklyScoreView.vue';
import Dashboard from '@/models/dashboard/Dashboard';
import DifficultQuestionsView from '@/views/student/DifficultQuestionsView.vue';


@Component({

  components: { GlobalStatsView , 
                FailedAnswersView ,
                WeeklyScoresView,
                DifficultQuestionsView }
})
export default class StatsView extends Vue {
  dashboard: Dashboard | null = null;
  show: string = 'Global';

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async onDifficultQuestionsRefresh(){
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
  
  async onFailedAnswersRefresh(){
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async onWeeklyScoresRefresh() {
    await this.$store.dispatch('loading');
    try {
      this.dashboard = await RemoteServices.getUserDashboard();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>
