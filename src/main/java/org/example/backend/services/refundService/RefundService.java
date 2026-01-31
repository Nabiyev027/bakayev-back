package org.example.backend.services.refundService;

import org.example.backend.dto.RefundDto;
import org.example.backend.dtoResponse.RefundResDto;

import java.util.List;
import java.util.UUID;

public interface RefundService {

    List<RefundResDto> getRefunds(String filialId, String teacherId, String groupId, String studentId);

    void addRefund(RefundDto refundDto);

    void deleteRefund(UUID refundId);
}
