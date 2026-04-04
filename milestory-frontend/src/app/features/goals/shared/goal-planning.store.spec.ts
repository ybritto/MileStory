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
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { PreviewGoalPlan200Response } from '../../../../api/model/previewGoalPlan200Response';
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
    checkpoints: [
      {
        checkpointId: 'checkpoint-1',
        sequenceNumber: 1,
        checkpointDate: '2026-01-31',
        targetValue: 2,
        note: 'Start gently.',
        origin: 'SUGGESTED',
      },
    ],
    ...overrides,
  };
}
