import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { FlightSearchComponent } from './components/flight-search/flight-search';
import { BookingComponent } from './components/booking/booking';
import { PaymentComponent } from './components/payment/payment';
import { BookingConfirmationComponent } from './components/booking-confirmation/booking-confirmation';
import { MyBookings } from './components/my-bookings/my-bookings';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password';
import { ResetPasswordComponent } from './components/reset-password/reset-password';
 
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'flights', component: FlightSearchComponent },
  { path: 'booking/:flightId', component: BookingComponent },
  { path: 'payment/:bookingId', component: PaymentComponent },
  { path: 'confirmation/:bookingId', component: BookingConfirmationComponent },
  { path: 'my-bookings', component: MyBookings},
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
];