package ar.edu.um.turnos.reservas.reservation.domain.model;

import static ar.edu.um.turnos.reservas.reservation.domain.model.ReservationStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ReservationStatusTest {

    @ParameterizedTest
    @EnumSource(names = { "CANCELLED", "EXPIRED", "INVALID" })
    void terminalStatesHaveNoExit(ReservationStatus terminal) {
        assertThat(terminal.isTerminal()).isTrue();
        for (ReservationStatus target : ReservationStatus.values()) {
            assertThat(terminal.canTransitionTo(target)).as("%s -> %s", terminal, target).isFalse();
        }
    }

    @Test
    void confirmedOnlyAllowsCancellation() {
        for (ReservationStatus target : ReservationStatus.values()) {
            assertThat(CONFIRMED.canTransitionTo(target)).as("CONFIRMED -> %s", target).isEqualTo(target == CANCELLED);
        }
    }

    @ParameterizedTest
    @EnumSource(names = { "HELD", "WAITING_FOR_PHONE", "PHONE_SUBMITTED", "PHONE_REJECTED" })
    void pendingStatesCanExpireOrBecomeInvalid(ReservationStatus pending) {
        assertThat(pending.canTransitionTo(EXPIRED)).isTrue();
        assertThat(pending.canTransitionTo(INVALID)).isTrue();
        assertThat(pending.isProcessFinished()).isFalse();
    }

    @Test
    void stateNeverGoesBack() {
        assertThat(PHONE_SUBMITTED.canTransitionTo(WAITING_FOR_PHONE)).isFalse();
        assertThat(WAITING_FOR_PHONE.canTransitionTo(HELD)).isFalse();
        assertThat(CONFIRMED.canTransitionTo(PHONE_SUBMITTED)).isFalse();
    }

    @Test
    void cancellationOnlyFromConfirmed() {
        for (ReservationStatus source : ReservationStatus.values()) {
            assertThat(source.canTransitionTo(CANCELLED)).as("%s -> CANCELLED", source).isEqualTo(source == CONFIRMED);
        }
    }

    @Test
    void confirmedFinishesTheProcessButIsNotTerminal() {
        assertThat(CONFIRMED.isProcessFinished()).isTrue();
        assertThat(CONFIRMED.isTerminal()).isFalse();
    }
}
