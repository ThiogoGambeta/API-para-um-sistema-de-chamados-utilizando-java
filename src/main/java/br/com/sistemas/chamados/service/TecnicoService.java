package br.com.sistemas.chamados.service;

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TecnicoService {

    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TecnicoResponse criar(TecnicoRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new RegraNegocioException(
                "Já existe um técnico cadastrado com este e-mail"
            );
        }

        Tecnico tecnico = new Tecnico(
            request.nome(),
            request.email(),
            request.perfil()
        );

        return toResponse(repository.save(tecnico));
    }

    @Transactional(readOnly = true)
    public List<TecnicoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TecnicoResponse buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public TecnicoResponse atualizar(Long id, TecnicoRequest request) {
        Tecnico tecnico = buscarEntidade(id);

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new RegraNegocioException(
                "Já existe outro técnico cadastrado com este e-mail"
            );
        }

        tecnico.setNome(request.nome());
        tecnico.setEmail(request.email());
        tecnico.setPerfil(request.perfil());

        return toResponse(repository.save(tecnico));
    }

    @Transactional
    public void excluir(Long id) {
        Tecnico tecnico = buscarEntidade(id);
        repository.delete(tecnico);
    }

    private Tecnico buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                    new RecursoNaoEncontradoException(
                        "Técnico não encontrado com ID: " + id
                    )
                );
    }

    private TecnicoResponse toResponse(Tecnico tecnico) {
        return new TecnicoResponse(
            tecnico.getId(),
            tecnico.getNome(),
            tecnico.getEmail(),
            tecnico.getPerfil()
        );
    }
}
