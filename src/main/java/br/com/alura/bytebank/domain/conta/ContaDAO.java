package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;
import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsolePlugin;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private Connection connection;

    Set<Conta> contas = new HashSet<>();

    private String query;

    public ContaDAO(Connection connection) {
        this.connection = connection;
    }

    public void abrirConta(DadosAberturaConta dadosDaConta) {

        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente);

        query = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email) VALUES (?, ?, ?, ?, ?)";

        try {
            // ConnectionFactory é uma classe que retorna uma conexão com o banco de dados
            PreparedStatement statement = connection.prepareStatement(query);// Prepara a query para ser executada
            statement.setInt(1, conta.getNumero()); // Substitui o primeiro ? pelo número da conta
            statement.setBigDecimal(2, conta.getSaldo()); // Substitui o segundo ? pelo saldo da conta
            statement.setString(3, dadosDaConta.dadosCliente().nome()); // Substitui o terceiro ? pelo nome do cliente
            statement.setString(4, dadosDaConta.dadosCliente().cpf()); // Substitui o quarto ? pelo cpf do cliente
            statement.setString(5, dadosDaConta.dadosCliente().email()); // Substitui o quinto ? pelo email do cliente
            statement.execute(); // Executa a query
        } catch (
                SQLException e) {
            throw new RuntimeException("Erro ao abrir conta!", e);
        }
    }

    public Set<Conta> listarContas() {

        Set<Conta> conta = new HashSet<>();

        query = "select * from conta";

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(query);
            while (resultSet.next()) {
                var cliente = new Cliente(
                        resultSet.getString("cliente_nome"),
                        resultSet.getString("cliente_cpf"),
                        resultSet.getString("cliente_email")
                );

                conta.add(new Conta(
                        resultSet.getInt("numero"),
                        resultSet.getBigDecimal("saldo"),
                        cliente
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar contas", e);
        }
        return conta;
    }

    public Set<Conta> buscarContaPorNumero(Integer numero) {
        return listarContas();
    }

    public void removerConta(Integer numeroDaConta) {

        query = "delete from conta where numero = ?";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, numeroDaConta);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir a conta número " + numeroDaConta, e);
        }
    }

    public void depositar(Integer numeroDaConta, BigDecimal valor) {
        Set<Conta> contas = listarContas();
        Conta conta = contas.stream()
                .filter(c -> c.getNumero().equals(numeroDaConta))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
        try {
            BigDecimal valorDeposito = conta.getSaldo() == null ? valor : conta.getSaldo().add(valor);

            query = "update conta set saldo = ? where numero = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBigDecimal(1, valorDeposito);
            statement.setInt(2, conta.getNumero());
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar depósito", e);
        }
    }
}
