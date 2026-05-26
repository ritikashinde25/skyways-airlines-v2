import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FlightService {

  private skyscannerUrl = 'http://localhost:8080/api/skyscanner';

  constructor(private http: HttpClient) {}

  searchAirport(query: string): Observable<any> {
    return this.http.get(
      `${this.skyscannerUrl}/search-airport?query=${query}`);
  }

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
}