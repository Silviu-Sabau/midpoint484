CREATE INDEX iAExtensionPolyNorm
    ON m_assignment_ext_poly (norm) INITRANS 30;

CREATE INDEX iExtensionPolyNorm
    ON m_object_ext_poly (norm) INITRANS 30;
