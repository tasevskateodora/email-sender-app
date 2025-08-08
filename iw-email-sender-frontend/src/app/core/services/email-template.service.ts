import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { EmailTemplate } from '../../shared/models/index';

@Injectable({
  providedIn: 'root'
})
export class EmailTemplateService {
  private apiUrl = 'http://localhost:8080/api/v1/email-templates';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('access_token');
    console.log('Template service - Token exists:', !!token);

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` })
    });

    console.log('Template service headers:', headers.keys());
    return headers;
  }

  getAll(): Observable<EmailTemplate[]> {
    console.log('Getting all templates');
    const headers = this.getAuthHeaders();

    return this.http.get<EmailTemplate[]>(this.apiUrl, { headers }).pipe(
      tap({
        next: (templates) => {
          console.log('Got templates:', templates.length);
        },
        error: (error) => {
          console.error('Failed to get templates:', error);
        }
      })
    );
  }

  getById(id: string): Observable<EmailTemplate> {
    console.log('Getting template by ID:', id);
    const headers = this.getAuthHeaders();

    return this.http.get<EmailTemplate>(`${this.apiUrl}/${id}`, { headers }).pipe(
      tap({
        next: (template) => {
          console.log('Got template:', template);
        },
        error: (error) => {
          console.error('Failed to get template:', error);
        }
      })
    );
  }

  create(template: EmailTemplate): Observable<EmailTemplate> {
    console.log('Creating template:', template);
    const headers = this.getAuthHeaders();

    return this.http.post<EmailTemplate>(this.apiUrl, template, { headers }).pipe(
      tap({
        next: (created) => {
          console.log('Template created:', created);
        },
        error: (error) => {
          console.error('Template creation failed:', error);
        }
      })
    );
  }

  update(id: string, template: EmailTemplate): Observable<EmailTemplate> {
    console.log('Updating template:', id, template);
    const headers = this.getAuthHeaders();

    return this.http.put<EmailTemplate>(`${this.apiUrl}/${id}`, template, { headers }).pipe(
      tap({
        next: (updated) => {
          console.log('Template updated:', updated);
        },
        error: (error) => {
          console.error('Template update failed:', error);
        }
      })
    );
  }

  delete(id: string): Observable<void> {
    console.log('Deleting template:', id);
    const headers = this.getAuthHeaders();

    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers }).pipe(
      tap({
        next: () => {
          console.log('Template deleted:', id);
        },
        error: (error) => {
          console.error('Template deletion failed:', error);
        }
      })
    );
  }
}
