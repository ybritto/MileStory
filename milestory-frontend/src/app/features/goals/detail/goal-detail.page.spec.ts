import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ListGoalCategories200ResponseInner } from '../../../../api/model/listGoalCategories200ResponseInner';
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { GoalPlanningStore } from '../shared/goal-planning.store';
import { GoalDetailPage } from './goal-detail.page';

describe('GoalDetailPage', () => {
  let fixture: ComponentFixture<GoalDetailPage>;
  let loadGoal: ReturnType<typeof vi.fn>;
  let loadGoalCategories: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    loadGoal = vi.fn();
    loadGoalCategories = vi.fn();
    navigate = vi.fn();

    await TestBed.configureTestingModule({
      imports: [GoalDetailPage],
      providers: [
        {
          provide: GoalPlanningStore,
          useValue: {
            goal: signal(createGoal()).asReadonly(),
            goalCategories: signal(createCategories()).asReadonly(),
            viewState: signal({ kind: 'idle' }).asReadonly(),
            loadGoal,
            loadGoalCategories,
            archiveGoal: vi.fn(),
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: vi.fn(() => 'goal-id'),
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GoalDetailPage);
    fixture.detectChanges();
  });

  it('renders the planned path summary and checkpoint plan', () => {
    expect(textContent()).toContain('Read 24 books');
    expect(textContent()).toContain('Planned path');
    expect(textContent()).toContain('Monthly milestones carry the target across the year.');
    expect(textContent()).toContain('Customized from suggestion');
    expect(textContent()).toContain('Category-based plan');
    expect(textContent()).not.toContain('CATEGORY_AWARE');
    expect(fixture.nativeElement.querySelectorAll('.checkpoint-card').length).toBe(2);
  });

  it('shows archive and edit actions for an active goal', () => {
    expect(textContent()).toContain('Archive goal');
    expect(textContent()).toContain('Edit plan');
  });

  it('centers archived action pills like buttons', () => {
    const styles = ((GoalDetailPage as unknown as { ɵcmp: { styles: string[] } }).ɵcmp.styles).join('\n');

    expect(styles).toMatch(/display:\s*inline-flex/);
    expect(styles).toMatch(/align-items:\s*center/);
    expect(styles).toMatch(/justify-content:\s*center/);
  });

  it('adds breathing room above the action row', () => {
    const styles = ((GoalDetailPage as unknown as { ɵcmp: { styles: string[] } }).ɵcmp.styles).join('\n');

    expect(styles).toMatch(/margin-top:\s*var\(--space-lg\)/);
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
  ];
}

function createGoal(): ListGoals200ResponseInner {
  return {
    goalId: 'goal-id',
    planningYear: 2026,
    title: 'Read 24 books',
    categoryId: 'starter-category-id',
    targetValue: 24,
    unit: 'books',
    motivation: 'Build a steady reading habit.',
    notes: 'Mix novels and nonfiction.',
    status: 'ACTIVE',
    suggestionBasis: 'CATEGORY_AWARE',
    customizedFromSuggestion: true,
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
        note: 'Start with a manageable pace.',
        origin: 'SUGGESTED',
        progressContextLabel: 'Expected by now',
        progressContextDetail: 'You planned to reach 2 books by this point.',
      },
      {
        checkpointId: 'checkpoint-2',
        sequenceNumber: 2,
        checkpointDate: '2026-02-28',
        targetValue: 4,
        note: 'Hold steady through February.',
        origin: 'SUGGESTED',
        progressContextLabel: 'Upcoming checkpoint',
        progressContextDetail: 'The next target is 4 books by February 28.',
      },
    ],
  };
}

function textContent(): string {
  return document.body.textContent ?? '';
}
