import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { RestoreGoalRequest } from '../../../../api/model/restoreGoalRequest';
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { GoalPlanningStore } from '../shared/goal-planning.store';

@Component({
  selector: 'app-goal-archive-page',
  imports: [CommonModule],
  templateUrl: './goal-archive.page.html',
  styleUrl: './goal-archive.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GoalArchivePage {
  private readonly goalPlanningStore = inject(GoalPlanningStore);
  private readonly router = inject(Router);

  readonly goals = this.goalPlanningStore.goals;
  readonly categories = this.goalPlanningStore.goalCategories;
  readonly archiveCount = computed(() => this.goals().length);

  constructor() {
    this.goalPlanningStore.loadGoalCategories();
    this.goalPlanningStore.loadGoals('ARCHIVED');
  }

  categoryLabel(categoryId: string): string {
    return (
      this.categories().find((category) => category.categoryId === categoryId)?.displayName ??
      categoryId
    );
  }

  restore(goalId: string, mode: RestoreGoalRequest.ModeEnum): void {
    this.goalPlanningStore.restoreGoal(goalId, mode).subscribe({
      next: () => {
        this.goalPlanningStore.loadGoals('ARCHIVED');
      },
    });
  }

  viewGoal(goal: ListGoals200ResponseInner): void {
    void this.router.navigate(['/goals', goal.goalId]);
  }
}
