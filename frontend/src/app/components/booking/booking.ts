import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { BookingService } from '../../services/booking';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.html',
  styleUrl: './booking.css'
})
export class BookingComponent implements OnInit {

  flight: any = null;
  selectedSeat = 'MIDDLE';
  basePrice = 0;
  totalPrice = 0;

  booking: any = {
    username: localStorage.getItem('username') || '',
    flightId: '',
    flightNumber: '',
    origin: '',
    destination: '',
    bookingDate: '',
    totalPrice: 0,
    travelClass: '',
    seatType: ''
  };

  message = '';
  isError = false;
  isSuccess = false;

  seatTypes = ['MIDDLE', 'AISLE', 'WINDOW'];

  seatCharges: any = {
    'MIDDLE': 0,
    'AISLE': 500,
    'WINDOW': 1000
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}

  ngOnInit() {
    // Get flight from localStorage (set by flight search page)
    const flightData = localStorage.getItem('selectedFlight');
    if (flightData) {
      this.flight = JSON.parse(flightData);
      this.basePrice = this.flight.price.amount;
      this.booking.flightId = this.flight.id;
      this.booking.flightNumber = 
        this.flight.legs[0].carriers[0].name;
      this.booking.origin = this.flight.legs[0].origin;
      this.booking.destination = this.flight.legs[0].destination;
      this.booking.bookingDate = new Date()
        .toISOString().split('T')[0];
      this.booking.travelClass = 'ECONOMY';
      this.calculatePrice();
    } else {
      this.message = 'No flight selected!';
      this.isError = true;
    }
  }

  calculatePrice() {
    this.totalPrice = this.basePrice + 
      this.seatCharges[this.selectedSeat];
    this.booking.totalPrice = this.totalPrice;
    this.booking.seatType = this.selectedSeat;
  }

  onBook() {
  this.bookingService.createBooking(this.booking).subscribe({
    next: (response) => {
      this.message = 'Booking created! Redirecting to payment...';
      this.isError = false;
      this.isSuccess = true;
      // Save amount for payment page
      localStorage.setItem('bookingAmount', 
        JSON.stringify(this.totalPrice));
      localStorage.removeItem('selectedFlight');
      setTimeout(() => {
        this.router.navigate(['/payment', response.id]);
      }, 1000);
    },
    error: () => {
      this.message = 'Booking failed. Please try again!';
      this.isError = true;
    }
  });
}

  goBack() {
    this.router.navigate(['/flights']);
  }
}