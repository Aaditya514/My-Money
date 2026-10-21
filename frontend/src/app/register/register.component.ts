import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string = '';
  successNotice: string = '';
  loading: boolean = false;
  isSubmitting: boolean = false;
  registrationSuccess: boolean = false;
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }

    this.registerForm = this.fb.group({
      name: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(50),
        Validators.pattern(/^[a-zA-Z\s'\-]+$/)
      ]],
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.maxLength(100)
      ]],
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^[0-9]{10}$/)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(32)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    if (!field) return false;
    return !!(field.touched && field.invalid);
  }

  isPasswordMismatch(): boolean {
    const confirmField = this.registerForm.get('confirmPassword');
    return !!(confirmField?.touched && this.registerForm.hasError('passwordMismatch'));
  }

  onSubmit(): void {
    if (this.loading || this.isSubmitting || this.registrationSuccess) {
      return;
    }

    // Mark all fields as touched to trigger validation messages
    Object.keys(this.registerForm.controls).forEach(key => {
      this.registerForm.get(key)?.markAsTouched();
    });

    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.isSubmitting = true;
    this.errorMessage = '';

    const { name, phoneNumber, email, password } = this.registerForm.value;

    this.authService.register({
      name: name.trim(),
      email: email.trim().toLowerCase(),
      phoneNumber: phoneNumber.trim(),
      password
    }).subscribe({
      next: (response) => {
        this.loading = false;
        this.isSubmitting = false;
        if (response.success) {
          this.registrationSuccess = true;
          this.successNotice = 'Account created successfully! Welcome to Finance Tracker.';
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 800);
        } else {
          this.errorMessage = response.message || 'Registration failed. Please try again.';
        }
      },
      error: (err) => {
        this.loading = false;
        this.isSubmitting = false;

        // If user already authenticated in a concurrent request
        if (this.authService.isLoggedIn()) {
          this.registrationSuccess = true;
          this.successNotice = 'Welcome to Finance Tracker! Redirecting to your dashboard...';
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 600);
          return;
        }

        if (err.status === 0) {
          this.errorMessage = 'Cannot reach backend server. Please check your connection and try again.';
        } else {
          this.errorMessage = err.error?.message || 'Registration failed. Please check your details and try again.';
        }
      }
    });
  }
}
