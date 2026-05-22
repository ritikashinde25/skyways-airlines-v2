import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FlightService {

  private baseUrl = 'http://localhost:8080/api/flights';
  private skyscannerUrl = 'http://localhost:8080/api/skyscanner';

  constructor(private http: HttpClient) {}

  // Search airport to get skyId and entityId
  searchAirport(query: string): Observable<any> {
    return this.http.get(
      `${this.skyscannerUrl}/search-airport?query=${query}`);
  }

  // Search real flights via Skyscanner
  searchSkyscannerFlights(
      originSkyId: string,
      destinationSkyId: string,
      originEntityId: string,
      destinationEntityId: string,
      date: string,
      cabinClass: string = 'economy',
      adults: number = 1): Observable<any> {
    return this.http.get(
      `${this.skyscannerUrl}/search` +
      `?originSkyId=${originSkyId}` +
      `&destinationSkyId=${destinationSkyId}` +
      `&originEntityId=${originEntityId}` +
      `&destinationEntityId=${destinationEntityId}` +
      `&date=${date}` +
      `&cabinClass=${cabinClass}` +
      `&adults=${adults}`
    );
  }

  // Get cheapest flights for a month
  getCheapestOneway(originSkyId: string,
      destinationSkyId: string,
      month: string): Observable<any> {
    return this.http.get(
      `${this.skyscannerUrl}/cheapest-oneway` +
      `?originSkyId=${originSkyId}` +
      `&destinationSkyId=${destinationSkyId}` +
      `&month=${month}`
    );
  }

  getFlightById(id: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/${id}`);
  }
}