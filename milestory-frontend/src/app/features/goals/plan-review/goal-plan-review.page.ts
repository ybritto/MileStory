import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { CreateGoalRequest } from '../../../../api/model/createGoalRequest';
import { CreateGoalRequestCheckpointsInner } from '../../../../api/model/createGoalRequestCheckpointsInner';
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { PreviewGoalPlan200Response } from '../../../../api/model/previewGoalPlan200Response';
import { UpdateGoalRequest } from '../../../../api/model/updateGoalRequest';
import { GoalPlanningStore } from '../shared/goal-planning.store';

@Component({
  selector: 'app-goal-plan-review-page',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './goal-plan-review.page.html',
  styleUrl: './goal-plan-review.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GoalPlanReviewPage {
  private readonly goalPlanningStore = inject(GoalPlanningStore);
  private readonly formBuilder = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly editingGoalId = this.route.snapshot.paramMap.get('goalId');
  private hasSeededPlan = false;

  readonly checkpointForm = this.formBuilder.array<FormGroup>([]);
  readonly checkpointEntries = signal<FormGroup[]>([]);
  readonly reviewForm = this.formBuilder.group({
    checkpoints: this.checkpointForm,
  });
  readonly viewState = this.goalPlanningStore.viewState;
  readonly previewPayload = this.goalPlanningStore.previewPayload;
  readonly customizedFromSuggestion = this.goalPlanningStore.customizedFromSuggestion;
  readonly currentGoal = this.goalPlanningStore.goal;
  readonly isEditing = computed(() => Boolean(this.editingGoalId));
  readonly snapshot = computed<PreviewGoalPlan200Response | ListGoals200ResponseInner | null>(
    () => this.previewPayload() ?? this.currentGoal(),
  );
  readonly summaryTitle = computed(() => this.snapshot()?.title ?? 'Review your plan');
  readonly isFallbackSuggestion = computed(
    () => this.snapshot()?.suggestionBasis === PreviewGoalPlan200Response.SuggestionBasisEnum.GenericFallback,
  );
  readonly goalSummary = computed(() => {
    const snapshot = this.snapshot();

    if (!snapshot) {
      return [];
    }

    return [
      `${snapshot.targetValue} ${snapshot.unit}`,
      snapshot.motivation,
      snapshot.notes,
      snapshot.plannedPathSummary,
    ];
  });

  constructor() {
    if (this.editingGoalId) {
      this.goalPlanningStore.loadGoal(this.editingGoalId);
    }

    effect(() => {
      const snapshot = this.snapshot();

      if (!snapshot || this.hasSeededPlan) {
        return;
      }

      this.hasSeededPlan = true;
      this.syncCheckpointForm(snapshot.checkpoints);
    });

    effect(() => {
      const customized = this.hasCustomizedFromSuggestion();
      this.goalPlanningStore.setCustomizedFromSuggestion(customized);
    });
  }

  get form(): FormArray<FormGroup> {
    return this.checkpointForm;
  }

  addCheckpoint(): void {
    const nextIndex = this.checkpointForm.length + 1;
    const nextDate = this.resolveNewCheckpointDate();
    const checkpoint = this.createCheckpointGroup(nextIndex, nextDate, 0, '', 'USER_ADDED');
    this.checkpointForm.push(checkpoint);
    this.checkpointEntries.update((entries) => [...entries, checkpoint]);
    this.goalPlanningStore.setCustomizedFromSuggestion(true);
  }

  removeCheckpoint(index: number): void {
    if (index < 0 || index >= this.checkpointForm.length) {
      return;
    }

    this.checkpointForm.removeAt(index);
    this.checkpointEntries.update((entries) => entries.filter((_, entryIndex) => entryIndex !== index));
    this.resequenceCheckpoints();
    this.goalPlanningStore.setCustomizedFromSuggestion(true);
  }

  trackCheckpoint(index: number, control: AbstractControl): number {
    return index;
  }

  savePlan(): void {
    const snapshot = this.snapshot();

    if (!snapshot) {
      return;
    }

    const request = this.buildRequest(snapshot);
    const goalId = this.editingGoalId;

    this.goalPlanningStore
      .saveGoal(goalId, request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (goal) => {
          void this.router.navigate(['/goals', goal.goalId]);
        },
      });
  }

  checkpointLabel(index: number): string {
    return `Checkpoint ${index + 1}`;
  }

  isCustomized(): boolean {
    const snapshot = this.snapshot();

    if (!snapshot) {
      return false;
    }

    return this.goalPlanningStore.customizedFromSuggestion() || this.hasCustomizedFromSuggestion();
  }

  private syncCheckpointForm(checkpoints: PreviewGoalPlan200Response['checkpoints']): void {
    this.checkpointForm.clear();
    const nextEntries: FormGroup[] = [];

    checkpoints.forEach((checkpoint, index) => {
      nextEntries.push(
        this.createCheckpointGroup(
          checkpoint.sequenceNumber || index + 1,
          checkpoint.checkpointDate,
          checkpoint.targetValue,
          checkpoint.note,
          checkpoint.origin,
          checkpoint.originalCheckpointDate,
          checkpoint.originalTargetValue,
        ),
      );
    });

    nextEntries.forEach((checkpoint) => this.checkpointForm.push(checkpoint));
    this.checkpointEntries.set(nextEntries);

    this.checkpointForm.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.goalPlanningStore.setCustomizedFromSuggestion(this.hasCustomizedFromSuggestion());
    });
  }

  private createCheckpointGroup(
    sequenceNumber: number,
    checkpointDate: string,
    targetValue: number,
    note: string,
    origin: CreateGoalRequestCheckpointsInner.OriginEnum,
    originalCheckpointDate?: string,
    originalTargetValue?: number,
  ): FormGroup {
    return this.formBuilder.group({
      checkpointDate: this.formBuilder.nonNullable.control(checkpointDate, [Validators.required]),
      targetValue: this.formBuilder.nonNullable.control(targetValue, [Validators.required]),
      note: this.formBuilder.nonNullable.control(note),
      sequenceNumber: this.formBuilder.nonNullable.control(sequenceNumber),
      origin: this.formBuilder.nonNullable.control(origin),
      originalCheckpointDate: this.formBuilder.nonNullable.control(originalCheckpointDate ?? checkpointDate),
      originalTargetValue: this.formBuilder.nonNullable.control(originalTargetValue ?? targetValue),
    });
  }

  private resequenceCheckpoints(): void {
    this.checkpointForm.controls.forEach((control, index) => {
      control.get('sequenceNumber')?.setValue(index + 1, { emitEvent: false });
    });
  }

  private hasCustomizedFromSuggestion(): boolean {
    return this.checkpointForm.controls.some((control) => {
      const date = control.get('checkpointDate')?.value;
      const value = Number(control.get('targetValue')?.value);
      const originalDate = control.get('originalCheckpointDate')?.value;
      const originalValue = Number(control.get('originalTargetValue')?.value);
      const note = control.get('note')?.value ?? '';

      return date !== originalDate || value !== originalValue || note.trim().length > 0;
    });
  }

  private resolveNewCheckpointDate(): string {
    if (this.checkpointForm.length === 0) {
      return `${this.snapshot()?.planningYear ?? new Date().getFullYear()}-12-31`;
    }

    const last = this.checkpointForm.at(this.checkpointForm.length - 1)?.get('checkpointDate')?.value as string | undefined;
    if (!last) {
      return `${this.snapshot()?.planningYear ?? new Date().getFullYear()}-12-31`;
    }

    const date = new Date(`${last}T00:00:00`);
    date.setMonth(date.getMonth() + 1);
    return date.toISOString().slice(0, 10);
  }

  private buildRequest(snapshot: PreviewGoalPlan200Response | ListGoals200ResponseInner): CreateGoalRequest | UpdateGoalRequest {
    const checkpoints = this.checkpointForm.controls.map((control) => ({
      checkpointDate: control.get('checkpointDate')?.value,
      targetValue: Number(control.get('targetValue')?.value),
      note: control.get('note')?.value ?? '',
      origin: control.get('origin')?.value,
    }));

    return {
      title: snapshot.title,
      categoryId: snapshot.categoryId,
      targetValue: snapshot.targetValue,
      unit: snapshot.unit,
      motivation: snapshot.motivation,
      notes: snapshot.notes,
      suggestionBasis: snapshot.suggestionBasis,
      customizedFromSuggestion: this.goalPlanningStore.customizedFromSuggestion(),
      checkpoints,
    };
  }
}
