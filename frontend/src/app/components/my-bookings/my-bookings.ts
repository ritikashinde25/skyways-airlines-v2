import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BookingService } from '../../services/booking';
 
@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-bookings.html',
  styleUrl: './my-bookings.css'
})
export class MyBookings implements OnInit {
 
  bookings: any[] = [];
  username = localStorage.getItem('username') || '';
  message = '';
 
  constructor(
    private bookingService: BookingService,
    private router: Router
  ) {}
 
  ngOnInit() {
    this.bookingService.getBookingsByUsername(this.username).subscribe({
      next: (data) => {
        this.bookings = data;
        if (data.length === 0) {
          this.message = 'No bookings found!';
        }
      },
      error: () => {
        this.message = 'Failed to load bookings!';
      }
    });
  }
 
  cancelBooking(id: number) {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.bookingService.cancelBooking(id).subscribe({
        next: () => {
          this.message = 'Booking cancelled successfully!';
          this.ngOnInit();
        },
        error: () => {
          this.message = 'Cancellation failed!';
        }
      });
    }
  }
 
  goToFlights() {
    this.router.navigate(['/flights']);
  }
 
  logout() {
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
 