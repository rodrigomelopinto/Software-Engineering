<template>
  <v-container fluid>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="weeklyScores"
        :sort-by="['week']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <h2>Weekly Scores</h2>
            <v-spacer />
            <v-card-actions>
              <v-btn
                  color="primary"
                  dark
                  data-cy="weeklyScoreRefreshButton"
                  @click="WeeklyScoresRefresh"
              >REFRESH</v-btn>
            </v-card-actions>
          </v-card-title>
        </template>

        <template v-slot:[`item.percentageCorrect`]="{ item }">
          {{ item.percentageCorrect }}
        </template>
        
        <template v-slot:[`item.uniquelyAnswered`]="{ item }">
          {{ item.uniquelyAnswered }}
        </template>

        <template v-slot:[`item.numberAnswered`]="{ item }">
          {{ item.numberAnswered }}
        </template>
        
        <template v-slot:[`item.week`]="{ item }">
          {{ item.week }}
        </template>

        <template v-slot:[`item.action`]="{ item }">
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <v-icon
                  class="mr-2 action-button"
                  v-on="on"
                  data-cy="deleteWeeklyScoreButton"
                  @click="deleteWeeklyScore(item)"
                  color="red"
                  >delete</v-icon>
              </template>
            <span>Delete WeeklyScore</span>
            </v-tooltip>
        </template>

      </v-data-table>
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import WeeklyScore from '@/models/dashboard/WeeklyScore';
import Dashboard from '@/models/dashboard/Dashboard';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import RemoteServices from '@/services/RemoteServices';

@Component({components: { AnimatedNumber },})
export default class WeeklyScoreView extends Vue {
  @Prop() readonly dashboardId!: number;
  weeklyScores: WeeklyScore[] = [];
  headers: object = [

    {text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,},

    {text: 'Week',
    value: 'week',
    width: '50%',
    align: 'left'},

    {text: 'Number Answered',
    value: 'numberAnswered',
    width: '50%',
    align: 'left' },

    {text: 'Uniquely Answered',
    value: 'uniquelyAnswered',
    width: '50%',
    align: 'left' },

    {text: 'Percentage Correct',
    value: 'percentageCorrect',
    width: '50%',
    align: 'left' }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try{
        this.weeklyScores = await RemoteServices.getUserWeeklyScores(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async WeeklyScoresRefresh(){
    await this.$store.dispatch('loading');
    try{
      this.weeklyScores = await RemoteServices.updateUserWeeklyScores(this.dashboardId);
      this.$emit('refresh');
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

    async deleteWeeklyScore(toDeleteWeeklyScore: WeeklyScore) {
    await this.$store.dispatch('loading');
    try{
        await RemoteServices.removeUserWeeklyScores(toDeleteWeeklyScore.id);
        this.weeklyScores = this.weeklyScores.filter(
          (WeeklyScore) => WeeklyScore.id != toDeleteWeeklyScore.id
        );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
    
  }

}

</script>