import { Component, OnInit } from '@angular/core';
import {MatSnackBar, MatSnackBarModule} from '@angular/material/snack-bar';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import { AuthService } from '../../../core/services/auth';
import { EmailJob } from '../../../shared/models';
import { JobModalComponent } from '../job-modal/job-modal.component';
import { EmailJobService } from '../../../core/services/email-job.service';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatChipsModule} from '@angular/material/chips';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTableModule} from '@angular/material/table';
import {MatTooltipModule} from '@angular/material/tooltip';
import {NgClass, SlicePipe} from '@angular/common';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-email-job-list',
  templateUrl: './email-job-list.html',
  styleUrls: ['./email-job-list.css'],
  standalone: true,
  imports: [
    MatIconModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTableModule,
    MatTooltipModule,
    SlicePipe,
    NgClass,
    MatSlideToggleModule,
    CommonModule
  ]
})
export class EmailJobListComponent implements OnInit {
  emailJobs: EmailJob[] = [];
  loading = false;

  displayedColumns: string[] = [
    'senderEmail',
    'receiverEmails',
    'recurrencePattern',
    'enabled',
    'nextRunTime',
    'actions'
  ];

  constructor(
    private emailJobService: EmailJobService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadEmailJobs();
  }

  loadEmailJobs(): void {
    this.loading = true;
    const currentUser = this.authService.getCurrentUser();

    if (!currentUser) {
      this.loading = false;
      this.showError('User not authenticated.');
      return;
    }

    const loadObservable = this.authService.isAdmin()
      ? this.emailJobService.getAll()
      : this.emailJobService.getByUserId(currentUser.id!);

    loadObservable.subscribe({
      next: (jobs) => {
        this.emailJobs = jobs;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading email jobs:', error);
        this.showError('Failed to load email jobs');
        this.loading = false;
      }
    });
  }

  toggleJobStatus(job: EmailJob): void {
    if (!job.id) return;

    const newStatus = !job.enabled;
    const operation = newStatus
      ? this.emailJobService.enableJob(job.id)
      : this.emailJobService.disableJob(job.id);

    operation.subscribe({
      next: (response) => {
        job.enabled = response.enabled;
        this.showSuccess(response.message);
      },
      error: (error) => {
        console.error('Error toggling job status:', error);
        this.showError('Failed to update job status');
      }
    });
  }

  deleteJob(job: EmailJob): void {
    if (!job.id) return;

    if (confirm(`Are you sure you want to delete the email job for ${job.receiverEmails}?`)) {
      this.emailJobService.delete(job.id).subscribe({
        next: () => {
          this.emailJobs = this.emailJobs.filter(j => j.id !== job.id);
          this.showSuccess('Email job deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting job:', error);
          this.showError('Failed to delete email job');
        }
      });
    }
  }

  canDeleteJob(): boolean {
    return this.authService.isAdmin();
  }

  canCreateJob(): boolean {
    return this.authService.isLoggedIn();
  }

  createNewJob(): void {
    const dialogRef = this.dialog.open(JobModalComponent, {
      width: '700px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEmailJobs(); // Refresh the list
        this.showSuccess('Email job created successfully!');
      }
    });
  }

  editJob(job: EmailJob): void {
    const dialogRef = this.dialog.open(JobModalComponent, {
      width: '700px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: {
        mode: 'edit',
        job: job
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEmailJobs();
        this.showSuccess('Email job updated successfully!');
      }
    });
  }

  formatDateTime(dateTime?: string): string {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleString();
  }

  formatRecurrence(pattern: string): string {
    return pattern.charAt(0).toUpperCase() + pattern.slice(1).toLowerCase();
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}

