import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { EmailJob, EmailJobResponse } from '../../shared/models/index';

@Injectable({
  providedIn: 'root'
})
export class EmailJobService {
 private apiUrl = 'http://localhost:8080/api/v1/email-jobs';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('access_token');
    console.log('Manual auth - Token exists:', !!token);

    if (token) {
      console.log('Manual auth - Token preview:', token.substring(0, 30) + '...');
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    });

    console.log('Manual headers created:', headers.keys());
    return headers;
  }

  create(job: EmailJob): Observable<EmailJob> {
    const headers = this.getAuthHeaders();
    return this.http.post<EmailJob>(`${this.apiUrl}/user`, job, { headers });
  }

  getAll(): Observable<EmailJob[]> {
    console.log('Getting all jobs');
    const headers = this.getAuthHeaders();

    return this.http.get<EmailJob[]>(this.apiUrl, { headers }).pipe(
      tap({
        next: (jobs) => {
          console.log('Got all jobs:', jobs.length);
        },
        error: (error) => {
          console.error('Failed to get all jobs:', error);
        }
      })
    );
  }

  getByUserId(userId: string): Observable<EmailJob[]> {
    console.log('Getting jobs for user:', userId);
    const headers = this.getAuthHeaders();

    return this.http.get<EmailJob[]>(`${this.apiUrl}/user/${userId}`, { headers }).pipe(
      tap({
        next: (jobs) => {
          console.log('Got user jobs:', jobs.length);
        },
        error: (error) => {
          console.error('Failed to get user jobs:', error);
        }
      })
    );
  }

  update(id: string, userId: string, job: EmailJob): Observable<EmailJob> {
    console.log('Updating job:', id, 'for user:', userId);
    const headers = this.getAuthHeaders();

    return this.http.put<EmailJob>(`${this.apiUrl}/${id}/user/${userId}`, job, { headers }).pipe(
      tap({
        next: (response) => {
          console.log('Job updated successfully:', response);
        },
        error: (error) => {
          console.error('Job update failed:', error);
        }
      })
    );
  }

  delete(id: string): Observable<void> {
    console.log('Deleting job:', id);
    const headers = this.getAuthHeaders();

    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers }).pipe(
      tap({
        next: () => {
          console.log('Job deleted successfully:', id);
        },
        error: (error) => {
          console.error('Job deletion failed:', error);
        }
      })
    );
  }

  enableJob(id: string): Observable<EmailJobResponse> {
    console.log('Enabling job:', id);
    const headers = this.getAuthHeaders();

    return this.http.put<EmailJobResponse>(`${this.apiUrl}/${id}/enable`, {}, { headers }).pipe(
      tap({
        next: (response) => {
          console.log('Job enabled:', response);
        },
        error: (error) => {
          console.error('Job enable failed:', error);
        }
      })
    );
  }

  disableJob(id: string): Observable<EmailJobResponse> {
    console.log('Disabling job:', id);
    const headers = this.getAuthHeaders();

    return this.http.put<EmailJobResponse>(`${this.apiUrl}/${id}/disable`, {}, { headers }).pipe(
      tap({
        next: (response) => {
          console.log('Job disabled:', response);
        },
        error: (error) => {
          console.error('Job disable failed:', error);
        }
      })
    );
  }

  toggleJobStatus(id: string, enabled: boolean): Observable<EmailJobResponse> {
    return enabled ? this.enableJob(id) : this.disableJob(id);
  }

  getById(id: string): Observable<EmailJob> {
    console.log('Getting job by ID:', id);
    const headers = this.getAuthHeaders();

    return this.http.get<EmailJob>(`${this.apiUrl}/${id}`, { headers }).pipe(
      tap({
        next: (job) => {
          console.log('Got job by ID:', job);
        },
        error: (error) => {
          console.error('Failed to get job by ID:', error);
        }
      })
    );
  }

}
