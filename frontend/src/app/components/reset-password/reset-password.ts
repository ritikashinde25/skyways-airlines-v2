import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css'
})
export class ResetPasswordComponent implements OnInit {

  token = '';
  newPassword = '';
  confirmPassword = '';
  message = '';
  isError = false;
  isSuccess = false;
  isLoading = false;
  passwordStrength = '';

  constructor(private authService: AuthService, private router: Router, private route: ActivatedRoute) {}

  hasUpper(p: string) { return /[A-Z]/.test(p); }
  hasLower(p: string) { return /[a-z]/.test(p); }
  hasNumber(p: string) { return /[0-9]/.test(p); }
  hasSpecial(p: string) { return /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(p); }

  ngOnInit() {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (!this.token) { this.message = 'Invalid reset link!'; this.isError = true; }
  }

  validatePassword(password: string): string | null {
    if (password.length < 8) return 'Password must be at least 8 characters!';
    if (!this.hasUpper(password)) return 'Password must contain at least 1 uppercase letter!';
    if (!this.hasLower(password)) return 'Password must contain at least 1 lowercase letter!';
    if (!this.hasNumber(password)) return 'Password must contain at least 1 number!';
    if (!this.hasSpecial(password)) return 'Password must contain at least 1 special character!';
    return null;
  }

  checkPasswordStrength(password: string) {
    if (!password) { this.passwordStrength = ''; return; }
    let score = 0;
    if (password.length >= 8) score++;
    if (this.hasUpper(password)) score++;
    if (this.hasLower(password)) score++;
    if (this.hasNumber(password)) score++;
    if (this.hasSpecial(password)) score++;
    if (score <= 2) this.passwordStrength = 'weak';
    else if (score <= 3) this.passwordStrength = 'medium';
    else if (score <= 4) this.passwordStrength = 'strong';
    else this.passwordStrength = 'very-strong';
  }

  onSubmit() {
    if (!this.newPassword || !this.confirmPassword) {
      this.message = 'Please fill in all fields!'; this.isError = true; return;
    }
    const passwordError = this.validatePassword(this.newPassword);
    if (passwordError) { this.message = passwordError; this.isError = true; return; }
    if (this.newPassword !== this.confirmPassword) {
      this.message = 'Passwords do not match!'; this.isError = true; return;
    }
    this.isLoading = true;
    this.message = '';
    this.authService.resetPassword(this.token, this.newPassword).subscribe({
      next: () => {
        this.isLoading = false;
        this.isSuccess = true;
        this.isError = false;
        this.message = 'Password reset successfully! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.isLoading = false;
        this.isError = true;
        this.message = err.error?.message || 'Reset failed. Link may have expired!';
      }
    });
  }
}