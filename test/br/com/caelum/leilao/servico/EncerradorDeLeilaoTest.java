package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeilao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncerradorDeLeilaoTest {

    private Calendar data;

    @Before
    public void declara() {
        this.data = Calendar.getInstance();
    }

    @Test
    public void encerraLeilaoAntigo() {
        data.set(1999, 10, 20);

        var tvDePlasma = new CriadorDeLeilao().para("Tv de plasma").naData(data).constroi();
        var geladeira = new CriadorDeLeilao().para("Geladeira").naData(data).constroi();
        var leiloesAntigo = Arrays.asList(tvDePlasma, geladeira);

        RepositorioDeLeilao leilaoDaoMock = mock(RepositorioDeLeilao.class);
        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);

        when(leilaoDaoMock.correntes()).thenReturn(leiloesAntigo);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDaoMock, enviadorDeEmailMock);
        encerrador.encerra();

        Assert.assertTrue(tvDePlasma.isEncerrado());
        Assert.assertTrue(geladeira.isEncerrado());
    }

    @Test
    public void deveAtualizarLeiloesEncerrados() {
        data.set(1999, 10, 1);
        Leilao radio_de_carro = new CriadorDeLeilao().para("Rádio de carro").naData(data).constroi();

        RepositorioDeLeilao repositorioDeLeilaoMock = mock(RepositorioDeLeilao.class);
        when(repositorioDeLeilaoMock.correntes()).thenReturn(Collections.singletonList(radio_de_carro));

        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(repositorioDeLeilaoMock, enviadorDeEmailMock);
        encerradorDeLeilao.encerra();

        verify(repositorioDeLeilaoMock, atLeastOnce()).atualiza(radio_de_carro);
    }

    @Test
    public void deveSerEnviadoPorEmailNaOrdemCorreta() {
        data.set(1999, 10, 1);
        Leilao radio_de_carro = new CriadorDeLeilao().para("Rádio de carro").naData(data).constroi();

        RepositorioDeLeilao repositorioDeLeilaoMock = mock(RepositorioDeLeilao.class);
        when(repositorioDeLeilaoMock.correntes()).thenReturn(Collections.singletonList(radio_de_carro));

        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(repositorioDeLeilaoMock, enviadorDeEmailMock);
        encerradorDeLeilao.encerra();

        InOrder inOrder = inOrder(repositorioDeLeilaoMock, enviadorDeEmailMock);
        inOrder.verify(repositorioDeLeilaoMock).atualiza(radio_de_carro);
        inOrder.verify(enviadorDeEmailMock).envia(radio_de_carro);
    }

    @Test
    public void naoDeveEncerrarLeiloesQueComecaramMenosDeUmaSemanaAtras() {
        data.add(Calendar.DAY_OF_MONTH, -1);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
                .naData(data).constroi();
        Leilao leilao2 = new CriadorDeLeilao().para("Geladeira")
                .naData(data).constroi();

        RepositorioDeLeilao daoFalso = mock(RepositorioDeLeilao.class);
        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1, leilao2));

        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso, enviadorDeEmailMock);
        encerrador.encerra();

        assertEquals(0, encerrador.getTotalEncerrados());
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());

        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
    }

    @Test
    public void deveContinuarExecucaoMesmoSeBancoEstiverFora() {
        data.set(1999, 10, 20);

        var tvDePlasma = new CriadorDeLeilao().para("Tv de plasma").naData(data).constroi();
        var geladeira = new CriadorDeLeilao().para("Geladeira").naData(data).constroi();

        RepositorioDeLeilao repositorioDeLeilaoMock = mock(RepositorioDeLeilao.class);
        when(repositorioDeLeilaoMock.correntes()).thenReturn(Arrays.asList(tvDePlasma, geladeira));

        doThrow(new RuntimeException()).when(repositorioDeLeilaoMock).atualiza(tvDePlasma);

        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);
        doThrow(new RuntimeException()).when(enviadorDeEmailMock).envia(tvDePlasma);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(repositorioDeLeilaoMock, enviadorDeEmailMock);
        encerradorDeLeilao.encerra();

        assertEquals(2, encerradorDeLeilao.getTotalEncerrados());
        assertTrue(tvDePlasma.isEncerrado());
        assertTrue(geladeira.isEncerrado());
    }

    @Test
    public void nuncaEnviarEmailSeTodosOsLeiloesFalharem() {
        data.set(1999, 10, 20);

        var tvDePlasma = new CriadorDeLeilao().para("Tv de plasma").naData(data).constroi();
        var geladeira = new CriadorDeLeilao().para("Geladeira").naData(data).constroi();

        RepositorioDeLeilao repositorioDeLeilaoMock = mock(RepositorioDeLeilao.class);
        when(repositorioDeLeilaoMock.correntes()).thenReturn(Arrays.asList(tvDePlasma, geladeira));

        doThrow(new RuntimeException()).when(repositorioDeLeilaoMock).atualiza(tvDePlasma);
        doThrow(new RuntimeException()).when(repositorioDeLeilaoMock).atualiza(geladeira);

        EnviadorDeEmail enviadorDeEmailMock = mock(EnviadorDeEmail.class);

        EncerradorDeLeilao encerradorDeLeilao = new EncerradorDeLeilao(repositorioDeLeilaoMock, enviadorDeEmailMock);
        encerradorDeLeilao.encerra();

        verify(enviadorDeEmailMock, never()).envia(tvDePlasma);
        verify(enviadorDeEmailMock, never()).envia(geladeira);
    }
}
