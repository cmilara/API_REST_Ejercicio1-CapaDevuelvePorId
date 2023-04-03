package com.example.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.entities.Cliente;

public interface ClienteService {

    public List<Cliente> findAll(Sort sort);
    public Page<Cliente> findAll(Pageable pageable);
    public Cliente findById(long id);
    public Cliente save(Cliente cliente);
    public void delete(Cliente cliente);
    
}
