import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
 
@Injectable({
  providedIn: 'root'
})
export class BookingService {
 
  private baseUrl = 'http://localhost:8080/api/bookings';
 
  constructor(private http: HttpClient) {}
 
  createBooking(booking: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, booking);
  }
 
  getAllBookings(): Observable<any> {
    return this.http.get(`${this.baseUrl}/all`);
  }
 
  getBookingsByUsername(username: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/user/${username}`);
  }
 
  getBookingById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/${id}`);
  }
 
  cancelBooking(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/cancel/${id}`, {},
      { responseType: 'text' });
  }
}
 