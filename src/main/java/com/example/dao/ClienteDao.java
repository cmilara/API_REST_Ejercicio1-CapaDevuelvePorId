package com.example.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entities.Cliente;

public interface ClienteDao extends JpaRepository<Cliente, Long>{
      // @Query(value = "select c from Cliente c left join fetch c.hotel")

   // @Query(value = "delete from telefonos where estudiante_id = :idEstudiante", nativeQuery = true)

 // @Query(value = "select * from cliente  left join hotel on hotel.id = cliente.hotel_id  left join mascota on cliente.id = mascota.cliente_id", nativeQuery = true)
  // @Query(value = "select c from Cliente c inner join fetch c.mascotas left join fetch c.hotel") 

 @Query(value = "select c from Cliente c inner join fetch c.hotel inner join fetch c.mascotas")

   public List<Cliente> findAll(Sort sort);

 //  @Query(value = "select distinct * from cliente  left join hotel on hotel.id = cliente.hotel_id  left join mascota on cliente.id = mascota.cliente_id", nativeQuery = true, countQuery = "select count(c) from Cliente c left join c.hotel left join c.mascotas" )
  @Query(value = "select c from Cliente c inner join fetch c.hotel inner join fetch c.mascotas", 
  countQuery = "select count(c) from Cliente c left join c.hotel left join c.mascotas")
 // @Query(value = "select c from Cliente  c left join fetch c.hotel left join fetch c.mascotas", countQuery = "select count(c) from Cliente c left join c.hotel left join c.mascotas")

    public Page<Cliente> findAll(Pageable pageable);

  @Query(value = "select c from Cliente c left join fetch c.hotel left join fetch c.mascotas where c.id = :id")
 // @Query(value = "select c from Cliente c left join fetch c.hotel where c.id = :id")
    public Cliente findById(long id);



}
