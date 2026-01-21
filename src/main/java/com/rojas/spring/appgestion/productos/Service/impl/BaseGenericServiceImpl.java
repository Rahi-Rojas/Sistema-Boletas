package com.rojas.spring.appgestion.productos.Service.impl;

import com.rojas.spring.appgestion.productos.Exception.ApiErrorException;
import com.rojas.spring.appgestion.productos.Service.BaseGenericService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Motor genérico de servicios actualizado al estilo Dinope.
 */
public abstract class BaseGenericServiceImpl<T, RQ, RS, ID, R extends JpaRepository<T, ID>>
        implements BaseGenericService<RQ, RS, ID> {

    protected final R repository;

    protected BaseGenericServiceImpl(R repository) {
        this.repository = repository;
    }

    // Métodos abstractos para el mapeo que los hijos deben implementar
    protected abstract RS mapToResponse(T entity);
    protected abstract T mapToEntity(RQ request);

    @Override
    @Transactional(readOnly = true)
    public List<RS> findAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RS findById(ID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ApiErrorException(
                        "No se encontró el registro con el ID: " + id,
                        HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public RS create(RQ request) {
        try {
            T entity = mapToEntity(request);
            return mapToResponse(repository.save(entity));
        } catch (Exception e) {
            throw new ApiErrorException(
                    "Ocurrió un error al crear el registro",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public RS update(ID id, RQ request) {
        return repository.findById(id)
                .map(existingEntity -> {
                    T entityUpdates = mapToEntity(request);
                    try {
                        // Usamos Reflection para asegurar que el ID se mantenga en el update
                        Field idField = findField(entityUpdates.getClass(), "id");
                        idField.setAccessible(true);
                        idField.set(entityUpdates, id);

                        // Sincronizar el campo isActive si existe para no perderlo en el update
                        try {
                            Field activeField = findField(entityUpdates.getClass(), "isActive");
                            activeField.setAccessible(true);
                            // Obtenemos el valor actual de la base de datos
                            Field existingActiveField = findField(existingEntity.getClass(), "isActive");
                            existingActiveField.setAccessible(true);
                            activeField.set(entityUpdates, existingActiveField.get(existingEntity));
                        } catch (Exception ignored) {
                            // Si la entidad no tiene isActive, ignoramos este paso
                        }

                    } catch (Exception e) {
                        throw new ApiErrorException(
                                "Error al procesar la actualización del ID",
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                    return mapToResponse(repository.save(entityUpdates));
                })
                .orElseThrow(() -> new ApiErrorException(
                        "No se pudo actualizar: El ID " + id + " no existe",
                        HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        if (!repository.existsById(id)) {
            throw new ApiErrorException(
                    "No se puede eliminar: ID " + id + " no encontrado",
                    HttpStatus.NOT_FOUND);
        }
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new ApiErrorException(
                    "Error al eliminar el registro",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            throw e;
        }
    }
}