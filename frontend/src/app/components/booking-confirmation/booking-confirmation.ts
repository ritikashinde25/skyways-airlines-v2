import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { BookingService } from '../../services/booking';
 
@Component({
  selector: 'app-booking-confirmation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './booking-confirmation.html',
  styleUrl: './booking-confirmation.css'
})
export class BookingConfirmationComponent implements OnInit {
 
  booking: any = null;
  bookingId: any;
  username = localStorage.getItem('username') || '';
 
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}
 
  ngOnInit() {
    this.bookingId = this.route.snapshot.paramMap.get('bookingId');
    this.bookingService.getBookingById(this.bookingId).subscribe({
      next: (data) => {
        this.booking = data;
      },
      error: (err) => {
        console.error('Error fetching booking', err);
      }
    });
  }
 
  goToFlights() {
    this.router.navigate(['/flights']);
  }
 
  logout() {
    localStorage.removeItem('username');
    this.router.navigate(['/login']);
  }
}
 