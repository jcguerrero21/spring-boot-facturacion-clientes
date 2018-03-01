package jc.guerrero.app.controllers;

import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jc.guerrero.app.models.entity.Cliente;
import jc.guerrero.app.models.service.IClienteService;
import jc.guerrero.app.models.service.IUploadFileService;
import jc.guerrero.app.utilidades.paginator.PageRender;

@Controller // Para indicar que esta clase es un controlador
@SessionAttributes("cliente") // con esto coge todos los datos persistentes hasta el id, por lo tanto en el
// html no necesitariamos hacer un input hidden del id para nada
public class ClienteController {

	protected final Log logger = LogFactory.getLog(this.getClass());

	@Autowired // Con esta anotación buscará por el proyecto la clase indicada para poder
	// implementarla
	// @Qualifier("ClienteDaoJPA") //Con está anotación indicamos el nombre del
	// componente concreto por si tenemos más de uno(para JDBC, Hibernate, etc)
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadFileService;

	@RequestMapping(value = { "/listar", "/" }, method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model,
						 Authentication authentication) {

		if (authentication != null) {
			logger.info("Hola usuario autenticado, tu username es: ".concat(authentication.getName()));
		}

//		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//		if(auth != null) {
//			logger.info("Utilizando forma estática SecurityContextHolder.getContext().getAuthentication(): Hola usuario autenticado, tu username: ".concat(authentication.getName()));
//		}

		Pageable pageRequest = PageRequest.of(page, 4); // Así sería con springBoot 2
		// (spring 5)
		// Pageable pageRequest = new PageRequest(page, 4); Así sería en springBoot
		// 1.5.10 RELEASE

		Page<Cliente> clientes = clienteService.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";

	}

	@RequestMapping(value = "/form")
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario de Cliente");
		return "form";
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
						  @RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito" : "Cliente creado con éxito";

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			return "form";
		}

		if (!foto.isEmpty()) {

			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {

				uploadFileService.delete(cliente.getFoto());
			}

			String uniqueFilename = uploadFileService.copy(foto);

			flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");

			cliente.setFoto(uniqueFilename);
		}

		clienteService.save(cliente);
		status.setComplete(); // con esto terminamos la sesión del cliente y borramos todos sus datos de la
		// sesión
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

	@RequestMapping(value = "/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

		Cliente cliente = null;

		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute("error", "El ID del cliente no existe en la base de datos");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El ID del cliente no puede ser cero");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar Cliente");
		return "form";
	}

	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("success", "¡Cliente borrado con éxito!");

			if (uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito");
			}

		}
		return "redirect:/listar";
	}

	@GetMapping(value = "ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
			return "redirect:/listar";
		}

		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente: " + cliente.getNombre());

		return "ver";
	}

	@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {

		Resource recurso = uploadFileService.load(filename);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}
}
