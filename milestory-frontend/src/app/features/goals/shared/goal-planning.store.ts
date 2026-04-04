import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';

import { GoalCategoriesService } from '../../../../api/api/goalCategories.service';
import { GoalPlanningService } from '../../../../api/api/goalPlanning.service';
import { GoalsService } from '../../../../api/api/goals.service';
import { ArchiveGoal200Response } from '../../../../api/model/archiveGoal200Response';
import { CreateGoalCategoryRequest } from '../../../../api/model/createGoalCategoryRequest';
import { CreateGoalRequest } from '../../../../api/model/createGoalRequest';
import { ListGoalCategories200ResponseInner } from '../../../../api/model/listGoalCategories200ResponseInner';
import { ListGoals200ResponseInnerProgressEntriesInner } from '../../../../api/model/listGoals200ResponseInnerProgressEntriesInner';
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { PreviewGoalPlan200Response } from '../../../../api/model/previewGoalPlan200Response';
import { PreviewGoalPlanRequest } from '../../../../api/model/previewGoalPlanRequest';
import { RecordGoalProgressEntryRequest } from '../../../../api/model/recordGoalProgressEntryRequest';
import { RestoreGoalRequest } from '../../../../api/model/restoreGoalRequest';
import { UpdateGoalRequest } from '../../../../api/model/updateGoalRequest';

export interface GoalDraftInput {
  title: string;
  categoryMode: 'starter' | 'custom';
  categoryId: string;
  customCategoryName: string;
  targetValue: number;
  unit: string;
  motivation: string;
  notes: string;
}

export type GoalPlanningViewState =
  | { kind: 'idle' }
  | { kind: 'loading' }
  | { kind: 'previewReady' }
  | { kind: 'saving' }
  | { kind: 'error'; message: string };

export type ProgressOverlayState =
  | { kind: 'closed' }
  | { kind: 'open' }
  | { kind: 'submitting' }
  | { kind: 'blocked'; message: string };

@Injectable({
  providedIn: 'root',
})
export class GoalPlanningStore {
  private readonly goalCategoriesService = inject(GoalCategoriesService);
  private readonly goalPlanningService = inject(GoalPlanningService);
  private readonly goalsService = inject(GoalsService);

  private readonly categoriesState = signal<ListGoalCategories200ResponseInner[]>([]);
  private readonly previewState = signal<PreviewGoalPlan200Response | null>(null);
  private readonly customizedState = signal(false);
  private readonly viewStateState = signal<GoalPlanningViewState>({ kind: 'idle' });
  private readonly goalState = signal<ListGoals200ResponseInner | null>(null);
  private readonly goalsState = signal<ListGoals200ResponseInner[]>([]);
  private readonly progressOverlayStateState = signal<ProgressOverlayState>({ kind: 'closed' });
  private readonly successMessageState = signal<string | null>(null);
  private currentGoalId: string | null = null;

  readonly goalCategories = this.categoriesState.asReadonly();
  readonly previewPayload = this.previewState.asReadonly();
  readonly customizedFromSuggestion = this.customizedState.asReadonly();
  readonly viewState = this.viewStateState.asReadonly();
  readonly goal = this.goalState.asReadonly();
  readonly goals = this.goalsState.asReadonly();
  readonly progressOverlayState = this.progressOverlayStateState.asReadonly();
  readonly successMessage = this.successMessageState.asReadonly();

  loadGoalCategories(): void {
    this.goalCategoriesService.listGoalCategories().subscribe({
      next: (categories) => this.categoriesState.set(categories),
      error: () =>
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not load the goal categories. Confirm the backend goal API is available, then try again.',
        }),
    });
  }

  setCustomizedFromSuggestion(customizedFromSuggestion: boolean): void {
    this.customizedState.set(customizedFromSuggestion);
  }

  previewDraft(draft: GoalDraftInput): Observable<PreviewGoalPlan200Response> {
    this.viewStateState.set({ kind: 'loading' });

    return this.resolveCategoryId(draft).pipe(
      switchMap((categoryId) =>
        this.goalPlanningService.previewGoalPlan(this.toPreviewRequest(draft, categoryId)),
      ),
      tap((payload) => {
        this.previewState.set(payload);
        this.customizedState.set(payload.customizedFromSuggestion);
        this.viewStateState.set({ kind: 'previewReady' });
      }),
      catchError((error: unknown) => {
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not build the goal preview. Confirm the backend goal API is available, then try again.',
        });
        return throwError(() => error);
      }),
    );
  }

  saveGoal(goalId: string | null, request: CreateGoalRequest | UpdateGoalRequest): Observable<ListGoals200ResponseInner> {
    this.viewStateState.set({ kind: 'saving' });

    const operation = goalId
      ? this.goalsService.updateGoal(goalId, request)
      : this.goalsService.createGoal(request);

    return operation.pipe(
      tap((goal) => {
        this.goalState.set(goal);
        this.previewState.set(goalToPreview(goal));
        this.customizedState.set(goal.customizedFromSuggestion);
        this.viewStateState.set({ kind: 'idle' });
        this.upsertGoal(goal);
      }),
      catchError((error: unknown) => {
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not save the goal. Confirm the backend goal API is available, then try again.',
        });
        return throwError(() => error);
      }),
    );
  }

  loadGoal(goalId: string): void {
    if (this.currentGoalId !== goalId) {
      this.successMessageState.set(null);
      this.progressOverlayStateState.set({ kind: 'closed' });
      this.currentGoalId = goalId;
    }

    this.goalsService.getGoal(goalId).subscribe({
      next: (goal) => {
        this.goalState.set(goal);
        this.previewState.set(goalToPreview(goal));
        this.customizedState.set(goal.customizedFromSuggestion);
        this.upsertGoal(goal);
      },
      error: () =>
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not load the goal details. Confirm the backend goal API is available, then try again.',
        }),
    });
  }

  openProgressOverlay(): void {
    this.successMessageState.set(null);

    if (this.goal()?.status === 'ARCHIVED') {
      this.progressOverlayStateState.set({
        kind: 'blocked',
        message: 'Archived goals no longer accept progress updates.',
      });
      return;
    }

    this.progressOverlayStateState.set({ kind: 'open' });
  }

  closeProgressOverlay(): void {
    this.progressOverlayStateState.set({ kind: 'closed' });
  }

  recordProgress(
    goalId: string,
    request: RecordGoalProgressEntryRequest,
  ): Observable<ListGoals200ResponseInnerProgressEntriesInner> {
    this.progressOverlayStateState.set({ kind: 'submitting' });

    return this.goalsService.recordGoalProgressEntry(goalId, request).pipe(
      switchMap((entry) =>
        this.goalsService.getGoal(goalId).pipe(
          tap((goal) => {
            this.goalState.set(goal);
            this.previewState.set(goalToPreview(goal));
            this.customizedState.set(goal.customizedFromSuggestion);
            this.upsertGoal(goal);
            this.successMessageState.set(getProgressSuccessMessage(entry));
            this.progressOverlayStateState.set({ kind: 'closed' });
          }),
          map(() => entry),
        ),
      ),
      catchError((error: unknown) => {
        this.progressOverlayStateState.set({ kind: 'open' });
        return throwError(() => error);
      }),
    );
  }

  loadGoals(status?: 'ACTIVE' | 'ARCHIVED'): void {
    this.goalsService.listGoals(status).subscribe({
      next: (goals) => this.goalsState.set(goals),
      error: () =>
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not load the goal archive. Confirm the backend goal API is available, then try again.',
        }),
    });
  }

  archiveGoal(goalId: string): Observable<ArchiveGoal200Response> {
    this.viewStateState.set({ kind: 'saving' });

    return this.goalsService.archiveGoal(goalId).pipe(
      tap((payload) => {
        this.goalState.update((goal) =>
          goal && goal.goalId === goalId ? { ...goal, status: 'ARCHIVED', archivedAt: payload.archivedAt } : goal,
        );
        this.goalsState.update((goals) => goals.filter((goal) => goal.goalId !== goalId));
        this.viewStateState.set({ kind: 'idle' });
      }),
      catchError((error: unknown) => {
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not archive the goal. Confirm the backend goal API is available, then try again.',
        });
        return throwError(() => error);
      }),
    );
  }

  restoreGoal(goalId: string, mode: RestoreGoalRequest.ModeEnum): Observable<ListGoals200ResponseInner> {
    this.viewStateState.set({ kind: 'saving' });

    return this.goalsService.restoreGoal(goalId, { mode }).pipe(
      tap((goal) => {
        this.goalState.set(goal);
        this.previewState.set(goalToPreview(goal));
        this.customizedState.set(goal.customizedFromSuggestion);
        this.viewStateState.set({ kind: 'idle' });
        this.goalsState.update((goals) => goals.filter((entry) => entry.goalId !== goalId));
      }),
      catchError((error: unknown) => {
        this.viewStateState.set({
          kind: 'error',
          message:
            'Milestory could not restore the goal. Confirm the backend goal API is available, then try again.',
        });
        return throwError(() => error);
      }),
    );
  }

  private resolveCategoryId(draft: GoalDraftInput): Observable<string> {
    if (draft.categoryMode !== 'custom') {
      return of(draft.categoryId);
    }

    const request: CreateGoalCategoryRequest = {
      displayName: draft.customCategoryName,
    };

    return this.goalCategoriesService.createGoalCategory(request).pipe(map((category) => category.categoryId));
  }

  private toPreviewRequest(draft: GoalDraftInput, categoryId: string): PreviewGoalPlanRequest {
    return {
      title: draft.title,
      categoryId,
      targetValue: draft.targetValue,
      unit: draft.unit,
      motivation: draft.motivation,
      notes: draft.notes,
    };
  }

  private upsertGoal(goal: ListGoals200ResponseInner): void {
    this.goalsState.update((goals) => {
      const index = goals.findIndex((entry) => entry.goalId === goal.goalId);

      if (index === -1) {
        return [goal, ...goals];
      }

      const nextGoals = [...goals];
      nextGoals[index] = goal;
      return nextGoals;
    });
  }
}

function getProgressSuccessMessage(entry: ListGoals200ResponseInnerProgressEntriesInner): string {
  return entry.entryType === ListGoals200ResponseInnerProgressEntriesInner.EntryTypeEnum.Correction
    ? 'Correction saved. Milestory updated your status from the new total.'
    : 'Progress updated. Your status has been refreshed.';
}

function goalToPreview(goal: ListGoals200ResponseInner): PreviewGoalPlan200Response {
  return {
    year: goal.planningYear,
    planningYear: goal.planningYear,
    title: goal.title,
    categoryId: goal.categoryId,
    targetValue: goal.targetValue,
    unit: goal.unit,
    motivation: goal.motivation,
    notes: goal.notes,
    suggestionBasis: goal.suggestionBasis,
    customizedFromSuggestion: goal.customizedFromSuggestion,
    plannedPathSummary: goal.plannedPathSummary,
    checkpoints: goal.checkpoints,
  };
}
