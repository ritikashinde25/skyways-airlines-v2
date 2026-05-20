import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
 
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
 
  user = {
    username: '',
    email: '',
    password: ''
  };
 
  message = '';
  isError = false;
 
  constructor(private authService: AuthService, private router: Router) {}
 
  onRegister() {
    if (!this.user.username || !this.user.email || !this.user.password) {
      this.message = 'Please fill in all fields!';
      this.isError = true;
      return;
    }
 
    this.authService.register(this.user).subscribe({
      next: (response) => {
        if (response.includes('successfully')) {
          this.message = 'Registration successful! Please login.';
          this.isError = false;
          setTimeout(() => this.router.navigate(['/login']), 1500);
        } else {
          this.message = response;
          this.isError = true;
        }
      },
      error: (err) => {
        this.message = 'Registration failed. Please try again!';
        this.isError = true;
      }
    });
  }
}