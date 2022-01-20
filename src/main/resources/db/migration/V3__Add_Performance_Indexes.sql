-- Add indexes for better query performance

CREATE INDEX idx_serialized_unit_batch_id ON serialized_unit(batch_id);
CREATE INDEX idx_serialized_unit_status ON serialized_unit(status);
CREATE INDEX idx_serialized_unit_gtin ON serialized_unit(gtin);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
