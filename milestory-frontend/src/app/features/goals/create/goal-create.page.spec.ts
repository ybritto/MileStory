import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { ListGoalCategories200ResponseInner } from '../../../../api/model/listGoalCategories200ResponseInner';
import { GoalPlanningStore } from '../shared/goal-planning.store';
import { GoalCreatePage } from './goal-create.page';

describe('GoalCreatePage', () => {
  let fixture: ComponentFixture<GoalCreatePage>;
  let previewDraft: ReturnType<typeof vi.fn>;
  let loadGoalCategories: ReturnType<typeof vi.fn>;
  let navigate: ReturnType<typeof vi.fn>;
  let categories = signal<ListGoalCategories200ResponseInner[]>([]);

  beforeEach(async () => {
    previewDraft = vi.fn();
    loadGoalCategories = vi.fn();
    navigate = vi.fn();
    categories = signal(createCategories());

    await TestBed.configureTestingModule({
      imports: [GoalCreatePage],
      providers: [
        {
          provide: GoalPlanningStore,
          useValue: {
            goal: signal(null).asReadonly(),
            goalCategories: categories.asReadonly(),
            loadGoalCategories,
            previewDraft,
            viewState: signal({ kind: 'idle' }).asReadonly(),
            previewPayload: signal(null).asReadonly(),
            customizedFromSuggestion: signal(false).asReadonly(),
            setCustomizedFromSuggestion: vi.fn(),
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

    fixture = TestBed.createComponent(GoalCreatePage);
    fixture.detectChanges();
  });

  it('renders starter categories and the custom category path', () => {
    expect(loadGoalCategories).toHaveBeenCalledTimes(1);
    expect(textContent()).toContain('Create a yearly goal');
    expect(textContent()).toContain('Reading');
    expect(textContent()).toContain('Financial');
    expect(textContent()).toContain('Create custom category');
    expect(fixture.nativeElement.querySelector('input[formControlName="customCategoryName"]')).not
      .toBeNull();
  });

  it('sends the draft into preview mode and continues to the review route', async () => {
    fixture.componentInstance.form.patchValue({
      title: 'Practice writing every day',
      categoryMode: 'custom',
      categoryId: 'starter-category-id',
      customCategoryName: 'Creative Practice',
      targetValue: 180,
      unit: 'sessions',
      motivation: 'Build a durable writing habit.',
      notes: 'Use a short prompt every weekday.',
    });

    previewDraft.mockReturnValue({
      subscribe: (handlers: { next?: () => void }) => handlers.next?.(),
    });

    fixture.nativeElement.querySelector('button[type="submit"]').click();

    expect(previewDraft).toHaveBeenCalledWith({
      title: 'Practice writing every day',
      categoryMode: 'custom',
      categoryId: 'starter-category-id',
      customCategoryName: 'Creative Practice',
      targetValue: 180,
      unit: 'sessions',
      motivation: 'Build a durable writing habit.',
      notes: 'Use a short prompt every weekday.',
    });
    expect(navigate).toHaveBeenCalledWith(['/goals/new/plan']);
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
      categoryId: 'financial-category-id',
      key: 'financial',
      displayName: 'Financial',
      systemDefined: true,
    },
  ];
}

function textContent(): string {
  return document.body.textContent ?? '';
}
