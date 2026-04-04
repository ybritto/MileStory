import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ListGoalCategories200ResponseInner } from '../../../../api/model/listGoalCategories200ResponseInner';
import { ListGoals200ResponseInner } from '../../../../api/model/listGoals200ResponseInner';
import { GoalPlanningStore } from '../shared/goal-planning.store';
import { GoalArchivePage } from './goal-archive.page';

describe('GoalArchivePage', () => {
  let fixture: ComponentFixture<GoalArchivePage>;
  let loadGoals: ReturnType<typeof vi.fn>;
  let restoreGoal: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    loadGoals = vi.fn();
    restoreGoal = vi.fn();
    navigate = vi.fn();
    restoreGoal.mockReturnValue(of(createGoal()));

    await TestBed.configureTestingModule({
      imports: [GoalArchivePage],
      providers: [
        {
          provide: GoalPlanningStore,
          useValue: {
            goalCategories: signal(createCategories()).asReadonly(),
            goals: signal([createGoal()]).asReadonly(),
            goal: signal(null).asReadonly(),
            viewState: signal({ kind: 'idle' }).asReadonly(),
            loadGoalCategories: vi.fn(),
            loadGoals,
            restoreGoal,
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GoalArchivePage);
    fixture.detectChanges();
  });

  it('renders archived goals as read-only entries', () => {
    expect(textContent()).toContain('Archive');
    expect(textContent()).toContain('Archived');
    expect(textContent()).toContain('Restoring keeps the plan readable');
    expect(fixture.nativeElement.querySelectorAll('.archive-card').length).toBe(1);
  });

  it('offers keep existing and regenerate restore actions', () => {
    fixture.nativeElement.querySelector('[data-action="keep-existing"]').click();
    fixture.nativeElement.querySelector('[data-action="regenerate"]').click();

    expect(restoreGoal).toHaveBeenNthCalledWith(1, 'goal-id', 'KEEP_EXISTING');
    expect(restoreGoal).toHaveBeenNthCalledWith(2, 'goal-id', 'REGENERATE');
  });

  it('centers archive action pills like buttons', () => {
    const styles = ((GoalArchivePage as unknown as { ɵcmp: { styles: string[] } }).ɵcmp.styles).join('\n');

    expect(styles).toMatch(/display:\s*inline-flex/);
    expect(styles).toMatch(/align-items:\s*center/);
    expect(styles).toMatch(/justify-content:\s*center/);
  });
});

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
    status: 'ARCHIVED',
    suggestionBasis: 'CATEGORY_AWARE',
    customizedFromSuggestion: true,
    plannedPathSummary: 'Monthly milestones carry the target across the year.',
    currentProgressValue: 18,
    progressPercentOfTarget: 75,
    expectedProgressValueToday: 20,
    paceStatus: 'BEHIND',
    paceSummary: "You're behind the pace you planned for today, but the year still has room to recover.",
    paceDetail: 'You are 2 books behind the pace expected by today.',
    archivedAt: '2026-04-03T22:29:05Z',
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
    ],
  };
}

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

function textContent(): string {
  return document.body.textContent ?? '';
}
