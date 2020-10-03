package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.infra.dao.Relogio;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;

import java.util.Calendar;
import java.util.List;

public class GeradorDePagamento {

    private final RepositorioDePagamentos pagamentos;
    private final RepositorioDeLeilao leiloes;
    private final Avaliador avaliador;
    private Relogio relogio;

    public GeradorDePagamento(RepositorioDeLeilao leiloes,
                              RepositorioDePagamentos pagamentos,
                              Avaliador avaliador, Relogio relogio) {
        this.leiloes = leiloes;
        this.pagamentos = pagamentos;
        this.avaliador = avaliador;
        this.relogio = relogio;
    }

    public GeradorDePagamento(RepositorioDeLeilao leiloes,
                              RepositorioDePagamentos pagamentos,
                              Avaliador avaliador) {
        this(leiloes, pagamentos, avaliador, new RelogioDoSistema());
    }

    public void gera() {
        List<Leilao> leiloesEncerrados = leiloes.encerrados();
        for (Leilao leilao : leiloesEncerrados) {
            avaliador.avalia(leilao);

            Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), getPrimeiroDiaUtil());
            pagamentos.salva(novoPagamento);
        }
    }

    private Calendar getPrimeiroDiaUtil() {
        Calendar data = relogio.hoje();
        int diaDaSemana = data.get(Calendar.DAY_OF_WEEK);

        if (diaDaSemana == Calendar.SATURDAY)
            data.add(Calendar.DAY_OF_MONTH, 2);
        else if (diaDaSemana == Calendar.SUNDAY)
            data.add(Calendar.DAY_OF_MONTH, 1);

        return data;
    }
}
