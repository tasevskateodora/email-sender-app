/*
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Api {

}
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmailJob, EmailJobResponse, EmailTemplate, User } from '../../shared/models';

@Injectable({
  providedIn: 'root'
})
export class EmailJobService {
  private apiUrl = 'http://localhost:8080/api/v1/email-jobs';

  constructor(private http: HttpClient) {}

  // Create email job for specific user
  createEmailJob(userId: string, emailJob: EmailJob): Observable<EmailJob> {
    return this.http.post<EmailJob>(`${this.apiUrl}/user/${userId}`, emailJob);
  }

  // Get all email jobs (ADMIN only)
  getAllEmailJobs(): Observable<EmailJob[]> {
    return this.http.get<EmailJob[]>(this.apiUrl);
  }

  // Get email job by ID
  getEmailJobById(id: string): Observable<EmailJob> {
    return this.http.get<EmailJob>(`${this.apiUrl}/${id}`);
  }

  // Update email job
  updateEmailJob(id: string, userId: string, emailJob: EmailJob): Observable<EmailJob> {
    return this.http.put<EmailJob>(`${this.apiUrl}/${id}/user/${userId}`, emailJob);
  }

  // Delete email job (ADMIN only)
  deleteEmailJob(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Get email jobs by user
  getEmailJobsByUser(userId: string): Observable<EmailJob[]> {
    return this.http.get<EmailJob[]>(`${this.apiUrl}/user/${userId}`);
  }

  // Enable email job
  enableEmailJob(id: string): Observable<EmailJobResponse> {
    return this.http.put<EmailJobResponse>(`${this.apiUrl}/${id}/enable`, {});
  }

  // Disable email job
  disableEmailJob(id: string): Observable<EmailJobResponse> {
    return this.http.put<EmailJobResponse>(`${this.apiUrl}/${id}/disable`, {});
  }

  // Toggle job status
  toggleJobStatus(id: string, enabled: boolean): Observable<EmailJobResponse> {
    return enabled ? this.enableEmailJob(id) : this.disableEmailJob(id);
  }
}

@Injectable({
  providedIn: 'root'
})
export class EmailTemplateService {
  private apiUrl = 'http://localhost:8080/api/v1/email-templates';

  constructor(private http: HttpClient) {}

  createTemplate(template: EmailTemplate): Observable<EmailTemplate> {
    return this.http.post<EmailTemplate>(this.apiUrl, template);
  }

  getAllTemplates(): Observable<EmailTemplate[]> {
    return this.http.get<EmailTemplate[]>(this.apiUrl);
  }

  getTemplateById(id: string): Observable<EmailTemplate> {
    return this.http.get<EmailTemplate>(`${this.apiUrl}/${id}`);
  }

  updateTemplate(id: string, template: EmailTemplate): Observable<EmailTemplate> {
    return this.http.put<EmailTemplate>(`${this.apiUrl}/${id}`, template);
  }

  deleteTemplate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = 'http://localhost:8080/api/v1/users';

  constructor(private http: HttpClient) {}

  createUser(user: User): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  updateUser(id: string, user: User): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
