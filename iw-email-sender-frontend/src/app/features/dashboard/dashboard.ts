import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { EmailJobService } from '../../core/services/api';
import { EmailJob, User } from '../../shared/models';

import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule
  ],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  recentJobs: EmailJob[] = [];
  stats = {
    totalJobs: 0,
    activeJobs: 0,
    completedToday: 0,
    pendingJobs: 0
  };
  loading = false;

  constructor(
    private authService: AuthService,
    private emailJobService: EmailJobService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    if (!this.currentUser) return;

    this.loading = true;

    this.emailJobService.getEmailJobsByUser(this.currentUser.id!).subscribe({
      next: (jobs) => {
        this.recentJobs = jobs.slice(0, 5);
        this.calculateStats(jobs);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard data:', error);
        this.loading = false;
      }
    });
  }

  private calculateStats(jobs: EmailJob[]): void {
    this.stats.totalJobs = jobs.length;
    this.stats.activeJobs = jobs.filter(job => job.enabled).length;
    this.stats.pendingJobs = jobs.filter(job => !job.enabled).length;
    this.stats.completedToday = 0; // Placeholder
  }

  navigateToEmailJobs(): void {
    this.router.navigate(['/email-jobs']);
  }

  createNewJob(): void {
    this.router.navigate(['/email-jobs']);
  }

  getWelcomeMessage(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  formatRecurrence(pattern: string): string {
    return pattern.toLowerCase().charAt(0).toUpperCase() + pattern.toLowerCase().slice(1);
  }
}
