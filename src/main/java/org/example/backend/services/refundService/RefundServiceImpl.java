package org.example.backend.services.refundService;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.RefundDto;
import org.example.backend.dtoResponse.RefundResDto;
import org.example.backend.entity.Refund;
import org.example.backend.entity.User;
import org.example.backend.repository.RefundRepo;
import org.example.backend.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepo refundRepo;
    private final UserRepo userRepo;

    @Override
    @Transactional(readOnly = true)
    public List<RefundResDto> getRefunds(
            String filialId,
            String teacherId,
            String groupId,
            String studentId
    ) {
        List<RefundResDto> refundsDtos = new ArrayList<>();

        UUID fId = parseUUID(filialId);
        UUID tId = parseUUID(teacherId);
        UUID gId = parseUUID(groupId);
        UUID sId = parseUUID(studentId);

        List<Refund> refunds = refundRepo.findRefunds(fId, tId, gId, sId);

        refunds.forEach(refund -> {
            RefundResDto refundDto = new RefundResDto();
            refundDto.setId(refund.getId());
            refundDto.setStudentName(refund.getStudent().getFirstName() + " " + refund.getStudent().getLastName());
            refundDto.setAmount(refund.getAmount());
            refundDto.setDate(refund.getDate());
            refundDto.setReceptionName(refund.getReceptionist().getFirstName()+" "+refund.getReceptionist().getLastName());
            refundsDtos.add(refundDto);
        });

        return refundsDtos;
    }

    @Override
    @Transactional
    public void addRefund(RefundDto dto) {

        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new RuntimeException("Amount noto‘g‘ri");
        }

        User student = userRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        User reception = userRepo.findById(dto.getReceptionId())
                .orElseThrow(() -> new RuntimeException("Reception not found"));

        Refund refund = new Refund();
        refund.setStudent(student);
        refund.setReceptionist(reception);
        refund.setAmount(dto.getAmount());
        refund.setDate(LocalDate.now());

        refundRepo.save(refund);

    }

    @Override
    public void deleteRefund(UUID refundId) {
        if (!refundRepo.existsById(refundId)) {
            throw new RuntimeException("Refund is not found");
        }
        refundRepo.deleteById(refundId);
    }


    private UUID parseUUID(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return UUID.fromString(value);
    }


}
