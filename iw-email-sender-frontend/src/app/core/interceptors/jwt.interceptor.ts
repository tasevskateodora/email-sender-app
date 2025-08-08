import { HttpInterceptorFn } from '@angular/common/http';
import { tap } from 'rxjs/operators';

export const AuthInterceptor: HttpInterceptorFn = (req, next) => {

  console.log('FUNCTIONAL INTERCEPTOR CALLED!');
  console.log('Request URL:', req.url);
  console.log('Request method:', req.method);

  const token = localStorage.getItem('access_token');
  console.log('Token exists:', !!token);

  if (token) {
    console.log('Token preview:', token.substring(0, 30) + '...');

    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    console.log('Authorization header added!');
    console.log('Request headers:');
    authReq.headers.keys().forEach(key => {
      console.log(`  ${key}: ${authReq.headers.get(key)?.substring(0, 50)}...`);
    });

    return next(authReq).pipe(
      tap({
        next: (event) => {
          console.log('HTTP request successful for:', req.url);
        },
        error: (error) => {
          console.error('HTTP request failed for:', req.url, error);
        }
      })
    );
  } else {
    console.log('No token found!');
  }

  console.log('Proceeding with original request (no token)');
  return next(req);
};
