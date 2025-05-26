package org.example.backend.services.referenceService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReferenceWithStatus;
import org.example.backend.entity.Reference;
import org.example.backend.entity.ReferenceStatus;
import org.example.backend.entity.User;
import org.example.backend.repository.ReferenceRepo;
import org.example.backend.repository.ReferenceStatusRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferenceServiceImpl implements ReferenceService{
    private final ReferenceRepo referenceRepo;
    private final ReferenceStatusRepo referenceStatusRepo;
    private final UserRepo userRepo;

    @Transactional
    @Override
    public List<ReferenceWithStatus> getReference() {
        List<ReferenceWithStatus> referenceWithStatusList = new ArrayList<>();
        List<Reference> referenceList = referenceRepo.findAll();

        for (Reference reference : referenceList) {
            ReferenceWithStatus referenceWithStatus = new ReferenceWithStatus();
            referenceWithStatus.setId(reference.getId().toString());
            referenceWithStatus.setName(reference.getName());
            referenceWithStatus.setPhone(reference.getPhone());
            referenceWithStatus.setTelegramUserName(reference.getTelegramUserName());

            // ReferenceStatus bo'lsa, uning qiymatlarini qo'shish
            if (reference.getReferenceStatus() != null) {
                referenceWithStatus.setStatus(reference.getReferenceStatus().isStatus());

                // Receptionist bo'lsa, uning ismi va familiyasini qo'shish
                if (reference.getReferenceStatus().getReceptionist() != null) {
                    String receptionName = reference.getReferenceStatus().getReceptionist().getFirstName() + " " + reference.getReferenceStatus().getReceptionist().getLastName();
                    referenceWithStatus.setReceptionName(receptionName);
                } else {
                    referenceWithStatus.setReceptionName("Receptionist not available");
                }

                referenceWithStatus.setCalledDateTime(reference.getReferenceStatus().getCalledDateTime());
            } else {
                referenceWithStatus.setReceptionName("Reference Status not available");
            }

            referenceWithStatusList.add(referenceWithStatus);
        }
        return referenceWithStatusList;
    }

    @Transactional
    public void deleteReference(UUID referenceId) {
        // Reference obyektini ID orqali olish
        Optional<Reference> optionalReference = referenceRepo.findById(referenceId);

        // Agar Reference mavjud bo'lsa
        if (optionalReference.isPresent()) {
            Reference reference = optionalReference.get();

            // Agar ReferenceStatus mavjud bo'lsa, uni ham o'chirish
            if (reference.getReferenceStatus() != null) {
                // ReferenceStatus obyektini o'chirish (agar kerak bo'lsa)
                referenceStatusRepo.delete(reference.getReferenceStatus());
            }

            // Reference obyektini o'chirish
            referenceRepo.delete(reference);
        } else {
            throw new EntityNotFoundException("Reference with ID " + referenceId + " not found");
        }
    }


    @Override
    public void acceptReference(UUID userId, UUID referenceId) {
        User user = userRepo.findById(userId).get();
        ReferenceStatus byReferenceId = referenceStatusRepo.findByReferenceId(referenceId);
        byReferenceId.setStatus(true);
        byReferenceId.setReceptionist(user);
        byReferenceId.setCalledDateTime(LocalDateTime.now());
        System.out.println(LocalDateTime.now());
        referenceStatusRepo.save(byReferenceId);

    }

}
