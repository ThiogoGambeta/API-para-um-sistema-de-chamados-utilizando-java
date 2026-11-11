
package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ChamadoRepository extends
        JpaRepository<Chamado, Long>,
        JpaSpecificationExecutor<Chamado> {

    List<Chamado> findByClienteIdOrderByDataAberturaDesc(Long clienteId);

}
