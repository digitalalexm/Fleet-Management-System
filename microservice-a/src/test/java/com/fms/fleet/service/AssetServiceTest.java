package com.fms.fleet.service;

import com.fms.fleet.exception.ResourceNotFoundException;
import com.fms.fleet.model.dto.Dtos.*;
import com.fms.fleet.model.entity.*;
import com.fms.fleet.model.mapper.DtoMapper;
import com.fms.fleet.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {
    @Mock AssetRepository repo;
    @Spy  DtoMapper mapper;
    @InjectMocks AssetService service;

    Asset car() { return new Asset(UUID.randomUUID(),AssetType.CAR,"CAR-001","Toyota","Corolla","ACTIVE",null,OffsetDateTime.now(),"XYZ",BigDecimal.ZERO,"PETROL",null,null,null,null,null); }

    @Test void
    getAll() {
        when(repo.findAll()).thenReturn(List.of(car(),car()));
        assertEquals(2,service.getAll().size());
    }

    @Test void
    getById() {
        Asset a=car(); when(repo.findById(a.id())).thenReturn(Optional.of(a));
        assertEquals(a.id(),service.getById(a.id()).id());
    }

    @Test void
    getByIdThrowsWhenMissing() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,()->service.getById(UUID.randomUUID())
        );
    }

    @Test void
    createDelegates() {
        Asset a=car();
        when(repo.create(any())).thenReturn(a);
        service.create(new AssetRequest("CAR","N",null,null,null,null,"P",BigDecimal.ZERO,"PETROL",null,null,null,null,null));
        verify(repo).create(any());
    }

    @Test void
    deleteThrowsWhenMissing() {
        when(repo.delete(any())).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,()->service.delete(UUID.randomUUID()));
    }
}
