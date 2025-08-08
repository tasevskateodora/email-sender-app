import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EmailTemplate } from '../../../shared/models';
import { EmailTemplateService } from '../../../core/services/email-template.service';

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-template-modal',
  templateUrl: './template-modal.component.html',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatTabsModule,
    MatCardModule
  ]
})
export class TemplateModalComponent implements OnInit {
  templateForm!: FormGroup;
  isEditMode: boolean;
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<TemplateModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { template?: EmailTemplate; mode: 'create' | 'edit' },
    private emailTemplateService: EmailTemplateService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = data.mode === 'edit';
    this.initializeForm();
  }

  ngOnInit(): void {
    if (this.isEditMode && this.data.template) {
      this.populateForm(this.data.template);
    }
  }

  private initializeForm(): void {
    this.templateForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      subject: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
      body: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  private populateForm(template: EmailTemplate): void {
    this.templateForm.patchValue({
      name: template.name,
      subject: template.subject,
      body: template.body
    });
  }

  onSubmit(): void {
    if (this.templateForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    const formValue = this.templateForm.value;

    const templateData: EmailTemplate = {
      name: formValue.name.trim(),
      subject: formValue.subject.trim(),
      body: formValue.body.trim()
    };

    const operation = this.isEditMode && this.data.template?.id
      ? this.emailTemplateService.update(this.data.template.id, templateData)
      : this.emailTemplateService.create(templateData);

    operation.subscribe({
      next: (result) => {
        this.loading = false;
        const message = this.isEditMode ? 'Template updated successfully!' : 'Template created successfully!';
        this.showSuccess(message);
        this.dialogRef.close(result);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error saving template:', error);
        this.showError('Failed to save template. Please try again.');
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  previewTemplate(): void {
    const formValue = this.templateForm.value;
    if (!formValue.subject || !formValue.body) {
      this.showError('Please fill in subject and body to preview');
      return;
    }

    // Simple preview - in a real app, you might want a separate preview dialog
    const previewWindow = window.open('', '_blank', 'width=600,height=400');
    if (previewWindow) {
      previewWindow.document.write(`
        <html>
          <head>
            <title>Email Preview - ${formValue.subject}</title>
            <style>
              body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
              .email-container { border: 1px solid #ddd; padding: 20px; border-radius: 8px; }
              .subject { font-size: 18px; font-weight: bold; margin-bottom: 15px; color: #333; }
              .body { white-space: pre-wrap; }
            </style>
          </head>
          <body>
            <div class="email-container">
              <div class="subject">Subject: ${formValue.subject}</div>
              <div class="body">${formValue.body}</div>
            </div>
          </body>
        </html>
      `);
      previewWindow.document.close();
    }
  }

  insertVariable(variable: string): void {
    const bodyControl = this.templateForm.get('body');
    if (bodyControl) {
      const currentValue = bodyControl.value || '';
      const newValue = currentValue + `{{${variable}}}`;
      bodyControl.setValue(newValue);
    }
  }

  getAvailableVariables(): string[] {
    return [
      'recipient_name',
      'sender_name',
      'current_date',
      'company_name',
      'unsubscribe_link'
    ];
  }

  private markFormGroupTouched(): void {
    Object.keys(this.templateForm.controls).forEach(key => {
      const control = this.templateForm.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.templateForm.get(fieldName);

    if (control?.hasError('required')) {
      return `${this.getFieldDisplayName(fieldName)} is required`;
    }

    if (control?.hasError('minlength')) {
      const requiredLength = control.errors?.['minlength'].requiredLength;
      return `${this.getFieldDisplayName(fieldName)} must be at least ${requiredLength} characters long`;
    }

    if (control?.hasError('maxlength')) {
      const requiredLength = control.errors?.['maxlength'].requiredLength;
      return `${this.getFieldDisplayName(fieldName)} cannot exceed ${requiredLength} characters`;
    }

    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      name: 'Template Name',
      subject: 'Email Subject',
      body: 'Email Body'
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
    return this.isEditMode ? 'Edit Email Template' : 'Create New Email Template';
  }

  getCharacterCount(fieldName: string): number {
    const control = this.templateForm.get(fieldName);
    return control?.value?.length || 0;
  }

  getMaxLength(fieldName: string): number {
    const maxLengths: { [key: string]: number } = {
      name: 100,
      subject: 200,
      body: 10000
    };
    return maxLengths[fieldName] || 0;
  }
}
