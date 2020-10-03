package br.com.caelum.leilao.servico;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.infra.dao.RepositorioDeLeilao;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Calendar;

public class EncerradorDeLeilaoTest {


    @Test
    public void encerraLeilaoAntigo() {
        Calendar antigo = Calendar.getInstance();
        antigo.set(1999, 10, 20);

        var tvDePlasma = new CriadorDeLeilao().para("Tv de plasma").naData(antigo).constroi();
        var geladeira = new CriadorDeLeilao().para("Geladeira").naData(antigo).constroi();
        var leiloesAntigo = Arrays.asList(tvDePlasma, geladeira);

        RepositorioDeLeilao leilaoDaoMock = Mockito.mock(RepositorioDeLeilao.class);

        Mockito.when(leilaoDaoMock.correntes()).thenReturn(leiloesAntigo);

        EncerradorDeLeilao encerrador = new EncerradorDeLeilao(leilaoDaoMock);
        encerrador.encerra();

        Assert.assertTrue(tvDePlasma.isEncerrado());
        Assert.assertTrue(geladeira.isEncerrado());
    }
}
