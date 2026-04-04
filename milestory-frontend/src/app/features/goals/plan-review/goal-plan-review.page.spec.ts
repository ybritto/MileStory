import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { PreviewGoalPlan200Response } from '../../../../api/model/previewGoalPlan200Response';
import { GoalPlanningStore } from '../shared/goal-planning.store';
import { GoalPlanReviewPage } from './goal-plan-review.page';

describe('GoalPlanReviewPage', () => {
  let fixture: ComponentFixture<GoalPlanReviewPage>;
  let navigate: ReturnType<typeof vi.fn>;
  let saveGoal: ReturnType<typeof vi.fn>;
  let setCustomizedFromSuggestion: ReturnType<typeof vi.fn>;
  let preview = signal(createPreview(false));

  beforeEach(async () => {
    navigate = vi.fn();
    saveGoal = vi.fn();
    setCustomizedFromSuggestion = vi.fn();
    preview = signal(createPreview(false));

    await TestBed.configureTestingModule({
      imports: [GoalPlanReviewPage],
      providers: [
        {
          provide: GoalPlanningStore,
          useValue: {
            goal: signal(null).asReadonly(),
            previewPayload: preview.asReadonly(),
            customizedFromSuggestion: signal(false).asReadonly(),
            viewState: signal({ kind: 'previewReady' }).asReadonly(),
            loadGoalCategories: vi.fn(),
            setCustomizedFromSuggestion,
            saveGoal,
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
                get: vi.fn(() => null),
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(GoalPlanReviewPage);
    fixture.detectChanges();
  });

  it('renders the suggestion summary and fallback guidance', () => {
    expect(textContent()).toContain('Review and refine your plan');
    expect(textContent()).toContain('GENERIC_FALLBACK');
    expect(textContent()).toContain('A generic plan keeps the path moving');
    expect(fixture.nativeElement.querySelectorAll('.checkpoint-card').length).toBe(2);
  });

  it('allows checkpoint edits, add/remove actions, and customized labeling', async () => {
    fixture.componentInstance.form.at(0).get('targetValue')?.setValue(20);
    fixture.componentInstance.addCheckpoint();
    fixture.detectChanges();

    expect(setCustomizedFromSuggestion).toHaveBeenCalledWith(true);
    expect(textContent()).toContain('Customized from suggestion');
    expect(fixture.nativeElement.querySelectorAll('.checkpoint-card').length).toBe(3);

    fixture.componentInstance.removeCheckpoint(2);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.checkpoint-card').length).toBe(2);
  });
});

function createPreview(customized: boolean): PreviewGoalPlan200Response {
  return {
    year: 2026,
    planningYear: 2026,
    title: 'Read 24 books',
    categoryId: 'starter-category-id',
    targetValue: 24,
    unit: 'books',
    motivation: 'Build a steady reading habit.',
    notes: 'Mix novels and nonfiction.',
    suggestionBasis: PreviewGoalPlan200Response.SuggestionBasisEnum.GenericFallback,
    customizedFromSuggestion: customized,
    plannedPathSummary: 'A gentle monthly path with room to refine it.',
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
