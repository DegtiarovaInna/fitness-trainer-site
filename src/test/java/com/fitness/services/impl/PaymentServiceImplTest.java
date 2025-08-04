package com.fitness.services.impl;

import com.fitness.dto.PaymentDTO;
import com.fitness.dto.PaymentFilter;
import com.fitness.enums.BookingStatus;
import com.fitness.enums.PaymentStatus;
import com.fitness.exceptions.BookingNotFoundException;
import com.fitness.exceptions.StripeApiException;
import com.fitness.mappers.PaymentMapper;
import com.fitness.models.Booking;
import com.fitness.models.Payment;
import com.fitness.models.TimeSlot;
import com.fitness.repositories.BookingRepository;
import com.fitness.repositories.PaymentRepository;
import com.fitness.services.interfaces.EmailService;
import com.fitness.services.interfaces.PromoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.model.Event.Data;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PaymentServiceImplTest {
    private PaymentRepository paymentRepo;
    private BookingRepository bookingRepo;
    private EmailService emailService;
    private PaymentMapper paymentMapper;
    private PromoService promoService;
    private PaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        paymentRepo  = mock(PaymentRepository.class);
        bookingRepo  = mock(BookingRepository.class);
        emailService = mock(EmailService.class);
        paymentMapper= mock(PaymentMapper.class);
        promoService = mock(PromoService.class);

        service = new PaymentServiceImpl(
                paymentRepo, bookingRepo, emailService, paymentMapper, promoService
        );
        ReflectionTestUtils.setField(service, "webhookSecret", "whsec_test");
    }

    @Test
    void createPaymentIntent_bookingNotFound() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class,
                () -> service.createPaymentIntent(1L, null)
        );
    }

    @Test
    void createPaymentIntent_stripeThrows() {
        Booking b = new Booking();
        b.setId(2L);
        b.setTimeSlot(new TimeSlot());
        b.getTimeSlot().setPriceCents(1000L);

        when(bookingRepo.findById(2L)).thenReturn(Optional.of(b));
        when(promoService.calculateDiscountAmount(null, 1000L)).thenReturn(0L);

        try (MockedStatic<PaymentIntent> pimock = mockStatic(PaymentIntent.class)) {
            pimock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(mock(StripeException.class));

            StripeApiException ex = assertThrows(
                    StripeApiException.class,
                    () -> service.createPaymentIntent(2L, null)
            );
            assertTrue(ex.getMessage().toLowerCase().contains("payment"));
        }
    }

    @Test
    void createPaymentIntent_successful() {
        Booking b = new Booking();
        b.setId(3L);
        b.setTimeSlot(new TimeSlot());
        b.getTimeSlot().setPriceCents(2000L);

        when(bookingRepo.findById(3L)).thenReturn(Optional.of(b));
        when(promoService.calculateDiscountAmount("FIT20", 2000L)).thenReturn(400L);

        PaymentIntent fakePI = mock(PaymentIntent.class);
        when(fakePI.getId()).thenReturn("pi_123");
        when(fakePI.getClientSecret()).thenReturn("secret_456");

        try (MockedStatic<PaymentIntent> pimock = mockStatic(PaymentIntent.class)) {
            pimock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(fakePI);

            when(paymentRepo.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            PaymentDTO outDto = new PaymentDTO();
            when(paymentMapper.paymentToPaymentDTO(any(Payment.class))).thenReturn(outDto);

            PaymentDTO result = service.createPaymentIntent(3L, "FIT20");

            assertSame(outDto, result);
            assertEquals("secret_456", result.getClientSecret());
        }
    }

    @Test
    void handleWebhook_signatureInvalid() {
        try (MockedStatic<Webhook> whmock = mockStatic(Webhook.class)) {
            whmock.when(() -> Webhook.constructEvent(anyString(), anyString(), anyString()))
                    .thenThrow(mock(SignatureVerificationException.class));

            assertThrows(StripeApiException.class,
                    () -> service.handleWebhook("payload", "sig")
            );
        }
    }

    @Test
    void handleWebhook_successfulAndFailed() {
        String piId = "pi_ok";
        Long   bkId = 7L;

        Payment payment = Payment.builder()
                .paymentIntentId(piId)
                .bookingId(bkId)
                .status(PaymentStatus.CREATED)
                .build();
        when(paymentRepo.findByPaymentIntentId(piId))
                .thenReturn(Optional.of(payment));

        Booking booking = new Booking();
        booking.setId(bkId);
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepo.findById(bkId))
                .thenReturn(Optional.of(booking));

        Data eventData = mock(Data.class);
        PaymentIntent piMock = mock(PaymentIntent.class);
        when(piMock.getId()).thenReturn(piId);
        when(eventData.getObject()).thenReturn(piMock);

        Event successEvent = mock(Event.class);
        when(successEvent.getType()).thenReturn("payment_intent.succeeded");
        doReturn(eventData).when(successEvent).getData();

        Event failedEvent = mock(Event.class);
        when(failedEvent.getType()).thenReturn("payment_intent.payment_failed");
        doReturn(eventData).when(failedEvent).getData();

        try (MockedStatic<Webhook> wh = mockStatic(Webhook.class)) {
            wh.when(() -> Webhook.constructEvent("p","s","whsec_test"))
                    .thenReturn(successEvent);

            service.handleWebhook("p","s");
            assertEquals(PaymentStatus.SUCCEEDED,     payment.getStatus());
            assertEquals(BookingStatus.CONFIRMED,     booking.getStatus());
            verify(emailService).sendBookingConfirmationEmail(any(), eq(booking));

            payment.setStatus(PaymentStatus.CREATED);
            booking.setStatus(BookingStatus.PENDING);

            wh.when(() -> Webhook.constructEvent("p","s","whsec_test"))
                    .thenReturn(failedEvent);

            service.handleWebhook("p","s");
            assertEquals(PaymentStatus.FAILED,         payment.getStatus());
            assertEquals(BookingStatus.PAYMENT_FAILED, booking.getStatus());
            verify(emailService).sendBookingCancellationEmail(any(), eq(booking));
        }
    }
    @Test
    void search_mapsEntitiesToDTO() {
        Payment p = Payment.builder()
                .id(11L)
                .status(PaymentStatus.SUCCEEDED)
                .build();
        var page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);
        when(paymentRepo.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(page);

        PaymentDTO dto = new PaymentDTO();
        dto.setId(11L);
        when(paymentMapper.paymentToPaymentDTO(p)).thenReturn(dto);

        var result = service.search(new PaymentFilter(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(11L, result.getContent().get(0).getId());
    }

    @Test
    void refund_successful(){
        Payment pay = Payment.builder()
                .id(9L)
                .paymentIntentId("pi_ok")
                .amount(4000L)
                .status(PaymentStatus.SUCCEEDED)
                .build();
        when(paymentRepo.findById(9L)).thenReturn(Optional.of(pay));
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<Refund> rs = mockStatic(Refund.class)) {
            rs.when(() -> Refund.create(any(RefundCreateParams.class)))
                    .thenReturn(mock(Refund.class));

            service.refund(9L, 3000);

            assertEquals(PaymentStatus.REFUNDED, pay.getStatus(),
                    "status must be changed");
            rs.verify(() -> Refund.create(argThat((RefundCreateParams p1) ->
                    p1.getAmount().equals(3000L) &&
                            p1.getPaymentIntent().equals("pi_ok"))));
        }
    }

    @Test
    void refund_amountTooHigh() {
        Payment pay = Payment.builder()
                .id(8L)
                .paymentIntentId("pi_fail")
                .amount(4000L)
                .status(PaymentStatus.SUCCEEDED)
                .build();
        when(paymentRepo.findById(8L)).thenReturn(Optional.of(pay));

        assertThrows(IllegalArgumentException.class,
                () -> service.refund(8L, 10_000));
    }
}
