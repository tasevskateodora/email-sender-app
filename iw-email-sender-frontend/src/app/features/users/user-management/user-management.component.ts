import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService, UserDto } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth';
import { UserModalComponent } from './user-modal/user-modal.component';

import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  //styleUrls: ['./user-management.component.css'],
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
    MatSlideToggleModule,
    MatDialogModule,
    MatSnackBarModule
  ]
})
export class UserManagementComponent implements OnInit {
  users: UserDto[] = [];
  loading = false;
  displayedColumns: string[] = ['username', 'roles', 'enabled', 'createdAt', 'actions'];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    if (!this.isAdmin()) {
      this.showError('Access denied. Admin privileges required.');
      return;
    }
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.showError('Failed to load users');
        this.loading = false;
      }
    });
  }

  createNewUser(): void {
    const dialogRef = this.dialog.open(UserModalComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: { mode: 'create' }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
        this.showSuccess('User created successfully!');
      }
    });
  }

  editUser(user: UserDto): void {
    const dialogRef = this.dialog.open(UserModalComponent, {
      width: '600px',
      maxWidth: '90vw',
      maxHeight: '90vh',
      disableClose: true,
      data: {
        mode: 'edit',
        user: user
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
        this.showSuccess('User updated successfully!');
      }
    });
  }

  toggleUserStatus(user: UserDto): void {
    if (!user.id) return;

    // Prevent admin from disabling themselves
    const currentUser = this.authService.getCurrentUser();
    if (currentUser?.id === user.id && user.enabled) {
      this.showError('You cannot disable your own account');
      return;
    }

    const updatedUser = { ...user, enabled: !user.enabled };

    this.userService.update(user.id, updatedUser).subscribe({
      next: (result) => {
        user.enabled = result.enabled;
        const status = result.enabled ? 'enabled' : 'disabled';
        this.showSuccess(`User ${user.username} has been ${status}`);
      },
      error: (error) => {
        console.error('Error updating user status:', error);
        this.showError('Failed to update user status');
      }
    });
  }

  deleteUser(user: UserDto): void {
    if (!user.id) return;

    // Prevent admin from deleting themselves
    const currentUser = this.authService.getCurrentUser();
    if (currentUser?.id === user.id) {
      this.showError('You cannot delete your own account');
      return;
    }

    const confirmMessage = `Are you sure you want to delete user "${user.username}"?`;
    if (confirm(confirmMessage)) {
      this.userService.delete(user.id).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== user.id);
          this.showSuccess('User deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.showError('Failed to delete user');
        }
      });
    }
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  formatDateTime(dateTime: string | undefined): string {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleDateString();
  }

  getRoleDisplayName(roleName: string): string {
    return roleName.replace('ROLE_', '');
  }

  getRoleColor(roleName: string): string {
    return roleName === 'ROLE_ADMIN' ? 'warn' : 'primary';
  }

  isCurrentUser(user: UserDto): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser?.id === user.id;
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
