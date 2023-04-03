package com.example.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entities.Hotel;

public interface HotelDao extends JpaRepository<Hotel, Long>{
    
}
