import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

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
import { RecordGoalProgressEntryRequest } from '../../../../api/model/recordGoalProgressEntryRequest';
import { RestoreGoalRequest } from '../../../../api/model/restoreGoalRequest';
import { UpdateGoalRequest } from '../../../../api/model/updateGoalRequest';
import {
  GoalDraftInput,
  GoalPlanningStore,
} from './goal-planning.store';

describe('GoalPlanningStore', () => {
  let goalCategoriesService: {
    listGoalCategories: ReturnType<typeof vi.fn>;
    createGoalCategory: ReturnType<typeof vi.fn>;
  };
  let goalPlanningService: {
    previewGoalPlan: ReturnType<typeof vi.fn>;
  };
  let goalsService: {
    createGoal: ReturnType<typeof vi.fn>;
    updateGoal: ReturnType<typeof vi.fn>;
    getGoal: ReturnType<typeof vi.fn>;
    listGoals: ReturnType<typeof vi.fn>;
    archiveGoal: ReturnType<typeof vi.fn>;
    restoreGoal: ReturnType<typeof vi.fn>;
    recordGoalProgressEntry: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    goalCategoriesService = {
      listGoalCategories: vi.fn(),
      createGoalCategory: vi.fn(),
    };
    goalPlanningService = {
      previewGoalPlan: vi.fn(),
    };
    goalsService = {
      createGoal: vi.fn(),
      updateGoal: vi.fn(),
      getGoal: vi.fn(),
      listGoals: vi.fn(),
      archiveGoal: vi.fn(),
      restoreGoal: vi.fn(),
      recordGoalProgressEntry: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        GoalPlanningStore,
        {
          provide: GoalCategoriesService,
          useValue: goalCategoriesService as unknown as GoalCategoriesService,
        },
        {
          provide: GoalPlanningService,
          useValue: goalPlanningService as unknown as GoalPlanningService,
        },
        {
          provide: GoalsService,
          useValue: goalsService as unknown as GoalsService,
        },
      ],
    });
  });

  it('loads goal categories into signal state', () => {
    goalCategoriesService.listGoalCategories.mockReturnValue(of(createCategories()));

    const store = TestBed.inject(GoalPlanningStore);

    store.loadGoalCategories();

    expect(goalCategoriesService.listGoalCategories).toHaveBeenCalledTimes(1);
    expect(store.goalCategories()).toEqual(createCategories());
  });

  it('previews a custom draft and records backend customization metadata', () => {
    goalCategoriesService.createGoalCategory.mockReturnValue(of(createCategories()[1]));
    goalPlanningService.previewGoalPlan.mockReturnValue(of(createPreview()));

    const store = TestBed.inject(GoalPlanningStore);

    const draft: GoalDraftInput = {
      title: 'Practice writing every day',
      categoryMode: 'custom',
      categoryId: 'placeholder',
      customCategoryName: 'Creative Practice',
      targetValue: 180,
      unit: 'sessions',
      motivation: 'Build a durable writing habit.',
      notes: 'Use a short prompt every weekday.',
    };

    store.previewDraft(draft).subscribe();

    expect(goalCategoriesService.createGoalCategory).toHaveBeenCalledWith({
      displayName: 'Creative Practice',
    } satisfies CreateGoalCategoryRequest);
    expect(goalPlanningService.previewGoalPlan).toHaveBeenCalledTimes(1);
    expect(store.viewState().kind).toBe('previewReady');
    expect(store.previewPayload()).toEqual(createPreview());
    expect(store.customizedFromSuggestion()).toBe(true);
  });

  it('creates a goal when no id is supplied', () => {
    goalsService.createGoal.mockReturnValue(of(createGoal()));

    const store = TestBed.inject(GoalPlanningStore);

    const request: CreateGoalRequest = {
      title: 'Read 24 books',
      categoryId: 'goal-category-id',
      targetValue: 24,
      unit: 'books',
      motivation: 'Keep a steady reading practice.',
      notes: 'Mix fiction and long-form nonfiction.',
      suggestionBasis: CreateGoalRequest.SuggestionBasisEnum.CategoryAware,
      customizedFromSuggestion: false,
      checkpoints: [],
    };

    store.saveGoal(null, request).subscribe((goal) => {
      expect(goal).toEqual(createGoal());
    });

    expect(goalsService.createGoal).toHaveBeenCalledWith(request);
    expect(store.goal()).toEqual(createGoal());
  });

  it('updates a goal when an id is supplied', () => {
    goalsService.updateGoal.mockReturnValue(of(createGoal({ title: 'Updated goal' })));

    const store = TestBed.inject(GoalPlanningStore);

    const request: UpdateGoalRequest = {
      title: 'Updated goal',
      categoryId: 'goal-category-id',
      targetValue: 42,
      unit: 'hours',
      motivation: 'Keep the habit strong.',
      notes: 'Balance effort through the year.',
      suggestionBasis: UpdateGoalRequest.SuggestionBasisEnum.GenericFallback,
      customizedFromSuggestion: true,
      checkpoints: [],
    };

    store.saveGoal('goal-id', request).subscribe((goal) => {
      expect(goal.title).toBe('Updated goal');
    });

    expect(goalsService.updateGoal).toHaveBeenCalledWith('goal-id', request);
    expect(store.goal()?.title).toBe('Updated goal');
  });

  it('loads archived goals and restores them with the requested mode', () => {
    goalsService.listGoals.mockReturnValue(of([createGoal({ status: 'ARCHIVED' })]));
    goalsService.restoreGoal.mockReturnValue(of(createGoal()));

    const store = TestBed.inject(GoalPlanningStore);

    store.loadGoals('ARCHIVED');

    expect(goalsService.listGoals).toHaveBeenCalledWith('ARCHIVED');
    expect(store.goals()).toEqual([createGoal({ status: 'ARCHIVED' })]);

    store.restoreGoal('goal-id', RestoreGoalRequest.ModeEnum.KeepExisting).subscribe();

    expect(goalsService.restoreGoal).toHaveBeenCalledWith('goal-id', {
      mode: RestoreGoalRequest.ModeEnum.KeepExisting,
    });
    expect(store.goals()).toEqual([]);
  });

  it('records a progress update, reloads the goal detail, and exposes the normal success message', () => {
    const refreshedGoal = createGoal({
      progressEntries: [createProgressEntry()],
    });
    const request: RecordGoalProgressEntryRequest = {
      entryDate: '2026-04-04',
      progressValue: 9,
      note: 'Finished another chapter.',
    };

    goalsService.recordGoalProgressEntry.mockReturnValue(of(createProgressEntry()));
    goalsService.getGoal.mockReturnValue(of(refreshedGoal));

    const store = TestBed.inject(GoalPlanningStore);

    store.recordProgress('goal-id', request).subscribe((entry: ListGoals200ResponseInnerProgressEntriesInner) => {
      expect(entry).toEqual(createProgressEntry());
    });

    expect(goalsService.recordGoalProgressEntry).toHaveBeenCalledWith('goal-id', request);
    expect(goalsService.getGoal).toHaveBeenCalledWith('goal-id');
    expect(store.goal()).toEqual(refreshedGoal);
    expect(store.successMessage()).toBe('Progress updated. Your status has been refreshed.');
  });

  it('uses the correction success message when the backend marks the update as a correction', () => {
    goalsService.recordGoalProgressEntry.mockReturnValue(
      of(
        createProgressEntry({
          entryType: ListGoals200ResponseInnerProgressEntriesInner.EntryTypeEnum.Correction,
        }),
      ),
    );
    goalsService.getGoal.mockReturnValue(of(createGoal()));

    const store = TestBed.inject(GoalPlanningStore);

    store
      .recordProgress('goal-id', {
        entryDate: '2026-04-04',
        progressValue: 7,
      })
      .subscribe();

    expect(store.successMessage()).toBe('Correction saved. Milestory updated your status from the new total.');
  });

  it('blocks opening the progress overlay for an archived goal through explicit store state', () => {
    const store = TestBed.inject(GoalPlanningStore);

    goalsService.getGoal.mockReturnValue(of(createGoal({ status: 'ARCHIVED' })));
    store.loadGoal('goal-id');
    store.openProgressOverlay();

    const overlayState = store.progressOverlayState();

    expect(overlayState.kind).toBe('blocked');
    if (overlayState.kind !== 'blocked') {
      throw new Error('Expected blocked overlay state for archived goal.');
    }
    expect(overlayState.message).toBe('Archived goals no longer accept progress updates.');
  });
});

function createCategories(): ListGoalCategories200ResponseInner[] {
  return [
    {
      categoryId: 'starter-category-id',
      key: 'reading',
      displayName: 'Reading',
      systemDefined: true,
    },
    {
      categoryId: 'custom-category-id',
      key: 'creative-practice',
      displayName: 'Creative Practice',
      systemDefined: false,
    },
  ];
}

function createPreview(): PreviewGoalPlan200Response {
  return {
    year: 2026,
    planningYear: 2026,
    title: 'Practice writing every day',
    categoryId: 'custom-category-id',
    targetValue: 180,
    unit: 'sessions',
    motivation: 'Build a durable writing habit.',
    notes: 'Use a short prompt every weekday.',
    suggestionBasis: PreviewGoalPlan200Response.SuggestionBasisEnum.GenericFallback,
    customizedFromSuggestion: true,
    plannedPathSummary: 'A steady monthly path with room to refine it.',
    checkpoints: [
      {
        checkpointId: 'checkpoint-1',
        sequenceNumber: 1,
        checkpointDate: '2026-01-31',
        targetValue: 15,
        note: 'Start gently.',
        origin: 'SUGGESTED',
        progressContextLabel: 'Expected by now',
        progressContextDetail: 'You planned to reach 15 sessions by this point.',
      },
    ],
  };
}

function createGoal(
  overrides: Partial<ListGoals200ResponseInner> = {},
): ListGoals200ResponseInner {
  return {
    goalId: 'goal-id',
    planningYear: 2026,
    title: 'Read 24 books',
    categoryId: 'starter-category-id',
    targetValue: 24,
    unit: 'books',
    motivation: 'Keep a steady reading practice.',
    notes: 'Mix fiction and long-form nonfiction.',
    status: 'ACTIVE',
    suggestionBasis: 'CATEGORY_AWARE',
    customizedFromSuggestion: false,
    plannedPathSummary: 'Monthly milestones carry the target across the year.',
    currentProgressValue: 6,
    progressPercentOfTarget: 25,
    expectedProgressValueToday: 6,
    paceStatus: 'ON_PACE',
    paceSummary: "You're right where this goal expected you to be today.",
    paceDetail: 'You are matching the planned pace for today.',
    progressEntries: [],
    checkpoints: [
      {
        checkpointId: 'checkpoint-1',
        sequenceNumber: 1,
        checkpointDate: '2026-01-31',
        targetValue: 2,
        note: 'Start gently.',
        origin: 'SUGGESTED',
        progressContextLabel: 'Expected by now',
        progressContextDetail: 'You planned to reach 2 books by this point.',
      },
    ],
    ...overrides,
  };
}

function createProgressEntry(
  overrides: Partial<ListGoals200ResponseInnerProgressEntriesInner> = {},
): ListGoals200ResponseInnerProgressEntriesInner {
  return {
    progressEntryId: 'progress-entry-id',
    entryDate: '2026-04-04',
    progressValue: 9,
    note: 'Finished another chapter.',
    entryType: ListGoals200ResponseInnerProgressEntriesInner.EntryTypeEnum.Normal,
    recordedAt: '2026-04-04T10:00:00Z',
    ...overrides,
  };
}
