package com.example.dao;


import org.springframework.data.jpa.repository.JpaRepository;


import com.example.entities.Mascota;

public interface MascotaDao extends JpaRepository<Mascota, Long>{


 
    
}
