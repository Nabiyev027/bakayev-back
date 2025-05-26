package org.example.backend.services.filialService;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Filial;
import org.example.backend.repository.FilialRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FilialServiceImpl implements FilialService {

    private final FilialRepo filialRepo;

    @Override
    public List<Filial> getFilials() {
        List<Filial> filials = filialRepo.findAll();
        return filials;
    }

    @Override
    public void createFilial(Filial filial) {
        Filial newFilial = new Filial();
        newFilial.setName(filial.getName());
        newFilial.setDescription(filial.getDescription());
        newFilial.setLocation(filial.getLocation());
        filialRepo.save(newFilial);
    }

    @Override
    public void updateFilial(UUID id, Filial filial) {
        Optional<Filial> optionalFilial = filialRepo.findById(id);
        if (optionalFilial.isEmpty()) {
            throw new RuntimeException("Filial topilmadi: " + id);
        }

        Filial filial1 = optionalFilial.get();
        filial1.setName(filial.getName());
        filial1.setDescription(filial.getDescription());
        filial1.setLocation(filial.getLocation());
        filialRepo.save(filial1);
    }

    @Override
    public void deleteFilial(UUID id) {
        Optional<Filial> optionalFilial = filialRepo.findById(id);
        if (optionalFilial.isEmpty()) {
            throw new RuntimeException("Filial topilmadi: " + id);
        }

        filialRepo.deleteById(id);
    }


}
