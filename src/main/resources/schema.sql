-- dialect:postgresql
CREATE UNIQUE INDEX IF NOT EXISTS ux_booking_active_slot
  ON booking (time_slot_id)
  WHERE status <> 'CANCELLED';