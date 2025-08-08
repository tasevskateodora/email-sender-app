import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth';
import { EmailTemplate } from '../../../shared/models';
import { TemplateModalComponent } from '../template-modal/template-modal.component';
import { EmailTemplateService } from '../../../core/services/email-template.service';


import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-templates-list',
  templateUrl: './templates-list.component.html',
  //styleUrls: ['./templates-list.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule
  ]
})
export class TemplatesListComponent implements OnInit {
  templates: EmailTemplate[] = [];
  loading = false;
  displayedColumns: string[] = ['name', 'subject', 'createdAt', 'actions'];

  constructor(
    private emailTemplateService: EmailTemplateService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.loading = true;
    this.emailTemplateService.getAll().subscribe({
      next: (templates) => {
        this.templates = templates;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading templates:', error);
        this.showError('Failed to load email templates');
        this.loading = false;
      }
    });
  }

  createNewTemplate(): void {
    const dialogRef = this.dialog.open(TemplateModalComponent, {
      width: '800px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTemplates();
        this.showSuccess('Template created successfully!');
      }
    });
  }

  editTemplate(template: EmailTemplate): void {
    const dialogRef = this.dialog.open(TemplateModalComponent, {
      width: '800px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: {
        mode: 'edit',
        template: template
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTemplates();
        this.showSuccess('Template updated successfully!');
      }
    });
  }

  deleteTemplate(template: EmailTemplate): void {
    if (!template.id) return;

    const confirmMessage = `Are you sure you want to delete the template "${template.name}"?`;
    if (confirm(confirmMessage)) {
      this.emailTemplateService.delete(template.id).subscribe({
        next: () => {
          this.templates = this.templates.filter(t => t.id !== template.id);
          this.showSuccess('Template deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting template:', error);
          this.showError('Failed to delete template');
        }
      });
    }
  }

  previewTemplate(template: EmailTemplate): void {
    // TODO: Implement template preview dialog
    this.showInfo('Template preview functionality coming soon!');
  }

  canDeleteTemplate(): boolean {
    return this.authService.isAdmin();
  }

  canCreateTemplate(): boolean {
    return this.authService.isLoggedIn();
  }

  formatDateTime(dateTime: string | undefined): string {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleDateString();
  }

  truncateText(text: string, maxLength: number = 50): string {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
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

  private showInfo(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['info-snackbar']
    });
  }
}
