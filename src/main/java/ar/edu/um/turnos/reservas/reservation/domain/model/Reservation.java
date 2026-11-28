package ar.edu.um.turnos.reservas.reservation.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    private Long id;
    private String reservationProcessId;
    private String holdId;
    private String userLogin;
    private String externalPatientId;
    private Long professionalId;
    private LocalDate date;
    private LocalTime startTime;
    private ReservationStatus status;
    private String statusReason;
    private String requestEventId;
    private Long catedraReservationId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static Reservation fromHold(
        String reservationProcessId,
        String holdId,
        String userLogin,
        String externalPatientId,
        Long professionalId,
        LocalDate date,
        LocalTime startTime,
        Instant expiresAt,
        Instant now
    ) {
        return Reservation.builder()
            .reservationProcessId(reservationProcessId)
            .holdId(holdId)
            .userLogin(userLogin)
            .externalPatientId(externalPatientId)
            .professionalId(professionalId)
            .date(date)
            .startTime(startTime)
            .status(ReservationStatus.HELD)
            .expiresAt(expiresAt)
            .createdAt(now)
            .updatedAt(now)
            .build();
    }

    public boolean transitionTo(ReservationStatus target, String reason, Instant now) {
        if (status == target || !status.canTransitionTo(target)) {
            return false;
        }
        status = target;
        statusReason = reason;
        updatedAt = now;
        return true;
    }

    public void registerPhoneRequest(String requestEventId, Instant now) {
        this.requestEventId = requestEventId;
        this.updatedAt = now;
    }

    public void registerCatedraReservationId(Long catedraReservationId, Instant now) {
        this.catedraReservationId = catedraReservationId;
        this.updatedAt = now;
    }

    public boolean belongsTo(String login) {
        return userLogin != null && userLogin.equals(login);
    }
}
