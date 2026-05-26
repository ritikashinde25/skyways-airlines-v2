import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPasswordComponent {

  email = '';
  message = '';
  isError = false;
  isSuccess = false;
  isLoading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.email) {
      this.message = 'Please enter your email!';
      this.isError = true;
      return;
    }

    this.isLoading = true;
    this.message = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        this.isSuccess = true;
        this.isError = false;
        this.message = 'Password reset link sent to your email! '
          + 'Please check your inbox.';
      },
      error: (err) => {
        this.isLoading = false;
        this.isError = true;
        this.message = err.error?.message ||
          'Email not found. Please try again!';
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}