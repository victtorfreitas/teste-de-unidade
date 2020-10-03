package br.com.caelum.leilao.infra.dao;

import br.com.caelum.leilao.dominio.Leilao;

import java.util.List;

public interface RepositorioDeLeilao {
    void salva(Leilao leilao);

    List<Leilao> encerrados();

    List<Leilao> correntes();

    void atualiza(Leilao leilao);
}
