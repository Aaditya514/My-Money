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
  showPassword: boolean = false;
  showConfirmPassword: boolean = false;
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
      email: ['', [
        Validators.required,
        Validators.email,
        Validators.maxLength(100)
      ]],
      phoneNumber: ['', [
        Validators.pattern(/^[0-9]{10}$/)
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
    const nameField = this.registerForm.get('name');
    const emailField = this.registerForm.get('email');
    const phoneField = this.registerForm.get('phoneNumber');
    const passField = this.registerForm.get('password');
    const confirmField = this.registerForm.get('confirmPassword');

    nameField?.markAsTouched();
    emailField?.markAsTouched();
    if (phoneField?.value) {
      phoneField?.markAsTouched();
    }
    passField?.markAsTouched();
    confirmField?.markAsTouched();

    if (nameField?.invalid || emailField?.invalid || (phoneField?.value && phoneField?.invalid) || passField?.invalid || confirmField?.invalid || this.isPasswordMismatch()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successNotice = '';

    const email = emailField?.value?.trim().toLowerCase();

    this.authService.sendOtp({ email }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.otpStep = true;
          this.simulatedOtp = res.simulatedOtp || '';
          if (this.simulatedOtp) {
            this.registerForm.patchValue({ otp: this.simulatedOtp });
            this.successNotice = `Verification code auto-filled (${this.simulatedOtp}). Click Verify & Create Account to continue!`;
          } else {
            this.successNotice = `Verification code sent to ${email}. Check your inbox!`;
          }
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
          this.errorMessage = 'Backend service is starting up (takes ~50s on free tiers). Please wait a moment and try again.';
        } else {
          this.errorMessage = err.error?.message || 'Unable to send OTP. Please check your email.';
        }
      }
    });
  }

  onResendOtp(): void {
    if (this.resendCooldown > 0) return;
    const email = this.registerForm.get('email')?.value?.trim().toLowerCase();
    if (!email) return;

    this.loading = true;
    this.errorMessage = '';

    this.authService.sendOtp({ email }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.simulatedOtp = res.simulatedOtp || '';
          if (this.simulatedOtp) {
            this.registerForm.patchValue({ otp: this.simulatedOtp });
            this.successNotice = `New verification code auto-filled (${this.simulatedOtp}).`;
          } else {
            this.successNotice = `New verification code sent to ${email}.`;
          }
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

  autoFillOtp(): void {
    if (this.simulatedOtp) {
      this.registerForm.patchValue({ otp: this.simulatedOtp });
    }
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
      email: email.trim().toLowerCase(),
      phoneNumber: phoneNumber ? phoneNumber.trim() : undefined,
      password,
      otp: otp ? otp.trim() : undefined
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
          this.errorMessage = err.error?.message || 'Registration failed. Please check the code and try again.';
        }
      }
    });
  }
}
