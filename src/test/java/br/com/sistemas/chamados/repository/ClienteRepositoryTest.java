package br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClienteRepositoryTest {
    @Autowired private ClienteRepository repository;

    @Test
    void devePersistirClienteEConsultarPeloEmail() {
        Cliente salvo = repository.saveAndFlush(new Cliente("Ana", "ana@test.local", "47999999999"));
        assertNotNull(salvo.getId());
        assertTrue(repository.existsByEmail("ana@test.local"));
    }

    @Test
    void naoDevePermitirEmailDuplicadoNoBanco() {
        repository.saveAndFlush(new Cliente("Ana", "duplicado@test.local", null));
        assertThrows(DataIntegrityViolationException.class, () ->
                repository.saveAndFlush(new Cliente("Outra", "duplicado@test.local", null)));
    }
}
