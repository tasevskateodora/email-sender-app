import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideNativeDateAdapter } from '@angular/material/core';

import { App } from './app/app';
import { routes } from './app/app.routes';

import { AuthInterceptor } from './app/core/interceptors/jwt.interceptor';

console.log('Starting Angular with AuthInterceptor');
bootstrapApplication(App, {
  providers: [
    provideRouter(routes),

    provideHttpClient(withInterceptors([AuthInterceptor])),

    provideAnimations(),
    provideNativeDateAdapter(),

  ]
}).then(() => {
  console.log('Angular started with interceptors configured');
}).catch(err => {
  console.error('Angular bootstrap error:', err);
});



