import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private baseUrl = 'http://localhost:8080/api/payments';
  private stripeUrl = 'http://localhost:8080/api/stripe';

  constructor(private http: HttpClient) {}

  processPayment(payment: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/process`, payment);
  }

  getPaymentsByUsername(username: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/user/${username}`);
  }

  createPaymentIntent(amount: number,
      description: string = 'SkyWays Flight Booking'):
      Observable<any> {
    return this.http.post(
      `${this.stripeUrl}/create-payment-intent` +
      `?amount=${amount}&description=${description}`, {});
  }

  confirmPayment(paymentIntentId: string): Observable<any> {
    return this.http.get(
      `${this.stripeUrl}/confirm/${paymentIntentId}`);
  }
}