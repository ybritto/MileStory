import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ListGoalCategories200ResponseInner } from '../../../../api/model/listGoalCategories200ResponseInner';
import { GoalPlanningStore, GoalDraftInput } from '../shared/goal-planning.store';

@Component({
  selector: 'app-goal-create-page',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './goal-create.page.html',
  styleUrl: './goal-create.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GoalCreatePage {
  private readonly goalPlanningStore = inject(GoalPlanningStore);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly goalId = this.route.snapshot.paramMap.get('goalId');
  private hasSeededGoal = false;

  readonly goalCategories = this.goalPlanningStore.goalCategories;
  readonly viewState = this.goalPlanningStore.viewState;
  readonly currentGoal = this.goalPlanningStore.goal;
  readonly isEditing = computed(() => Boolean(this.goalId));
  readonly titleText = computed(() =>
    this.isEditing() ? 'Edit a yearly goal' : 'Create a yearly goal',
  );
  readonly form = this.formBuilder.group({
    title: this.formBuilder.nonNullable.control('', [Validators.required]),
    categoryMode: this.formBuilder.nonNullable.control<'starter' | 'custom'>('starter'),
    categoryId: this.formBuilder.nonNullable.control('', [Validators.required]),
    customCategoryName: this.formBuilder.nonNullable.control(''),
    targetValue: this.formBuilder.nonNullable.control(0, [Validators.required, Validators.min(1)]),
    unit: this.formBuilder.nonNullable.control('', [Validators.required]),
    motivation: this.formBuilder.nonNullable.control('', [Validators.required]),
    notes: this.formBuilder.nonNullable.control('', [Validators.required]),
  });
  readonly selectedMode = signal<'starter' | 'custom'>('starter');

  constructor() {
    this.goalPlanningStore.loadGoalCategories();

    if (this.goalId) {
      this.goalPlanningStore.loadGoal(this.goalId);
    }

    effect(() => {
      const goal = this.currentGoal();

      if (!this.goalId || !goal || this.hasSeededGoal) {
        return;
      }

      this.hasSeededGoal = true;
      this.form.patchValue({
        title: goal.title,
        categoryMode: 'starter',
        categoryId: goal.categoryId,
        customCategoryName: '',
        targetValue: goal.targetValue,
        unit: goal.unit,
        motivation: goal.motivation,
        notes: goal.notes,
      });
      this.selectedMode.set('starter');
    });

    effect(() => {
      if (this.form.controls.categoryMode.value !== this.selectedMode()) {
        this.selectedMode.set(this.form.controls.categoryMode.value);
      }
    });
  }

  selectCategory(category: ListGoalCategories200ResponseInner): void {
    this.form.patchValue({
      categoryMode: 'starter',
      categoryId: category.categoryId,
      customCategoryName: '',
    });
  }

  selectCustomCategory(): void {
    this.form.patchValue({
      categoryMode: 'custom',
      categoryId: '',
      customCategoryName: '',
    });
  }

  continue(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const draft = this.form.getRawValue() as GoalDraftInput;
    const nextRoute = this.goalId ? ['/goals', this.goalId, 'edit', 'plan'] : ['/goals/new/plan'];

    this.goalPlanningStore.previewDraft(draft).subscribe({
      next: () => {
        void this.router.navigate(nextRoute);
      },
    });
  }

  isSelectedCategory(categoryId: string): boolean {
    return this.form.controls.categoryId.value === categoryId;
  }
}
