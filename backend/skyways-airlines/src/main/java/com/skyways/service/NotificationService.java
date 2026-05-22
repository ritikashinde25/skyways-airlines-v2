package com.skyways.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.skyways.entity.Notification;
import com.skyways.enums.NotificationStatus;
import com.skyways.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final SendGrid sendGrid;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    // ─── Core SendGrid email sender ───────────────────────────────
    private NotificationStatus sendEmail(String toEmail,
                                          String subject,
                                          String htmlBody) {
        try {
            logger.info("=== SENDGRID EMAIL ATTEMPT ===");
            logger.info("From: {} <{}>", fromName, fromEmail);
            logger.info("To: {}", toEmail);
            logger.info("Subject: {}", subject);

            Email from      = new Email(fromEmail, fromName);
            Email to        = new Email(toEmail);
            Content content = new Content("text/html", htmlBody);
            Mail mail       = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            logger.info("Calling SendGrid API...");
            Response response = sendGrid.api(request);

            int statusCode  = response.getStatusCode();
            String body     = response.getBody();
            String headers  = response.getHeaders().toString();

            logger.info("=== SENDGRID RESPONSE ===");
            logger.info("Status Code: {}", statusCode);
            logger.info("Response Body: {}", body);
            logger.info("Response Headers: {}", headers);

            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Email sent successfully to: {}", toEmail);
                return NotificationStatus.SENT;
            } else {
                logger.error("SendGrid FAILED with status: {}", statusCode);
                logger.error("Error body: {}", body);
                return NotificationStatus.FAILED;
            }

        } catch (IOException e) {
            logger.error("SendGrid IOException: {}", e.getMessage());
            logger.error("Full exception: ", e);
            return NotificationStatus.FAILED;
        }
    }

    // ─── Generic send ─────────────────────────────────────────────
    public Notification sendNotification(Notification notification) {
        logger.info("Sending notification to: {}", notification.getUsername());
        String htmlBody = "<p>" + notification.getMessage() + "</p>";
        NotificationStatus status = sendEmail(
                notification.getEmail(),
                notification.getSubject(),
                htmlBody
        );
        notification.setStatus(status);
        notification.setSentAt(LocalDateTime.now().toString());
        Notification saved = notificationRepository.save(notification);
        logger.info("Notification saved with ID: {}", saved.getId());
        return saved;
    }

    // ─── Booking Confirmation ─────────────────────────────────────
    public Notification sendBookingConfirmation(String username,
                                                 String email,
                                                 String flightNumber,
                                                 String origin,
                                                 String destination,
                                                 String flightDate,
                                                 String departureTime,
                                                 String arrivalTime,
                                                 String seatNumber,
                                                 String travelClass,
                                                 String pnr) {
        logger.info("Sending booking confirmation to: {}", username);

        String bookedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String subject = "Booking Confirmed - " + pnr + " | SkyWays Airlines";

        String htmlBody = buildBookingConfirmationHtml(
                username, pnr, origin, departureTime, flightNumber,
                destination, arrivalTime, flightDate, seatNumber,
                travelClass, bookedAt);

        NotificationStatus status = sendEmail(email, subject, htmlBody);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("BOOKING_CONFIRMATION")
                .subject(subject)
                .message("Booking confirmed | PNR: " + pnr
                        + " | Flight: " + flightNumber
                        + " | " + origin + " to " + destination
                        + " | Date: " + flightDate)
                .status(status)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    // ─── Booking Cancellation ─────────────────────────────────────
    public Notification sendCancellationNotice(String username,
                                                String email,
                                                String flightNumber,
                                                String pnr,
                                                String origin,
                                                String destination,
                                                String flightDate,
                                                String departureTime,
                                                String seatNumber,
                                                String travelClass) {
        logger.info("Sending cancellation notice to: {}", username);

        String cancelledAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String subject = "Booking Cancelled - " + pnr + " | SkyWays Airlines";

        String htmlBody = buildCancellationHtml(
                username, pnr, flightNumber, origin, destination,
                flightDate, departureTime, seatNumber, travelClass, cancelledAt);

        NotificationStatus status = sendEmail(email, subject, htmlBody);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("CANCELLATION")
                .subject(subject)
                .message("Booking " + pnr + " cancelled for flight "
                        + flightNumber + " | " + origin + " to " + destination)
                .status(status)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    // ─── Flight Delay Alert ───────────────────────────────────────
    public Notification sendFlightDelayAlert(String username,
                                              String email,
                                              String flightNumber,
                                              String newDepartureTime) {
        logger.info("Sending delay alert to: {}", username);

        String subject = "Flight Delay Notice - " + flightNumber + " | SkyWays Airlines";
        String htmlBody = "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px;\">"
            + "<div style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);padding:30px;text-align:center;border-radius:12px 12px 0 0;\">"
            + "<h1 style=\"color:#fff;margin:0;\">SKYWAYS AIRLINES</h1></div>"
            + "<div style=\"background:#fff;padding:30px;border-radius:0 0 12px 12px;box-shadow:0 2px 8px rgba(0,0,0,0.1);\">"
            + "<h2 style=\"color:#f57c00;\">Flight Delay Notice</h2>"
            + "<p>Dear <b>" + username + "</b>,</p>"
            + "<p>We regret to inform you that flight <b>" + flightNumber + "</b> has been delayed.</p>"
            + "<div style=\"background:#fff3e0;padding:20px;border-radius:8px;border-left:4px solid #f57c00;margin:20px 0;\">"
            + "<p style=\"margin:0;font-size:16px;\"><b>New Departure Time:</b><br/>"
            + "<span style=\"font-size:24px;color:#f57c00;font-weight:bold;\">" + newDepartureTime + "</span></p>"
            + "</div>"
            + "<p>Please arrive at the airport accordingly. We sincerely apologize for the inconvenience.</p>"
            + "<p style=\"color:#888;font-size:12px;\">For live updates, call <b>1800-SKY-WAYS</b> (Toll Free)</p>"
            + "</div></div>";

        NotificationStatus status = sendEmail(email, subject, htmlBody);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("FLIGHT_DELAY")
                .subject(subject)
                .message("Flight " + flightNumber + " delayed. New time: " + newDepartureTime)
                .status(status)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    // ─── Fetch helpers ────────────────────────────────────────────
    public List<Notification> getAllNotifications() {
        logger.info("Fetching all notifications");
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUsername(String username) {
        logger.info("Fetching notifications for: {}", username);
        return notificationRepository.findByUsername(username);
    }

    // ─── Build Booking Confirmation HTML ──────────────────────────
    private String buildBookingConfirmationHtml(
            String username, String pnr,
            String origin, String departureTime,
            String flightNumber, String destination,
            String arrivalTime, String flightDate,
            String seatNumber, String travelClass,
            String bookedAt) {

        return "<!DOCTYPE html><html><head>"
            + "<meta charset=\"UTF-8\"/>"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>"
            + "</head>"
            + "<body style=\"margin:0;padding:0;background:#f0f4f8;font-family:Arial,sans-serif;\">"
            + "<div style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);padding:40px 20px;text-align:center;\">"
            + "<h1 style=\"color:#fff;margin:0;font-size:32px;letter-spacing:2px;\">SKYWAYS AIRLINES</h1>"
            + "<p style=\"color:#bbdefb;margin:8px 0 0;font-size:14px;\">YOUR JOURNEY BEGINS HERE</p>"
            + "</div>"
            + "<div style=\"background:#43a047;padding:20px;text-align:center;\">"
            + "<h2 style=\"color:#fff;margin:0;font-size:22px;\">Booking Confirmed!</h2>"
            + "<p style=\"color:#c8e6c9;margin:6px 0 0;font-size:14px;\">Your adventure is ready for takeoff</p>"
            + "</div>"
            + "<div style=\"max-width:600px;margin:30px auto;padding:0 16px;\">"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<p style=\"font-size:18px;color:#1a73e8;margin:0 0 12px;\">Dear <b>" + username + "</b>,</p>"
            + "<p style=\"color:#555;line-height:1.7;margin:0;\">Thank you for choosing <b>SkyWays Airlines</b>! "
            + "Your booking has been <b style=\"color:#43a047;\">successfully confirmed</b>. "
            + "Please review your flight details below and keep this email for your records.</p>"
            + "</div>"
            + "<div style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);border-radius:12px;padding:24px;text-align:center;margin-bottom:20px;\">"
            + "<p style=\"color:#bbdefb;margin:0 0 6px;font-size:13px;letter-spacing:2px;text-transform:uppercase;\">Booking Reference (PNR)</p>"
            + "<h2 style=\"color:#fff;margin:0;font-size:36px;letter-spacing:6px;font-family:monospace;\">" + pnr + "</h2>"
            + "<p style=\"color:#bbdefb;margin:8px 0 0;font-size:12px;\">Use this code for check-in and boarding</p>"
            + "</div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#1a73e8;margin:0 0 20px;font-size:16px;border-bottom:2px solid #e3f2fd;padding-bottom:10px;\">Flight Details</h3>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:24px;\">"
            + "<tr>"
            + "<td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#0d47a1;margin:0;\">" + origin + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Departure</p>"
            + "<p style=\"color:#1a73e8;font-size:16px;font-weight:bold;margin:4px 0 0;\">" + departureTime + "</p>"
            + "</td>"
            + "<td style=\"text-align:center;\">"
            + "<p style=\"color:#1a73e8;font-size:22px;margin:0;\">&#9992;</p>"
            + "<hr style=\"border-top:2px dashed #1a73e8;margin:4px 0;\"/>"
            + "<p style=\"color:#888;font-size:12px;margin:4px 0 0;\">" + flightNumber + "</p>"
            + "</td>"
            + "<td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#0d47a1;margin:0;\">" + destination + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Arrival</p>"
            + "<p style=\"color:#1a73e8;font-size:16px;font-weight:bold;margin:4px 0 0;\">" + arrivalTime + "</p>"
            + "</td>"
            + "</tr>"
            + "</table>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;width:40%;\">Flight Number</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + flightNumber + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Travel Date</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + flightDate + "</td></tr>"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Departure Time</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + departureTime + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Arrival Time</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + arrivalTime + "</td></tr>"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Seat Number</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + seatNumber + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Travel Class</td><td style=\"padding:12px 16px;\"><span style=\"background:#e3f2fd;color:#1a73e8;padding:4px 12px;border-radius:20px;font-size:13px;font-weight:bold;\">" + travelClass + "</span></td></tr>"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Booked On</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + bookedAt + "</td></tr>"
            + "</table>"
            + "</div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#1a73e8;margin:0 0 20px;font-size:16px;border-bottom:2px solid #e3f2fd;padding-bottom:10px;\">Important Travel Instructions</h3>"
            + "<div style=\"background:#e3f2fd;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#0d47a1;font-weight:bold;margin:0 0 12px;font-size:14px;\">BEFORE YOUR TRAVEL</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128467; <b>Online Check-in:</b> Opens <b>48 hours</b> before and closes <b>1 hour</b> before departure.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128196; <b>Boarding Pass:</b> Print or save on your phone before reaching the airport.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128197; <b>Valid ID Proof:</b> Carry original Aadhaar, Passport, Voter ID, or Driving License. <b>Photocopies NOT accepted.</b></p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#128241; <b>Flight Status:</b> Track using PNR <b>" + pnr + "</b> on SkyWays app before leaving.</p>"
            + "</div>"
            + "<div style=\"background:#e8f5e9;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#2e7d32;font-weight:bold;margin:0 0 12px;font-size:14px;\">AIRPORT ARRIVAL TIMINGS</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; <b>Domestic Flights:</b> Arrive at least <b>2 hours before</b> departure.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; <b>International Flights:</b> Arrive at least <b>3 hours before</b> departure.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; <b>Check-in Counter:</b> Closes <b>60 minutes before</b> departure. Late arrivals will <b>not</b> be boarded.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#9989; <b>Boarding Gate:</b> Gates close <b>20 minutes before</b> departure. Be at the gate on time.</p>"
            + "</div>"
            + "<div style=\"background:#fff3e0;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#e65100;font-weight:bold;margin:0 0 12px;font-size:14px;\">BAGGAGE ALLOWANCE</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128230; <b>Economy Class:</b> 15 kg check-in + 7 kg cabin baggage.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128230; <b>Business Class:</b> 30 kg check-in + 10 kg cabin baggage.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128230; <b>Excess Baggage:</b> Rs. 500/kg charges apply. Pre-book online to save 30%.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#128230; <b>Cabin Bag Size:</b> Max 55x35x25 cm. One cabin bag + one personal item allowed.</p>"
            + "</div>"
            + "<div style=\"background:#ffebee;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#c62828;font-weight:bold;margin:0 0 12px;font-size:14px;\">PROHIBITED ITEMS</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#10060; <b>Cabin Baggage:</b> No sharp objects, scissors, or knives. No liquids over 100ml.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#10060; <b>Banned Items:</b> No explosives, flammable materials, or radioactive substances.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#10060; <b>Electronics:</b> Laptops must be removed from bags during security screening.</p>"
            + "</div>"
            + "<div style=\"background:#f3e5f5;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#6a1b9a;font-weight:bold;margin:0 0 12px;font-size:14px;\">SECURITY AND BOARDING</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128272; <b>Security Check:</b> Remove belt, shoes, and metal objects. Keep boarding pass and ID ready.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128272; <b>Boarding Priority:</b> Special needs, families with infants, and Business Class board first.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#128272; <b>Mobile Phones:</b> Switch to airplane mode once onboard. Wi-Fi on select flights.</p>"
            + "</div>"
            + "<div style=\"background:#fce4ec;border-radius:8px;padding:14px 18px;\">"
            + "<p style=\"color:#880e4f;font-weight:bold;margin:0 0 12px;font-size:14px;\">CANCELLATION AND CHANGES</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128179; <b>Free Cancellation:</b> Cancel within <b>24 hours</b> of booking for a full refund.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128179; <b>After 24 hours:</b> Rs. 1500 cancellation charge. Refund in 5-7 business days.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#128179; <b>Date/Seat Change:</b> Allowed up to <b>4 hours before departure</b> with Rs. 500 fee.</p>"
            + "</div>"
            + "</div>"
            + "<div style=\"background:#e3f2fd;border-radius:12px;padding:20px 28px;margin-bottom:20px;border-left:4px solid #1a73e8;\">"
            + "<h3 style=\"color:#1a73e8;margin:0 0 10px;font-size:15px;\">Need Help?</h3>"
            + "<p style=\"color:#555;font-size:13px;margin:0;line-height:1.7;\">Customer Support: <b>1800-SKY-WAYS</b> (Toll Free)<br/>Email: <b>support@skywaysairlines.com</b><br/>Available: <b>24/7</b></p>"
            + "</div>"
            + "<div style=\"text-align:center;padding:20px 0 40px;\">"
            + "<p style=\"color:#888;font-size:13px;margin:0 0 8px;\">Have a safe and pleasant journey!</p>"
            + "<p style=\"color:#1a73e8;font-size:20px;font-weight:bold;margin:0;\">SkyWays Airlines</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:8px 0 0;\">This is an automated email. Please do not reply directly.</p>"
            + "</div>"
            + "</div>"
            + "</body></html>";
    }

    // ─── Build Cancellation HTML ───────────────────────────────────
    private String buildCancellationHtml(
            String username, String pnr, String flightNumber,
            String origin, String destination, String flightDate,
            String departureTime, String seatNumber,
            String travelClass, String cancelledAt) {

        return "<!DOCTYPE html><html><head>"
            + "<meta charset=\"UTF-8\"/>"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>"
            + "</head>"
            + "<body style=\"margin:0;padding:0;background:#f0f4f8;font-family:Arial,sans-serif;\">"
            + "<div style=\"background:linear-gradient(135deg,#1a73e8,#0d47a1);padding:40px 20px;text-align:center;\">"
            + "<h1 style=\"color:#fff;margin:0;font-size:32px;letter-spacing:2px;\">SKYWAYS AIRLINES</h1>"
            + "<p style=\"color:#bbdefb;margin:8px 0 0;font-size:14px;\">YOUR JOURNEY BEGINS HERE</p>"
            + "</div>"
            + "<div style=\"background:#e53935;padding:20px;text-align:center;\">"
            + "<h2 style=\"color:#fff;margin:0;font-size:22px;\">Booking Cancelled</h2>"
            + "<p style=\"color:#ffcdd2;margin:6px 0 0;font-size:14px;\">Your booking has been successfully cancelled</p>"
            + "</div>"
            + "<div style=\"max-width:600px;margin:30px auto;padding:0 16px;\">"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<p style=\"font-size:18px;color:#e53935;margin:0 0 12px;\">Dear <b>" + username + "</b>,</p>"
            + "<p style=\"color:#555;line-height:1.7;margin:0;\">We are sorry to see you go. Your booking has been "
            + "<b style=\"color:#e53935;\">successfully cancelled</b> as per your request. "
            + "Below are the details of your cancelled booking.</p>"
            + "</div>"
            + "<div style=\"background:linear-gradient(135deg,#e53935,#b71c1c);border-radius:12px;padding:24px;text-align:center;margin-bottom:20px;\">"
            + "<p style=\"color:#ffcdd2;margin:0 0 6px;font-size:13px;letter-spacing:2px;text-transform:uppercase;\">Cancelled Booking Reference</p>"
            + "<h2 style=\"color:#fff;margin:0;font-size:36px;letter-spacing:6px;font-family:monospace;\">" + pnr + "</h2>"
            + "<p style=\"color:#ffcdd2;margin:8px 0 0;font-size:12px;\">Cancelled on: " + cancelledAt + "</p>"
            + "</div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#e53935;margin:0 0 20px;font-size:16px;border-bottom:2px solid #ffcdd2;padding-bottom:10px;\">Cancelled Flight Details</h3>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-bottom:24px;\">"
            + "<tr>"
            + "<td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#b71c1c;margin:0;\">" + origin + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Origin</p>"
            + "<p style=\"color:#e53935;font-size:16px;font-weight:bold;margin:4px 0 0;\">" + departureTime + "</p>"
            + "</td>"
            + "<td style=\"text-align:center;\">"
            + "<p style=\"color:#e53935;font-size:22px;margin:0;\">&#9992;</p>"
            + "<hr style=\"border-top:2px dashed #e53935;margin:4px 0;\"/>"
            + "<p style=\"color:#888;font-size:12px;margin:4px 0 0;\">" + flightNumber + "</p>"
            + "</td>"
            + "<td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#b71c1c;margin:0;\">" + destination + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Destination</p>"
            + "</td>"
            + "</tr>"
            + "</table>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;width:40%;\">Flight Number</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + flightNumber + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Travel Date</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + flightDate + "</td></tr>"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Route</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + origin + " to " + destination + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Seat Number</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">" + seatNumber + "</td></tr>"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Travel Class</td><td style=\"padding:12px 16px;\"><span style=\"background:#ffcdd2;color:#e53935;padding:4px 12px;border-radius:20px;font-size:13px;font-weight:bold;\">" + travelClass + "</span></td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">Status</td><td style=\"padding:12px 16px;\"><span style=\"background:#ffcdd2;color:#e53935;padding:4px 12px;border-radius:20px;font-size:13px;font-weight:bold;\">CANCELLED</span></td></tr>"
            + "</table>"
            + "</div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#1a73e8;margin:0 0 20px;font-size:16px;border-bottom:2px solid #e3f2fd;padding-bottom:10px;\">Refund and Cancellation Policy</h3>"
            + "<div style=\"background:#e8f5e9;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#2e7d32;font-weight:bold;margin:0 0 12px;font-size:14px;\">REFUND DETAILS</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; Refund will be processed to your <b>original payment method</b>.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; Credit/Debit Card refunds: <b>5-7 business days</b>.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9989; UPI/Net Banking refunds: <b>2-3 business days</b>.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#9989; You will receive a refund confirmation email once processed.</p>"
            + "</div>"
            + "<div style=\"background:#fff3e0;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#e65100;font-weight:bold;margin:0 0 12px;font-size:14px;\">CANCELLATION CHARGES</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128197; Within <b>24 hours of booking</b>: <b style=\"color:#43a047;\">Full Refund (No charges)</b></p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128197; <b>7+ days before departure</b>: Rs. 500 cancellation fee.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128197; <b>3-7 days before departure</b>: Rs. 1000 cancellation fee.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#128197; <b>24-72 hours before departure</b>: Rs. 1500 cancellation fee.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#128197; <b>Less than 24 hours before departure</b>: <b style=\"color:#e53935;\">No Refund</b>.</p>"
            + "</div>"
            + "<div style=\"background:#ffebee;border-radius:8px;padding:14px 18px;margin-bottom:16px;\">"
            + "<p style=\"color:#c62828;font-weight:bold;margin:0 0 12px;font-size:14px;\">NO-SHOW POLICY</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#10060; No-show without cancelling: <b>No Refund</b>.</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#10060; Missed boarding due to late arrival: <b>No Refund or rebooking</b>.</p>"
            + "</div>"
            + "<div style=\"background:#e3f2fd;border-radius:8px;padding:14px 18px;\">"
            + "<p style=\"color:#0d47a1;font-weight:bold;margin:0 0 12px;font-size:14px;\">REBOOK YOUR FLIGHT</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9992; Want to travel on a different date? Rebook on SkyWays app or website.</p>"
            + "<p style=\"margin:0 0 8px;color:#333;font-size:13px;line-height:1.7;\">&#9992; Use promo code <b>SKYBACK10</b> for <b>10% off</b> your next booking!</p>"
            + "<p style=\"margin:0;color:#333;font-size:13px;line-height:1.7;\">&#9992; SkyWays Miles earned on this booking will be <b>retained</b> in your account.</p>"
            + "</div>"
            + "</div>"
            + "<div style=\"background:#e3f2fd;border-radius:12px;padding:20px 28px;margin-bottom:20px;border-left:4px solid #1a73e8;\">"
            + "<h3 style=\"color:#1a73e8;margin:0 0 10px;font-size:15px;\">Need Help?</h3>"
            + "<p style=\"color:#555;font-size:13px;margin:0;line-height:1.7;\">Customer Support: <b>1800-SKY-WAYS</b> (Toll Free)<br/>Email: <b>support@skywaysairlines.com</b><br/>Available: <b>24/7</b></p>"
            + "</div>"
            + "<div style=\"text-align:center;padding:20px 0 40px;\">"
            + "<p style=\"color:#888;font-size:13px;margin:0 0 8px;\">We hope to welcome you on board again soon!</p>"
            + "<p style=\"color:#1a73e8;font-size:20px;font-weight:bold;margin:0;\">SkyWays Airlines</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:8px 0 0;\">This is an automated email. Please do not reply directly.</p>"
            + "</div>"
            + "</div>"
            + "</body></html>";
    }
}