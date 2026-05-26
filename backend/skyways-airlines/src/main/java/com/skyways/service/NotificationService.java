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

    private NotificationStatus sendEmail(String toEmail,
            String subject, String htmlBody) {
        try {
            logger.info("Sending email to: {}", toEmail);
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            int statusCode = response.getStatusCode();
            logger.info("SendGrid response status: {}", statusCode);

            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Email sent successfully to: {}", toEmail);
                return NotificationStatus.SENT;
            } else {
                logger.error("SendGrid failed: {}", response.getBody());
                return NotificationStatus.FAILED;
            }
        } catch (IOException e) {
            logger.error("SendGrid error: {}", e.getMessage());
            return NotificationStatus.FAILED;
        }
    }

    public Notification sendNotification(Notification notification) {
        logger.info("Sending notification to: {}",
            notification.getUsername());
        String htmlBody = "<p>" + notification.getMessage() + "</p>";
        NotificationStatus status = sendEmail(
            notification.getEmail(),
            notification.getSubject(),
            htmlBody);
        notification.setStatus(status);
        notification.setSentAt(LocalDateTime.now().toString());
        return notificationRepository.save(notification);
    }

    public Notification sendBookingConfirmation(String username,
            String email, String flightNumber,
            String origin, String destination) {
        logger.info("Sending booking confirmation to: {}", username);

        String subject = "Booking Confirmed - SkyWays Airlines";
        String htmlBody = buildBookingConfirmationHtml(
            username, flightNumber, origin, destination);

        NotificationStatus status = sendEmail(email, subject, htmlBody);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("BOOKING_CONFIRMATION")
                .subject(subject)
                .message("Booking confirmed for flight " + flightNumber
                    + " from " + origin + " to " + destination)
                .status(status)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    public Notification sendCancellationNotice(String username,
            String email, String flightNumber,
            String origin, String destination,
            String bookingDate, double refundAmount,
            double totalPaid, double refundPercentage) {
        logger.info("Sending cancellation notice to: {}", username);

        String subject = "Booking Cancelled - SkyWays Airlines";
        String htmlBody = buildCancellationHtml(
            username, flightNumber, origin, destination,
            bookingDate, refundAmount, totalPaid, refundPercentage);

        NotificationStatus status = sendEmail(email, subject, htmlBody);

        Notification notification = Notification.builder()
                .username(username)
                .email(email)
                .type("CANCELLATION")
                .subject(subject)
                .message("Booking cancelled for flight " + flightNumber
                    + " from " + origin + " to " + destination)
                .status(status)
                .sentAt(LocalDateTime.now().toString())
                .build();

        return notificationRepository.save(notification);
    }

    private String buildBookingConfirmationHtml(String username,
            String flightNumber, String origin, String destination) {
        String bookedAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return "<!DOCTYPE html><html><body style=\"margin:0;padding:0;"
            + "background:#f0f4f8;font-family:Arial,sans-serif;\">"
            + "<div style=\"background:linear-gradient(135deg,#0a1628,#1a2d4f);"
            + "padding:40px 20px;text-align:center;\">"
            + "<h1 style=\"color:#c9a84c;margin:0;font-size:32px;\">"
            + "SKYWAYS AIRLINES</h1>"
            + "<p style=\"color:#f0c060;margin:8px 0 0;font-size:14px;\">"
            + "YOUR JOURNEY BEGINS HERE</p></div>"
            + "<div style=\"background:#43a047;padding:20px;text-align:center;\">"
            + "<h2 style=\"color:#fff;margin:0;\">Booking Confirmed!</h2>"
            + "<p style=\"color:#c8e6c9;margin:6px 0 0;\">"
            + "Your adventure is ready for takeoff</p></div>"
            + "<div style=\"max-width:600px;margin:30px auto;padding:0 16px;\">"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<p style=\"font-size:18px;color:#0a1628;margin:0 0 12px;\">"
            + "Dear <b>" + username + "</b>,</p>"
            + "<p style=\"color:#555;line-height:1.7;margin:0;\">"
            + "Thank you for choosing <b>SkyWays Airlines</b>! "
            + "Your booking has been <b style=\"color:#43a047;\">successfully confirmed</b>."
            + "</p></div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#0a1628;margin:0 0 20px;"
            + "border-bottom:2px solid #c9a84c;padding-bottom:10px;\">"
            + "Flight Details</h3>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#0a1628;margin:0;\">"
            + origin + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Origin</p>"
            + "</td><td style=\"text-align:center;\">"
            + "<p style=\"color:#c9a84c;font-size:24px;margin:0;\">&#9992;</p>"
            + "<p style=\"color:#888;font-size:12px;margin:4px 0 0;\">"
            + flightNumber + "</p>"
            + "</td><td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#0a1628;margin:0;\">"
            + destination + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Destination</p>"
            + "</td></tr></table>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
            + " style=\"border-collapse:collapse;margin-top:20px;\">"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;"
            + "color:#888;font-size:13px;width:40%;\">Flight Number</td>"
            + "<td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + flightNumber + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">"
            + "Route</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + origin + " to " + destination + "</td></tr>"
            + "<tr style=\"background:#f8f9fa;\"><td style=\"padding:12px 16px;"
            + "color:#888;font-size:13px;\">Booked On</td>"
            + "<td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + bookedAt + "</td></tr></table></div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#0a1628;margin:0 0 16px;"
            + "border-bottom:2px solid #c9a84c;padding-bottom:10px;\">"
            + "Important Instructions</h3>"
            + "<p style=\"color:#333;font-size:13px;line-height:1.8;margin:0;\">"
            + "&#128467; <b>Check-in:</b> Online check-in opens 48 hours before departure.<br/>"
            + "&#127968; <b>Airport Arrival:</b> Arrive at least 2 hours before domestic flights.<br/>"
            + "&#128197; <b>Valid ID:</b> Carry government-issued photo ID.<br/>"
            + "&#128230; <b>Baggage:</b> Economy: 15kg check-in + 7kg cabin.<br/>"
            + "&#10060; <b>Prohibited:</b> No liquids over 100ml in cabin baggage.<br/>"
            + "&#128272; <b>Boarding Gate:</b> Gates close 20 minutes before departure.<br/>"
            + "&#128179; <b>Cancellation:</b> Free cancellation within 24 hours of booking."
            + "</p></div>"
            + "<div style=\"background:#e3f2fd;border-radius:12px;padding:20px 28px;"
            + "margin-bottom:20px;border-left:4px solid #0a1628;\">"
            + "<h3 style=\"color:#0a1628;margin:0 0 10px;\">Need Help?</h3>"
            + "<p style=\"color:#555;font-size:13px;margin:0;line-height:1.7;\">"
            + "Customer Support: <b>1800-SKY-WAYS</b> (Toll Free)<br/>"
            + "Email: <b>support@skywaysairlines.com</b><br/>"
            + "Available: <b>24/7</b></p></div>"
            + "<div style=\"text-align:center;padding:20px 0 40px;\">"
            + "<p style=\"color:#888;font-size:13px;margin:0 0 8px;\">"
            + "Have a safe and pleasant journey!</p>"
            + "<p style=\"color:#0a1628;font-size:20px;font-weight:bold;margin:0;\">"
            + "SkyWays Airlines</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:8px 0 0;\">"
            + "This is an automated email. Please do not reply directly.</p>"
            + "</div></div></body></html>";
    }

    private String buildCancellationHtml(String username,
            String flightNumber, String origin, String destination,
            String bookingDate, double refundAmount,
            double totalPaid, double refundPercentage) {
        String cancelledAt = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return "<!DOCTYPE html><html><body style=\"margin:0;padding:0;"
            + "background:#f0f4f8;font-family:Arial,sans-serif;\">"
            + "<div style=\"background:linear-gradient(135deg,#0a1628,#1a2d4f);"
            + "padding:40px 20px;text-align:center;\">"
            + "<h1 style=\"color:#c9a84c;margin:0;font-size:32px;\">"
            + "SKYWAYS AIRLINES</h1>"
            + "<p style=\"color:#f0c060;margin:8px 0 0;font-size:14px;\">"
            + "YOUR JOURNEY BEGINS HERE</p></div>"
            + "<div style=\"background:#e53935;padding:20px;text-align:center;\">"
            + "<h2 style=\"color:#fff;margin:0;\">Booking Cancelled</h2>"
            + "<p style=\"color:#ffcdd2;margin:6px 0 0;\">"
            + "Your booking has been successfully cancelled</p></div>"
            + "<div style=\"max-width:600px;margin:30px auto;padding:0 16px;\">"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<p style=\"font-size:18px;color:#e53935;margin:0 0 12px;\">"
            + "Dear <b>" + username + "</b>,</p>"
            + "<p style=\"color:#555;line-height:1.7;margin:0;\">We are sorry to see you go. "
            + "Your booking has been <b style=\"color:#e53935;\">successfully cancelled</b>."
            + "</p></div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#e53935;margin:0 0 20px;"
            + "border-bottom:2px solid #ffcdd2;padding-bottom:10px;\">"
            + "Cancelled Flight Details</h3>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">"
            + "<tr><td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#b71c1c;margin:0;\">"
            + origin + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Origin</p>"
            + "</td><td style=\"text-align:center;\">"
            + "<p style=\"color:#e53935;font-size:24px;margin:0;\">&#9992;</p>"
            + "<p style=\"color:#888;font-size:12px;margin:4px 0 0;\">"
            + flightNumber + "</p>"
            + "</td><td style=\"text-align:center;width:35%;\">"
            + "<p style=\"font-size:28px;font-weight:bold;color:#b71c1c;margin:0;\">"
            + destination + "</p>"
            + "<p style=\"color:#888;font-size:13px;margin:4px 0 0;\">Destination</p>"
            + "</td></tr></table>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
            + " style=\"border-collapse:collapse;margin-top:20px;\">"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;"
            + "color:#888;font-size:13px;width:40%;\">Flight Number</td>"
            + "<td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + flightNumber + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">"
            + "Travel Date</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + bookingDate + "</td></tr>"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;"
            + "color:#888;font-size:13px;\">Route</td>"
            + "<td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + origin + " to " + destination + "</td></tr>"
            + "<tr><td style=\"padding:12px 16px;color:#888;font-size:13px;\">"
            + "Cancelled On</td><td style=\"padding:12px 16px;color:#333;font-weight:bold;\">"
            + cancelledAt + "</td></tr>"
            + "<tr style=\"background:#ffebee;\"><td style=\"padding:12px 16px;"
            + "color:#888;font-size:13px;\">Status</td>"
            + "<td style=\"padding:12px 16px;\">"
            + "<span style=\"background:#ffcdd2;color:#e53935;padding:4px 12px;"
            + "border-radius:20px;font-size:13px;font-weight:bold;\">CANCELLED</span>"
            + "</td></tr></table></div>"
            + "<div style=\"background:#fff;border-radius:12px;padding:28px;"
            + "margin-bottom:20px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
            + "<h3 style=\"color:#0a1628;margin:0 0 20px;"
            + "border-bottom:2px solid #c9a84c;padding-bottom:10px;\">"
            + "Refund Details</h3>"
            + "<div style=\"background:#e8f5e9;border-radius:8px;padding:14px 18px;"
            + "margin-bottom:16px;\">"
            + "<p style=\"color:#2e7d32;font-weight:bold;margin:0 0 12px;\">REFUND SUMMARY</p>"
            + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\""
            + " style=\"border-collapse:collapse;\">"
            + "<tr><td style=\"padding:10px;color:#555;font-size:13px;\">Total Paid</td>"
            + "<td style=\"padding:10px;color:#333;font-weight:bold;font-size:15px;\">"
            + "INR " + (int)totalPaid + "</td></tr>"
            + "<tr style=\"background:#f1f8e9;\"><td style=\"padding:10px;color:#555;"
            + "font-size:13px;\">Refund Percentage</td>"
            + "<td style=\"padding:10px;color:#43a047;font-weight:bold;font-size:15px;\">"
            + (int)refundPercentage + "%</td></tr>"
            + "<tr><td style=\"padding:10px;color:#555;font-size:13px;\">Refund Amount</td>"
            + "<td style=\"padding:10px;color:#43a047;font-weight:bold;font-size:18px;\">"
            + "INR " + (int)refundAmount + "</td></tr>"
            + "<tr style=\"background:#f1f8e9;\"><td style=\"padding:10px;color:#555;"
            + "font-size:13px;\">Deduction</td>"
            + "<td style=\"padding:10px;color:#e53935;font-weight:bold;font-size:15px;\">"
            + "INR " + (int)(totalPaid - refundAmount) + "</td></tr>"
            + "</table></div>"
            + "<div style=\"background:#fff3e0;border-radius:8px;padding:14px 18px;"
            + "margin-bottom:16px;\">"
            + "<p style=\"color:#e65100;font-weight:bold;margin:0 0 12px;\">REFUND TIMELINE</p>"
            + "<p style=\"color:#333;font-size:13px;line-height:1.8;margin:0;\">"
            + "&#9989; Refund processed to original payment method.<br/>"
            + "&#9989; Credit/Debit cards: <b>5-7 business days</b>.<br/>"
            + "&#9989; UPI/Net Banking: <b>2-3 business days</b>.<br/>"
            + "&#9989; You will receive a refund confirmation email once processed."
            + "</p></div>"
            + "<div style=\"background:#ffebee;border-radius:8px;padding:14px 18px;\">"
            + "<p style=\"color:#c62828;font-weight:bold;margin:0 0 12px;\">"
            + "CANCELLATION POLICY</p>"
            + "<p style=\"color:#333;font-size:13px;line-height:1.8;margin:0;\">"
            + "&#128197; Cancelled 7+ days before travel: <b style=\"color:#43a047;\">100% refund</b><br/>"
            + "&#128197; Cancelled 3-7 days before travel: <b>75% refund</b><br/>"
            + "&#128197; Cancelled 1-3 days before travel: <b>50% refund</b><br/>"
            + "&#128197; Same day cancellation: <b style=\"color:#e53935;\">No refund</b>"
            + "</p></div></div>"
            + "<div style=\"background:#e3f2fd;border-radius:12px;padding:20px 28px;"
            + "margin-bottom:20px;border-left:4px solid #0a1628;\">"
            + "<h3 style=\"color:#0a1628;margin:0 0 10px;\">Need Help?</h3>"
            + "<p style=\"color:#555;font-size:13px;margin:0;line-height:1.7;\">"
            + "Customer Support: <b>1800-SKY-WAYS</b> (Toll Free)<br/>"
            + "Email: <b>support@skywaysairlines.com</b><br/>"
            + "Available: <b>24/7</b></p></div>"
            + "<div style=\"text-align:center;padding:20px 0 40px;\">"
            + "<p style=\"color:#888;font-size:13px;margin:0 0 8px;\">"
            + "We hope to welcome you on board again soon!</p>"
            + "<p style=\"color:#0a1628;font-size:20px;font-weight:bold;margin:0;\">"
            + "SkyWays Airlines</p>"
            + "<p style=\"color:#bbb;font-size:11px;margin:8px 0 0;\">"
            + "This is an automated email. Please do not reply directly.</p>"
            + "</div></div></body></html>";
    }

    public List<Notification> getAllNotifications() {
        logger.info("Fetching all notifications");
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsByUsername(String username) {
        logger.info("Fetching notifications for: {}", username);
        return notificationRepository.findByUsername(username);
    }
}