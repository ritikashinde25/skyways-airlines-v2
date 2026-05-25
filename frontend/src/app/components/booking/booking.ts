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
  availability: any = null;

  booking: any = {
    username: localStorage.getItem('username') || '',
    flightId: '',
    flightNumber: '',
    origin: '',
    destination: '',
    bookingDate: '',
    totalPrice: 0,
    travelClass: 'ECONOMY',
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
    const flightData = localStorage.getItem('selectedFlight');
    if (flightData) {
      this.flight = JSON.parse(flightData);
      this.basePrice = this.flight.price.amount;
      this.booking.flightId = this.flight.id;
      this.booking.flightNumber =
        this.flight.legs[0].carriers[0].name;
      this.booking.origin = this.flight.legs[0].origin;
      this.booking.destination = 
        this.flight.legs[0].destination;
      const travelDate = localStorage.getItem('travelDate');
      this.booking.bookingDate = travelDate ||
        new Date().toISOString().split('T')[0];
      this.calculatePrice();

      // Check seat availability
      this.checkAvailability();
    } else {
      this.message = 'No flight selected!';
      this.isError = true;
    }
  }

  checkAvailability() {
    this.bookingService.getFlightAvailability(
      this.booking.flightId).subscribe({
      next: (data) => {
        this.availability = data;
        // Auto select available seat
        if (!data.middleAvailable && 
            this.selectedSeat === 'MIDDLE') {
          if (data.aisleAvailable) {
            this.selectedSeat = 'AISLE';
          } else if (data.windowAvailable) {
            this.selectedSeat = 'WINDOW';
          }
        }
        this.calculatePrice();
      },
      error: () => {
        console.log('Could not check availability');
      }
    });
  }

  isSeatAvailable(seat: string): boolean {
    if (!this.availability) return true;
    if (seat === 'WINDOW') return this.availability.windowAvailable;
    if (seat === 'AISLE') return this.availability.aisleAvailable;
    if (seat === 'MIDDLE') return this.availability.middleAvailable;
    return true;
  }

  calculatePrice() {
    this.totalPrice = this.basePrice +
      this.seatCharges[this.selectedSeat];
    this.booking.totalPrice = this.totalPrice;
    this.booking.seatType = this.selectedSeat;
  }

  selectSeat(seat: string) {
    if (!this.isSeatAvailable(seat)) return;
    this.selectedSeat = seat;
    this.calculatePrice();
  }

  onBook() {
    if (!this.isSeatAvailable(this.selectedSeat)) {
      this.message = 'Selected seat is not available!';
      this.isError = true;
      return;
    }

    if (this.availability && this.availability.isFullyBooked) {
      this.message = 'Flight is fully booked!';
      this.isError = true;
      return;
    }

    this.bookingService.createBooking(this.booking).subscribe({
      next: (response) => {
        this.message = 
          'Booking created! Redirecting to payment...';
        this.isError = false;
        this.isSuccess = true;
        localStorage.setItem('bookingAmount',
          JSON.stringify(this.totalPrice));
        localStorage.removeItem('selectedFlight');
        setTimeout(() => {
          this.router.navigate(['/payment', response.id]);
        }, 1000);
      },
      error: (err) => {
        this.message = err.error?.message || 
          'Booking failed. Please try again!';
        this.isError = true;
      }
    });
  }

  goBack() {
    this.router.navigate(['/flights']);
  }
}