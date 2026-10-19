import { Component, OnDestroy } from '@angular/core';
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
export class RegisterComponent implements OnDestroy {
  registerForm: FormGroup;
  errorMessage: string = '';
  successNotice: string = '';
  loading: boolean = false;
  otpStep: boolean = false;
  simulatedOtp: string = '';
  resendCooldown: number = 0;
  private cooldownTimer: any = null;

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
      phoneNumber: ['', [
        Validators.required,
        Validators.pattern(/^[0-9]{10}$/)
      ]],
      email: ['', [
        Validators.email,
        Validators.maxLength(100)
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(32)
      ]],
      confirmPassword: ['', [Validators.required]],
      otp: ['', [Validators.pattern(/^[0-9]{6}$/)]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnDestroy(): void {
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
    }
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

  private startCooldown(): void {
    this.resendCooldown = 30;
    if (this.cooldownTimer) {
      clearInterval(this.cooldownTimer);
    }
    this.cooldownTimer = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) {
        clearInterval(this.cooldownTimer);
        this.cooldownTimer = null;
      }
    }, 1000);
  }

  onRequestOtp(): void {
    // Validate required fields for step 1
    const nameField = this.registerForm.get('name');
    const phoneField = this.registerForm.get('phoneNumber');
    const passField = this.registerForm.get('password');
    const confirmField = this.registerForm.get('confirmPassword');
    const emailField = this.registerForm.get('email');

    nameField?.markAsTouched();
    phoneField?.markAsTouched();
    passField?.markAsTouched();
    confirmField?.markAsTouched();
    if (emailField?.value) {
      emailField?.markAsTouched();
    }

    if (nameField?.invalid || phoneField?.invalid || passField?.invalid || confirmField?.invalid || this.isPasswordMismatch() || (emailField?.value && emailField?.invalid)) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successNotice = '';

    const phone = phoneField?.value?.trim();

    this.authService.sendOtp(phone).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.otpStep = true;
          this.simulatedOtp = res.simulatedOtp || '';
          this.successNotice = `OTP sent to +91 ${phone}.`;
          this.startCooldown();
          this.registerForm.get('otp')?.setValidators([Validators.required, Validators.pattern(/^[0-9]{6}$/)]);
          this.registerForm.get('otp')?.updateValueAndValidity();
        } else {
          this.errorMessage = res.message || 'Failed to send OTP.';
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.errorMessage = 'Backend service is starting up (Render free tier spin-up takes ~50s). Please wait a moment and click "Send OTP Verification" again.';
        } else {
          this.errorMessage = err.error?.message || 'Unable to send OTP. Please check your phone number.';
        }
      }
    });
  }

  onResendOtp(): void {
    if (this.resendCooldown > 0) return;
    const phone = this.registerForm.get('phoneNumber')?.value?.trim();
    if (!phone) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.sendOtp(phone).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.simulatedOtp = res.simulatedOtp || '';
          this.successNotice = `New OTP sent to +91 ${phone}.`;
          this.startCooldown();
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.errorMessage = 'Backend service is starting up. Please try resending in a few moments.';
        } else {
          this.errorMessage = err.error?.message || 'Failed to resend OTP.';
        }
      }
    });
  }

  onBackToDetails(): void {
    this.otpStep = false;
    this.errorMessage = '';
    this.successNotice = '';
    this.registerForm.get('otp')?.clearValidators();
    this.registerForm.get('otp')?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (!this.otpStep) {
      this.onRequestOtp();
      return;
    }

    const otpField = this.registerForm.get('otp');
    otpField?.markAsTouched();

    if (otpField?.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { name, phoneNumber, email, password, otp } = this.registerForm.value;

    this.authService.register({
      name: name.trim(),
      phoneNumber: phoneNumber.trim(),
      password,
      otp: otp.trim(),
      email: email ? email.trim().toLowerCase() : undefined
    }).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.errorMessage = 'Cannot reach backend server. Please check connection and try again.';
        } else {
          this.errorMessage = err.error?.message || 'Registration failed. Please check the OTP and try again.';
        }
      }
    });
  }
}
