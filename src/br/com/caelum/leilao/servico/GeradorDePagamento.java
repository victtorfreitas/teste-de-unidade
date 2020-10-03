package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;

import java.util.Calendar;
import java.util.List;

public class GeradorDePagamento {

    private final RepositorioDePagamentos pagamentos;
    private final RepositorioDeLeilao leiloes;
    private final Avaliador avaliador;

    public GeradorDePagamento(RepositorioDeLeilao leiloes,
                              RepositorioDePagamentos pagamentos,
                              Avaliador avaliador) {
        this.leiloes = leiloes;
        this.pagamentos = pagamentos;
        this.avaliador = avaliador;
    }

    public void gera() {
        List<Leilao> leiloesEncerrados = leiloes.encerrados();
        for (Leilao leilao : leiloesEncerrados) {
            avaliador.avalia(leilao);

            Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), Calendar.getInstance());
            pagamentos.salva(novoPagamento);
        }
    }
}
