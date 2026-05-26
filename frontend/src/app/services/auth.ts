import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  register(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, user,
      { responseType: 'text' });
  }

  login(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, user);
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/forgot-password?email=${email}`,
      {},
      { responseType: 'text' });
  }

  resetPassword(token: string,
      newPassword: string): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/reset-password?token=${token}&newPassword=${newPassword}`,
      {},
      { responseType: 'text' });
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('token') !== null;
  }

  logout(): void {
    localStorage.removeItem('username');
    localStorage.removeItem('token');
    localStorage.removeItem('email');
  }

  getUsername(): string {
    return localStorage.getItem('username') || '';
  }

  getToken(): string {
    return localStorage.getItem('token') || '';
  }
}