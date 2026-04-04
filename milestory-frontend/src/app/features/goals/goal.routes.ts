import { Routes } from '@angular/router';

export const GOAL_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'new',
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./create/goal-create.page').then((module) => module.GoalCreatePage),
  },
  {
    path: 'new/plan',
    loadComponent: () =>
      import('./plan-review/goal-plan-review.page').then((module) => module.GoalPlanReviewPage),
  },
  {
    path: ':goalId/edit',
    loadComponent: () =>
      import('./create/goal-create.page').then((module) => module.GoalCreatePage),
  },
  {
    path: ':goalId/edit/plan',
    loadComponent: () =>
      import('./plan-review/goal-plan-review.page').then((module) => module.GoalPlanReviewPage),
  },
  {
    path: 'archive',
    loadComponent: () =>
      import('./archive/goal-archive.page').then((module) => module.GoalArchivePage),
  },
  {
    path: ':goalId',
    loadComponent: () =>
      import('./detail/goal-detail.page').then((module) => module.GoalDetailPage),
  },
];
