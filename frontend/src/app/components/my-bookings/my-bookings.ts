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
  isError = false;

  // Cancellation popup
  showCancelPopup = false;
  selectedBookingId: any = null;
  cancelResponse: any = null;
  isCancelling = false;

  constructor(
    private bookingService: BookingService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadBookings();
  }

  loadBookings() {
    this.bookingService.getBookingsByUsername(
      this.username).subscribe({
      next: (data) => {
        this.bookings = data;
        if (data.length === 0) {
          this.message = 'No bookings found!';
        }
      },
      error: () => {
        this.message = 'Failed to load bookings!';
        this.isError = true;
      }
    });
  }

  openCancelPopup(bookingId: any) {
    this.selectedBookingId = bookingId;
    this.showCancelPopup = true;
    this.cancelResponse = null;
  }

  closeCancelPopup() {
    this.showCancelPopup = false;
    this.selectedBookingId = null;
    this.cancelResponse = null;
  }

  confirmCancel() {
    this.isCancelling = true;
    this.bookingService.cancelBooking(
      this.selectedBookingId).subscribe({
      next: (response) => {
        this.isCancelling = false;
        this.cancelResponse = response;
        this.loadBookings();
      },
      error: () => {
        this.isCancelling = false;
        this.message = 'Cancellation failed!';
        this.isError = true;
        this.closeCancelPopup();
      }
    });
  }

  goToFlights() {
    this.router.navigate(['/flights']);
  }

  logout() {
    localStorage.removeItem('username');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}