package ar.edu.um.turnos.reservas.reservation.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {
    HELD,
    WAITING_FOR_PHONE,
    PHONE_SUBMITTED,
    PHONE_REJECTED,
    CONFIRMED,
    CANCELLED,
    EXPIRED,
    INVALID;

    /** Estados sin ninguna salida posible. */
    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED || this == INVALID;
    }

    /** El proceso de reserva termino: ya no se espera ningun evento de la catedra. */
    public boolean isProcessFinished() {
        return this == CONFIRMED || isTerminal();
    }

    public boolean canTransitionTo(ReservationStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<ReservationStatus> allowedTargets() {
        return switch (this) {
            case HELD -> EnumSet.of(WAITING_FOR_PHONE, EXPIRED, INVALID);
            // CONFIRMED y PHONE_REJECTED tambien desde WAITING_FOR_PHONE: el resultado por Kafka
            // puede llegar antes de que se guarde localmente el PHONE_SUBMITTED (enunciado 7: converger).
            case WAITING_FOR_PHONE -> EnumSet.of(PHONE_SUBMITTED, CONFIRMED, PHONE_REJECTED, EXPIRED, INVALID);
            case PHONE_SUBMITTED -> EnumSet.of(CONFIRMED, PHONE_REJECTED, EXPIRED, INVALID);
            case PHONE_REJECTED -> EnumSet.of(PHONE_SUBMITTED, CONFIRMED, EXPIRED, INVALID);
            case CONFIRMED -> EnumSet.of(CANCELLED);
            case CANCELLED, EXPIRED, INVALID -> EnumSet.noneOf(ReservationStatus.class);
        };
    }
}
