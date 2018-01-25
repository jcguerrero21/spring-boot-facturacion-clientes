package jc.guerrero.app.models.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jc.guerrero.app.models.dao.IClienteDao;
import jc.guerrero.app.models.dao.IFacturaDao;
import jc.guerrero.app.models.dao.IProductoDao;
import jc.guerrero.app.models.entity.Cliente;
import jc.guerrero.app.models.entity.Factura;
import jc.guerrero.app.models.entity.Producto;

@Service
public class ClienteServiceImpl implements IClienteService {

	@Autowired
	private IClienteDao clienteDao;

	@Autowired
	private IProductoDao productoDao;

	@Autowired
	private IFacturaDao facturaDao;
	
	@Override
	@Transactional(readOnly = true) // con esta anotación marcamos este método transacional y le marcamos solo de lectura porque es solo para obtener y leer datos si ponemos readOnly
	public List<Cliente> findAll() {
		return (List<Cliente>) clienteDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Cliente> findAll(Pageable pageable) {
		return clienteDao.findAll(pageable);
	}

	@Override
	@Transactional
	public void save(Cliente cliente) {
		clienteDao.save(cliente);
	}

	@Override
	@Transactional(readOnly = true)
	public Cliente findOne(Long id) {
		return clienteDao.findOne(id);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		clienteDao.delete(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Producto> findByNombre(String term) {
		return productoDao.findByNombreLikeIgnoreCase("%"+term+"%");
	}

	@Override
	@Transactional
	public void saveFactura(Factura factura) {
		facturaDao.save(factura);
	}

	@Override
	@Transactional(readOnly = true)
	public Producto findProductoById(Long id) {
		return productoDao.findOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public Factura findFacturaById(Long id) {
		return facturaDao.findOne(id);
	}

	@Override
	@Transactional
	public void deleteFactura(Long id) {
		facturaDao.delete(id);
	}

	@Override
	@Transactional(readOnly= true)
	public Factura fetchByIdWithClienteWhitItemFacturaWithProducto(Long id) {
		return facturaDao.fetchByIdWithClienteWhitItemFacturaWithProducto(id);
	}

}
