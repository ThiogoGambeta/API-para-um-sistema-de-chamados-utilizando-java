
package br.com.sistemas.chamados.specification;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

import org.springframework.data.jpa.domain.Specification;

public class ChamadoSpecification {

    private ChamadoSpecification() {
    }

    public static Specification<Chamado> comStatus(
            StatusChamado status) {

        return (root, query, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Chamado> comPrioridade(
            Prioridade prioridade) {

        return (root, query, cb) ->
                prioridade == null
                        ? cb.conjunction()
                        : cb.equal(root.get("prioridade"), prioridade);
    }

    public static Specification<Chamado> comCliente(
            Long clienteId) {

        return (root, query, cb) ->
                clienteId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("cliente").get("id"), clienteId);
    }
}
