package com.fitness.services.impl;

import com.fitness.dto.PaymentDTO;
import com.fitness.enums.BookingStatus;
import com.fitness.enums.PaymentStatus;
import com.fitness.exceptions.BookingNotFoundException;
import com.fitness.exceptions.StripeApiException;
import com.fitness.exceptions.errorMessage.ErrorMessage;
import com.fitness.mappers.PaymentMapper;
import com.fitness.models.Booking;
import com.fitness.models.Payment;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.PaymentRepository;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.PaymentService;
import com.fitness.services.interfaces.PromoService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final EmailService emailService;
    private final PaymentMapper paymentMapper;
    private final PromoService promoService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Override
    public PaymentDTO createPaymentIntent(Long bookingId, String promoCode) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(ErrorMessage.BOOKING_NOT_FOUND)
                );

        long amount = booking.getTimeSlot().getPriceCents();
        long discount = promoService.calculateDiscountAmount(promoCode, amount);
        amount = Math.max(0, amount - discount);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("eur")
                .putMetadata("bookingId", bookingId.toString())
                .putMetadata("promoCode", promoCode == null ? "" : promoCode)
                .build();

        try {
            PaymentIntent pi = PaymentIntent.create(params);

            Payment payment = Payment.builder()
                    .paymentIntentId(pi.getId())
                    .bookingId(bookingId)
                    .amount(amount)
                    .currency("eur")
                    .status(PaymentStatus.CREATED)
                    .promoCode(promoCode)
                    .build();
            paymentRepo.save(payment);

            PaymentDTO dto = paymentMapper.paymentToPaymentDTO(payment);
            dto.setClientSecret(pi.getClientSecret());
            dto.setPromoCode(promoCode);
            return dto;

        } catch (StripeException e) {
            throw new StripeApiException(ErrorMessage.STRIPE_API_ERROR, e);
        }
    }

    @Override
    public void handleWebhook(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded" ->
                        processSuccess((PaymentIntent) event.getData().getObject());
                case "payment_intent.payment_failed" ->
                        processFailure((PaymentIntent) event.getData().getObject());
                default -> { /* игнорируем */ }
            }

        } catch (SignatureVerificationException e) {
            throw new StripeApiException(ErrorMessage.STRIPE_API_ERROR, e);
        }
    }

    private void processSuccess(PaymentIntent pi) {
        var pay = paymentRepo.findByPaymentIntentId(pi.getId()).orElseThrow();
        pay.setStatus(PaymentStatus.SUCCEEDED);
        paymentRepo.save(pay);

        var b = bookingRepo.findById(pay.getBookingId()).orElseThrow();
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepo.save(b);

        emailService.sendBookingConfirmationEmail(b.getUser(), b);
    }

    private void processFailure(PaymentIntent pi) {
        var pay = paymentRepo.findByPaymentIntentId(pi.getId()).orElseThrow();
        pay.setStatus(PaymentStatus.FAILED);
        paymentRepo.save(pay);

        var b = bookingRepo.findById(pay.getBookingId()).orElseThrow();
        b.setStatus(BookingStatus.PAYMENT_FAILED);
        bookingRepo.save(b);

        emailService.sendBookingCancellationEmail(b.getUser(), b);
    }

}
