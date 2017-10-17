package com.ewally.test.aj.data.repository;

import com.ewally.test.aj.data.entity.Ticket;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * Ticket repository interface
 *
 * Created by amanjain
 */
@RepositoryRestResource(exported = false)
public interface TicketRepository extends CrudRepository<Ticket, String> {
    Ticket findByCode(String code);
}
