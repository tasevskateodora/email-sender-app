import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EmailExecution {
  id: string;
  executedAt: string;
  status: 'SUCCESS' | 'FAIL';
  errorMessage: string;
  retryAttempt: number;
  createdAt: string;
  emailJobId: string;
  jobSenderEmail: string;
  jobReceiverEmails: string;
}

@Injectable({ providedIn: 'root' })
export class EmailExecutionService {
  private apiUrl = 'http://localhost:8080/api/email-executions';

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
    getAll(): Observable<EmailExecution[]> {
    return this.http.get<EmailExecution[]>(this.apiUrl);
  }
}
