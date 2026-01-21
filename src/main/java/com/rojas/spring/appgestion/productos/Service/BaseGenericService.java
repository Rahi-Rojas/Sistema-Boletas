package com.rojas.spring.appgestion.productos.Service;

import java.util.List;
import java.util.Optional;

public interface BaseGenericService<RQ, RS, ID> {
    List<RS> findAll();
    RS findById(ID id);
    RS create(RQ request);
    RS update(ID id, RQ request);
    void deleteById(ID id);
}