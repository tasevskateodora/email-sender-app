import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth';
import { EmailJob, EmailTemplate, RecurrencePattern } from '../../../shared/models';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { EmailTemplateService } from '../../../core/services/email-template.service';
import { EmailJobService } from '../../../core/services/email-job.service';

@Component({
  selector: 'app-job-modal',
  templateUrl: './job-modal.component.html',
  styleUrls: ['./job-modal.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule
  ]
})
export class JobModalComponent implements OnInit {
  jobForm!: FormGroup;
  isEditMode: boolean;
  loading = false;
  loadingTemplates = false;
  templates: EmailTemplate[] = [];
  recurrencePatterns = Object.values(RecurrencePattern);

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<JobModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { job?: EmailJob; mode: 'create' | 'edit' },
    private emailJobService: EmailJobService,
    private emailTemplateService: EmailTemplateService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = data.mode === 'edit';
    this.initializeForm();
  }

  ngOnInit(): void {

    this.loadTemplates().then(() => {
      if (this.isEditMode && this.data.job) {
        this.populateForm(this.data.job);
      }
    });
  }

  private initializeForm(): void {
    this.jobForm = this.formBuilder.group({
      senderEmail: ['', [Validators.required, Validators.email]],
      receiverEmails: ['', [Validators.required, this.emailListValidator]],
      startDate: ['', Validators.required],
      endDate: [''],
      sendTime: ['09:00', Validators.required],
      recurrencePattern: [RecurrencePattern.DAILY, Validators.required],
      emailTemplateId: [''],
      oneTime: [false],
      enabled: [true]
    });

    this.jobForm.get('oneTime')?.valueChanges.subscribe(isOneTime => {
      if (isOneTime) {
        this.jobForm.patchValue({ recurrencePattern: RecurrencePattern.ONE_TIME });
      }
    });

    this.jobForm.get('recurrencePattern')?.valueChanges.subscribe(pattern => {
      const isOneTime = pattern === RecurrencePattern.ONE_TIME;
      this.jobForm.patchValue({ oneTime: isOneTime }, { emitEvent: false });
    });
  }

  private emailListValidator(control: any) {
    if (!control.value) return null;

    const emails = control.value.split(',').map((email: string) => email.trim());
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    for (const email of emails) {
      if (!emailRegex.test(email)) {
        return { invalidEmailList: true };
      }
    }
    return null;
  }

  private loadTemplates(): Promise<void> {
    this.loadingTemplates = true;

    return new Promise((resolve, reject) => {
      this.emailTemplateService.getAll().subscribe({
        next: (templates) => {
          this.templates = templates;
          this.loadingTemplates = false;

          resolve();
        },
        error: (error) => {
          this.showError('Failed to load email templates');
          this.loadingTemplates = false;
          reject(error);
        }
      });
    });
  }

  private populateForm(job: EmailJob): void {

    let emailTemplateId = '';

    if (job.emailTemplateId !== null && job.emailTemplateId !== undefined && job.emailTemplateId !== '') {
      emailTemplateId = String(job.emailTemplateId);
    } else if (job.emailTemplate?.id !== null && job.emailTemplate?.id !== undefined && job.emailTemplate?.id !== '') {
      emailTemplateId = String(job.emailTemplate.id);
    } else {
    }

    if (emailTemplateId) {
      const matchingTemplate = this.templates.find(t => String(t.id) === emailTemplateId);
    }

    const startDate = job.startDate ? new Date(job.startDate) : null;
    const endDate = job.endDate ? new Date(job.endDate) : null;

    this.jobForm.patchValue({
      senderEmail: job.senderEmail,
      receiverEmails: job.receiverEmails,
      startDate: startDate,
      endDate: endDate,
      sendTime: job.sendTime,
      recurrencePattern: job.recurrencePattern,
      emailTemplateId: emailTemplateId,
      oneTime: job.oneTime,
      enabled: job.enabled
    });

  }

  onSubmit(): void {
    if (this.jobForm.invalid) {
      this.markFormGroupTouched();
      return;
    }
    this.loading = true;
    const currentUser = this.authService.getCurrentUser();

    if (!currentUser?.id) {
      this.showError('User not found');
      this.loading = false;
      return;
    }

    const formValue = this.jobForm.value;
    const startDateTime = this.combineDateAndTime(formValue.startDate, formValue.sendTime);
    const endDateTime = formValue.endDate ? this.combineDateAndTime(formValue.endDate, formValue.sendTime) : undefined;

    const jobData: EmailJob = {
      senderEmail: formValue.senderEmail,
      receiverEmails: formValue.receiverEmails,
      startDate: startDateTime,
      endDate: endDateTime || '',
      sendTime: formValue.sendTime,
      recurrencePattern: formValue.recurrencePattern,
      oneTime: formValue.oneTime,
      enabled: formValue.enabled,
      emailTemplateId: formValue.emailTemplateId
    };


    if (formValue.emailTemplateId) {
      const selectedTemplate = this.templates.find(t => String(t.id) === String(formValue.emailTemplateId));
      if (selectedTemplate) {
        jobData.emailTemplate = selectedTemplate;
      }
    }

    const operation = this.isEditMode && this.data.job?.id
      ? this.emailJobService.update(this.data.job.id, currentUser.id, jobData)
      : this.emailJobService.create(jobData);

    operation.subscribe({
      next: (result) => {
        this.loading = false;
        const message = this.isEditMode ? 'Job updated successfully!' : 'Job created successfully!';
        this.showSuccess(message);
        this.dialogRef.close(result);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error saving job:', error);
        this.showError('Failed to save job. Please try again.');
      }
    });
  }

  private combineDateAndTime(date: Date, time: string): string {
    const [hours, minutes] = time.split(':');
    const combined = new Date(date);
    combined.setHours(parseInt(hours), parseInt(minutes), 0, 0);
    return combined.toISOString();
  }

  private markFormGroupTouched(): void {
    Object.keys(this.jobForm.controls).forEach(key => {
      const control = this.jobForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  getErrorMessage(fieldName: string): string {
    const control = this.jobForm.get(fieldName);

    if (control?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} is required`;
    }

    if (control?.hasError('email')) {
      return 'Please enter a valid email address';
    }

    if (control?.hasError('invalidEmailList')) {
      return 'Please enter valid email addresses separated by commas';
    }

    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      senderEmail: 'Sender Email',
      receiverEmails: 'Receiver Emails',
      startDate: 'Start Date',
      sendTime: 'Send Time',
      recurrencePattern: 'Recurrence Pattern'
    };
    return displayNames[fieldName] || fieldName;
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

  getModalTitle(): string {
    return this.isEditMode ? 'Edit Email Job' : 'Create New Email Job';
  }

  formatRecurrencePattern(pattern: string): string {
    return pattern.toLowerCase().replace('_', ' ')
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}
