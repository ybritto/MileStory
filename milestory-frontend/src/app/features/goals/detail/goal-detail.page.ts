import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { GoalPlanningStore } from '../shared/goal-planning.store';

@Component({
  selector: 'app-goal-detail-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './goal-detail.page.html',
  styleUrl: './goal-detail.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GoalDetailPage {
  private readonly goalPlanningStore = inject(GoalPlanningStore);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly goal = this.goalPlanningStore.goal;
  readonly categories = this.goalPlanningStore.goalCategories;
  readonly categoryLabel = computed(() => {
    const goal = this.goal();

    if (!goal) {
      return '';
    }

    return (
      this.categories().find((category) => category.categoryId === goal.categoryId)?.displayName ??
      goal.categoryId
    );
  });
  readonly checkpointCount = computed(() => this.goal()?.checkpoints.length ?? 0);
  readonly checkpointCards = computed(() => this.goal()?.checkpoints ?? []);

  constructor() {
    this.goalPlanningStore.loadGoalCategories();

    const goalId = this.route.snapshot.paramMap.get('goalId');
    if (goalId) {
      this.goalPlanningStore.loadGoal(goalId);
    }
  }

  editGoal(): void {
    const goal = this.goal();
    if (!goal) {
      return;
    }

    void this.router.navigate(['/goals', goal.goalId, 'edit']);
  }

  archiveGoal(): void {
    const goal = this.goal();
    if (!goal) {
      return;
    }

    this.goalPlanningStore.archiveGoal(goal.goalId).subscribe({
      next: () => {
        void this.router.navigate(['/goals/archive']);
      },
    });
  }

}
