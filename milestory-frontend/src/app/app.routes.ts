import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/home/home.page').then((module) => module.HomePage),
  },
  {
    path: 'goals',
    loadChildren: () =>
      import('./features/goals/goal.routes').then((module) => module.GOAL_ROUTES),
  },
];
