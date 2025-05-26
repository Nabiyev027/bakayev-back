package org.example.backend.services.referenceService;


import org.example.backend.dto.ReferenceWithStatus;

import java.util.List;
import java.util.UUID;

public interface ReferenceService {

    List<ReferenceWithStatus> getReference();

    void deleteReference(UUID referenceId);

    void acceptReference(UUID userId, UUID referenceId);

}
