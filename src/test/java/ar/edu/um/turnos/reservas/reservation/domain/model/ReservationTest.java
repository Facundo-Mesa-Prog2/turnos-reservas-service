package ar.edu.um.turnos.reservas.reservation.domain.model;

import static ar.edu.um.turnos.reservas.reservation.domain.model.ReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationTest {

    private static final Instant T0 = Instant.parse("2026-12-01T12:00:00Z");
    private static final Instant T1 = T0.plusSeconds(60);

    private Reservation newReservation() {
        return Reservation.fromHold(
            "process-1",
            "hold-1",
            "ana",
            "patient-abc",
            15L,
            LocalDate.of(2026, 12, 7),
            LocalTime.of(9, 30),
            T0.plusSeconds(600),
            T0
        );
    }

    @Test
    void startsHeldAndOwnedByTheUser() {
        Reservation reservation = newReservation();

        assertThat(reservation.getStatus()).isEqualTo(HELD);
        assertThat(reservation.belongsTo("ana")).isTrue();
        assertThat(reservation.belongsTo("otro")).isFalse();
        assertThat(reservation.getCreatedAt()).isEqualTo(T0);
    }

    @Test
    void followsTheHappyPath() {
        Reservation reservation = newReservation();

        assertThat(reservation.transitionTo(WAITING_FOR_PHONE, null, T1)).isTrue();
        assertThat(reservation.transitionTo(PHONE_SUBMITTED, null, T1)).isTrue();
        assertThat(reservation.transitionTo(CONFIRMED, null, T1)).isTrue();

        assertThat(reservation.getStatus()).isEqualTo(CONFIRMED);
        assertThat(reservation.getUpdatedAt()).isEqualTo(T1);
    }

    @Test
    void rejectedPhoneCanBeRetried() {
        Reservation reservation = newReservation();
        reservation.transitionTo(WAITING_FOR_PHONE, null, T1);
        reservation.transitionTo(PHONE_SUBMITTED, null, T1);

        assertThat(reservation.transitionTo(PHONE_REJECTED, "INVALID_PHONE_NUMBER", T1)).isTrue();
        assertThat(reservation.getStatusReason()).isEqualTo("INVALID_PHONE_NUMBER");
        assertThat(reservation.transitionTo(PHONE_SUBMITTED, null, T1)).isTrue();
    }

    @Test
    void lateEventCannotReopenFinalState() {
        Reservation reservation = newReservation();
        reservation.transitionTo(EXPIRED, "HOLD_EXPIRED", T1);

        assertThat(reservation.transitionTo(CONFIRMED, null, T1)).isFalse();
        assertThat(reservation.getStatus()).isEqualTo(EXPIRED);
        assertThat(reservation.getStatusReason()).isEqualTo("HOLD_EXPIRED");
    }

    @Test
    void repeatedTransitionIsIgnored() {
        Reservation reservation = newReservation();
        reservation.transitionTo(WAITING_FOR_PHONE, null, T0);

        assertThat(reservation.transitionTo(WAITING_FOR_PHONE, null, T1)).isFalse();
        assertThat(reservation.getUpdatedAt()).isEqualTo(T0);
    }

    @Test
    void confirmationArrivingBeforeLocalPhoneSubmittedIsAccepted() {
        Reservation reservation = newReservation();
        reservation.transitionTo(WAITING_FOR_PHONE, null, T0);

        assertThat(reservation.transitionTo(CONFIRMED, null, T1)).isTrue();
    }
}
