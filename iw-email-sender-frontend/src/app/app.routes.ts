import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  // Public routes
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent)
  },

  // Protected routes
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then(m => m.DashboardComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'email-jobs',
    loadComponent: () => import('./features/email-jobs/email-job-list/email-job-list').then(m => m.EmailJobListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'templates',
    loadComponent: () => import('./features/templates/templates-list/templates-list.component').then(m => m.TemplatesListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'admin',
    children: [
      {
        path: 'users',
        loadComponent: () => import('./features/users/user-management/user-management.component').then(m => m.UserManagementComponent),
        canActivate: [AuthGuard]
      },
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full'
      }
    ]
  },

  // Default redirects
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];
