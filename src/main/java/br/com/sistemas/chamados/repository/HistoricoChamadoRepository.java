
package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoChamadoRepository
        extends JpaRepository<HistoricoChamado, Long> {

    List<HistoricoChamado> findByChamadoIdOrderByDataAlteracaoAsc(Long chamadoId);

    boolean existsByChamadoId(Long chamadoId);
}
