import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FlightService } from '../../services/flight';

@Component({
  selector: 'app-flight-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './flight-search.html',
  styleUrl: './flight-search.css'
})
export class FlightSearchComponent implements OnInit {

  origin = '';
  destination = '';
  travelDate = '';
  cabinClass = 'economy';
  adults = 1;
  flights: any[] = [];
  message = '';
  isLoading = false;
  searched = false;
  username = localStorage.getItem('username') || '';

  originAirports: any[] = [];
  destinationAirports: any[] = [];
  selectedOrigin: any = null;
  selectedDestination: any = null;
  showOriginDropdown = false;
  showDestinationDropdown = false;

  private originTimeout: any;
  private destinationTimeout: any;

  constructor(
    private flightService: FlightService,
    private router: Router
  ) {}

  ngOnInit() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.travelDate = tomorrow.toISOString().split('T')[0];
  }

  searchOriginAirport() {
    this.selectedOrigin = null;
    clearTimeout(this.originTimeout);
    if (this.origin.length < 2) {
      this.originAirports = [];
      this.showOriginDropdown = false;
      return;
    }
    this.originTimeout = setTimeout(() => {
      this.flightService.searchAirport(this.origin).subscribe({
        next: (data: any) => {
          this.originAirports = data.places || [];
          this.showOriginDropdown = 
            this.originAirports.length > 0;
        },
        error: () => {
          this.originAirports = [];
          this.showOriginDropdown = false;
        }
      });
    }, 400);
  }

  searchDestinationAirport() {
    this.selectedDestination = null;
    clearTimeout(this.destinationTimeout);
    if (this.destination.length < 2) {
      this.destinationAirports = [];
      this.showDestinationDropdown = false;
      return;
    }
    this.destinationTimeout = setTimeout(() => {
      this.flightService.searchAirport(this.destination)
        .subscribe({
          next: (data: any) => {
            this.destinationAirports = data.places || [];
            this.showDestinationDropdown = 
              this.destinationAirports.length > 0;
          },
          error: () => {
            this.destinationAirports = [];
            this.showDestinationDropdown = false;
          }
        });
    }, 400);
  }

  selectOrigin(airport: any) {
    this.selectedOrigin = airport;
    this.origin = airport.name + 
      (airport.iataCode ? ' (' + airport.iataCode + ')' : '');
    this.showOriginDropdown = false;
    this.originAirports = [];
  }

  selectDestination(airport: any) {
    this.selectedDestination = airport;
    this.destination = airport.name + 
      (airport.iataCode ? ' (' + airport.iataCode + ')' : '');
    this.showDestinationDropdown = false;
    this.destinationAirports = [];
  }

  onSearch() {
    if (!this.selectedOrigin || !this.selectedDestination) {
      this.message = 
        'Please select origin and destination airports!';
      return;
    }
    if (!this.travelDate) {
      this.message = 'Please select a travel date!';
      return;
    }

    this.isLoading = true;
    this.message = '';
    this.searched = true;
    this.flights = [];

    this.flightService.searchSkyscannerFlights(
      this.selectedOrigin.skyId,
      this.selectedDestination.skyId,
      this.selectedOrigin.entityId,
      this.selectedDestination.entityId,
      this.travelDate,
      this.cabinClass,
      this.adults
    ).subscribe({
      next: (data: any) => {
        this.isLoading = false;
        this.flights = data.itineraries || [];
        if (this.flights.length === 0) {
          this.message = 'No flights found!';
        }
      },
      error: () => {
        this.isLoading = false;
        this.message = 
          'Failed to search flights. Please try again!';
      }
    });
  }

  onClear() {
    this.origin = '';
    this.destination = '';
    this.flights = [];
    this.message = '';
    this.searched = false;
    this.selectedOrigin = null;
    this.selectedDestination = null;
    this.originAirports = [];
    this.destinationAirports = [];
  }

  onBook(flight: any) {
    localStorage.setItem('selectedFlight', 
    JSON.stringify(flight));
    localStorage.setItem('travelDate', this.travelDate);
    this.router.navigate(['/booking', 'skyscanner']);
  }

  goToMyBookings() {
    this.router.navigate(['/my-bookings']);
  }

  logout() {
    localStorage.removeItem('username');
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}