import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { EmailJobService } from '../../../core/services/email-job.service';
import { EmailTemplateService } from '../../../core/services/email-template.service';
import { ActivatedRoute, Router } from '@angular/router';
import {EmailJob, EmailTemplate} from '../../../shared/models';
import { AuthService } from '../../../core/services/auth';
import { Api } from '../../../core/services/api';


@Component({
  selector: 'app-email-job-form',
  templateUrl: './email-job-form.component.html',
  styleUrls: ['./email-job-form.component.css'],
  standalone: true,
  imports: [ReactiveFormsModule],
})
export class EmailJobFormComponent implements OnInit {

  emailJobForm!: FormGroup;
  templates: EmailTemplate[] = [];
  loading = false;
  errorMessage = '';

  @Input() jobId?: string;
  // Optional input for current user ID (needed for save)
 // @Input() userId!: string;

  @Output() formSaved = new EventEmitter<EmailJob>();

  constructor(
    private fb: FormBuilder,
    private emailJobService: EmailJobService,
    private emailTemplateService: EmailTemplateService,
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private apiService: Api,
  )  {
    console.log("CONSTRUCTOR CALLED!!!");
    alert("Constructor called!");
  }

  ngOnInit(): void {
    this.initForm();
    //this.loadTemplates();

  /*  if (this.jobId) {
      this.loadJob(this.jobId);

    }*/

    this.loadTemplates().then(() => {
      console.log("Form value after patch:", this.emailJobForm.value);
      if (this.jobId) {
        this.loadJob(this.jobId);
      }
    });
  }



  private initForm(): void {
    this.emailJobForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: [''],
      recurrencePattern: ['NONE', Validators.required],
      senderEmail: ['', [Validators.required, Validators.email]],
      receiverEmails: ['', [Validators.required]],
      enabled: [true],
      isOneTime: [false],
      sendTime: ['', Validators.required],
      emailTemplateId: ['', Validators.required],
    });
  }

  loadTemplates(): Promise<void> {
    return new Promise((resolve) => {
      this.apiService.getTemplates().subscribe((templates) => {
        this.templates = templates;

        if (this.templates.length > 0) {
          console.log('Type of template id:', typeof this.templates[0].id);
        }
        console.log('Type of form emailTemplateId:', typeof this.emailJobForm.get('emailTemplateId')?.value);
        resolve();
      });
    });
  }

  /*private loadTemplates(): void {
    this.emailTemplateService.getAll().subscribe({
      next: (templates) => this.templates = templates,
      error: (err) => this.errorMessage = 'Failed to load templates'
    });
  }*/

  private loadJob(id: string): void {
    this.loading = true;
    this.emailJobService.getById(id).subscribe({
      next: (job) => {
        this.loading = false;
        console.log("email tamplate id",job.emailTemplateId)
        console.log("Loaded job data:", job);
        console.log("Job emailTemplateId type:", typeof job.emailTemplateId, "value:", job.emailTemplateId);
        this.emailJobForm.patchValue({
          startDate: job.startDate ? job.startDate.substring(0, 16) : '',
          endDate: job.endDate ? job.endDate.substring(0, 16) : '',
          recurrencePattern: job.recurrencePattern,
          senderEmail: job.senderEmail,
          receiverEmails: job.receiverEmails,
          enabled: job.enabled,
          isOneTime: job.oneTime,
          sendTime: job.sendTime,
         //emailTemplateId: job.emailTemplate ? job.emailTemplate.id : null,
          emailTemplateId: job.emailTemplate?.id || ''
        });
        console.log("Form value after patch:", this.emailJobForm.value);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load job data';
      }
    });
  }

  onSubmit(): void {

    console.log(this.emailJobForm.value);
    if (this.emailJobForm.invalid) {
      this.emailJobForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    const formValue = this.emailJobForm.value;
    const selectedTemplate = this.templates.find(t => t.id === formValue.emailTemplateId);
    const emailJob: EmailJob = {
      startDate: formValue.startDate,
      endDate: formValue.endDate || null,
      recurrencePattern: formValue.recurrencePattern,
      senderEmail: formValue.senderEmail,
      receiverEmails: formValue.receiverEmails,
      enabled: formValue.enabled,
      oneTime: formValue.isOneTime,
      sendTime: formValue.sendTime,
      //emailTemplateId: formValue.emailTemplateId
      emailTemplateId: formValue.emailTemplateId || null
    };

    console.log("Selected Template ID:", formValue.emailTemplateId);
    console.log("Matched Template:", selectedTemplate);

    const userId = this.authService.getUserIdFromToken();
    if (!userId) {
      this.loading = false;
      this.errorMessage = 'User not authenticated';
      return;
    }

      if (this.jobId) {
        this.emailJobService.update(this.jobId, userId, emailJob).subscribe({
          next: (updatedJob) => {
            this.loading = false;
            this.formSaved.emit(updatedJob);
            this.router.navigate(['/email-jobs']);
          },
          error: (err) => {
            this.loading = false;
            this.errorMessage = 'Failed to update job';
            console.error(err);
          }
        });
      }

    else {
      this.emailJobService.create(emailJob).subscribe({
        next: (createdJob) => {
          this.loading = false;
          this.formSaved.emit(createdJob);
          this.router.navigate(['/email-jobs']);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = 'Failed to create job';
          console.error(err);
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/email-jobs']);
  }
}


