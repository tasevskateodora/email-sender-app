import { Component, Inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  ValidatorFn,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService, UserDto } from '../../../../core/services/user.service';

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-user-modal',
  templateUrl: './user-modal.component.html',
  //styleUrls: ['./user-modal.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule
  ]
})
export class UserModalComponent implements OnInit {
  userForm!: FormGroup;
  isEditMode: boolean;
  loading = false;
  hidePassword = true;
  availableRoles = ['ROLE_USER', 'ROLE_ADMIN'];

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<UserModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user?: UserDto; mode: 'create' | 'edit' },
    private userService: UserService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = data.mode === 'edit';
    this.initializeForm();
  }

  ngOnInit(): void {
    if (this.isEditMode && this.data.user) {
      this.populateForm(this.data.user);
    }
  }

  private initializeForm(): void {
    this.userForm = this.formBuilder.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z0-9._-]+$/)
      ]],
      password: [
        '',
        this.isEditMode
          ? [Validators.minLength(6)]
          : [Validators.required, Validators.minLength(6)]
      ],
      confirmPassword: [''],
      roleNames: [['ROLE_USER'], Validators.required],
      enabled: [true]
    });

    this.userForm.addValidators(this.passwordMatchValidator);
  }

private passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  if (!(control instanceof FormGroup)) {
    return null;
  }

  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  return password && confirmPassword && password !== confirmPassword ? { passwordMismatch: true } : null;
};


/* private passwordMatchValidator = (group: FormGroup) => {
   const password = group.get('password')?.value;
   const confirmPassword = group.get('confirmPassword')?.value;

   if (password && confirmPassword && password !== confirmPassword) {
     return { passwordMismatch: true };
   }
   return null;
 };*/

  private populateForm(user: UserDto): void {
    this.userForm.patchValue({
      username: user.username,
      password: '', // Don't pre-fill password for security
      confirmPassword: '',
      roleNames: user.roleNames || ['ROLE_USER'],
      enabled: user.enabled !== false
    });

    // Make username readonly for edit mode
    if (this.isEditMode) {
      this.userForm.get('username')?.disable();
    }
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    const formValue = this.userForm.getRawValue(); // getRawValue to include disabled fields

    const userData: UserDto = {
      username: formValue.username.trim(),
      roleNames: formValue.roleNames,
      enabled: formValue.enabled
    };

    // Only include password if it's provided
    if (formValue.password) {
      userData.password = formValue.password;
    }

    const operation = this.isEditMode && this.data.user?.id
      ? this.userService.update(this.data.user.id, userData)
      : this.userService.create(userData);

    operation.subscribe({
      next: (result) => {
        this.loading = false;
        const message = this.isEditMode ? 'User updated successfully!' : 'User created successfully!';
        this.showSuccess(message);
        this.dialogRef.close(result);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error saving user:', error);

        let errorMessage = 'Failed to save user. Please try again.';
        if (error.status === 409) {
          errorMessage = 'Username already exists. Please choose a different username.';
        } else if (error.status === 400) {
          errorMessage = 'Invalid user data. Please check your input.';
        }

        this.showError(errorMessage);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  private markFormGroupTouched(): void {
    Object.keys(this.userForm.controls).forEach(key => {
      const control = this.userForm.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.userForm.get(fieldName);

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

    if (control?.hasError('pattern')) {
      return 'Username can only contain letters, numbers, dots, hyphens, and underscores';
    }

    // Check form-level password mismatch error
    if (fieldName === 'confirmPassword' && this.userForm.hasError('passwordMismatch')) {
      return 'Passwords do not match';
    }

    return '';
  }

  private getFieldDisplayName(fieldName: string): string {
    const displayNames: { [key: string]: string } = {
      username: 'Username',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      roleNames: 'Roles'
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
    return this.isEditMode ? 'Edit User' : 'Create New User';
  }

  getRoleDisplayName(role: string): string {
    return role.replace('ROLE_', '');
  }

  shouldShowPasswordField(): boolean {
    return !this.isEditMode || this.userForm.get('password')?.value;
  }

  isPasswordRequired(): boolean {
    return !this.isEditMode;
  }
}
