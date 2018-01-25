package jc.guerrero.app.models.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import jc.guerrero.app.models.entity.Cliente;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long> { //<Cliente, sería la clase y Long sería la llave o clave primaría de nuestra clase cliente
	
	
	
}
