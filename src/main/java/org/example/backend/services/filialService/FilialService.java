package org.example.backend.services.filialService;

import org.example.backend.entity.Filial;

import java.util.List;
import java.util.UUID;

public interface FilialService {
    List<Filial> getFilials();

    void createFilial(Filial filial);

    void updateFilial(UUID id, Filial filial);

    void deleteFilial(UUID id);
}
