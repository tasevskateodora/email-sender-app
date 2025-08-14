import { Component, OnInit } from '@angular/core';
import { EmailExecutionService, EmailExecution } from '../../core/services/email-execution.service';
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
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatDialogModule} from '@angular/material/dialog';
import {MatFormField, MatInputModule} from '@angular/material/input';
import {MatOption, MatSelect, MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';

@Component({
  selector: 'app-email-executions',
  templateUrl: './email-executions.component.html',
  styleUrls: ['./email-executions.component.css'],
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
    NgClass,
    MatSlideToggleModule,
    CommonModule,
    MatFormField,
    MatSelect,
    MatOption,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule
  ]
})
export class EmailExecutionsComponent implements OnInit {
  executions: EmailExecution[] = [];
  filteredExecutions: EmailExecution[] = [];
  selectedStatus: string = 'ALL';

  displayedColumns: string[] = [
    'id', 'executedAt', 'status', 'errorMessage',
    'retryAttempt', 'jobSenderEmail', 'jobReceiverEmails'
  ];

  constructor(private emailExecutionService: EmailExecutionService) {}

  ngOnInit(): void {
    this.loadExecutions();
  }

  loadExecutions(): void {
    this.emailExecutionService.getAll().subscribe(data => {
      this.executions = data;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    if (this.selectedStatus === 'ALL') {
      this.filteredExecutions = this.executions;
    } else {
      this.filteredExecutions = this.executions.filter(e => e.status === this.selectedStatus);
    }
  }
}
