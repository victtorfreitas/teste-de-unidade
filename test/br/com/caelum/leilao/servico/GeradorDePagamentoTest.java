package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.infra.dao.Relogio;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDePagamentos;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Calendar;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GeradorDePagamentoTest {

    @Test
    public void deveGerarUmPagamentoParaUmLeilaoEncerrado() {
        RepositorioDeLeilao repositorioDeLeilao = mock(RepositorioDeLeilao.class);
        RepositorioDePagamentos repositorioDePagamentos = mock(RepositorioDePagamentos.class);
        Avaliador avaliador = mock(Avaliador.class);

        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José da Silva"), 2000.0)
                .lance(new Usuario("Maria Pereira"), 2500.0)
                .constroi();

        when(repositorioDeLeilao.encerrados()).thenReturn(Collections.singletonList(leilao));
        when(avaliador.getMaiorLance()).thenReturn(2500.0);

        GeradorDePagamento gerador = new GeradorDePagamento(repositorioDeLeilao, repositorioDePagamentos, avaliador);
        gerador.gera();
        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioDePagamentos).salva(argumentCaptor.capture());

        Pagamento pagamentoGerado = argumentCaptor.getValue();
        assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);

    }

    @Test
    public void deveSerPagoNoProximoDiaUtil() {
        RepositorioDeLeilao repositorioDeLeilao = mock(RepositorioDeLeilao.class);
        RepositorioDePagamentos repositorioDePagamentos = mock(RepositorioDePagamentos.class);
        Relogio relogio = mock(Relogio.class);
        Leilao leilao = new CriadorDeLeilao()
                .para("Playstation")
                .lance(new Usuario("José da Silva"), 2000.0)
                .lance(new Usuario("Maria Pereira"), 2500.0)
                .constroi();
        when(repositorioDeLeilao.encerrados()).thenReturn(Collections.singletonList(leilao));

        Calendar dia = Calendar.getInstance();
        dia.set(2020, Calendar.OCTOBER, 3);
        when(relogio.hoje()).thenReturn(dia);

        GeradorDePagamento gerador = new GeradorDePagamento(repositorioDeLeilao, repositorioDePagamentos, new Avaliador(), relogio);
        gerador.gera();
        ArgumentCaptor<Pagamento> argumentCaptor = ArgumentCaptor.forClass(Pagamento.class);
        verify(repositorioDePagamentos).salva(argumentCaptor.capture());

        Pagamento pagamentoGerado = argumentCaptor.getValue();
        assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
    }
}
